package com.example.calculator.entity;
import java.io.Serializable;
import java.util.Objects;

// handles composite key for the Tariff entity which is the combination of country and partner
public class TariffId implements Serializable {
    private String country;
    private String partner;
    public TariffId() {}
    public TariffId(String country, String partner) {
        this.country = country;
        this.partner = partner;
    }
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
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TariffId tariffId = (TariffId) o;
        return Objects.equals(country, tariffId.country) && Objects.equals(partner, tariffId.partner);
    }
    @Override
    public int hashCode() {
        return Objects.hash(country, partner);
    }
}

