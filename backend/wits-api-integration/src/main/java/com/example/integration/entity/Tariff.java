package com.example.integration.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

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

    @Id
    @Column(name = "\"hs_code\"")
    private String hsCode;

    @Id
    @Column(name = "\"year\"")
    private int year;

    @Column(name = "\"ahs_weighted\"")
    private Double ahsWeighted;

    @Column(name = "\"mfn_weighted\"")
    private Double mfnWeighted;

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPartner() { return partner; }
    public void setPartner(String partner) { this.partner = partner; }
    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public Double getAhsWeighted() { return ahsWeighted; }
    public void setAhsWeighted(Double ahsWeighted) { this.ahsWeighted = ahsWeighted; }
    public Double getMfnWeighted() { return mfnWeighted; }
    public void setMfnWeighted(Double mfnWeighted) { this.mfnWeighted = mfnWeighted; }
}
