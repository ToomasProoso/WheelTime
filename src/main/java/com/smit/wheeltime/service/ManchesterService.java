package com.smit.wheeltime.service;

import com.smit.wheeltime.exception.AppointmentExceptions;
import com.smit.wheeltime.models.TireChangeBookingRequest;
import com.smit.wheeltime.models.TireChangeTime;
import com.smit.wheeltime.models.TireChangeTimeBookingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Service
public class ManchesterService implements WorkshopService {

    @Value("${workshop.api.manchester}")
    private String manchesterApiUrl;

    @Value("${workshop.manchester.name}")
    private String manchesterName;

    @Value("${workshop.manchester.address}")
    private String manchesterAddress;

    @Value("${workshop.manchester.vehicleTypes}")
    private String[] manchesterVehicleTypes;

    private final RestTemplate restTemplate;

    public ManchesterService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<TireChangeTime> fetchAppointments(String from, String until, String vehicleType) {
        String requestUrl = buildManchesterRequestUrl(from);

        try {
            ResponseEntity<List<TireChangeTime>> response = restTemplate.exchange(
                    requestUrl,
                    GET,
                    null,
                    new ParameterizedTypeReference<List<TireChangeTime>>() {
                    }
            );
            return filterManchesterAppointments(response.getBody(), from, vehicleType);
        } catch (Exception e) {
            AppointmentExceptions.throwBookingException("Error fetching Manchester appointments: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private String buildManchesterRequestUrl(String from) {
        return fromHttpUrl(manchesterApiUrl + "/tire-change-times")
                .queryParam("from", from)
                .toUriString();
    }

    private List<TireChangeTime> filterManchesterAppointments(
            List<TireChangeTime> appointments, String from, String vehicleType) {
        if (appointments == null || appointments.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDate selectedDate = LocalDate.parse(from);
        LocalDateTime now = now();

        return appointments.stream()
                .filter(time -> time.isAvailable()
                        && isSameDay(time.getTime(), selectedDate)
                        && LocalDateTime.parse(time.getTime(), ISO_DATE_TIME).isAfter(now))
                .peek(this::setManchesterWorkshopDetails)
                .filter(time -> vehicleType == null || time.getVehicleType().contains(vehicleType))
                .collect(Collectors.toList());
    }

    private void setManchesterWorkshopDetails(TireChangeTime time) {
        time.setWorkshop(manchesterName);
        time.setAddress(manchesterAddress);
        if (time.getVehicleType() == null) {
            time.setVehicleType(String.join(", ", manchesterVehicleTypes));
        }
    }

    private boolean isSameDay(String time, LocalDate selectedDate) {
        LocalDateTime appointmentTime = LocalDateTime.parse(time, ISO_DATE_TIME);
        return appointmentTime.toLocalDate().equals(selectedDate);
    }

    @Override
    public TireChangeTimeBookingResponse bookAppointment(String id, TireChangeBookingRequest request) {
        String apiUrl = manchesterApiUrl;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("time", request.getTime());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TireChangeBookingRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<TireChangeTimeBookingResponse> response = restTemplate.postForEntity(
                    apiUrl + "/tire-change-times/" + id + "/booking", entity, TireChangeTimeBookingResponse.class);

            if (response.getStatusCode() == OK) {
                return response.getBody();
            } else {
                AppointmentExceptions.throwBookingException("Failed to book appointment");
            }
        } catch (HttpClientErrorException e) {
            handleHttpClientErrorException(e);
        } catch (Exception e) {
            AppointmentExceptions.throwBookingException("Error booking appointment: " + e.getMessage());
        }
        return null;
    }

    private void handleHttpClientErrorException(HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
            AppointmentExceptions.throwBookingException("Failed to book appointment: " + e.getResponseBodyAsString());
        } else {
            AppointmentExceptions.throwBookingException("Error booking appointment: " + e.getMessage());
        }
    }
}
