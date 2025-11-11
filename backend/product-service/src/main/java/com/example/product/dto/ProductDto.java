package com.example.product.dto;

// to handle product information like name, cost and unit
public class ProductDto {
    private String name;
    private double cost;
    private String unit;

    public ProductDto() {}

    public ProductDto(String name, double cost, String unit) {
        this.name = name;
        this.cost = cost;
        this.unit = unit;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}

