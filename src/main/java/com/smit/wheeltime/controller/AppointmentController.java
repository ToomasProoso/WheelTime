package com.smit.wheeltime.controller;

import com.smit.wheeltime.model.TireChangeBookingRequest;
import com.smit.wheeltime.model.TireChangeTime;
import com.smit.wheeltime.model.TireChangeTimeBookingResponse;
import com.smit.wheeltime.service.ExternalWorkshopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final ExternalWorkshopService externalWorkshopService;

    @Autowired
    public AppointmentController(ExternalWorkshopService externalWorkshopService) {
        this.externalWorkshopService = externalWorkshopService;
    }

    @GetMapping
    public List<TireChangeTime> getAvailableAppointments(@RequestParam String workshop) {
        if (workshop.equals("manchester")) {
            return externalWorkshopService.fetchManchesterAppointments();
        } else if (workshop.equals("london")) {
            return externalWorkshopService.fetchLondonAppointments();
        } else {
            throw new IllegalArgumentException("Unknown workshop: " + workshop);
        }
    }

    @PostMapping("/{id}/booking")
    public TireChangeTimeBookingResponse bookAppointment(@RequestParam String workshop, @PathVariable String id, @RequestBody TireChangeBookingRequest request) {
        if (workshop.equals("manchester")) {
            return externalWorkshopService.bookAppointment("http://localhost:9003", id, request);
        } else if (workshop.equals("london")) {
            return externalWorkshopService.bookAppointment("http://localhost:9004", id, request);
        } else {
            throw new IllegalArgumentException("Unknown workshop: " + workshop);
        }
    }
}

