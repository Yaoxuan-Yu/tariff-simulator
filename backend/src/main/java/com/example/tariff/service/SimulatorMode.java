package com.example.tariff.service;
import com.example.tariff.dto.TariffResponse;
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

