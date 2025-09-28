package com.example.tariff.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "\"Product Prices (Test)\"")
public class Product {

    @Id
    @Column(name = "\"ID\"")
    private Long id;

    @Column(name = "\"HS Code\"")
    private String hsCode;

    @Column(name = "\"Product\"")
    private String product;

    @Column(name = "\"Brand\"")
    private String brand;

    @Column(name = "\"ProductCost (USD 1000s)\"")
    private Double productCost;

    @Column(name = "\"Unit\"")
    private String unit;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Double getProductCost() { return productCost; }
    public void setProductCost(Double productCost) { this.productCost = productCost; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
