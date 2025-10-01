package com.example.tariff.service;

import com.example.tariff.dto.TariffResponse;

// interface for the tariff modes (global and simulator)
public interface TariffMode {
    TariffResponse calculate(String importCountry, String exportCountry, String product, String brand, double quantity, String customCost);

}
