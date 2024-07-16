package com.smit.wheeltime.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WebConfigTest {

    private WebConfig webConfig;

    @BeforeEach
    void setUp() {
        webConfig = new WebConfig();
    }

    @Test
    void configureMessageConverters() {
        List<HttpMessageConverter<?>> converters = new java.util.ArrayList<>();
        webConfig.configureMessageConverters(converters);
        boolean found = converters.stream()
                .anyMatch(converter -> converter instanceof MappingJackson2XmlHttpMessageConverter);
        assertTrue(found, "MappingJackson2XmlHttpMessageConverter should be present in the converters");
    }
}
