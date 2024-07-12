package com.smit.wheeltime.controller;

import com.smit.wheeltime.model.TireChangeBookingRequest;
import com.smit.wheeltime.model.TireChangeTime;
import com.smit.wheeltime.model.TireChangeTimeBookingResponse;
import com.smit.wheeltime.service.ExternalWorkshopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<TireChangeTimeBookingResponse> bookAppointment(@RequestParam String workshop, @PathVariable String id, @RequestBody TireChangeBookingRequest request) {
        try {
            TireChangeTimeBookingResponse response = externalWorkshopService.bookAppointment(workshop, id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new TireChangeTimeBookingResponse("failure", e.getMessage()));
        }
    }

    @PutMapping("/{id}/booking")
    public ResponseEntity<TireChangeTimeBookingResponse> bookAppointmentPut(@RequestParam String workshop, @PathVariable String id, @RequestBody TireChangeBookingRequest request) {
        try {
            TireChangeTimeBookingResponse response = externalWorkshopService.bookAppointment(workshop, id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new TireChangeTimeBookingResponse("failure", e.getMessage()));
        }
    }
}