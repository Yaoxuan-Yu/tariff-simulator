package com.example.tariff.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String product;
    private String brand;
    private Double productCost;
    private String unit;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Double getProductCost() { return productCost; }
    public void setProductCost(Double productCost) { this.productCost = productCost; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
