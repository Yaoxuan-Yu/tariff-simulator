package com.example.calculator.dto;

import java.util.List;

public class TariffComparisonRequest {

    private String product;
    private String brand;
    private String exportingFrom;
    private List<String> importingToCountries;
    private double quantity;
    private String customCost;
    private String currency;

    public TariffComparisonRequest() {
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getExportingFrom() {
        return exportingFrom;
    }

    public void setExportingFrom(String exportingFrom) {
        this.exportingFrom = exportingFrom;
    }

    public List<String> getImportingToCountries() {
        return importingToCountries;
    }

    public void setImportingToCountries(List<String> importingToCountries) {
        this.importingToCountries = importingToCountries;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getCustomCost() {
        return customCost;
    }

    public void setCustomCost(String customCost) {
        this.customCost = customCost;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}

