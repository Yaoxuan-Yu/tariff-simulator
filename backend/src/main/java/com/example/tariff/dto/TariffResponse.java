package com.example.tariff.dto;

public class TariffResponse {
    private String importCountry;
    private String exportCountry;
    private String product;
    private double averageProductCost;
    private double ahsRate;
    private double ahsTariffAmount;
    private double mfnRate;
    private double mfnTariffAmount;

    public TariffResponse(String importCountry, String exportCountry, String product,
                          double averageProductCost,
                          double ahsRate, double ahsTariffAmount,
                          double mfnRate, double mfnTariffAmount) {
        this.importCountry = importCountry;
        this.exportCountry = exportCountry;
        this.product = product;
        this.averageProductCost = averageProductCost;
        this.ahsRate = ahsRate;
        this.ahsTariffAmount = ahsTariffAmount;
        this.mfnRate = mfnRate;
        this.mfnTariffAmount = mfnTariffAmount;
    }

    // Getters only (read-only DTO)
    public String getImportCountry() { return importCountry; }
    public String getExportCountry() { return exportCountry; }
    public String getProduct() { return product; }
    public double getAverageProductCost() { return averageProductCost; }
    public double getAhsRate() { return ahsRate; }
    public double getAhsTariffAmount() { return ahsTariffAmount; }
    public double getMfnRate() { return mfnRate; }
    public double getMfnTariffAmount() { return mfnTariffAmount; }
}
