package com.example.calculator.dto;

/**
 * DTO for returning tariff data with calculated amounts in selected currency
 */
public class TariffDTO {

    private String country;
    private String partner;
    private Double ahsWeightedPercentage;
    private Double mfnWeightedPercentage;
    private Double ahsTariffAmount;  // Calculated tariff amount in selected currency
    private Double mfnTariffAmount;  // Calculated tariff amount in selected currency
    private String currency;

    public TariffDTO() {
    }

    public TariffDTO(String country, String partner, Double ahsWeightedPercentage,
            Double mfnWeightedPercentage, Double ahsTariffAmount,
            Double mfnTariffAmount, String currency) {
        this.country = country;
        this.partner = partner;
        this.ahsWeightedPercentage = ahsWeightedPercentage;
        this.mfnWeightedPercentage = mfnWeightedPercentage;
        this.ahsTariffAmount = ahsTariffAmount;
        this.mfnTariffAmount = mfnTariffAmount;
        this.currency = currency;
    }

    // Getters and Setters
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public Double getAhsWeightedPercentage() {
        return ahsWeightedPercentage;
    }

    public void setAhsWeightedPercentage(Double ahsWeightedPercentage) {
        this.ahsWeightedPercentage = ahsWeightedPercentage;
    }

    public Double getMfnWeightedPercentage() {
        return mfnWeightedPercentage;
    }

    public void setMfnWeightedPercentage(Double mfnWeightedPercentage) {
        this.mfnWeightedPercentage = mfnWeightedPercentage;
    }

    public Double getAhsTariffAmount() {
        return ahsTariffAmount;
    }

    public void setAhsTariffAmount(Double ahsTariffAmount) {
        this.ahsTariffAmount = ahsTariffAmount;
    }

    public Double getMfnTariffAmount() {
        return mfnTariffAmount;
    }

    public void setMfnTariffAmount(Double mfnTariffAmount) {
        this.mfnTariffAmount = mfnTariffAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}

