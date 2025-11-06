package com.example.tariffs.entity;
import jakarta.persistence.*;
import java.io.Serializable;

// retrieves tariff information like country, partner, ahsWeighted and mfnWeighted from the Tariff Rates (Test) table in supabase 
@Entity
@Table(name = "\"Tariff Rates (Test)\"")
@IdClass(TariffId.class)
public class Tariff implements Serializable {
    @Id
    @Column(name = "\"country\"")
    private String country;
    @Id
    @Column(name = "\"partner\"")
    private String partner;
    
    @Column(name = "\"ahs_weighted\"")
    private Double ahsWeighted;
    
    @Column(name = "\"mfn_weighted\"")
    private Double mfnWeighted;

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPartner() { return partner; }
    public void setPartner(String partner) { this.partner = partner; }
    public Double getAhsWeighted() { return ahsWeighted; }
    public void setAhsWeighted(Double ahsWeighted) { this.ahsWeighted = ahsWeighted; }
    public Double getMfnWeighted() { return mfnWeighted; }
    public void setMfnWeighted(Double mfnWeighted) { this.mfnWeighted = mfnWeighted; }
}

