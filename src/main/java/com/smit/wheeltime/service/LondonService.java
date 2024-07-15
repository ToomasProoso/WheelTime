package com.smit.wheeltime.service;

import com.smit.wheeltime.models.TireChangeBookingRequest;
import com.smit.wheeltime.models.TireChangeTime;
import com.smit.wheeltime.models.TireChangeTimeBookingResponse;

import com.smit.wheeltime.util.XmlResponseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.*;
import static java.time.format.DateTimeFormatter.*;
import static org.slf4j.LoggerFactory.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.util.UriComponentsBuilder.*;

@Service
public class LondonService implements WorkshopService {

    private static final Logger logger = getLogger(LondonService.class);

    @Value("${workshop.api.london}")
    private String londonApiUrl;

    @Value("${workshop.london.name}")
    private String londonName;

    @Value("${workshop.london.address}")
    private String londonAddress;

    @Value("${workshop.london.vehicleTypes}")
    private String[] londonVehicleTypes;

    private final RestTemplate restTemplate;
    private final XmlResponseParser xmlResponseParser;

    public LondonService(RestTemplate restTemplate, XmlResponseParser xmlResponseParser) {
        this.restTemplate = restTemplate;
        this.xmlResponseParser = xmlResponseParser;
    }

    @Override
    public List<TireChangeTime> fetchAppointments(String from, String until, String vehicleType) {
        String requestUrl = buildLondonRequestUrl(from, until);
        logger.info("Fetching London appointments with URL: {}", requestUrl);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            List<TireChangeTime> times = xmlResponseParser.parseLondonResponse(
                    response.getBody(), londonName, londonAddress, londonVehicleTypes[0]);
            return filterLondonAppointments(times, from, until, vehicleType);
        } catch (Exception e) {
            logger.error("Error fetching London appointments: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private String buildLondonRequestUrl(String from, String until) {
        return fromHttpUrl(londonApiUrl + "/tire-change-times/available")
                .queryParam("from", from)
                .queryParam("until", until)
                .toUriString();
    }

    private List<TireChangeTime> filterLondonAppointments(
            List<TireChangeTime> times, String from, String until, String vehicleType) {
        LocalDateTime now = now();
        return times.stream()
                .filter(time -> isWithinDateRange(time.getTime(), from, until)
                        && parse(time.getTime(), ISO_DATE_TIME).isAfter(now))
                .filter(time -> vehicleType == null || time.getVehicleType().equalsIgnoreCase(vehicleType))
                .peek(this::setLondonWorkshopDetails)
                .collect(Collectors.toList());
    }

    private void setLondonWorkshopDetails(TireChangeTime time) {
        time.setWorkshop(londonName);
        time.setAddress(londonAddress);
    }

    private boolean isWithinDateRange(String time, String from, String until) {
        LocalDateTime appointmentTime = parse(time, ISO_DATE_TIME);
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate untilDate = until != null ? LocalDate.parse(until) : fromDate;
        return !appointmentTime.toLocalDate().isBefore(fromDate) && !appointmentTime.toLocalDate().isAfter(untilDate);
    }

    @Override
    public TireChangeTimeBookingResponse bookAppointment(String id, TireChangeBookingRequest request) {
        String apiUrl = londonApiUrl;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("time", request.getTime());
            headers.setContentType(APPLICATION_XML);

            String xmlBody = String.format(
                    "<tireChangeBookingRequest><contactInformation>%s</contactInformation><time>%s</time></tireChangeBookingRequest>",
                    request.getContactInformation(), request.getTime());
            HttpEntity<String> entity = new HttpEntity<>(xmlBody, headers);

            ResponseEntity<TireChangeTimeBookingResponse> response = restTemplate.exchange(
                    apiUrl + "/tire-change-times/" + id + "/booking"
                    , PUT, entity, TireChangeTimeBookingResponse.class);

            if (response.getStatusCode() == OK) {
                return response.getBody();
            } else {
                logger.error("Failed to book appointment: {}", response.getStatusCode());
                throw new RuntimeException("Failed to book appointment");
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == UNPROCESSABLE_ENTITY) {
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
