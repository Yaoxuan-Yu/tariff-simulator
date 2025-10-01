package com.example.tariff.service;

import com.example.tariff.dto.TariffResponse;
import org.springframework.stereotype.Component;

// global mode is the default mode that is used when no mode is specified (between global mode and simulator mode)
@Component
public class GlobalMode implements TariffMode{
    private final TariffService tariffService;

    public GlobalMode(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    @Override
    public TariffResponse calculate(String importCountry, String exportCountry, String product, String brand, double quantity, String customCost) {
        return tariffService.calculate(product, brand, exportCountry, importCountry, quantity, customCost);
    }
}
