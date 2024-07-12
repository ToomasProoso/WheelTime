package com.smit.wheeltime.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "tireChangeBookingRequest")
public class TireChangeBookingRequest {
    private String contactInformation;
    private String time;
    // You can add other fields here if required by the API
}


