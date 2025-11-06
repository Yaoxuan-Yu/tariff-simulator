package com.example.calculator.service;
import com.example.calculator.dto.TariffResponse;
import org.springframework.stereotype.Component;

// simulator mode is the mode that is used when the user wants to define their own tariff rules
@Component
public class SimulatorMode implements TariffMode {
    private final TariffService tariffService;

    public SimulatorMode(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    @Override
    public TariffResponse calculate(
        String importCountry,
        String exportCountry,
        String product,
        String brand,
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

