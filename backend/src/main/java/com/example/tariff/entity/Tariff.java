package com.example.tariff.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tariffs")
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String country;
    private String partner;
    private Double ahsWeighted;
    private Double mfnWeighted;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPartner() { return partner; }
    public void setPartner(String partner) { this.partner = partner; }

    public Double getAhsWeighted() { return ahsWeighted; }
    public void setAhsWeighted(Double ahsWeighted) { this.ahsWeighted = ahsWeighted; }

    public Double getMfnWeighted() { return mfnWeighted; }
    public void setMfnWeighted(Double mfnWeighted) { this.mfnWeighted = mfnWeighted; }
}
