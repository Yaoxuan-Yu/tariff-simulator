package com.example.integration.dto;

public class TariffRateDto {
    private String country;
    private String partner;
    private String hsCode;
    private Integer year;
    private double ahsWeighted;
    private double mfnWeighted;

    public TariffRateDto() {}

    public TariffRateDto(String country, String partner, String hsCode, Integer year, double ahsWeighted, double mfnWeighted) {
        this.country = country;
        this.partner = partner;
        this.hsCode = hsCode;
        this.year = year;
        this.ahsWeighted = ahsWeighted;
        this.mfnWeighted = mfnWeighted;
    }

    // getters and setters
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPartner() { return partner; }
    public void setPartner(String partner) { this.partner = partner; }

    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public double getAhsWeighted() { return ahsWeighted; }
    public void setAhsWeighted(double ahsWeighted) { this.ahsWeighted = ahsWeighted; }

    public double getMfnWeighted() { return mfnWeighted; }
    public void setMfnWeighted(double mfnWeighted) { this.mfnWeighted = mfnWeighted; }
}
