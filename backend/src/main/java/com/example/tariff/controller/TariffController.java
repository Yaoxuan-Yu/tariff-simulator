package com.example.tariff.controller;

import com.example.tariff.dto.TariffResponse;
import com.example.tariff.service.TariffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TariffController {

    private final TariffService tariffService;

    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    @GetMapping("/tariff")
    public ResponseEntity<?> calculateTariff(
            @RequestParam String importCountry,
            @RequestParam String exportCountry,
            @RequestParam String hsCode,
            @RequestParam String brand
    ) {
        try {
            TariffResponse response = tariffService.calculate(importCountry, exportCountry, hsCode, brand);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/countries")
    public ResponseEntity<List<String>> getAllCountries() {
        return ResponseEntity.ok(tariffService.getAllCountries());
    }

    @GetMapping("/partners")
    public ResponseEntity<List<String>> getAllPartners() {
        return ResponseEntity.ok(tariffService.getAllPartners());
    }

    @GetMapping("/hs-codes")
    public ResponseEntity<List<String>> getAllHsCodes() {
        return ResponseEntity.ok(tariffService.getAllHsCodes());
    }

    @GetMapping("/brands")
    public ResponseEntity<List<String>> getBrandsByHsCode(@RequestParam String hsCode) {
        return ResponseEntity.ok(tariffService.getBrandsByHsCode(hsCode));
    }
}
