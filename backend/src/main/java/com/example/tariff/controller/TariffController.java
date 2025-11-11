package com.example.tariff.controller;

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

import com.example.tariff.dto.BrandInfo;
import com.example.tariff.dto.TariffDTO;
import com.example.tariff.dto.TariffDefinitionsResponse;
import com.example.tariff.dto.TariffResponse;
import com.example.tariff.service.CsvExportService;
import com.example.tariff.service.CurrencyService;
import com.example.tariff.service.ModeManager;
import com.example.tariff.service.TariffService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@Tag(name = "Tariffs", description = "A summary of all our API endpoints for tariff calculations and related data")
@RequestMapping("/api")
@CrossOrigin(origins = "*")

// this file exposes api endpoints for the calculations and other stuff
public class TariffController {

    private final TariffService tariffService;
    private final CsvExportService csvExportService;
    private final ModeManager modeManager;
    private final CurrencyService currencyService;

    public TariffController(TariffService tariffService, CsvExportService csvExportService,
            ModeManager modeManager, CurrencyService currencyService) {
        this.tariffService = tariffService;
        this.csvExportService = csvExportService;
        this.modeManager = modeManager;
        this.currencyService = currencyService;
    }

    @Operation(summary = "Calculate tariff rates for importing products between countries")
    @GetMapping("/tariff")
    public ResponseEntity<TariffResponse> calculateTariff(
            @RequestParam String product,
            @RequestParam String brand,
            @RequestParam String exportingFrom,
            @RequestParam String importingTo,
            @RequestParam double quantity,
            @RequestParam(required = false) String customCost,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String userTariffId
    ) {
        // Input validation
        if (product == null || product.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Product is required");
        }
        if (brand == null || brand.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Brand is required");
        }
        if (exportingFrom == null || exportingFrom.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Exporting country is required");
        }
        if (importingTo == null || importingTo.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Importing country is required");
        }
        if (quantity <= 0) {
            throw new com.example.tariff.exception.BadRequestException("Quantity must be greater than 0");
        }
        if (customCost != null && !customCost.trim().isEmpty()) {
            try {
                double cost = Double.parseDouble(customCost);
                if (cost < 0) {
                    throw new com.example.tariff.exception.BadRequestException("Custom cost cannot be negative");
                }
            } catch (NumberFormatException e) {
                throw new com.example.tariff.exception.BadRequestException("Invalid custom cost format");
            }
        }
        if (mode != null && mode.equalsIgnoreCase("user")) {
            modeManager.useSimulatorMode();
        } else {
            modeManager.useGlobalMode();
        }
        TariffResponse response = modeManager.calculate(
                importingTo,
                exportingFrom,
                product,
                brand,
                quantity,
                customCost
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retrieve list of all countries (exporting)")
    @GetMapping("/countries")
    public ResponseEntity<List<String>> getAllCountries() {
        return ResponseEntity.ok(tariffService.getAllCountries());
    }

    @Operation(summary = "Retrieve list of all countries (importing)")
    @GetMapping("/partners")
    public ResponseEntity<List<String>> getAllPartners() {
        return ResponseEntity.ok(tariffService.getAllPartners());
    }

    @Operation(summary = "Retrieve a list of all available products")
    @GetMapping("/products")
    public ResponseEntity<List<String>> getAllProducts() {
        return ResponseEntity.ok(tariffService.getAllProducts());
    }

    @Operation(summary = "Get available brands for a specific product")
    @GetMapping("/brands")
    public ResponseEntity<List<BrandInfo>> getBrandsByProduct(@RequestParam String product) {
        if (product == null || product.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Product is required");
        }
        return ResponseEntity.ok(tariffService.getBrandsByProduct(product));
    }

    @Operation(summary = "Retrieve all tariff definitions (both global and user-defined)")
    @GetMapping("/tariff-definitions")
    public ResponseEntity<TariffDefinitionsResponse> getTariffDefinitions() {
        TariffDefinitionsResponse response = tariffService.getTariffDefinitions();
        return ResponseEntity.ok(response);
    }

    // Separate endpoints for global vs user-defined tariff definitions
    @Operation(summary = "Retrieve only global/system tariff definitions that is currently stored in the database.")
    @GetMapping("/tariff-definitions/global")
    public ResponseEntity<TariffDefinitionsResponse> getGlobalTariffDefinitions() {
        return ResponseEntity.ok(tariffService.getGlobalTariffDefinitions());
    }

    @Operation(summary = "Retrieve only user-defined tariff definitions")
    @GetMapping("/tariff-definitions/user")
    public ResponseEntity<TariffDefinitionsResponse> getUserTariffDefinitions() {
        return ResponseEntity.ok(tariffService.getUserTariffDefinitions());
    }

    @Operation(summary = "Add a new user-defined tariff definition")
    @PostMapping("/tariff-definitions/user")
    public ResponseEntity<TariffDefinitionsResponse> addUserTariffDefinition(@RequestBody TariffDefinitionsResponse.TariffDefinitionDto dto) {
        if (dto == null) {
            throw new com.example.tariff.exception.BadRequestException("Tariff definition data is required");
        }
        return ResponseEntity.ok(tariffService.addUserTariffDefinition(dto));
    }

    @Operation(summary = "Export tariff calculation results as CSV file (feature yet to be implemented)")
    @GetMapping("/export")
    public void exportTariffAsCSV(
            @RequestParam String product,
            @RequestParam String brand,
            @RequestParam String exportingFrom,
            @RequestParam String importingTo,
            @RequestParam double quantity,
            @RequestParam(required = false) String customCost,
            HttpServletResponse response
    ) {
        // Input validation
        if (product == null || product.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Product is required");
        }
        if (brand == null || brand.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Brand is required");
        }
        if (exportingFrom == null || exportingFrom.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Exporting country is required");
        }
        if (importingTo == null || importingTo.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Importing country is required");
        }
        if (quantity <= 0) {
            throw new com.example.tariff.exception.BadRequestException("Quantity must be greater than 0");
        }
        TariffResponse tariffResponse = tariffService.calculate(product, brand, exportingFrom, importingTo, quantity, customCost);
        csvExportService.exportSingleTariffAsCSV(tariffResponse, response);
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

// Currency conversion endpoints
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
