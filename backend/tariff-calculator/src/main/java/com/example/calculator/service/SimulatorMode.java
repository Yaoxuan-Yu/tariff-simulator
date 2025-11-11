package com.example.calculator.service;
import org.springframework.stereotype.Component;

import com.example.calculator.dto.TariffResponse;

// simulator mode is the mode that is used when the user wants to define their own tariff rules
@Component
public class SimulatorMode implements TariffMode {
    public SimulatorMode(TariffService tariffService) {
    }

    @Override
    public TariffResponse calculate(
        String importCountry,
        String exportCountry,
        String product,
        double quantity,
        String customCost
    ) {
        // Note: SimulatorMode needs HttpSession, but TariffMode interface doesn't support it
        // This will need to be handled differently - either pass session through ModeManager
        // or call calculateWithMode directly from controller
        // For now, this is a placeholder
        throw new com.example.calculator.exception.NotImplementedException("SimulatorMode requires session - use calculateWithMode directly");
    }
}

