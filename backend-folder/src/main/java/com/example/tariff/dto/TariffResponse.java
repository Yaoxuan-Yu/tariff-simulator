package com.example.tariff.dto;

public class TariffResponse {
    private String importCountry;
    private String exportCountry;
    private String hsCode;
    private String brand;
    private double ahsRate;
    private double mfnRate;

    public TariffResponse(String importCountry, String exportCountry, String hsCode, String brand,
                          double ahsRate, double mfnRate) {
        this.importCountry = importCountry;
        this.exportCountry = exportCountry;
        this.hsCode = hsCode;
        this.brand = brand;
        this.ahsRate = ahsRate;
        this.mfnRate = mfnRate;
    }

    public String getImportCountry() { return importCountry; }
    public String getExportCountry() { return exportCountry; }
    public String getHsCode() { return hsCode; }
    public String getBrand() { return brand; }
    public double getAhsRate() { return ahsRate; }
    public double getMfnRate() { return mfnRate; }
}
