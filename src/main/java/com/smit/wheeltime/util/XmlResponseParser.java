package com.smit.wheeltime.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.smit.wheeltime.model.TireChangeTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class XmlResponseParser {

    private static final Logger logger = LoggerFactory.getLogger(XmlResponseParser.class);
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
            logger.error("Error parsing London appointments XML response: {}", e.getMessage(), e);
        }
        return times;
    }
}
