package com.example.tariff.service;
import com.example.tariff.dto.TariffResponse;
import org.springframework.stereotype.Component;

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
        // Reuse TariffService user-defined logic via calculateWithMode to avoid duplication
        return tariffService.calculateWithMode(
            product,
            brand,
            exportCountry,
            importCountry,
            quantity,
            customCost,
            "user",
            null
        );
    }
}

