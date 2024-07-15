package com.smit.wheeltime.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.smit.wheeltime.models.TireChangeTime;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.smit.wheeltime.exception.AppointmentExceptions.throwBookingException;

@Component
public class XmlResponseParser {

    private final XmlMapper xmlMapper = new XmlMapper();

    public List<TireChangeTime> parseLondonResponse(String responseBody, String workshopName, String workshopAddress, String defaultVehicleType) {
        List<TireChangeTime> times = new ArrayList<>();
        try {
            JsonNode node = xmlMapper.readTree(responseBody.getBytes());
            node.path("availableTime").forEach(item -> {
                TireChangeTime time = new TireChangeTime();
                time.setId(item.path("uuid").asText());
                time.setTime(item.path("time").asText());
                time.setAvailable(true);
                time.setWorkshop(workshopName);
                time.setAddress(workshopAddress);
                time.setVehicleType(defaultVehicleType);
                times.add(time);
            });
        } catch (Exception e) {
            throwBookingException("Error parsing London appointments XML response: " + e.getMessage());
        }
        return times;
    }
}
