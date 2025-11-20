package com.example.api.gateway.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.example.api.gateway.service.RoutingService;

public class TariffRoutingControllerTest {

    @Test
    void tariffRoutingController_Exists() {
        assertNotNull(TariffRoutingController.class);
    }

    @Test
    void tariffRoutingController_CanBeInstantiated() {
        RoutingService routingService = new RoutingService(new org.springframework.web.client.RestTemplate());
        TariffRoutingController controller = new TariffRoutingController(routingService);
        
        assertNotNull(controller);
    }
}

