package com.example.calculator.dto;

import java.util.List;

/**
 * DTO for time-period tariff comparison (Story 2) Currently uses dummy data,
 * ready for historical implementation
 */
public class TariffHistoryDTO {

    private boolean success;
    private HistoryData data;
    private String error;

    public TariffHistoryDTO() {
    }

    public TariffHistoryDTO(boolean success, HistoryData data) {
        this.success = success;
        this.data = data;
    }

    public TariffHistoryDTO(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public HistoryData getData() {
        return data;
    }

    public void setData(HistoryData data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /**
     * Historical comparison data
     */
    public static class HistoryData {

        private String product;
        private String brand;
        private String exportingFrom;
        private String importingTo;
        private String startDate;
        private String endDate;
        private List<TimePoint> timePoints;
        private ChartData chartData;

        public HistoryData() {
        }

        public HistoryData(String product, String brand, String exportingFrom,
                String importingTo, String startDate, String endDate,
                List<TimePoint> timePoints, ChartData chartData) {
            this.product = product;
            this.brand = brand;
            this.exportingFrom = exportingFrom;
            this.importingTo = importingTo;
            this.startDate = startDate;
            this.endDate = endDate;
            this.timePoints = timePoints;
            this.chartData = chartData;
        }

        // Getters and setters
        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
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

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public List<TimePoint> getTimePoints() {
            return timePoints;
        }

        public void setTimePoints(List<TimePoint> timePoints) {
            this.timePoints = timePoints;
        }

        public ChartData getChartData() {
            return chartData;
        }

        public void setChartData(ChartData chartData) {
            this.chartData = chartData;
        }
    }

    /**
     * Tariff data at a specific point in time
     */
    public static class TimePoint {

        private String date;
        private double tariffRate;
        private String tariffType;
        private double ahsRate;
        private double mfnRate;

        public TimePoint() {
        }

        public TimePoint(String date, double tariffRate, String tariffType,
                double ahsRate, double mfnRate) {
            this.date = date;
            this.tariffRate = tariffRate;
            this.tariffType = tariffType;
            this.ahsRate = ahsRate;
            this.mfnRate = mfnRate;
        }

        // Getters and setters
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public double getTariffRate() {
            return tariffRate;
        }

        public void setTariffRate(double tariffRate) {
            this.tariffRate = tariffRate;
        }

        public String getTariffType() {
            return tariffType;
        }

        public void setTariffType(String tariffType) {
            this.tariffType = tariffType;
        }

        public double getAhsRate() {
            return ahsRate;
        }

        public void setAhsRate(double ahsRate) {
            this.ahsRate = ahsRate;
        }

        public double getMfnRate() {
            return mfnRate;
        }

        public void setMfnRate(double mfnRate) {
            this.mfnRate = mfnRate;
        }
    }

    /**
     * Time-series chart data
     */
    public static class ChartData {

        private List<String> dates;
        private List<Double> tariffRates;
        private List<Double> ahsRates;
        private List<Double> mfnRates;

        public ChartData() {
        }

        public ChartData(List<String> dates, List<Double> tariffRates,
                List<Double> ahsRates, List<Double> mfnRates) {
            this.dates = dates;
            this.tariffRates = tariffRates;
            this.ahsRates = ahsRates;
            this.mfnRates = mfnRates;
        }

        // Getters and setters
        public List<String> getDates() {
            return dates;
        }

        public void setDates(List<String> dates) {
            this.dates = dates;
        }

        public List<Double> getTariffRates() {
            return tariffRates;
        }

        public void setTariffRates(List<Double> tariffRates) {
            this.tariffRates = tariffRates;
        }

        public List<Double> getAhsRates() {
            return ahsRates;
        }

        public void setAhsRates(List<Double> ahsRates) {
            this.ahsRates = ahsRates;
        }

        public List<Double> getMfnRates() {
            return mfnRates;
        }

        public void setMfnRates(List<Double> mfnRates) {
            this.mfnRates = mfnRates;
        }
    }
}

