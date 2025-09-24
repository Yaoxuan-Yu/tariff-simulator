package com.example.tariff.dto;

public class TariffResponse {
    private String importCountry;
    private String exportCountry;
    private String hsCode;
    private String brand;
    private double productCost;
    private double ahsRate;
    private double ahsTariffAmount;
    private double mfnRate;
    private double mfnTariffAmount;

    public TariffResponse(String importCountry, String exportCountry, String hsCode, String brand,
                          double productCost,
                          double ahsRate, double ahsTariffAmount,
                          double mfnRate, double mfnTariffAmount) {
        this.importCountry = importCountry;
        this.exportCountry = exportCountry;
        this.hsCode = hsCode;
        this.brand = brand;
        this.productCost = productCost;
        this.ahsRate = ahsRate;
        this.ahsTariffAmount = ahsTariffAmount;
        this.mfnRate = mfnRate;
        this.mfnTariffAmount = mfnTariffAmount;
    }

    public String getImportCountry() { return importCountry; }
    public String getExportCountry() { return exportCountry; }
    public String getHsCode() { return hsCode; }
    public String getBrand() { return brand; }
    public double getProductCost() { return productCost; }
    public double getAhsRate() { return ahsRate; }
    public double getAhsTariffAmount() { return ahsTariffAmount; }
    public double getMfnRate() { return mfnRate; }
    public double getMfnTariffAmount() { return mfnTariffAmount; }
}
