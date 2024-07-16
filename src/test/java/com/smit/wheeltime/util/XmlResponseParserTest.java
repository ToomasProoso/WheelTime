package com.smit.wheeltime.util;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.smit.wheeltime.models.TireChangeTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XmlResponseParserTest {

    private XmlResponseParser xmlResponseParser;
    private XmlMapper xmlMapper;

    @BeforeEach
    void setUp() {
        xmlResponseParser = new XmlResponseParser();
        xmlMapper = new XmlMapper();
        ReflectionTestUtils.setField(xmlResponseParser, "xmlMapper", xmlMapper);
    }

    @Test
    void parseLondonResponse_success() {
        String responseBody = "<tireChangeTimesResponse><availableTime><uuid>1</uuid><time>2024-07-16T09:00:00Z</time></availableTime><availableTime><uuid>2</uuid><time>2024-07-17T10:00:00Z</time></availableTime></tireChangeTimesResponse>";
        String workshopName = "London Workshop";
        String workshopAddress = "123 London Street";
        String defaultVehicleType = "car";

        List<TireChangeTime> result = xmlResponseParser.parseLondonResponse(responseBody, workshopName, workshopAddress, defaultVehicleType);

        assertNotNull(result);
        assertEquals(2, result.size());

        TireChangeTime time1 = result.getFirst();
        assertEquals("1", time1.getId());
        assertEquals("2024-07-16T09:00:00Z", time1.getTime());
        assertTrue(time1.isAvailable());
        assertEquals(workshopName, time1.getWorkshop());
        assertEquals(workshopAddress, time1.getAddress());
        assertEquals(defaultVehicleType, time1.getVehicleType());

        TireChangeTime time2 = result.get(1);
        assertEquals("2", time2.getId());
        assertEquals("2024-07-17T10:00:00Z", time2.getTime());
        assertTrue(time2.isAvailable());
        assertEquals(workshopName, time2.getWorkshop());
        assertEquals(workshopAddress, time2.getAddress());
        assertEquals(defaultVehicleType, time2.getVehicleType());
    }

    @Test
    void parseLondonResponse_invalidXml() {
        String invalidResponseBody = "<tireChangeTimesResponse><availableTime><uuid>1<time>2024-07-16T09:00:00Z</time></availableTime></tireChangeTimesResponse>";
        String workshopName = "London Workshop";
        String workshopAddress = "123 London Street";
        String defaultVehicleType = "car";

        Exception exception = assertThrows(RuntimeException.class, () -> {
            xmlResponseParser.parseLondonResponse(invalidResponseBody, workshopName, workshopAddress, defaultVehicleType);
        });

        assertTrue(exception.getMessage().contains("Error parsing London appointments XML response"));
    }
}
