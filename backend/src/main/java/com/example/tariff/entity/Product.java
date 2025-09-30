package com.example.tariff.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "\"Products (Test)\"")
public class Product {
    @Id
    @Column(name = "\"id\"")
    private Long id;
    @Column(name = "\"hs_code\"")
    private String hsCode;
    @Column(name = "\"product\"")
    private String name;
    @Column(name = "\"brand\"")
    private String brand;
    @Column(name = "\"product_cost_usd\"")
    private Double cost;
    @Column(name = "\"unit\"")
    private String unit;
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
