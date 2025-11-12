package com.example.integration.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.integration.service.TariffScheduler;

// NOTE: This controller matches the original AdminWitsController from the main service
@RestController
public class WitsIntegrationController {
    private final TariffScheduler tariffScheduler;

    public WitsIntegrationController(TariffScheduler tariffScheduler) {
        this.tariffScheduler = tariffScheduler;
    }

    @GetMapping("/admin/test-update-tariffs")
    public String testUpdateTariffs() {
        new Thread(() -> tariffScheduler.runUpdate()).start();
        return "Tariff update triggered, running in background!";
    }

}

