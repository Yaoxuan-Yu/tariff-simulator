package com.example.calculator.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.calculator.dto.TariffDefinitionsResponse;
import com.example.calculator.dto.TariffResponse;
import com.example.calculator.entity.Product;
import com.example.calculator.entity.Tariff;
import com.example.calculator.repository.ProductRepository;
import com.example.calculator.repository.TariffRepository;
import com.example.calculator.service.SessionTariffService;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

// has main business logic including tariff calculations and fta logic (ahs vs mfn)
@Service
public class TariffService {
    private final TariffRepository tariffRepository;
    private final ProductRepository productRepository;
    private final SessionTariffService sessionTariffService;
    
    public TariffService(TariffRepository tariffRepository, ProductRepository productRepository, 
                        SessionTariffService sessionTariffService) {
        this.tariffRepository = tariffRepository;
        this.productRepository = productRepository;
        this.sessionTariffService = sessionTariffService;
    }

    public TariffResponse calculateWithMode(
        String productName,
        String brand,
        String exportingFrom,
        String importingTo,
        double quantity,
        String customCost,
        String mode,
        String userTariffId,
        jakarta.servlet.http.HttpSession session
    ) {
        if (mode != null && mode.equalsIgnoreCase("user")) {
            // Use session tariffs for simulator mode (if session available)
            List<TariffDefinitionsResponse.TariffDefinitionDto> tariffsToSearch = 
                (session != null) ? sessionTariffService.getTariffDefinitions(session) : new ArrayList<>();
            
            TariffDefinitionsResponse.TariffDefinitionDto selected = findMatchingUserTariff(
                userTariffId, productName, exportingFrom, importingTo, tariffsToSearch
            );
            if (selected == null) {
                return new TariffResponse(false, "Selected user-defined tariff not found or not applicable");
            }

            try {
                List<Product> products = productRepository.findByNameAndBrand(productName, brand);
                if (products.isEmpty()) {
                    return new TariffResponse(false, "Product not found in database");
                }

                Product selectedProduct = products.get(0);
                double unitCost = customCost != null && !customCost.isEmpty() ?
                    Double.parseDouble(customCost) : selectedProduct.getCost();
                double productCost = unitCost * quantity;

                double tariffRate = selected.getRate();
                double tariffAmount = (productCost * tariffRate) / 100;
                double totalCost = productCost + tariffAmount;

                List<TariffResponse.BreakdownItem> breakdown = new ArrayList<>();
                breakdown.add(new TariffResponse.BreakdownItem(
                    "Product Cost", "Base Cost", "100%", productCost));
                breakdown.add(new TariffResponse.BreakdownItem(
                    "Import Tariff (" + selected.getType() + ")",
                    "Tariff",
                    String.format("%.2f%%", tariffRate),
                    tariffAmount));

                TariffResponse.TariffCalculationData data = new TariffResponse.TariffCalculationData(
                    selectedProduct.getName(),
                    selectedProduct.getBrand(),
                    exportingFrom,
                    importingTo,
                    quantity,
                    selectedProduct.getUnit(),
                    productCost,
                    totalCost,
                    tariffRate,
                    selected.getType() + " (user-defined)",
                    breakdown
                );

                return new TariffResponse(true, data);
            } catch (Exception e) {
                return new TariffResponse(false, "An unexpected error occurred during calculation: " + e.getMessage());
            }
        }

        // default: global
        return calculate(productName, brand, exportingFrom, importingTo, quantity, customCost);
    }

    private TariffDefinitionsResponse.TariffDefinitionDto findMatchingUserTariff(
        String userTariffId,
        String product,
        String exportingFrom,
        String importingTo,
        List<TariffDefinitionsResponse.TariffDefinitionDto> tariffsToSearch
    ) {
        // If an explicit ID is provided, prefer exact match
        if (userTariffId != null && !userTariffId.isEmpty()) {
            for (TariffDefinitionsResponse.TariffDefinitionDto dto : tariffsToSearch) {
                if (userTariffId.equals(dto.getId())
                    && product.equals(dto.getProduct())
                    && exportingFrom.equals(dto.getExportingFrom())
                    && importingTo.equals(dto.getImportingTo())) {
                    return dto;
                }
            }
            return null;
        }
        // Otherwise, pick the first applicable user-defined tariff for the route and product
        for (TariffDefinitionsResponse.TariffDefinitionDto dto : tariffsToSearch) {
            if (product.equals(dto.getProduct())
                && exportingFrom.equals(dto.getExportingFrom())
                && importingTo.equals(dto.getImportingTo())) {
                return dto;
            }
        }
        return null;
    }

    public TariffResponse calculate(String productName, String brand, String exportingFrom, String importingTo, 
                                   double quantity, String customCost) {
        try {
            // Validate inputs
            if (productName == null || productName.trim().isEmpty()) {
                throw new com.example.calculator.exception.ValidationException("Product name is required");
            }
            if (brand == null || brand.trim().isEmpty()) {
                throw new com.example.calculator.exception.ValidationException("Brand is required");
            }
            if (exportingFrom == null || exportingFrom.trim().isEmpty()) {
                throw new com.example.calculator.exception.ValidationException("Exporting country is required");
            }
            if (importingTo == null || importingTo.trim().isEmpty()) {
                throw new com.example.calculator.exception.ValidationException("Importing country is required");
            }
            if (quantity <= 0) {
                throw new com.example.calculator.exception.ValidationException("Quantity must be greater than 0");
            }

            List<Product> products = productRepository.findByNameAndBrand(productName, brand);
            if (products.isEmpty()) {
                throw new com.example.calculator.exception.NotFoundException("Product not found: " + productName + " - " + brand);
            }
            
            Product selectedProduct = products.get(0);

            Tariff tariff = tariffRepository.findByCountryAndPartner(importingTo, exportingFrom)
                    .orElse(null);
            
            if (tariff == null) {
                throw new com.example.calculator.exception.NotFoundException("Tariff data not available for " + exportingFrom + " to " + importingTo);
            }

            double unitCost = customCost != null && !customCost.isEmpty() ? 
                Double.parseDouble(customCost) : selectedProduct.getCost();
            double productCost = unitCost * quantity;
            

            boolean hasFTAStatus = hasFTA(importingTo, exportingFrom);
            double tariffRate = hasFTAStatus ? tariff.getAhsWeighted() : tariff.getMfnWeighted();

            double tariffAmount = (productCost * tariffRate) / 100;
            double totalCost = productCost + tariffAmount;

            List<TariffResponse.BreakdownItem> breakdown = new ArrayList<>();
            breakdown.add(new TariffResponse.BreakdownItem(
                "Product Cost", "Base Cost", "100%", productCost));
            breakdown.add(new TariffResponse.BreakdownItem(
                "Import Tariff (" + (hasFTAStatus ? "AHS" : "MFN") + ")", 
                "Tariff", 
                String.format("%.2f%%", tariffRate), 
                tariffAmount));
            // Create TariffCalculationData
            TariffResponse.TariffCalculationData data = new TariffResponse.TariffCalculationData(
                selectedProduct.getName(),
                selectedProduct.getBrand(),
                exportingFrom,
                importingTo,
                quantity,
                selectedProduct.getUnit(),
                productCost,
                totalCost,
                tariffRate,
                hasFTAStatus ? "AHS (with FTA)" : "MFN (no FTA)",
                breakdown
            );
            
            return new TariffResponse(true, data);
        } catch (com.example.calculator.exception.ValidationException | com.example.calculator.exception.NotFoundException e) {
            throw e; // Re-throw validation and not found exceptions
        } catch (NumberFormatException e) {
            throw new com.example.calculator.exception.ValidationException("Invalid custom cost format: " + customCost);
        } catch (Exception e) {
            throw new com.example.calculator.exception.DataAccessException("Database error during tariff calculation", e);
        }
    }

    private boolean hasFTA(String importCountry, String exportCountry) {
        List<String> ftaCountries = Arrays.asList(
            "Australia", "China", "Indonesia", "India", "Japan", 
            "Malaysia", "Philippines", "Singapore", "Vietnam"
        );
        
        return ftaCountries.contains(importCountry) && ftaCountries.contains(exportCountry);
    }
}

