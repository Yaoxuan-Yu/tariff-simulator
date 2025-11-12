package com.example.calculator.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.calculator.dto.TariffComparisonDTO;
import com.example.calculator.dto.TariffHistoryDTO;
import com.example.calculator.entity.Product;
import com.example.calculator.entity.Tariff;
import com.example.calculator.exception.NotFoundException;
import com.example.calculator.exception.ValidationException;
import com.example.calculator.repository.ProductRepository;
import com.example.calculator.repository.TariffRepository;

// service for handling multi-country tariff comparisons and history
@Service
public class TariffComparisonService {

    private static final String DEFAULT_CURRENCY = "USD";
    private static final double PERCENTAGE_DIVISOR = 100.0;
    private static final double MIN_QUANTITY = 0.0;
    private static final double VARIATION_FACTOR = 0.5;
    private static final int MONTHS_BACK_DEFAULT = 6;

    private final TariffRepository tariffRepository;
    private final ProductRepository productRepository;
    private final CurrencyService currencyService;

    public TariffComparisonService(
            TariffRepository tariffRepository,
            ProductRepository productRepository,
            CurrencyService currencyService) {
        this.tariffRepository = tariffRepository;
        this.productRepository = productRepository;
        this.currencyService = currencyService;
    }

    private static final List<String> FTA_COUNTRIES = Arrays.asList(
            "Australia", "China", "Indonesia", "India", "Japan",
            "Malaysia", "Philippines", "Singapore", "Vietnam"
    );

    // compare tariffs for same product across multiple countries
    public TariffComparisonDTO compareMultipleCountries(
            String product,
            String exportingFrom,
            List<String> importingToCountries,
            double quantity,
            String customCost,
            String currency) {

        try {
            // Validate inputs
            validateComparisonInputs(product, exportingFrom, importingToCountries, quantity);

            // Get product details
            List<Product> products = productRepository.findByName(product);
            if (products == null || products.isEmpty()) {
                throw new NotFoundException("Product not found: " + product);
            }
            Product selectedProduct = products.get(0);

            // Calculate unit cost
            double unitCost = (customCost != null && !customCost.isEmpty())
                    ? Double.parseDouble(customCost)
                    : selectedProduct.getCost();
            double productCost = unitCost * quantity;

            // target currency
            String targetCurrency = (currency != null && !currency.isEmpty()) ? currency.toUpperCase() : DEFAULT_CURRENCY;
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

    // get tariff history over time (currently using dummy data)
    public TariffHistoryDTO getTariffHistory(
            String product,
            String exportingFrom,
            String importingTo,
            String startDate,
            String endDate) {

        try {
            // Validate inputs
            if (product == null || product.trim().isEmpty()) {
                throw new ValidationException("Product is required");
            }
            if (exportingFrom == null || exportingFrom.trim().isEmpty()) {
                throw new ValidationException("Exporting country is required");
            }
            if (importingTo == null || importingTo.trim().isEmpty()) {
                throw new ValidationException("Importing country is required");
            }

            // Verify product exists
            List<Product> products = productRepository.findByName(product);
            if (products == null || products.isEmpty()) {
                throw new NotFoundException("Product not found: " + product);
            }
            Product selectedProduct = products.get(0);

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
                    selectedProduct.getName(),
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

    public List<Map<String, Object>> getTariffTrends(
            List<String> importCountries,
            List<String> exportCountries,
            List<String> products,
            String startDate,
            String endDate) {

        if (importCountries == null || importCountries.isEmpty()) {
            throw new ValidationException("At least one importing country is required");
        }
        if (exportCountries == null || exportCountries.isEmpty()) {
            throw new ValidationException("At least one exporting country is required");
        }
        if (products == null || products.isEmpty()) {
            throw new ValidationException("At least one product is required");
        }

        List<Map<String, Object>> seriesList = new ArrayList<>();

        for (String importCountry : importCountries) {
            for (String exportCountry : exportCountries) {
                for (String product : products) {
                    List<Product> productEntities = productRepository.findByName(product);
                    if (productEntities == null || productEntities.isEmpty()) {
                        continue; // Skip unknown products
                    }

                    Tariff tariff = tariffRepository.findByCountryAndPartner(importCountry, exportCountry)
                            .orElse(null);
                    if (tariff == null) {
                        continue; // Skip missing tariff data
                    }

                    boolean hasFTA = FTA_COUNTRIES.contains(importCountry) && FTA_COUNTRIES.contains(exportCountry);
                    Double ahs = tariff.getAhsWeighted();
                    Double mfn = tariff.getMfnWeighted();

                    double baseRate;
                    if (Boolean.TRUE.equals(hasFTA)) {
                        baseRate = ahs != null ? ahs : (mfn != null ? mfn : 0.0);
                    } else {
                        baseRate = mfn != null ? mfn : (ahs != null ? ahs : 0.0);
                    }

                    if (baseRate == 0.0 && ahs == null && mfn == null) {
                        continue; // No usable rate values
                    }

                    // Generate historical data (placeholder)
                    List<TariffHistoryDTO.TimePoint> timePoints = generateDummyHistoricalData(
                            startDate, endDate, baseRate, hasFTA ? "AHS" : "MFN",
                            ahs != null ? ahs : baseRate,
                            mfn != null ? mfn : baseRate);

                    List<Map<String, Object>> dataPoints = new ArrayList<>();
                    for (TariffHistoryDTO.TimePoint point : timePoints) {
                        Map<String, Object> pointMap = new HashMap<>();
                        pointMap.put("date", point.getDate());
                        pointMap.put("rate", point.getTariffRate());
                        pointMap.put("ahsRate", point.getAhsRate());
                        pointMap.put("mfnRate", point.getMfnRate());
                        dataPoints.add(pointMap);
                    }

                    Map<String, Object> series = new HashMap<>();
                    series.put("importCountry", importCountry);
                    series.put("exportCountry", exportCountry);
                    series.put("product", product);
                    series.put("dataPoints", dataPoints);
                    seriesList.add(series);
                }
            }
        }

        return seriesList;
    }

    // Helper methods
    private void validateComparisonInputs(String product, String exportingFrom,
            List<String> importingToCountries, double quantity) {
        if (product == null || product.trim().isEmpty()) {
            throw new ValidationException("Product is required");
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

        // calculate costs in target currency
        double productCost = currencyService.convertFromUSD(productCostUSD, targetCurrency);
        double tariffAmount = currencyService.convertFromUSD(
                (productCostUSD * tariffRate) / PERCENTAGE_DIVISOR, targetCurrency
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
                : LocalDate.now().minusMonths(MONTHS_BACK_DEFAULT);
        LocalDate end = (endDate != null && !endDate.isEmpty())
                ? LocalDate.parse(endDate)
                : LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

        // generate monthly data points (dummy data with slight variations)
        LocalDate current = start;
        while (!current.isAfter(end)) {
            // add slight random variation to simulate historical changes
            double variation = (Math.random() - 0.5) * VARIATION_FACTOR; // ±0.25%
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
