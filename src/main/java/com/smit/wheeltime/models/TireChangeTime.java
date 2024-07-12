package com.smit.wheeltime.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class TireChangeTime {
    private String id;
    private String time;
    @Setter
    @Getter
    private boolean available;
    private String vehicleType;
    private String workshop;
    private String address;
}
