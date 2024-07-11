package com.smit.wheeltime.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.smit.wheeltime.model.TireChangeBookingRequest;
import com.smit.wheeltime.model.TireChangeTime;
import com.smit.wheeltime.model.TireChangeTimeBookingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExternalWorkshopService {
    @Value("${workshop.api.manchester}")
    private String manchesterApiUrl;

    @Value("${workshop.api.london}")
    private String londonApiUrl;

    @Value("${workshop.manchester.name}")
    private String manchesterName;

    @Value("${workshop.manchester.address}")
    private String manchesterAddress;

    @Value("${workshop.manchester.vehicleTypes}")
    private String[] manchesterVehicleTypes;

    @Value("${workshop.london.name}")
    private String londonName;

    @Value("${workshop.london.address}")
    private String londonAddress;

    @Value("${workshop.london.vehicleTypes}")
    private String[] londonVehicleTypes;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<TireChangeTime> fetchAppointments(String workshop, String from, String until, String vehicleType) {
        if ("manchester".equalsIgnoreCase(workshop)) {
            ResponseEntity<List<TireChangeTime>> response = restTemplate.exchange(
                    UriComponentsBuilder.fromHttpUrl(manchesterApiUrl + "/tire-change-times")
                            .queryParam("from", from)
                            .toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<TireChangeTime>>() {}
            );
            LocalDate selectedDate = LocalDate.parse(from);
            return response.getBody().stream()
                    .filter(time -> time.isAvailable() && isSameDay(time.getTime(), selectedDate) && (vehicleType == null || vehicleType.equalsIgnoreCase(time.getVehicleType())))
                    .peek(time -> {
                        time.setWorkshop(manchesterName);
                        time.setAddress(manchesterAddress);
                        time.setVehicleType(manchesterVehicleTypes[0]);
                    })
                    .collect(Collectors.toList());
        } else if ("london".equalsIgnoreCase(workshop)) {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    UriComponentsBuilder.fromHttpUrl(londonApiUrl + "/tire-change-times/available")
                            .queryParam("from", from)
                            .queryParam("until", until)
                            .toUriString(),
                    String.class
            );
            try {
                XmlMapper xmlMapper = new XmlMapper();
                JsonNode node = xmlMapper.readTree(response.getBody().getBytes());
                List<TireChangeTime> times = new ArrayList<>();
                node.path("availableTime").forEach(item -> {
                    TireChangeTime time = new TireChangeTime();
                    time.setId(item.path("uuid").asText());
                    time.setTime(item.path("time").asText());
                    time.setAvailable(true);
                    time.setWorkshop(londonName);
                    time.setAddress(londonAddress);
                    time.setVehicleType(londonVehicleTypes[0]);
                    if (isWithinDateRange(time.getTime(), from, until) && (vehicleType == null || vehicleType.equalsIgnoreCase(time.getVehicleType()))) {
                        times.add(time);
                    }
                });
                return times;
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse XML response", e);
            }
        }
        return new ArrayList<>();
    }

    private boolean isSameDay(String time, LocalDate selectedDate) {
        LocalDateTime appointmentTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        return appointmentTime.toLocalDate().equals(selectedDate);
    }

    private boolean isWithinDateRange(String time, String from, String until) {
        LocalDateTime appointmentTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate untilDate = until != null ? LocalDate.parse(until) : fromDate;
        return !appointmentTime.toLocalDate().isBefore(fromDate) && !appointmentTime.toLocalDate().isAfter(untilDate);
    }

    public TireChangeTimeBookingResponse bookAppointment(String workshop, String id, TireChangeBookingRequest request) {
        if ("manchester".equalsIgnoreCase(workshop)) {
            ResponseEntity<TireChangeTimeBookingResponse> response = restTemplate.postForEntity(
                    manchesterApiUrl + "/tire-change-times/" + id + "/booking",
                    request,
                    TireChangeTimeBookingResponse.class
            );
            return response.getBody();
        } else if ("london".equalsIgnoreCase(workshop)) {
            ResponseEntity<TireChangeTimeBookingResponse> response = restTemplate.postForEntity(
                    londonApiUrl + "/tire-change-times/" + id + "/booking",
                    request,
                    TireChangeTimeBookingResponse.class
            );
            return response.getBody();
        }
        return null;
    }
}
