package com.example.calculator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.calculator.dto.TariffDefinitionsResponse;
import com.example.calculator.dto.TariffDTO;
import com.example.calculator.dto.TariffResponse;
import com.example.calculator.entity.Product;
import com.example.calculator.entity.Tariff;
import com.example.calculator.repository.ProductRepository;
import com.example.calculator.repository.TariffRepository;

import jakarta.servlet.http.HttpSession;

// main business logic for tariff calculations and FTA logic
@Service
public class TariffService {
    
    private static final String DEFAULT_CURRENCY = "USD";
    private static final String USER_MODE = "user";
    private static final double PERCENTAGE_DIVISOR = 100.0;
    private static final double DEFAULT_COST = 0.0;
    private static final String USER_DEFINED_SUFFIX = " (user-defined)";
    private static final String AHS_WITH_FTA = "AHS (with FTA)";
    private static final String MFN_NO_FTA = "MFN (no FTA)";
    private static final String BASE_COST_LABEL = "Product Cost";
    private static final String BASE_COST_TYPE = "Base Cost";
    private static final String BASE_COST_PERCENTAGE = "100%";
    private static final String TARIFF_LABEL_PREFIX = "Import Tariff (";
    private static final String TARIFF_TYPE = "Tariff";
    
    private static final List<String> FTA_COUNTRIES = List.of(
            "Australia", "China", "Indonesia", "India", "Japan",
            "Malaysia", "Philippines", "Singapore", "Vietnam"
    );

    private final TariffRepository tariffRepository;
    private final ProductRepository productRepository;
    private final SessionTariffService sessionTariffService;
    private final CurrencyService currencyService;

    public TariffService(
            TariffRepository tariffRepository,
            ProductRepository productRepository,
            SessionTariffService sessionTariffService,
            CurrencyService currencyService) {
        this.tariffRepository = tariffRepository;
        this.productRepository = productRepository;
        this.sessionTariffService = sessionTariffService;
        this.currencyService = currencyService;
    }

    // calculate tariff with mode support (user-defined or global)
    public TariffResponse calculateWithMode(
            String productName,
            String exportingFrom,
            String importingTo,
            double quantity,
            String customCost,
            String currency,
            String mode,
            String userTariffId,
            HttpSession session) {

        if (mode != null && mode.equalsIgnoreCase(USER_MODE)) {
            List<TariffDefinitionsResponse.TariffDefinitionDto> tariffsToSearch =
                    (session != null) ? sessionTariffService.getTariffDefinitions(session) : new ArrayList<>();

            TariffDefinitionsResponse.TariffDefinitionDto selected = findMatchingUserTariff(
                    userTariffId, productName, exportingFrom, importingTo, tariffsToSearch
            );
            if (selected == null) {
                return new TariffResponse(false, "Selected user-defined tariff not found or not applicable");
            }

            try {
                // 🔹 Find product by name only
                List<Product> products = productRepository.findByName(productName);
                if (products.isEmpty()) {
                    return new TariffResponse(false, "Product not found in database");
                }

                Product selectedProduct = products.get(0);
                Double dbCost = selectedProduct.getCost();
                double unitCost = (customCost != null && !customCost.isEmpty())
                        ? Double.parseDouble(customCost)
                        : (dbCost != null ? dbCost : DEFAULT_COST);

                double productCostUSD = unitCost * quantity;
                double tariffRate = selected.getRate();
                double tariffAmountUSD = (productCostUSD * tariffRate) / PERCENTAGE_DIVISOR;
                double totalCostUSD = productCostUSD + tariffAmountUSD;

                String targetCurrency = (currency != null && !currency.isEmpty()) ? currency.toUpperCase() : DEFAULT_CURRENCY;

                double productCost = currencyService.convertFromUSD(productCostUSD, targetCurrency);
                double tariffAmount = currencyService.convertFromUSD(tariffAmountUSD, targetCurrency);
                double totalCost = currencyService.convertFromUSD(totalCostUSD, targetCurrency);

                List<TariffResponse.BreakdownItem> breakdown = new ArrayList<>();
                breakdown.add(new TariffResponse.BreakdownItem(BASE_COST_LABEL, BASE_COST_TYPE, BASE_COST_PERCENTAGE, productCost));
                breakdown.add(new TariffResponse.BreakdownItem(
                        TARIFF_LABEL_PREFIX + selected.getType() + ")",
                        TARIFF_TYPE,
                        String.format("%.2f%%", tariffRate),
                        tariffAmount));

                TariffResponse.TariffCalculationData data = new TariffResponse.TariffCalculationData(
                        selectedProduct.getName(),
                        exportingFrom,
                        importingTo,
                        quantity,
                        selectedProduct.getUnit(),
                        productCost,
                        totalCost,
                        tariffRate,
                        selected.getType() + USER_DEFINED_SUFFIX,
                        breakdown,
                        targetCurrency
                );

                return new TariffResponse(true, data);
            } catch (NumberFormatException e) {
                return new TariffResponse(false, "Unexpected error during calculation: " + e.getMessage());
            }
        }

        // default: global mode
        return calculate(productName, exportingFrom, importingTo, quantity, customCost, currency);
    }

    // find matching user-defined tariff from session tariffs
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

    // calculate tariff using global tariff data (FTA-aware)
    public TariffResponse calculate(String productName, String exportingFrom,
                                    String importingTo, double quantity, String customCost, String currency) {
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

            // find product by name only
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
                    : (dbCost != null ? dbCost : DEFAULT_COST);
            double productCostUSD = unitCost * quantity;

            boolean hasFTAStatus = hasFTA(importingTo, exportingFrom);
            double tariffRate = hasFTAStatus ? tariff.getAhsWeighted() : tariff.getMfnWeighted();

            double tariffAmountUSD = (productCostUSD * tariffRate) / PERCENTAGE_DIVISOR;
            double totalCostUSD = productCostUSD + tariffAmountUSD;

            String targetCurrency = (currency != null && !currency.isEmpty()) ? currency.toUpperCase() : DEFAULT_CURRENCY;

            double productCost = currencyService.convertFromUSD(productCostUSD, targetCurrency);
            double tariffAmount = currencyService.convertFromUSD(tariffAmountUSD, targetCurrency);
            double totalCost = currencyService.convertFromUSD(totalCostUSD, targetCurrency);

            List<TariffResponse.BreakdownItem> breakdown = new ArrayList<>();
            breakdown.add(new TariffResponse.BreakdownItem(BASE_COST_LABEL, BASE_COST_TYPE, BASE_COST_PERCENTAGE, productCost));
            breakdown.add(new TariffResponse.BreakdownItem(
                    TARIFF_LABEL_PREFIX + (hasFTAStatus ? "AHS" : "MFN") + ")",
                    TARIFF_TYPE,
                    String.format("%.2f%%", tariffRate),
                    tariffAmount));

            TariffResponse.TariffCalculationData data = new TariffResponse.TariffCalculationData(
                    selectedProduct.getName(),
                    exportingFrom,
                    importingTo,
                    quantity,
                    selectedProduct.getUnit(),
                    productCost,
                    totalCost,
                    tariffRate,
                    hasFTAStatus ? AHS_WITH_FTA : MFN_NO_FTA,
                    breakdown,
                    targetCurrency
            );

            return new TariffResponse(true, data);
        } catch (com.example.calculator.exception.ValidationException |
                 com.example.calculator.exception.NotFoundException e) {
            throw e;
        } catch (NumberFormatException e) {
            throw new com.example.calculator.exception.ValidationException("Invalid custom cost format: " + customCost);
        }
    }

    // check if both countries are in FTA list
    private boolean hasFTA(String importCountry, String exportCountry) {
        return FTA_COUNTRIES.contains(importCountry) && FTA_COUNTRIES.contains(exportCountry);
    }

    // get all tariffs with optional currency conversion
    public List<TariffDTO> getAllTariffs(Double productCostUSD, String currency) {
        String targetCurrency = (currency != null && !currency.isEmpty()) ? currency : DEFAULT_CURRENCY;
        double costUSD = (productCostUSD != null) ? productCostUSD : DEFAULT_COST;
        List<Tariff> tariffs = tariffRepository.findAll();

        return tariffs.stream()
                .map(tariff -> convertTariffToDTO(tariff, costUSD, targetCurrency))
                .collect(Collectors.toList());
    }

    // get tariffs filtered by importing country
    public List<TariffDTO> getTariffsByCountry(String country, Double productCostUSD, String currency) {
        String targetCurrency = (currency != null && !currency.isEmpty()) ? currency : DEFAULT_CURRENCY;
        double costUSD = (productCostUSD != null) ? productCostUSD : DEFAULT_COST;
        List<Tariff> tariffs = tariffRepository.findByCountry(country);

        return tariffs.stream()
                .map(tariff -> convertTariffToDTO(tariff, costUSD, targetCurrency))
                .collect(Collectors.toList());
    }

    // get specific tariff by country and partner
    public TariffDTO getTariffByCountryAndPartner(String country, String partner,
                                                  Double productCostUSD, String currency) {
        String targetCurrency = (currency != null && !currency.isEmpty()) ? currency : DEFAULT_CURRENCY;
        double costUSD = (productCostUSD != null) ? productCostUSD : DEFAULT_COST;
        Tariff tariff = tariffRepository.findByCountryAndPartner(country, partner)
                .orElse(null);

        if (tariff == null) {
            return null;
        }

        return convertTariffToDTO(tariff, costUSD, targetCurrency);
    }

    // convert tariff entity to DTO with currency conversion
    private TariffDTO convertTariffToDTO(Tariff tariff, double productCostUSD, String targetCurrency) {
        Double ahsTariffAmount = null;
        Double mfnTariffAmount = null;

        if (productCostUSD > 0) {
            if (tariff.getAhsWeighted() != null) {
                ahsTariffAmount = calculateTariffAmount(
                        productCostUSD,
                        tariff.getAhsWeighted(),
                        targetCurrency
                );
            }

            if (tariff.getMfnWeighted() != null) {
                mfnTariffAmount = calculateTariffAmount(
                        productCostUSD,
                        tariff.getMfnWeighted(),
                        targetCurrency
                );
            }
        }

        return new TariffDTO(
                tariff.getCountry(),
                tariff.getPartner(),
                tariff.getAhsWeighted(),
                tariff.getMfnWeighted(),
                ahsTariffAmount,
                mfnTariffAmount,
                targetCurrency
        );
    }

    // calculate tariff amount in target currency
    private double calculateTariffAmount(double productCostUSD, double tariffRatePercent, String targetCurrency) {
        if (productCostUSD == 0.0 || tariffRatePercent == 0.0) {
            return 0.0;
        }

        double tariffAmountUSD = productCostUSD * (tariffRatePercent / PERCENTAGE_DIVISOR);
        return currencyService.convertFromUSD(tariffAmountUSD, targetCurrency);
    }
}
