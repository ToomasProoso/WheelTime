package com.smit.wheeltime.service;

import com.smit.wheeltime.models.TireChangeBookingRequest;
import com.smit.wheeltime.models.TireChangeTime;
import com.smit.wheeltime.models.TireChangeTimeBookingResponse;

import java.util.List;

public interface WorkshopService {
    List<TireChangeTime> fetchAppointments(String from, String until, String vehicleType);

    TireChangeTimeBookingResponse bookAppointment(String id, TireChangeBookingRequest request);
}

