package com.smit.wheeltime.service;

import com.smit.wheeltime.models.TireChangeBookingRequest;
import com.smit.wheeltime.models.TireChangeTime;
import com.smit.wheeltime.models.TireChangeTimeBookingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.*;
import static java.time.format.DateTimeFormatter.*;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.util.UriComponentsBuilder.*;

@Service
public class ManchesterService implements WorkshopService {

    private static final Logger logger = LoggerFactory.getLogger(ManchesterService.class);

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
        logger.info("Fetching Manchester appointments with URL: {}", requestUrl);

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
            logger.error("Error fetching Manchester appointments: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private String buildManchesterRequestUrl(String from) {
        return fromHttpUrl(manchesterApiUrl + "/tire-change-times")
                .queryParam("from", from)
                .toUriString();
    }

    private List<TireChangeTime> filterManchesterAppointments(
            List<TireChangeTime> appointments, String from, String vehicleType) {
        if (appointments == null || appointments.isEmpty()) {
            logger.warn("No appointments received from Manchester API");
            return new ArrayList<>();
        }

        LocalDate selectedDate = LocalDate.parse(from);
        LocalDateTime now = now();

        return appointments.stream()
                .filter(time -> time.isAvailable()
                        && isSameDay(time.getTime(), selectedDate)
                        && parse(time.getTime(), ISO_DATE_TIME).isAfter(now))
                .peek(this::setManchesterWorkshopDetails)
                .filter(time -> vehicleType == null || time.getVehicleType().contains(vehicleType))
                .collect(toList());
    }

    private void setManchesterWorkshopDetails(TireChangeTime time) {
        time.setWorkshop(manchesterName);
        time.setAddress(manchesterAddress);
        if (time.getVehicleType() == null) {
            time.setVehicleType(String.join(", ", manchesterVehicleTypes));
        }
    }

    private boolean isSameDay(String time, LocalDate selectedDate) {
        LocalDateTime appointmentTime = parse(time, ISO_DATE_TIME);
        return appointmentTime.toLocalDate().equals(selectedDate);
    }

    @Override
    public TireChangeTimeBookingResponse bookAppointment(String id, TireChangeBookingRequest request) {
        String apiUrl = manchesterApiUrl;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("time", request.getTime());
            headers.setContentType(APPLICATION_JSON);

            HttpEntity<TireChangeBookingRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<TireChangeTimeBookingResponse> response = restTemplate.postForEntity(
                    apiUrl + "/tire-change-times/" + id + "/booking", entity, TireChangeTimeBookingResponse.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                logger.error("Failed to book appointment: {}", response.getStatusCode());
                throw new RuntimeException("Failed to book appointment");
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                logger.error("Failed to book appointment: {}", e.getResponseBodyAsString());
                throw new RuntimeException("Failed to book appointment: " + e.getResponseBodyAsString());
            } else {
                logger.error("Error booking appointment", e);
                throw new RuntimeException("Error booking appointment", e);
            }
        } catch (Exception e) {
            logger.error("Error booking appointment", e);
            throw new RuntimeException("Error booking appointment", e);
        }
    }
}
