package com.example.product.dto;

import java.util.List;

public class TariffDefinitionsResponse {
    private boolean success;
    private String message;
    private List<TariffDefinitionDto> data;

    public TariffDefinitionsResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<TariffDefinitionDto> getData() {
        return data;
    }

    public void setData(List<TariffDefinitionDto> data) {
        this.data = data;
    }

    public static class TariffDefinitionDto {
        private String id;
        private String product;
        private String exportingFrom;
        private String importingTo;
        private String type;
        private Double rate;
        private String effectiveDate;
        private String expirationDate;

        public TariffDefinitionDto() {}

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public String getExportingFrom() {
            return exportingFrom;
        }

        public void setExportingFrom(String exportingFrom) {
            this.exportingFrom = exportingFrom;
        }

        public String getImportingTo() {
            return importingTo;
        }

        public void setImportingTo(String importingTo) {
            this.importingTo = importingTo;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Double getRate() {
            return rate;
        }

        public void setRate(Double rate) {
            this.rate = rate;
        }

        public String getEffectiveDate() {
            return effectiveDate;
        }

        public void setEffectiveDate(String effectiveDate) {
            this.effectiveDate = effectiveDate;
        }

        public String getExpirationDate() {
            return expirationDate;
        }

        public void setExpirationDate(String expirationDate) {
            this.expirationDate = expirationDate;
        }
    }
}

