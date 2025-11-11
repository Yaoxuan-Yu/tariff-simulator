package com.example.calculator.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.calculator.dto.TariffComparisonDTO;
import com.example.calculator.dto.TariffComparisonRequest;
import com.example.calculator.dto.TariffDTO;
import com.example.calculator.dto.TariffHistoryDTO;
import com.example.calculator.service.CurrencyService;
import com.example.calculator.service.TariffComparisonService;
import com.example.calculator.service.TariffService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Tariff Insights", description = "Comparison, history, and currency endpoints")
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TariffInsightsController {

    private final TariffService tariffService;
    private final TariffComparisonService comparisonService;
    private final CurrencyService currencyService;

    public TariffInsightsController(
            TariffService tariffService,
            TariffComparisonService comparisonService,
            CurrencyService currencyService) {
        this.tariffService = tariffService;
        this.comparisonService = comparisonService;
        this.currencyService = currencyService;
    }

    @Operation(summary = "Compare tariffs across multiple importing countries for the same product")
    @PostMapping("/tariffs/compare")
    public ResponseEntity<TariffComparisonDTO> compareMultipleCountries(
            @RequestBody TariffComparisonRequest request) {

        if (request == null) {
            throw new com.example.calculator.exception.BadRequestException("Request body is required");
        }
        if (request.getProduct() == null || request.getProduct().trim().isEmpty()) {
            throw new com.example.calculator.exception.BadRequestException("Product is required");
        }
        if (request.getBrand() == null || request.getBrand().trim().isEmpty()) {
            throw new com.example.calculator.exception.BadRequestException("Brand is required");
        }
        if (request.getExportingFrom() == null || request.getExportingFrom().trim().isEmpty()) {
            throw new com.example.calculator.exception.BadRequestException("Exporting country is required");
        }
        if (request.getImportingToCountries() == null || request.getImportingToCountries().isEmpty()) {
            throw new com.example.calculator.exception.BadRequestException("At least one importing country is required");
        }
        if (request.getQuantity() <= 0) {
            throw new com.example.calculator.exception.BadRequestException("Quantity must be greater than 0");
        }

        TariffComparisonDTO response = comparisonService.compareMultipleCountries(
                request.getProduct(),
                request.getBrand(),
                request.getExportingFrom(),
                request.getImportingToCountries(),
                request.getQuantity(),
                request.getCustomCost(),
                request.getCurrency()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get tariff rate history over a time period (currently using dummy data)")
    @GetMapping("/tariffs/history")
    public ResponseEntity<TariffHistoryDTO> getTariffHistory(
            @RequestParam String product,
            @RequestParam String brand,
            @RequestParam String exportingFrom,
            @RequestParam String importingTo,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        if (product == null || product.trim().isEmpty()) {
            throw new com.example.calculator.exception.BadRequestException("Product is required");
        }
        if (brand == null || brand.trim().isEmpty()) {
            throw new com.example.calculator.exception.BadRequestException("Brand is required");
        }
        if (exportingFrom == null || exportingFrom.trim().isEmpty()) {
            throw new com.example.calculator.exception.BadRequestException("Exporting country is required");
        }
        if (importingTo == null || importingTo.trim().isEmpty()) {
            throw new com.example.calculator.exception.BadRequestException("Importing country is required");
        }

        TariffHistoryDTO response = comparisonService.getTariffHistory(
                product,
                brand,
                exportingFrom,
                importingTo,
                startDate,
                endDate
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get list of supported currencies with rates")
    @GetMapping("/tariffs/currencies")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getSupportedCurrencies() {
        Map<String, Object> currencyData = currencyService.getSupportedCurrencies();

        @SuppressWarnings("unchecked")
        Map<String, String> currencyNames = (Map<String, String>) currencyData.get("currencies");

        @SuppressWarnings("unchecked")
        Map<String, Double> rates = (Map<String, Double>) currencyData.get("rates");

        String lastUpdated = (String) currencyData.get("lastUpdated");
        String formattedDate = lastUpdated != null && !lastUpdated.equals("Never")
                ? lastUpdated.substring(0, 10)
                : LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        List<Map<String, Object>> currencyList = new ArrayList<>();

        for (Map.Entry<String, String> entry : currencyNames.entrySet()) {
            String code = entry.getKey();
            Double rate = rates.getOrDefault(code, 1.0);

            Map<String, Object> currencyInfo = Map.of(
                    "code", code,
                    "name", entry.getValue(),
                    "rate", rate,
                    "lastUpdated", formattedDate
            );

            currencyList.add(currencyInfo);
        }

        return ResponseEntity.ok(Map.of("currency", currencyList));
    }

    @Operation(summary = "Get current exchange rate for a currency")
    @GetMapping("/tariffs/exchange-rate/{currency}")
    public ResponseEntity<Map<String, Object>> getExchangeRate(@PathVariable String currency) {
        double rate = currencyService.getExchangeRate(currency);

        Map<String, Object> response = Map.of(
                "currency", currency,
                "rate", rate,
                "baseCurrency", "USD"
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all tariffs with optional currency conversion")
    @GetMapping("/tariffs")
    public ResponseEntity<List<TariffDTO>> getAllTariffs(
            @RequestParam(required = false) Double productCost,
            @RequestParam(required = false, defaultValue = "USD") String currency) {

        List<TariffDTO> tariffs = tariffService.getAllTariffs(productCost, currency);
        return ResponseEntity.ok(tariffs);
    }

    @Operation(summary = "Get tariffs by country with tariff amounts in selected currency")
    @GetMapping("/tariffs/country/{country}")
    public ResponseEntity<List<TariffDTO>> getTariffsByCountry(
            @PathVariable String country,
            @RequestParam(required = false) Double productCost,
            @RequestParam(required = false, defaultValue = "USD") String currency) {

        List<TariffDTO> tariffs = tariffService.getTariffsByCountry(country, productCost, currency);
        return ResponseEntity.ok(tariffs);
    }

    @Operation(summary = "Get specific tariff by country and partner")
    @GetMapping("/tariffs/{country}/{partner}")
    public ResponseEntity<TariffDTO> getTariffByCountryAndPartner(
            @PathVariable String country,
            @PathVariable String partner,
            @RequestParam(required = false) Double productCost,
            @RequestParam(required = false, defaultValue = "USD") String currency) {

        TariffDTO tariff = tariffService.getTariffByCountryAndPartner(country, partner, productCost, currency);

        if (tariff == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(tariff);

    }
}

