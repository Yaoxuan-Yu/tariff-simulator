package com.example.integration.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.integration.service.TariffScheduler;

// routes admin requests for triggering WITS API tariff updates
@RestController
public class WitsIntegrationController {
    
    private static final String UPDATE_TRIGGERED_MSG = "Tariff update triggered, running in background!";

    private final TariffScheduler tariffScheduler;

    public WitsIntegrationController(TariffScheduler tariffScheduler) {
        this.tariffScheduler = tariffScheduler;
    }

    // GET /admin/test-update-tariffs -> trigger manual tariff update from WITS API
    @GetMapping("/admin/test-update-tariffs")
    public String testUpdateTariffs() {
        new Thread(() -> tariffScheduler.runUpdate()).start();
        return UPDATE_TRIGGERED_MSG;
    }
}

