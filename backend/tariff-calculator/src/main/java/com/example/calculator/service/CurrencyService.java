package com.example.calculator.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// service for handling currency conversions with caching and fallback
@Service
public class CurrencyService {

    private static final String DEFAULT_CURRENCY = "USD";
    private static final double DEFAULT_RATE = 1.0;
    private static final Duration CACHE_DURATION = Duration.ofHours(1);
    private static final String DEFAULT_API_KEY = "a10e795b9ec46cfbbd874b19";
    private static final String API_BASE_URL = "https://v6.exchangerate-api.com/v6/";

    private final RestTemplate restTemplate;

    @Value("${exchange.rate.api.key:" + DEFAULT_API_KEY + "}")
    private String apiKey;

    // cache for exchange rates
    private Map<String, Double> cachedRates = new HashMap<>();
    private LocalDateTime lastUpdated;

    // fallback rates in case API fails (as of Jan 2025)
    private static final Map<String, Double> FALLBACK_RATES = Map.of(
            "USD", 1.0,
            "AUD", 1.52,
            "INR", 83.12,
            "CNY", 7.25,
            "JPY", 149.50,
            "SGD", 1.34,
            "PHP", 56.50,
            "IDR", 15750.0,
            "MYR", 4.48,
            "VND", 24350.0
    );

    public CurrencyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // get exchange rate from USD to target currency
    public double getExchangeRate(String targetCurrency) {
        if (targetCurrency == null || targetCurrency.equals(DEFAULT_CURRENCY)) {
            return DEFAULT_RATE;
        }

        // Check if cache is valid
        if (isCacheValid()) {
            Double rate = cachedRates.get(targetCurrency.toUpperCase());
            if (rate != null) {
                return rate;
            }
        }

        // Try to fetch real-time rates
        try {
            Map<String, Double> freshRates = fetchRealTimeRates();
            if (freshRates != null && !freshRates.isEmpty()) {
                cachedRates = freshRates;
                lastUpdated = LocalDateTime.now();
                Double rate = cachedRates.get(targetCurrency.toUpperCase());
                if (rate != null) {
                    return rate;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch real-time rates: " + e.getMessage());
        }

        // fallback to static rates
        return FALLBACK_RATES.getOrDefault(targetCurrency.toUpperCase(), DEFAULT_RATE);
    }

    // convert amount from USD to target currency
    public double convertFromUSD(double amountInUSD, String targetCurrency) {
        if (amountInUSD == 0.0) {
            return 0.0;
        }
        double rate = getExchangeRate(targetCurrency);
        return amountInUSD * rate;
    }

    // check if cached rates are still valid
    private boolean isCacheValid() {
        if (lastUpdated == null || cachedRates.isEmpty()) {
            return false;
        }
        return Duration.between(lastUpdated, LocalDateTime.now()).compareTo(CACHE_DURATION) < 0;
    }

    // fetch real-time exchange rates from external API
    private Map<String, Double> fetchRealTimeRates() {
        try {
            String apiUrl = API_BASE_URL + apiKey + "/latest/USD";
            ExchangeRateResponse response = restTemplate.getForObject(apiUrl, ExchangeRateResponse.class);

            if (response != null && "success".equals(response.getResult())) {
                System.out.println("Successfully fetched real-time exchange rates");
                return response.getConversionRates();
            }

            System.err.println("API response was not successful");
            return null;

        } catch (RestClientException e) {
            System.err.println("Error calling exchange rate API: " + e.getMessage());
            return null;
        }
    }

    // get all supported currencies with current rates and last updated time
    public Map<String, Object> getSupportedCurrencies() {
        Map<String, Object> result = new HashMap<>();

        // Ensure we have fresh rates
        if (!isCacheValid()) {
            try {
                Map<String, Double> freshRates = fetchRealTimeRates();
                if (freshRates != null && !freshRates.isEmpty()) {
                    cachedRates = freshRates;
                    lastUpdated = LocalDateTime.now();
                }
            } catch (Exception e) {
                System.err.println("Failed to refresh rates: " + e.getMessage());
            }
        }

        // Currency list with rates
        Map<String, String> currencyNames = Map.of(
                "USD", "US Dollar",
                "AUD", "Australian Dollar",
                "INR", "Indian Rupee",
                "CNY", "Chinese Yuan",
                "JPY", "Japanese Yen",
                "SGD", "Singapore Dollar",
                "PHP", "Philippine Peso",
                "IDR", "Indonesian Rupiah",
                "MYR", "Malaysian Ringgit",
                "VND", "Vietnamese Dong"
        );

        result.put("currencies", currencyNames);
        result.put("rates", cachedRates.isEmpty() ? FALLBACK_RATES : cachedRates);
        result.put("lastUpdated", lastUpdated != null ? lastUpdated.toString() : "Never");

        return result;
    }

    // response class for ExchangeRate-API
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ExchangeRateResponse {

        private String result;
        private Map<String, Double> conversion_rates;

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public Map<String, Double> getConversionRates() {
            return conversion_rates;
        }

        public void setConversion_rates(Map<String, Double> conversion_rates) {
            this.conversion_rates = conversion_rates;
        }
    }
}
