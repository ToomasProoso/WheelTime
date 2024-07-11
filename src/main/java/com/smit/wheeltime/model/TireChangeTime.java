package com.smit.wheeltime.model;

import lombok.Data;

@Data
public class TireChangeTime {
    private String id;
    private String time;
    private boolean available;
    private String vehicleType;
    private String workshop;
    private String address;
}
