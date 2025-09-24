package com.example.tariff_simulator.dto;

// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Pattern;

//not sure if we need validation, bacause there will be drop down. 

public class TariffRequest {

    // @NotBlank(message = "country is required (ISO alpha-2 code, e.g. IN)")
    private String country1;
    private String country2;

    // Basic HS code pattern: 6 to 10 digits. Adjust if you want different rules.
    // @NotBlank(message = "hsCode is required")
    // @Pattern(regexp = "^\\d{6,10}$", message = "HS code must be 6 to 10 digits")
    private String hsCode;

    public TariffRequest() {}

    public TariffRequest(String country, String hsCode) {
        this.country = country;
        this.hsCode = hsCode;
    }

    public String getCountry1() {
        return country1;
    }

    public String getCountry1() {
        return country2;
    }

    public String getHsCode() {
        return hsCode;
    }

    
}