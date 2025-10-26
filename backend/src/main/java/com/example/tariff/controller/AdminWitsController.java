package com.example.tariff.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tariff.service.api.TariffScheduler;

@RestController
public class AdminWitsController {
    private final TariffScheduler tariffScheduler;

    public AdminWitsController(TariffScheduler tariffScheduler) {
        this.tariffScheduler = tariffScheduler;
    }

    @GetMapping("/admin/test-update-tariffs")
    public String testUpdateTariffs() {
        tariffScheduler.runUpdate();
        return "Test tariffs update triggered!";
    }
}