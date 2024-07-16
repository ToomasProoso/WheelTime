package com.smit.wheeltime.service;

import com.smit.wheeltime.models.TireChangeBookingRequest;
import com.smit.wheeltime.models.TireChangeTime;
import com.smit.wheeltime.models.TireChangeTimeBookingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

class ManchesterServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ManchesterService manchesterService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        setPrivateField(manchesterService, "manchesterApiUrl", "http://localhost:9003/api/v1");
        setPrivateField(manchesterService, "manchesterName", "Manchester Workshop");
        setPrivateField(manchesterService, "manchesterAddress", "123 Manchester Street");
        setPrivateField(manchesterService, "manchesterVehicleTypes", new String[]{"Sõiduauto, Veoauto"});
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void fetchAppointments_success() {
        String from = "2024-07-16";
        String vehicleType = "Sõiduauto, Veoauto";
        TireChangeTime expectedTime = new TireChangeTime();
        expectedTime.setTime("2024-07-16T09:00:00Z");
        expectedTime.setVehicleType(vehicleType);
        expectedTime.setAvailable(true);
        expectedTime.setId("1");

        List<TireChangeTime> expected = List.of(expectedTime);
        ResponseEntity<List<TireChangeTime>> responseEntity = new ResponseEntity<>(expected, OK);

        when(restTemplate.exchange(anyString(), eq(GET), isNull(), any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        List<TireChangeTime> actual = manchesterService.fetchAppointments(from, null, vehicleType);

        assertNotNull(actual);
        assertEquals(OK, responseEntity.getStatusCode());
    }

    @Test
    void fetchAppointments_exception() {
        String from = "2024-07-16";
        String vehicleType = "Sõiduauto, Veoauto";

        when(restTemplate.exchange(anyString(), eq(GET), isNull(), any(ParameterizedTypeReference.class))).thenThrow(new RuntimeException("Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            manchesterService.fetchAppointments(from, null, vehicleType);
        });

        assertEquals("Error fetching Manchester appointments: Error", exception.getMessage());
    }

    @Test
    void bookAppointment_success() {
        String id = "123";
        TireChangeBookingRequest request = new TireChangeBookingRequest();
        request.setContactInformation("test@example.com");
        request.setTime("2024-07-16T09:00:00Z");
        TireChangeTimeBookingResponse expectedResponse = new TireChangeTimeBookingResponse("success", "Booked successfully");

        HttpHeaders headers = new HttpHeaders();
        headers.add("time", request.getTime());
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<TireChangeBookingRequest> entity = new HttpEntity<>(request, headers);

        when(restTemplate.postForEntity(anyString(), eq(entity), eq(TireChangeTimeBookingResponse.class))).thenReturn(new ResponseEntity<>(expectedResponse, OK));

        TireChangeTimeBookingResponse actual = manchesterService.bookAppointment(id, request);

        assertNotNull(actual);
        assertEquals(expectedResponse, actual);
    }

    @Test
    void bookAppointment_unprocessableEntity() {
        String id = "123";
        TireChangeBookingRequest request = new TireChangeBookingRequest();
        request.setContactInformation("test@example.com");
        request.setTime("2024-07-16T09:00:00Z");

        HttpHeaders headers = new HttpHeaders();
        headers.add("time", request.getTime());
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<TireChangeBookingRequest> entity = new HttpEntity<>(request, headers);

        when(restTemplate.postForEntity(anyString(), eq(entity), eq(TireChangeTimeBookingResponse.class)))
                .thenThrow(new HttpClientErrorException(UNPROCESSABLE_ENTITY, "Unprocessable Entity"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            manchesterService.bookAppointment(id, request);
        });

        assertEquals("Failed to book appointment: ", exception.getMessage());
    }

    @Test
    void bookAppointment_otherException() {
        String id = "123";
        TireChangeBookingRequest request = new TireChangeBookingRequest();
        request.setContactInformation("test@example.com");
        request.setTime("2024-07-16T09:00:00Z");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TireChangeTimeBookingResponse.class)))
                .thenThrow(new RuntimeException("Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            manchesterService.bookAppointment(id, request);
        });

        assertEquals("Error booking appointment: Error", exception.getMessage());
    }
}
