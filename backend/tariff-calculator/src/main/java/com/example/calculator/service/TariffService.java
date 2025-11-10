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

import jakarta.servlet.http.HttpSession;

// Main business logic for tariff calculations and FTA logic
@Service
public class TariffService {
    private final TariffRepository tariffRepository;
    private final ProductRepository productRepository;
    private final SessionTariffService sessionTariffService;

    public TariffService(
            TariffRepository tariffRepository,
            ProductRepository productRepository,
            SessionTariffService sessionTariffService) {
        this.tariffRepository = tariffRepository;
        this.productRepository = productRepository;
        this.sessionTariffService = sessionTariffService;
    }

    public TariffResponse calculateWithMode(
            String productName,
            String exportingFrom,
            String importingTo,
            double quantity,
            String customCost,
            String mode,
            String userTariffId,
            HttpSession session) {

        if (mode != null && mode.equalsIgnoreCase("user")) {
            List<TariffDefinitionsResponse.TariffDefinitionDto> tariffsToSearch =
                    (session != null) ? sessionTariffService.getTariffDefinitions(session) : new ArrayList<>();

            TariffDefinitionsResponse.TariffDefinitionDto selected = findMatchingUserTariff(
                    userTariffId, productName, exportingFrom, importingTo, tariffsToSearch
            );
            if (selected == null) {
                return new TariffResponse(false, "Selected user-defined tariff not found or not applicable");
            }

            try {
                // 🔹 Find product by name only (brand removed)
                List<Product> products = productRepository.findByName(productName);
                if (products.isEmpty()) {
                    return new TariffResponse(false, "Product not found in database");
                }

                Product selectedProduct = products.get(0);
                Double dbCost = selectedProduct.getCost();
                double unitCost = (customCost != null && !customCost.isEmpty())
                        ? Double.parseDouble(customCost)
                        : (dbCost != null ? dbCost : 0.0);

                double productCost = unitCost * quantity;
                double tariffRate = selected.getRate();
                double tariffAmount = (productCost * tariffRate) / 100;
                double totalCost = productCost + tariffAmount;

                List<TariffResponse.BreakdownItem> breakdown = new ArrayList<>();
                breakdown.add(new TariffResponse.BreakdownItem("Product Cost", "Base Cost", "100%", productCost));
                breakdown.add(new TariffResponse.BreakdownItem(
                        "Import Tariff (" + selected.getType() + ")",
                        "Tariff",
                        String.format("%.2f%%", tariffRate),
                        tariffAmount));

                TariffResponse.TariffCalculationData data = new TariffResponse.TariffCalculationData(
                        selectedProduct.getName(),
                        null, // brand removed
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
            } catch (NumberFormatException e) {
                return new TariffResponse(false, "Unexpected error during calculation: " + e.getMessage());
            }
        }

        // default: global mode
        return calculate(productName, exportingFrom, importingTo, quantity, customCost);
    }

    private TariffDefinitionsResponse.TariffDefinitionDto findMatchingUserTariff(
            String userTariffId,
            String product,
            String exportingFrom,
            String importingTo,
            List<TariffDefinitionsResponse.TariffDefinitionDto> tariffsToSearch) {

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

        for (TariffDefinitionsResponse.TariffDefinitionDto dto : tariffsToSearch) {
            if (product.equals(dto.getProduct())
                    && exportingFrom.equals(dto.getExportingFrom())
                    && importingTo.equals(dto.getImportingTo())) {
                return dto;
            }
        }
        return null;
    }

    public TariffResponse calculate(String productName, String exportingFrom,
                                    String importingTo, double quantity, String customCost) {
        try {
            if (productName == null || productName.trim().isEmpty()) {
                throw new com.example.calculator.exception.ValidationException("Product name is required");
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

            // Find product by name only
            List<Product> products = productRepository.findByName(productName);
            if (products.isEmpty()) {
                throw new com.example.calculator.exception.NotFoundException("Product not found: " + productName);
            }

            Product selectedProduct = products.get(0);
            Tariff tariff = tariffRepository.findByCountryAndPartner(importingTo, exportingFrom).orElse(null);

            if (tariff == null) {
                throw new com.example.calculator.exception.NotFoundException(
                        "Tariff data not available for " + exportingFrom + " → " + importingTo);
            }

            Double dbCost = selectedProduct.getCost();
            double unitCost = (customCost != null && !customCost.isEmpty())
                    ? Double.parseDouble(customCost)
                    : (dbCost != null ? dbCost : 0.0);
            double productCost = unitCost * quantity;

            boolean hasFTAStatus = hasFTA(importingTo, exportingFrom);
            double tariffRate = hasFTAStatus ? tariff.getAhsWeighted() : tariff.getMfnWeighted();

            double tariffAmount = (productCost * tariffRate) / 100;
            double totalCost = productCost + tariffAmount;

            List<TariffResponse.BreakdownItem> breakdown = new ArrayList<>();
            breakdown.add(new TariffResponse.BreakdownItem("Product Cost", "Base Cost", "100%", productCost));
            breakdown.add(new TariffResponse.BreakdownItem(
                    "Import Tariff (" + (hasFTAStatus ? "AHS" : "MFN") + ")",
                    "Tariff",
                    String.format("%.2f%%", tariffRate),
                    tariffAmount));

            TariffResponse.TariffCalculationData data = new TariffResponse.TariffCalculationData(
                    selectedProduct.getName(),
                    null, // brand removed
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
        } catch (com.example.calculator.exception.ValidationException |
                 com.example.calculator.exception.NotFoundException e) {
            throw e;
        } catch (NumberFormatException e) {
            throw new com.example.calculator.exception.ValidationException("Invalid custom cost format: " + customCost);
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
