package com.smit.wheeltime.model;

import lombok.Data;

@Data
public class TireChangeBookingRequest {
    private String vehicleType;
    private String time;
}

