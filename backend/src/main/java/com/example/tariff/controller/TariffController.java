package com.example.tariff.controller;

import com.example.tariff.dto.TariffResponse;
import com.example.tariff.service.TariffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tariffs")
public class TariffController {

    private final TariffService tariffService;

    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    @GetMapping
    public ResponseEntity<TariffResponse> getTariff(
            @RequestParam String importCountry,
            @RequestParam String exportCountry,
            @RequestParam String product
    ) {
        return ResponseEntity.ok(tariffService.calculate(importCountry, exportCountry, product));
    }
}
