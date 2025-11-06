package com.example.calculator.service;

import com.example.calculator.dto.TariffResponse;

// interface for the tariff modes (global and simulator)
public interface TariffMode {
    TariffResponse calculate(String importCountry, String exportCountry, String product, String brand, double quantity, String customCost);

}

