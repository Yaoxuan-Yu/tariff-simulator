package com.example.tariff.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "\"Tariff Details (Test)\"")
public class Tariff {

    @Id
    @Column(name = "\"Country\"")
    private String country;

    @Column(name = "\"Partner\"")
    private String partner;
    
    @Column(name = "\"AHS (weighted)\"")
    private Double ahsWeighted;
    
    @Column(name = "\"MFN (weighted)\"")
    private Double mfnWeighted;

    // Getters and setters
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPartner() { return partner; }
    public void setPartner(String partner) { this.partner = partner; }

    public Double getAhsWeighted() { return ahsWeighted; }
    public void setAhsWeighted(Double ahsWeighted) { this.ahsWeighted = ahsWeighted; }

    public Double getMfnWeighted() { return mfnWeighted; }
    public void setMfnWeighted(Double mfnWeighted) { this.mfnWeighted = mfnWeighted; }
}
