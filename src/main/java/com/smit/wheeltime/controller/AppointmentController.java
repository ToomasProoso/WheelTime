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
    public List<TireChangeTime> getAvailableAppointments(@RequestParam String workshop,
                                                         @RequestParam String from,
                                                         @RequestParam(required = false) String until,
                                                         @RequestParam(required = false) String vehicleType) {
        return externalWorkshopService.fetchAppointments(workshop, from, until, vehicleType);
    }

    @PostMapping("/{id}/booking")
    public TireChangeTimeBookingResponse bookAppointment(@RequestParam String workshop, @PathVariable String id, @RequestBody TireChangeBookingRequest request) {
        return externalWorkshopService.bookAppointment(workshop, id, request);
    }
}
