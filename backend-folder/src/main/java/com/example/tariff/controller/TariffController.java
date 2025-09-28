package com.example.tariff.controller;

import com.example.tariff.dto.TariffResponse;
import com.example.tariff.service.TariffService;
import com.example.tariff.service.CsvExportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TariffController {

    private final TariffService tariffService;
    private final CsvExportService csvExportService;

    public TariffController(TariffService tariffService, CsvExportService csvExportService) {
        this.tariffService = tariffService;
        this.csvExportService = csvExportService;
    }

    @GetMapping("/tariff")
    public ResponseEntity<TariffResponse> calculateTariff(
            @RequestParam String importCountry,
            @RequestParam String exportCountry,
            @RequestParam String hsCode,
            @RequestParam String brand
    ) {
        TariffResponse response = tariffService.calculate(importCountry, exportCountry, hsCode, brand);
        return ResponseEntity.ok(response);
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

    @GetMapping("/export")
    public void exportTariffAsCSV(
            @RequestParam String importCountry,
            @RequestParam String exportCountry,
            @RequestParam String hsCode,
            @RequestParam String brand,
            HttpServletResponse response
    ) {
        TariffResponse tariffResponse = tariffService.calculate(importCountry, exportCountry, hsCode, brand);
        csvExportService.exportSingleTariffAsCSV(tariffResponse, response);
    }
}
