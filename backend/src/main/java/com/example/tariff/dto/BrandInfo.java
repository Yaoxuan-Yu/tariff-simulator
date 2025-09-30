package com.example.tariff.dto;
public class BrandInfo {
    private String brand;
    private double cost;
    private String unit;
    public BrandInfo() {}
    public BrandInfo(String brand, double cost, String unit) {
        this.brand = brand;
        this.cost = cost;
        this.unit = unit;
    }
    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public double getCost() {
        return cost;
    }
    public void setCost(double cost) {
        this.cost = cost;
    }
    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
}