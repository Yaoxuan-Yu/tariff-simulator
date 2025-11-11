package com.example.calculator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import com.example.calculator.dto.TariffResponse;
import com.example.calculator.service.TariffService;
import com.example.calculator.client.SessionManagementClient;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Tariff Calculation", description = "API endpoints for tariff calculations")
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TariffCalculationController {
    private final TariffService tariffService;
    private final SessionManagementClient sessionManagementClient;

    public TariffCalculationController(
            TariffService tariffService,
            SessionManagementClient sessionManagementClient) {
        this.tariffService = tariffService;
        this.sessionManagementClient = sessionManagementClient;
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
            @RequestParam(required = false) String userTariffId,
            HttpSession session) {
        
        // Input validation
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
        if (quantity <= 0) {
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
        if (mode != null && mode.equalsIgnoreCase("user")) {
            // For simulator mode, call TariffService directly with session to use session-based tariffs
            response = tariffService.calculateWithMode(
                product,
                brand,
                exportingFrom,
                importingTo,
                quantity,
                customCost,
                "user",
                userTariffId,
                session
            );
        } else {
            response = tariffService.calculate(
                product,
                brand,
                exportingFrom,
                importingTo,
                quantity,
                customCost
            );
        }

        // SAVE TO SESSION HISTORY via HTTP call to session-management service
        if (response.isSuccess() && response.getData() != null) {
            try {
                // Convert TariffResponse to Map for HTTP call
                Map<String, Object> calculationData = new HashMap<>();
                calculationData.put("success", true);
                
                Map<String, Object> dataMap = new HashMap<>();
                TariffResponse.TariffCalculationData data = response.getData();
                dataMap.put("product", data.getProduct());
                dataMap.put("brand", data.getBrand());
                dataMap.put("exportingFrom", data.getExportingFrom());
                dataMap.put("importingTo", data.getImportingTo());
                dataMap.put("quantity", data.getQuantity());
                dataMap.put("unit", data.getUnit());
                dataMap.put("productCost", data.getProductCost());
                dataMap.put("totalCost", data.getTotalCost());
                dataMap.put("tariffRate", data.getTariffRate());
                dataMap.put("tariffType", data.getTariffType());
                
                calculationData.put("data", dataMap);
                
                // Call session-management service via HTTP
                // Note: In distributed sessions, session ID is passed via header
                sessionManagementClient.saveCalculation(session.getId(), calculationData);
            } catch (Exception e) {
                System.err.println("Failed to save calculation to session history: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(response);
    }
}

