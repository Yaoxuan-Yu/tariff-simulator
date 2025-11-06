package com.example.export.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CalculationHistoryDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String productName;
    private String brand;
    private String exportingFrom;
    private String importingTo;
    private Double quantity;
    private String unit;
    private Double productCost;
    private Double tariffRate;
    private Double tariffAmount;
    private Double totalCost;
    private String tariffType;
    private LocalDateTime createdAt;


    public CalculationHistoryDto() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public CalculationHistoryDto(String productName, String brand, String exportingFrom,
                                String importingTo, Double quantity, String unit,
                                Double productCost, Double tariffRate, Double tariffAmount,
                                Double totalCost, String tariffType) {
        this.id = java.util.UUID.randomUUID().toString();
        this.productName = productName;
        this.brand = brand;
        this.exportingFrom = exportingFrom;
        this.importingTo = importingTo;
        this.quantity = quantity;
        this.unit = unit;
        this.productCost = productCost;
        this.tariffRate = tariffRate;
        this.tariffAmount = tariffAmount;
        this.totalCost = totalCost;
        this.tariffType = tariffType;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getExportingFrom() { return exportingFrom; }
    public void setExportingFrom(String exportingFrom) { this.exportingFrom = exportingFrom; }

    public String getImportingTo() { return importingTo; }
    public void setImportingTo(String importingTo) { this.importingTo = importingTo; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Double getProductCost() { return productCost; }
    public void setProductCost(Double productCost) { this.productCost = productCost; }

    public Double getTariffRate() { return tariffRate; }
    public void setTariffRate(Double tariffRate) { this.tariffRate = tariffRate; }

    public Double getTariffAmount() { return tariffAmount; }
    public void setTariffAmount(Double tariffAmount) { this.tariffAmount = tariffAmount; }

    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }

    public String getTariffType() { return tariffType; }
    public void setTariffType(String tariffType) { this.tariffType = tariffType; }

  

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}

