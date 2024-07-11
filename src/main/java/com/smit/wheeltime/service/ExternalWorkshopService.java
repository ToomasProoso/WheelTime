package com.smit.wheeltime.service;

import com.smit.wheeltime.model.TireChangeBookingRequest;
import com.smit.wheeltime.model.TireChangeTime;
import com.smit.wheeltime.model.TireChangeTimeBookingResponse;
import com.smit.wheeltime.util.XmlResponseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private RestTemplate restTemplate;

    private final XmlResponseParser xmlResponseParser = new XmlResponseParser();

    public List<TireChangeTime> fetchAppointments(String workshop, String from, String until, String vehicleType) {
        if ("manchester".equalsIgnoreCase(workshop)) {
            return fetchManchesterAppointments(from, vehicleType);
        } else if ("london".equalsIgnoreCase(workshop)) {
            return fetchLondonAppointments(from, until, vehicleType);
        }
        return new ArrayList<>();
    }

    private List<TireChangeTime> fetchManchesterAppointments(String from, String vehicleType) {
        String requestUrl = buildManchesterRequestUrl(from);
        logger.info("Fetching Manchester appointments with URL: {}", requestUrl);

        try {
            ResponseEntity<List<TireChangeTime>> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<TireChangeTime>>() {}
            );
            return filterManchesterAppointments(response.getBody(), from, vehicleType);
        } catch (Exception e) {
            logger.error("Error fetching Manchester appointments: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private String buildManchesterRequestUrl(String from) {
        return UriComponentsBuilder.fromHttpUrl(manchesterApiUrl + "/tire-change-times")
                .queryParam("from", from)
                .toUriString();
    }

    private List<TireChangeTime> filterManchesterAppointments(List<TireChangeTime> appointments, String from, String vehicleType) {
        if (appointments == null || appointments.isEmpty()) {
            logger.warn("No appointments received from Manchester API");
            return new ArrayList<>();
        }

        LocalDate selectedDate = LocalDate.parse(from);
        LocalDateTime now = LocalDateTime.now();

        return appointments.stream()
                .filter(time -> time.isAvailable() && isSameDay(time.getTime(), selectedDate) && LocalDateTime.parse(time.getTime(), DateTimeFormatter.ISO_DATE_TIME).isAfter(now))
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

    private List<TireChangeTime> fetchLondonAppointments(String from, String until, String vehicleType) {
        String requestUrl = buildLondonRequestUrl(from, until);
        logger.info("Fetching London appointments with URL: {}", requestUrl);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            List<TireChangeTime> times = xmlResponseParser.parseLondonResponse(response.getBody(), londonName, londonAddress, londonVehicleTypes[0]);
            return filterLondonAppointments(times, from, until, vehicleType);
        } catch (Exception e) {
            logger.error("Error fetching London appointments: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private String buildLondonRequestUrl(String from, String until) {
        return UriComponentsBuilder.fromHttpUrl(londonApiUrl + "/tire-change-times/available")
                .queryParam("from", from)
                .queryParam("until", until)
                .toUriString();
    }

    private List<TireChangeTime> filterLondonAppointments(List<TireChangeTime> times, String from, String until, String vehicleType) {
        LocalDateTime now = LocalDateTime.now();
        return times.stream()
                .filter(time -> isWithinDateRange(time.getTime(), from, until) && LocalDateTime.parse(time.getTime(), DateTimeFormatter.ISO_DATE_TIME).isAfter(now))
                .filter(time -> vehicleType == null || time.getVehicleType().equalsIgnoreCase(vehicleType))
                .peek(this::setLondonWorkshopDetails)
                .collect(Collectors.toList());
    }

    private void setLondonWorkshopDetails(TireChangeTime time) {
        time.setWorkshop(londonName);
        time.setAddress(londonAddress);
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
        String apiUrl = getWorkshopApiUrl(workshop);
        if (apiUrl == null) return null;

        ResponseEntity<TireChangeTimeBookingResponse> response = restTemplate.postForEntity(
                apiUrl + "/tire-change-times/" + id + "/booking",
                request,
                TireChangeTimeBookingResponse.class
        );
        return response.getBody();
    }

    private String getWorkshopApiUrl(String workshop) {
        if ("manchester".equalsIgnoreCase(workshop)) {
            return manchesterApiUrl;
        } else if ("london".equalsIgnoreCase(workshop)) {
            return londonApiUrl;
        }
        return null;
    }
}
