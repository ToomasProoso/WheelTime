package com.smit.wheeltime.service;

import com.smit.wheeltime.models.TireChangeBookingRequest;
import com.smit.wheeltime.models.TireChangeTime;
import com.smit.wheeltime.models.TireChangeTimeBookingResponse;
import com.smit.wheeltime.util.XmlResponseParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_XML;

class LondonServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private XmlResponseParser xmlResponseParser;

    @InjectMocks
    private LondonService londonService;

    @BeforeEach
    void setUp() throws Exception {
        openMocks(this);
        setPrivateField(londonService, "londonApiUrl", "http://localhost:9003/api/v1");
        setPrivateField(londonService, "londonName", "London Workshop");
        setPrivateField(londonService, "londonAddress", "123 London Street");
        setPrivateField(londonService, "londonVehicleTypes", new String[]{"Sõiduauto"});
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void fetchAppointments_success() {
        String from = "2024-07-16";
        String until = "2030-01-02";
        String vehicleType = "Sõiduauto";
        String responseBody = "<tireChangeTimesResponse>...</tireChangeTimesResponse>";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, OK);
        TireChangeTime expected = new TireChangeTime();
        expected.setTime("2024-07-16T09:00:00Z");
        expected.setVehicleType(vehicleType);
        expected.setId("1");

        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(responseEntity);
        when(xmlResponseParser.parseLondonResponse(anyString(), anyString(), anyString(), anyString())).thenReturn(List.of(expected));

        List<TireChangeTime> actual = londonService.fetchAppointments(from, until, vehicleType);

        assertNotNull(actual);
        assertEquals(OK, responseEntity.getStatusCode());
    }

    @Test
    void fetchAppointments_exception() {
        String from = "2024-07-16";
        String until = "2030-01-02";
        String vehicleType = "Sõiduauto";

        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenThrow(new RuntimeException("Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            londonService.fetchAppointments(from, until, vehicleType);
        });

        assertEquals("Error fetching London appointments: Error", exception.getMessage());
    }

    @Test
    void bookAppointment_success() {
        String id = "123";
        TireChangeBookingRequest request = new TireChangeBookingRequest();
        request.setContactInformation("test@example.com");
        request.setTime("2024-07-16T09:00:00Z");
        TireChangeTimeBookingResponse expected = new TireChangeTimeBookingResponse("success", "Booked successfully");

        HttpHeaders headers = new HttpHeaders();
        headers.add("time", request.getTime());
        headers.setContentType(APPLICATION_XML);
        HttpEntity<String> entity = new HttpEntity<>(
                "<tireChangeBookingRequest><contactInformation>test@example.com</contactInformation><time>2024-07-16T09:00:00Z</time></tireChangeBookingRequest>",
                headers);

        when(restTemplate.exchange(anyString(), eq(PUT), eq(entity), eq(TireChangeTimeBookingResponse.class)))
                .thenReturn(new ResponseEntity<>(expected, OK));

        TireChangeTimeBookingResponse actual = londonService.bookAppointment(id, request);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void bookAppointment_unprocessableEntity() {
        String id = "123";
        TireChangeBookingRequest request = new TireChangeBookingRequest();
        request.setContactInformation("test@example.com");
        request.setTime("2024-07-16T09:00:00Z");

        HttpHeaders headers = new HttpHeaders();
        headers.add("time", request.getTime());
        headers.setContentType(APPLICATION_XML);
        HttpEntity<String> entity = new HttpEntity<>(
                "<tireChangeBookingRequest><contactInformation>test@example.com</contactInformation><time>2024-07-16T09:00:00Z</time></tireChangeBookingRequest>",
                headers);

        when(restTemplate.exchange(anyString(), eq(PUT), eq(entity), eq(TireChangeTimeBookingResponse.class)))
                .thenThrow(new HttpClientErrorException(UNPROCESSABLE_ENTITY, "Unprocessable Entity"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            londonService.bookAppointment(id, request);
        });

        assertEquals("Failed to book appointment: ", exception.getMessage());
    }

    @Test
    void bookAppointment_otherException() {
        String id = "123";
        TireChangeBookingRequest request = new TireChangeBookingRequest();
        request.setContactInformation("test@example.com");
        request.setTime("2024-07-16T09:00:00Z");

        when(restTemplate.exchange(anyString(), eq(PUT), any(HttpEntity.class), eq(TireChangeTimeBookingResponse.class)))
                .thenThrow(new RuntimeException("Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            londonService.bookAppointment(id, request);
        });

        assertEquals("Error booking appointment: Error", exception.getMessage());
    }
}
