package com.smit.wheeltime.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smit.wheeltime.models.TireChangeBookingRequest;
import com.smit.wheeltime.models.TireChangeTime;
import com.smit.wheeltime.models.TireChangeTimeBookingResponse;
import com.smit.wheeltime.service.LondonService;
import com.smit.wheeltime.service.ManchesterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManchesterService manchesterService;

    @MockBean
    private LondonService londonService;

    @Autowired
    private ObjectMapper objectMapper;

    private TireChangeBookingRequest bookingRequest;

    @BeforeEach
    void setUp() {
        bookingRequest = new TireChangeBookingRequest();
        bookingRequest.setContactInformation("test@example.com");
        bookingRequest.setTime("2024-07-16T09:00:00Z");
    }

    @Test
    void getAvailableAppointments_manchester() throws Exception {
        TireChangeTime appointment = new TireChangeTime();
        appointment.setTime("2024-07-16T09:00:00Z");
        appointment.setVehicleType("Sõiduauto");
        appointment.setAvailable(true);
        appointment.setId("1");

        List<TireChangeTime> appointments = List.of(appointment);

        Mockito.when(manchesterService.fetchAppointments(anyString(), anyString(), anyString()))
                .thenReturn(appointments);

        mockMvc.perform(get("/api/appointments")
                        .param("workshop", "manchester")
                        .param("from", "2024-07-16")
                        .param("until", "2030-01-02")
                        .param("vehicleType", "Sõiduauto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].time").value("2024-07-16T09:00:00Z"))
                .andExpect(jsonPath("$[0].vehicleType").value("Sõiduauto"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void getAvailableAppointments_london() throws Exception {
        TireChangeTime appointment = new TireChangeTime();
        appointment.setTime("2024-07-16T09:00:00Z");
        appointment.setVehicleType("Sõiduauto");
        appointment.setAvailable(true);
        appointment.setId("1");

        List<TireChangeTime> appointments = List.of(appointment);

        Mockito.when(londonService.fetchAppointments(anyString(), anyString(), anyString()))
                .thenReturn(appointments);

        mockMvc.perform(get("/api/appointments")
                        .param("workshop", "london")
                        .param("from", "2024-07-16")
                        .param("until", "2030-01-02")
                        .param("vehicleType", "Sõiduauto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].time").value("2024-07-16T09:00:00Z"))
                .andExpect(jsonPath("$[0].vehicleType").value("Sõiduauto"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void bookAppointment_manchester() throws Exception {
        TireChangeTimeBookingResponse bookingResponse = new TireChangeTimeBookingResponse("success", "Booked successfully");

        Mockito.when(manchesterService.bookAppointment(anyString(), eq(bookingRequest)))
                .thenReturn(bookingResponse);

        mockMvc.perform(post("/api/appointments/123/booking")
                        .param("workshop", "manchester")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void bookAppointment_london() throws Exception {
        TireChangeTimeBookingResponse bookingResponse = new TireChangeTimeBookingResponse("success", "Booked successfully");

        Mockito.when(londonService.bookAppointment(anyString(), eq(bookingRequest)))
                .thenReturn(bookingResponse);

        mockMvc.perform(put("/api/appointments/123/booking")
                        .param("workshop", "london")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void bookAppointment_invalidWorkshop() throws Exception {
        mockMvc.perform(post("/api/appointments/123/booking")
                        .param("workshop", "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isMethodNotAllowed());
    }
}
