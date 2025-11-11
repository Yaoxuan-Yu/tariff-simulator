package com.example.tariff.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tariff.dto.TariffComparisonDTO;
import com.example.tariff.dto.TariffHistoryDTO;
import com.example.tariff.entity.Product;
import com.example.tariff.entity.Tariff;
import com.example.tariff.exception.NotFoundException;
import com.example.tariff.exception.ValidationException;
import com.example.tariff.repository.ProductRepository;
import com.example.tariff.repository.TariffRepository;

/**
 * Service for handling multi-country tariff comparisons (Stories 1, 2, 3)
 */
@Service
public class TariffComparisonService {

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CurrencyService currencyService;

    private static final List<String> FTA_COUNTRIES = Arrays.asList(
            "Australia", "China", "Indonesia", "India", "Japan",
            "Malaysia", "Philippines", "Singapore", "Vietnam"
    );

    /**
     * Story 1: Compare tariffs for same product across multiple countries
     */
    public TariffComparisonDTO compareMultipleCountries(
            String product,
            String brand,
            String exportingFrom,
            List<String> importingToCountries,
            double quantity,
            String customCost,
            String currency) {

        try {
            // Validate inputs
            validateComparisonInputs(product, brand, exportingFrom, importingToCountries, quantity);

            // Get product details
            List<Product> products = productRepository.findByNameAndBrand(product, brand);
            if (products.isEmpty()) {
                throw new NotFoundException("Product not found: " + product + " - " + brand);
            }
            Product selectedProduct = products.get(0);

            // Calculate unit cost
            double unitCost = (customCost != null && !customCost.isEmpty())
                    ? Double.parseDouble(customCost)
                    : selectedProduct.getCost();
            double productCost = unitCost * quantity;

            // Target currency
            String targetCurrency = (currency != null && !currency.isEmpty()) ? currency : "USD";

            // Build comparisons for each country
            List<TariffComparisonDTO.CountryComparison> comparisons = new ArrayList<>();
            for (String importingTo : importingToCountries) {
                TariffComparisonDTO.CountryComparison comparison = buildCountryComparison(
                        exportingFrom, importingTo, productCost, targetCurrency
                );
                if (comparison != null) {
                    comparisons.add(comparison);
                }
            }

            if (comparisons.isEmpty()) {
                throw new NotFoundException("No tariff data available for the selected countries");
            }

            // Sort by total cost (rank countries)
            comparisons.sort(Comparator.comparingDouble(TariffComparisonDTO.CountryComparison::getTotalCost));
            for (int i = 0; i < comparisons.size(); i++) {
                comparisons.get(i).setRank(i + 1);
            }

            // Build chart data
            TariffComparisonDTO.ChartData chartData = buildChartData(comparisons);

            // Build response
            TariffComparisonDTO.ComparisonData data = new TariffComparisonDTO.ComparisonData(
                    selectedProduct.getName(),
                    selectedProduct.getBrand(),
                    exportingFrom,
                    quantity,
                    selectedProduct.getUnit(),
                    unitCost,
                    targetCurrency,
                    comparisons,
                    chartData
            );

            return new TariffComparisonDTO(true, data);

        } catch (ValidationException | NotFoundException e) {
            return new TariffComparisonDTO(false, e.getMessage());
        } catch (Exception e) {
            return new TariffComparisonDTO(false, "Comparison failed: " + e.getMessage());
        }
    }

    /**
     * Story 2: View tariff trends over time (currently dummy data)
     */
    public TariffHistoryDTO getTariffHistory(
            String product,
            String brand,
            String exportingFrom,
            String importingTo,
            String startDate,
            String endDate) {

        try {
            // Validate inputs
            if (product == null || product.trim().isEmpty()) {
                throw new ValidationException("Product is required");
            }
            if (brand == null || brand.trim().isEmpty()) {
                throw new ValidationException("Brand is required");
            }
            if (exportingFrom == null || exportingFrom.trim().isEmpty()) {
                throw new ValidationException("Exporting country is required");
            }
            if (importingTo == null || importingTo.trim().isEmpty()) {
                throw new ValidationException("Importing country is required");
            }

            // Verify product exists
            List<Product> products = productRepository.findByNameAndBrand(product, brand);
            if (products.isEmpty()) {
                throw new NotFoundException("Product not found: " + product + " - " + brand);
            }

            // Get current tariff data
            Tariff currentTariff = tariffRepository.findByCountryAndPartner(importingTo, exportingFrom)
                    .orElseThrow(() -> new NotFoundException(
                    "Tariff data not available for " + exportingFrom + " to " + importingTo));

            boolean hasFTA = FTA_COUNTRIES.contains(importingTo) && FTA_COUNTRIES.contains(exportingFrom);
            double currentRate = hasFTA ? currentTariff.getAhsWeighted() : currentTariff.getMfnWeighted();
            String tariffType = hasFTA ? "AHS" : "MFN";

            // Generate dummy historical data (for future implementation)
            List<TariffHistoryDTO.TimePoint> timePoints = generateDummyHistoricalData(
                    startDate, endDate, currentRate, tariffType,
                    currentTariff.getAhsWeighted(), currentTariff.getMfnWeighted()
            );

            // Build chart data
            TariffHistoryDTO.ChartData chartData = buildHistoryChartData(timePoints);

            // Build response
            TariffHistoryDTO.HistoryData data = new TariffHistoryDTO.HistoryData(
                    products.get(0).getName(),
                    products.get(0).getBrand(),
                    exportingFrom,
                    importingTo,
                    startDate != null ? startDate : "2024-01-01",
                    endDate != null ? endDate : LocalDate.now().toString(),
                    timePoints,
                    chartData
            );

            return new TariffHistoryDTO(true, data);

        } catch (ValidationException | NotFoundException e) {
            return new TariffHistoryDTO(false, e.getMessage());
        } catch (Exception e) {
            return new TariffHistoryDTO(false, "History retrieval failed: " + e.getMessage());
        }
    }

    // Helper methods
    private void validateComparisonInputs(String product, String brand, String exportingFrom,
            List<String> importingToCountries, double quantity) {
        if (product == null || product.trim().isEmpty()) {
            throw new ValidationException("Product is required");
        }
        if (brand == null || brand.trim().isEmpty()) {
            throw new ValidationException("Brand is required");
        }
        if (exportingFrom == null || exportingFrom.trim().isEmpty()) {
            throw new ValidationException("Exporting country is required");
        }
        if (importingToCountries == null || importingToCountries.isEmpty()) {
            throw new ValidationException("At least one importing country is required");
        }
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be greater than 0");
        }
    }

    private TariffComparisonDTO.CountryComparison buildCountryComparison(
            String exportingFrom, String importingTo, double productCostUSD, String targetCurrency) {

        Tariff tariff = tariffRepository.findByCountryAndPartner(importingTo, exportingFrom)
                .orElse(null);

        if (tariff == null) {
            return null; // Skip countries with no tariff data
        }

        boolean hasFTA = FTA_COUNTRIES.contains(importingTo) && FTA_COUNTRIES.contains(exportingFrom);
        double tariffRate = hasFTA ? tariff.getAhsWeighted() : tariff.getMfnWeighted();
        String tariffType = hasFTA ? "AHS" : "MFN";

        // Calculate costs in target currency
        double productCost = currencyService.convertFromUSD(productCostUSD, targetCurrency);
        double tariffAmount = currencyService.convertFromUSD(
                (productCostUSD * tariffRate) / 100, targetCurrency
        );
        double totalCost = productCost + tariffAmount;

        return new TariffComparisonDTO.CountryComparison(
                importingTo,
                tariffRate,
                tariffType,
                productCost,
                tariffAmount,
                totalCost,
                hasFTA,
                0 // Will be set after sorting
        );
    }

    private TariffComparisonDTO.ChartData buildChartData(
            List<TariffComparisonDTO.CountryComparison> comparisons) {

        List<String> countries = comparisons.stream()
                .map(TariffComparisonDTO.CountryComparison::getCountry)
                .collect(Collectors.toList());

        List<Double> tariffRates = comparisons.stream()
                .map(TariffComparisonDTO.CountryComparison::getTariffRate)
                .collect(Collectors.toList());

        List<Double> tariffAmounts = comparisons.stream()
                .map(TariffComparisonDTO.CountryComparison::getTariffAmount)
                .collect(Collectors.toList());

        List<Double> totalCosts = comparisons.stream()
                .map(TariffComparisonDTO.CountryComparison::getTotalCost)
                .collect(Collectors.toList());

        List<String> tariffTypes = comparisons.stream()
                .map(TariffComparisonDTO.CountryComparison::getTariffType)
                .collect(Collectors.toList());

        return new TariffComparisonDTO.ChartData(
                countries, tariffRates, tariffAmounts, totalCosts, tariffTypes
        );
    }

    private List<TariffHistoryDTO.TimePoint> generateDummyHistoricalData(
            String startDate, String endDate, double currentRate, String tariffType,
            double ahsRate, double mfnRate) {

        List<TariffHistoryDTO.TimePoint> timePoints = new ArrayList<>();
        LocalDate start = (startDate != null && !startDate.isEmpty())
                ? LocalDate.parse(startDate)
                : LocalDate.now().minusMonths(6);
        LocalDate end = (endDate != null && !endDate.isEmpty())
                ? LocalDate.parse(endDate)
                : LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

        // Generate monthly data points (dummy data with slight variations)
        LocalDate current = start;
        while (!current.isAfter(end)) {
            // Add slight random variation to simulate historical changes
            double variation = (Math.random() - 0.5) * 0.5; // ±0.25%
            double historicalRate = Math.max(0, currentRate + variation);

            timePoints.add(new TariffHistoryDTO.TimePoint(
                    current.format(formatter),
                    historicalRate,
                    tariffType,
                    ahsRate,
                    mfnRate
            ));

            current = current.plusMonths(1);
        }

        return timePoints;
    }

    private TariffHistoryDTO.ChartData buildHistoryChartData(
            List<TariffHistoryDTO.TimePoint> timePoints) {

        List<String> dates = timePoints.stream()
                .map(TariffHistoryDTO.TimePoint::getDate)
                .collect(Collectors.toList());

        List<Double> tariffRates = timePoints.stream()
                .map(TariffHistoryDTO.TimePoint::getTariffRate)
                .collect(Collectors.toList());

        List<Double> ahsRates = timePoints.stream()
                .map(TariffHistoryDTO.TimePoint::getAhsRate)
                .collect(Collectors.toList());

        List<Double> mfnRates = timePoints.stream()
                .map(TariffHistoryDTO.TimePoint::getMfnRate)
                .collect(Collectors.toList());

        return new TariffHistoryDTO.ChartData(dates, tariffRates, ahsRates, mfnRates);
    }
}
