package com.smit.wheeltime.models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "tireChangeBookingRequest")
public class TireChangeBookingRequest {
    private String contactInformation;
    private String time;
}