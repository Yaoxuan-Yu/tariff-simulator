package com.example.calculator.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.calculator.client.SessionManagementClient;
import com.example.calculator.dto.TariffResponse;
import com.example.calculator.service.TariffService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;

// routes tariff calculation requests
@RestController
@Tag(name = "Tariff Calculation", description = "API endpoints for tariff calculations")
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TariffCalculationController {
    
    private static final String USER_MODE = "user";
    private static final double MIN_QUANTITY = 0.0;

    private final TariffService tariffService;
    private final SessionManagementClient sessionManagementClient;

    public TariffCalculationController(
            TariffService tariffService,
            SessionManagementClient sessionManagementClient) {
        this.tariffService = tariffService;
        this.sessionManagementClient = sessionManagementClient;
    }

    // GET /api/tariff -> calculate tariff rates for importing products
    @Operation(summary = "Calculate tariff rates for importing products between countries")
    @GetMapping("/tariff")
    public ResponseEntity<TariffResponse> calculateTariff(
            @RequestParam String product,
            @RequestParam String exportingFrom,
            @RequestParam String importingTo,
            @RequestParam double quantity,
            @RequestParam(required = false) String customCost,
            @RequestParam(required = false, defaultValue = "USD") String currency,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String userTariffId,
            HttpSession session) {

        // Input validation
        if (product == null || product.trim().isEmpty()) {
            throw new com.example.calculator.exception.BadRequestException("Product is required");
        }
        if (exportingFrom == null || exportingFrom.trim().isEmpty()) {
            throw new com.example.calculator.exception.BadRequestException("Exporting country is required");
        }
        if (importingTo == null || importingTo.trim().isEmpty()) {
            throw new com.example.calculator.exception.BadRequestException("Importing country is required");
        }
        if (quantity <= MIN_QUANTITY) {
            throw new com.example.calculator.exception.BadRequestException("Quantity must be greater than 0");
        }
        if (customCost != null && !customCost.trim().isEmpty()) {
            try {
                double cost = Double.parseDouble(customCost);
                if (cost < 0) {
                    throw new com.example.calculator.exception.BadRequestException("Custom cost cannot be negative");
                }
            } catch (NumberFormatException e) {
                throw new com.example.calculator.exception.BadRequestException("Invalid custom cost format");
            }
        }

        TariffResponse response;
        if (mode != null && mode.equalsIgnoreCase(USER_MODE)) {
            // for simulator mode, use session-based tariffs
            response = tariffService.calculateWithMode(
                product,
                exportingFrom,
                importingTo,
                quantity,
                customCost,
                currency,
                USER_MODE,
                userTariffId,
                session
            );
        } else {
            // default: global mode using database tariffs
            response = tariffService.calculate(
                product,
                exportingFrom,
                importingTo,
                quantity,
                customCost,
                currency
            );
        }

        // save to session history via HTTP call to session-management service
        if (response.isSuccess() && response.getData() != null) {
            try {
                // convert TariffResponse to Map for HTTP call
                Map<String, Object> calculationData = new HashMap<>();
                calculationData.put("success", true);

                Map<String, Object> dataMap = new HashMap<>();
                TariffResponse.TariffCalculationData data = response.getData();
                dataMap.put("product", data.getProduct());
                dataMap.put("exportingFrom", data.getExportingFrom());
                dataMap.put("importingTo", data.getImportingTo());
                dataMap.put("quantity", data.getQuantity());
                dataMap.put("unit", data.getUnit());
                dataMap.put("productCost", data.getProductCost());
                dataMap.put("totalCost", data.getTotalCost());
                dataMap.put("tariffRate", data.getTariffRate());
                dataMap.put("tariffType", data.getTariffType());
                dataMap.put("currency", data.getCurrency());

                calculationData.put("data", dataMap);

                // call session-management service via HTTP
                // note: in distributed sessions, session ID is passed via header
                sessionManagementClient.saveCalculation(session.getId(), calculationData);
            } catch (Exception e) {
                System.err.println("Failed to save calculation to session history: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(response);
    }
}

