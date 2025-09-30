package com.example.tariff.dto;
import java.util.List;
public class TariffResponse {
    private boolean success;
    private TariffCalculationData data;
    private String error;
    public TariffResponse() {}
    
    public TariffResponse(boolean success, TariffCalculationData data) {
        this.success = success;
        this.data = data;
    }
    
    public TariffResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }
    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public TariffCalculationData getData() { return data; }
    public void setData(TariffCalculationData data) { this.data = data; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public static class TariffCalculationData {
        private String product;
        private String brand;
        private String exportingFrom;
        private String importingTo;
        private double quantity;
        private String unit;
        private double productCost;
        private double totalCost;
        private double tariffRate;
        private String tariffType;
        private List<BreakdownItem> breakdown;

        public TariffCalculationData() {}

        public TariffCalculationData(String product, String brand, String exportingFrom, String importingTo,
                                   double quantity, String unit, double productCost, double totalCost,
                                   double tariffRate, String tariffType, List<BreakdownItem> breakdown) {
            this.product = product;
            this.brand = brand;
            this.exportingFrom = exportingFrom;
            this.importingTo = importingTo;
            this.quantity = quantity;
            this.unit = unit;
            this.productCost = productCost;
            this.totalCost = totalCost;
            this.tariffRate = tariffRate;
            this.tariffType = tariffType;
            this.breakdown = breakdown;
        }

        // Getters and setters
        public String getProduct() { return product; }
        public void setProduct(String product) { this.product = product; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public String getExportingFrom() { return exportingFrom; }
        public void setExportingFrom(String exportingFrom) { this.exportingFrom = exportingFrom; }
        public String getImportingTo() { return importingTo; }
        public void setImportingTo(String importingTo) { this.importingTo = importingTo; }
        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public double getProductCost() { return productCost; }
        public void setProductCost(double productCost) { this.productCost = productCost; }
        public double getTotalCost() { return totalCost; }
        public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
        public double getTariffRate() { return tariffRate; }
        public void setTariffRate(double tariffRate) { this.tariffRate = tariffRate; }
        public String getTariffType() { return tariffType; }
        public void setTariffType(String tariffType) { this.tariffType = tariffType; }
        public List<BreakdownItem> getBreakdown() { return breakdown; }
        public void setBreakdown(List<BreakdownItem> breakdown) { this.breakdown = breakdown; }
    }

    public static class BreakdownItem {
        private String description;
        private String type;
        private String rate;
        private double amount;
        
        public BreakdownItem() {}
        
        public BreakdownItem(String description, String type, String rate, double amount) {
            this.description = description;
            this.type = type;
            this.rate = rate;
            this.amount = amount;
        }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getRate() { return rate; }
        public void setRate(String rate) { this.rate = rate; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }
}