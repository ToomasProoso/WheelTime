package com.smit.wheeltime.models;

import lombok.Data;

@Data
public class TireChangeTimeBookingResponse {
    private String status;
    private String message;

    public TireChangeTimeBookingResponse(String failure, String message) {

    }
}
