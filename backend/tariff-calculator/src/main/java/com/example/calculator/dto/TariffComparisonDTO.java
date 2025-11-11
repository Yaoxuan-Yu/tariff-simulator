package com.example.calculator.dto;

import java.util.List;

/**
 * DTO for multi-country tariff comparison responses
 */
public class TariffComparisonDTO {

    private boolean success;
    private ComparisonData data;
    private String error;

    public TariffComparisonDTO() {
    }

    public TariffComparisonDTO(boolean success, ComparisonData data) {
        this.success = success;
        this.data = data;
    }

    public TariffComparisonDTO(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ComparisonData getData() {
        return data;
    }

    public void setData(ComparisonData data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /**
     * Main comparison data structure
     */
    public static class ComparisonData {

        private String product;
        private String exportingFrom;
        private double quantity;
        private String unit;
        private double productCostPerUnit;
        private String currency;
        private List<CountryComparison> comparisons;
        private ChartData chartData;

        public ComparisonData() {
        }

        public ComparisonData(String product, String exportingFrom,
                double quantity, String unit, double productCostPerUnit,
                String currency, List<CountryComparison> comparisons,
                ChartData chartData) {
            this.product = product;
            this.exportingFrom = exportingFrom;
            this.quantity = quantity;
            this.unit = unit;
            this.productCostPerUnit = productCostPerUnit;
            this.currency = currency;
            this.comparisons = comparisons;
            this.chartData = chartData;
        }

        // Getters and setters
        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public String getExportingFrom() {
            return exportingFrom;
        }

        public void setExportingFrom(String exportingFrom) {
            this.exportingFrom = exportingFrom;
        }

        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public double getProductCostPerUnit() {
            return productCostPerUnit;
        }

        public void setProductCostPerUnit(double productCostPerUnit) {
            this.productCostPerUnit = productCostPerUnit;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public List<CountryComparison> getComparisons() {
            return comparisons;
        }

        public void setComparisons(List<CountryComparison> comparisons) {
            this.comparisons = comparisons;
        }

        public ChartData getChartData() {
            return chartData;
        }

        public void setChartData(ChartData chartData) {
            this.chartData = chartData;
        }
    }

    /**
     * Individual country comparison data
     */
    public static class CountryComparison {

        private String country;
        private double tariffRate;
        private String tariffType;
        private double productCost;
        private double tariffAmount;
        private double totalCost;
        private boolean hasFTA;
        private int rank; // 1 = cheapest

        public CountryComparison() {
        }

        public CountryComparison(String country, double tariffRate, String tariffType,
                double productCost, double tariffAmount, double totalCost,
                boolean hasFTA, int rank) {
            this.country = country;
            this.tariffRate = tariffRate;
            this.tariffType = tariffType;
            this.productCost = productCost;
            this.tariffAmount = tariffAmount;
            this.totalCost = totalCost;
            this.hasFTA = hasFTA;
            this.rank = rank;
        }

        // Getters and setters
        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public double getTariffRate() {
            return tariffRate;
        }

        public void setTariffRate(double tariffRate) {
            this.tariffRate = tariffRate;
        }

        public String getTariffType() {
            return tariffType;
        }

        public void setTariffType(String tariffType) {
            this.tariffType = tariffType;
        }

        public double getProductCost() {
            return productCost;
        }

        public void setProductCost(double productCost) {
            this.productCost = productCost;
        }

        public double getTariffAmount() {
            return tariffAmount;
        }

        public void setTariffAmount(double tariffAmount) {
            this.tariffAmount = tariffAmount;
        }

        public double getTotalCost() {
            return totalCost;
        }

        public void setTotalCost(double totalCost) {
            this.totalCost = totalCost;
        }

        public boolean isHasFTA() {
            return hasFTA;
        }

        public void setHasFTA(boolean hasFTA) {
            this.hasFTA = hasFTA;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }
    }

    /**
     * Chart-ready visualization data
     */
    public static class ChartData {

        private List<String> countries;
        private List<Double> tariffRates;
        private List<Double> tariffAmounts;
        private List<Double> totalCosts;
        private List<String> tariffTypes;

        public ChartData() {
        }

        public ChartData(List<String> countries, List<Double> tariffRates,
                List<Double> tariffAmounts, List<Double> totalCosts,
                List<String> tariffTypes) {
            this.countries = countries;
            this.tariffRates = tariffRates;
            this.tariffAmounts = tariffAmounts;
            this.totalCosts = totalCosts;
            this.tariffTypes = tariffTypes;
        }

        // Getters and setters
        public List<String> getCountries() {
            return countries;
        }

        public void setCountries(List<String> countries) {
            this.countries = countries;
        }

        public List<Double> getTariffRates() {
            return tariffRates;
        }

        public void setTariffRates(List<Double> tariffRates) {
            this.tariffRates = tariffRates;
        }

        public List<Double> getTariffAmounts() {
            return tariffAmounts;
        }

        public void setTariffAmounts(List<Double> tariffAmounts) {
            this.tariffAmounts = tariffAmounts;
        }

        public List<Double> getTotalCosts() {
            return totalCosts;
        }

        public void setTotalCosts(List<Double> totalCosts) {
            this.totalCosts = totalCosts;
        }

        public List<String> getTariffTypes() {
            return tariffTypes;
        }

        public void setTariffTypes(List<String> tariffTypes) {
            this.tariffTypes = tariffTypes;
        }
    }
}

