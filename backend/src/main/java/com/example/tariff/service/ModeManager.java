package com.example.tariff.service;

import org.springframework.stereotype.Service;

import com.example.tariff.dto.TariffResponse;

@Service
public class ModeManager {
    private final GlobalMode globalMode;
    private final SimulatorMode simulatorMode;

    private TariffMode currentMode;

    public ModeManager(GlobalMode globalMode, SimulatorMode simulatorMode) {
        this.globalMode = globalMode;
        this.simulatorMode = simulatorMode;
        this.currentMode = globalMode; // default mode
    }

    // Switch mode dynamically
    public void useGlobalMode() {
        this.currentMode = globalMode;
    }

    public void useSimulatorMode(double ahsRate, double mfnRate) {
        simulatorMode.setRates(ahsRate, mfnRate);
        this.currentMode = simulatorMode;
    }

    public TariffResponse calculate(String importCountry, String exportCountry, String hsCode, String brand) {
        return currentMode.calculate(importCountry, exportCountry, hsCode, brand);
    }
}
