package com.smit.wheeltime.service;

import com.smit.wheeltime.model.TireChangeBookingRequest;
import com.smit.wheeltime.model.TireChangeTime;
import com.smit.wheeltime.model.TireChangeTimeBookingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ExternalWorkshopService {
    @Value("${workshop.api.manchester}")
    private String manchesterApiUrl;

    @Value("${workshop.api.london}")
    private String londonApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<TireChangeTime> fetchAppointments(String workshopUrl) {
        ResponseEntity<List<TireChangeTime>> response = restTemplate.exchange(
                workshopUrl + "/tire-change-times",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TireChangeTime>>() {}
        );
        return response.getBody();
    }

    public List<TireChangeTime> fetchManchesterAppointments() {
        return fetchAppointments(manchesterApiUrl);
    }

    public List<TireChangeTime> fetchLondonAppointments() {
        return fetchAppointments(londonApiUrl);
    }

    public TireChangeTimeBookingResponse bookAppointment(String workshopUrl, String id, TireChangeBookingRequest request) {
        ResponseEntity<TireChangeTimeBookingResponse> response = restTemplate.postForEntity(
                workshopUrl + "/tire-change-times/" + id + "/booking",
                request,
                TireChangeTimeBookingResponse.class
        );
        return response.getBody();
    }
}

