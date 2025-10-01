package com.example.tariff.dto;

// to handle tariff rate information like country, partner, ahsWeighted, mfnWeighted and productCost (optional)
public class TariffRateDto {
    private String country;
    private String partner;
    private double ahsWeighted;
    private double mfnWeighted;
    private Double productCost;

    public TariffRateDto() {}

    public TariffRateDto(String country, String partner, double ahsWeighted, double mfnWeighted) {
        this.country = country;
        this.partner = partner;
        this.ahsWeighted = ahsWeighted;
        this.mfnWeighted = mfnWeighted;
    }

    public TariffRateDto(String country, String partner, double ahsWeighted, double mfnWeighted, Double productCost) {
        this.country = country;
        this.partner = partner;
        this.ahsWeighted = ahsWeighted;
        this.mfnWeighted = mfnWeighted;
        this.productCost = productCost;
    }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPartner() { return partner; }
    public void setPartner(String partner) { this.partner = partner; }
    public double getAhsWeighted() { return ahsWeighted; }
    public void setAhsWeighted(double ahsWeighted) { this.ahsWeighted = ahsWeighted; }
    public double getMfnWeighted() { return mfnWeighted; }
    public void setMfnWeighted(double mfnWeighted) { this.mfnWeighted = mfnWeighted; }
    public Double getProductCost() { return productCost; }
    public void setProductCost(Double productCost) { this.productCost = productCost; }
}
