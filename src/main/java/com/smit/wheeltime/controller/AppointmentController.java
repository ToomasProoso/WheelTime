package com.smit.wheeltime.controller;

import com.smit.wheeltime.models.TireChangeBookingRequest;
import com.smit.wheeltime.models.TireChangeTime;
import com.smit.wheeltime.models.TireChangeTimeBookingResponse;
import com.smit.wheeltime.service.ManchesterService;
import com.smit.wheeltime.service.LondonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final ManchesterService manchesterService;
    private final LondonService londonService;

    @Autowired
    public AppointmentController(ManchesterService manchesterService, LondonService londonService) {
        this.manchesterService = manchesterService;
        this.londonService = londonService;
    }

    @GetMapping
    public List<TireChangeTime> getAvailableAppointments(@RequestParam String workshop,
                                                         @RequestParam String from,
                                                         @RequestParam(required = false) String until,
                                                         @RequestParam(required = false) String vehicleType) {
        if ("manchester".equalsIgnoreCase(workshop)) {
            return manchesterService.fetchAppointments(from, until, vehicleType);
        } else if ("london".equalsIgnoreCase(workshop)) {
            return londonService.fetchAppointments(from, until, vehicleType);
        }
        return List.of();
    }

    @PostMapping("/{id}/booking")
    public ResponseEntity<TireChangeTimeBookingResponse> bookAppointment(
            @RequestParam String workshop, @PathVariable String id, @RequestBody TireChangeBookingRequest request) {
        try {
            if ("manchester".equalsIgnoreCase(workshop)) {
                TireChangeTimeBookingResponse response = manchesterService.bookAppointment(id, request);
                return ok(response);
            } else {
                return status(METHOD_NOT_ALLOWED).body(
                        new TireChangeTimeBookingResponse("failure", "Use PUT for London workshop"));
            }
        } catch (RuntimeException e) {
            return status(BAD_REQUEST).body(
                    new TireChangeTimeBookingResponse("failure", e.getMessage()));
        }
    }

    @PutMapping("/{id}/booking")
    public ResponseEntity<TireChangeTimeBookingResponse> bookAppointmentPut(
            @RequestParam String workshop, @PathVariable String id, @RequestBody TireChangeBookingRequest request) {
        try {
            if ("london".equalsIgnoreCase(workshop)) {
                TireChangeTimeBookingResponse response = londonService.bookAppointment(id, request);
                return ok(response);
            } else {
                return status(METHOD_NOT_ALLOWED).body(
                        new TireChangeTimeBookingResponse("failure", "Use POST for Manchester workshop"));
            }
        } catch (RuntimeException e) {
            return status(BAD_REQUEST).body(
                    new TireChangeTimeBookingResponse("failure", e.getMessage()));
        }
    }
}
