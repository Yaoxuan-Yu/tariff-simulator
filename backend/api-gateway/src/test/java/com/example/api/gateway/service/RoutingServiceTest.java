package com.example.api.gateway.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class RoutingServiceTest {

    @Test
    void routingService_Exists() {
        assertNotNull(RoutingService.class);
    }

    @Test
    void buildTargetUrl_WithPath_ReturnsCorrectUrl() {
        RoutingService routingService = new RoutingService(new RestTemplate());
        String serviceUrl = "http://test-service:8080";
        String path = "/api/test";
        String result = routingService.buildTargetUrl(serviceUrl, path, null);
        
        assertEquals("http://test-service:8080/api/test", result);
    }

    @Test
    void buildTargetUrl_WithPathAndQuery_ReturnsCorrectUrl() {
        RoutingService routingService = new RoutingService(new RestTemplate());
        String serviceUrl = "http://test-service:8080";
        String path = "/api/test";
        String queryString = "param1=value1&param2=value2";
        String result = routingService.buildTargetUrl(serviceUrl, path, queryString);
        
        assertEquals("http://test-service:8080/api/test?param1=value1&param2=value2", result);
    }

    @Test
    void buildTargetUrl_WithEmptyQuery_ReturnsUrlWithoutQuery() {
        RoutingService routingService = new RoutingService(new RestTemplate());
        String serviceUrl = "http://test-service:8080";
        String path = "/api/test";
        String result = routingService.buildTargetUrl(serviceUrl, path, "");
        
        assertEquals("http://test-service:8080/api/test", result);
    }
}

