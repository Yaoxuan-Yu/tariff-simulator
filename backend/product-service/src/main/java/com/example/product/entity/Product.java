package com.example.product.entity;
import jakarta.persistence.*;

// retrieves product information like id, hsCode, name, cost and unit from the Products (Test) table in supabase
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
    @Column(name = "\"product_cost_usd\"")
    private Double cost;
    @Column(name = "\"unit\"")
    private String unit;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}

