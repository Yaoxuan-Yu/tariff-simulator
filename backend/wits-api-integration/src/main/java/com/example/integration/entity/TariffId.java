package com.example.integration.entity;

import java.io.Serializable;
import java.util.Objects;

public class TariffId implements Serializable {
    private String country;
    private String partner;
    private String hsCode;
    private int year;

    public TariffId() {}

    public TariffId(String country, String partner, String hsCode, int year) {
        this.country = country;
        this.partner = partner;
        this.hsCode = hsCode;
        this.year = year;
    }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPartner() { return partner; }
    public void setPartner(String partner) { this.partner = partner; }
    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TariffId tariffId = (TariffId) o;
        return year == tariffId.year &&
                Objects.equals(country, tariffId.country) &&
                Objects.equals(partner, tariffId.partner) &&
                Objects.equals(hsCode, tariffId.hsCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, partner, hsCode, year);
    }
}
