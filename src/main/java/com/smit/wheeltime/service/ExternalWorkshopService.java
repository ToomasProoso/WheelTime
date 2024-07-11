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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExternalWorkshopService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalWorkshopService.class);

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
            return fetchManchesterAppointments(from, vehicleType);
        } else if ("london".equalsIgnoreCase(workshop)) {
            return fetchLondonAppointments(from, until, vehicleType);
        }
        return new ArrayList<>();
    }

    private List<TireChangeTime> fetchManchesterAppointments(String from, String vehicleType) {
        try {
            String requestUrl = UriComponentsBuilder.fromHttpUrl(manchesterApiUrl + "/tire-change-times")
                    .queryParam("from", from)
                    .toUriString();
            logger.info("Fetching Manchester appointments with URL: {}", requestUrl);

            ResponseEntity<List<TireChangeTime>> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<TireChangeTime>>() {}
            );
            List<TireChangeTime> appointments = response.getBody();
            logger.info("Manchester appointments API response: {}", appointments);

            if (appointments == null || appointments.isEmpty()) {
                logger.warn("No appointments received from Manchester API");
                return new ArrayList<>();
            }

            LocalDate selectedDate = LocalDate.parse(from);
            LocalDateTime now = LocalDateTime.now();

            return appointments.stream()
                    .filter(time -> time.isAvailable() && isSameDay(time.getTime(), selectedDate) && LocalDateTime.parse(time.getTime(), DateTimeFormatter.ISO_DATE_TIME).isAfter(now))
                    .filter(time -> vehicleType == null || time.getVehicleType().equalsIgnoreCase(vehicleType))
                    .peek(time -> {
                        time.setWorkshop(manchesterName);
                        time.setAddress(manchesterAddress);
                        if (time.getVehicleType() == null) {
                            time.setVehicleType(String.join(", ", manchesterVehicleTypes));
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching Manchester appointments: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private List<TireChangeTime> fetchLondonAppointments(String from, String until, String vehicleType) {
        try {
            String requestUrl = UriComponentsBuilder.fromHttpUrl(londonApiUrl + "/tire-change-times/available")
                    .queryParam("from", from)
                    .queryParam("until", until)
                    .toUriString();
            logger.info("Fetching London appointments with URL: {}", requestUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);

            XmlMapper xmlMapper = new XmlMapper();
            JsonNode node = xmlMapper.readTree(response.getBody().getBytes());
            List<TireChangeTime> times = new ArrayList<>();
            logger.info("London appointments API response: {}", response.getBody());
            node.path("availableTime").forEach(item -> {
                TireChangeTime time = new TireChangeTime();
                time.setId(item.path("uuid").asText());
                time.setTime(item.path("time").asText());
                time.setAvailable(true);
                time.setWorkshop(londonName);
                time.setAddress(londonAddress);
                if (time.getVehicleType() == null) {
                    time.setVehicleType(londonVehicleTypes[0]);
                }
                if (isWithinDateRange(time.getTime(), from, until) && (vehicleType == null || time.getVehicleType().equalsIgnoreCase(vehicleType))) {
                    times.add(time);
                }
            });
            return times;
        } catch (Exception e) {
            logger.error("Error fetching London appointments: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
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
