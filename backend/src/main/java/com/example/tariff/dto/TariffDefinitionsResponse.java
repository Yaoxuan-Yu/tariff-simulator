package com.example.tariff.dto;

import java.util.List;

public class TariffDefinitionsResponse {
    private boolean success;
    private List<TariffDefinitionDto> data;
    private String error;

    public TariffDefinitionsResponse() {}

    public TariffDefinitionsResponse(boolean success, List<TariffDefinitionDto> data) {
        this.success = success;
        this.data = data;
    }

    public TariffDefinitionsResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public List<TariffDefinitionDto> getData() { return data; }
    public void setData(List<TariffDefinitionDto> data) { this.data = data; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public static class TariffDefinitionDto {
        private String id;
        private String product;
        private String exportingFrom;
        private String importingTo;
        private String type;
        private double rate;
        private String effectiveDate;
        private String expirationDate;

        public TariffDefinitionDto() {}

        public TariffDefinitionDto(String id, String product, String exportingFrom, String importingTo,
                                 String type, double rate, String effectiveDate, String expirationDate) {
            this.id = id;
            this.product = product;
            this.exportingFrom = exportingFrom;
            this.importingTo = importingTo;
            this.type = type;
            this.rate = rate;
            this.effectiveDate = effectiveDate;
            this.expirationDate = expirationDate;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getProduct() { return product; }
        public void setProduct(String product) { this.product = product; }
        public String getExportingFrom() { return exportingFrom; }
        public void setExportingFrom(String exportingFrom) { this.exportingFrom = exportingFrom; }
        public String getImportingTo() { return importingTo; }
        public void setImportingTo(String importingTo) { this.importingTo = importingTo; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public double getRate() { return rate; }
        public void setRate(double rate) { this.rate = rate; }
        public String getEffectiveDate() { return effectiveDate; }
        public void setEffectiveDate(String effectiveDate) { this.effectiveDate = effectiveDate; }
        public String getExpirationDate() { return expirationDate; }
        public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    }
}
