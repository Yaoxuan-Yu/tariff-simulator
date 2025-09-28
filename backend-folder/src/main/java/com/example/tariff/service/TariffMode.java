package com.example.tariff.service;

import com.example.tariff.dto.TariffResponse;

public interface TariffMode {
    TariffResponse calculate(String importCountry, String exportCountry, String hsCode, String brand);
}
