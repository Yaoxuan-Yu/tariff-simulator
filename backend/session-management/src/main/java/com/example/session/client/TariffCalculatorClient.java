package com.example.session.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP client for communicating with tariff-calculator service
 */
@Component
public class TariffCalculatorClient {
    
    @Value("${services.tariff-calculator.url:http://localhost:8081}")
    private String tariffCalculatorUrl;
    
    private final RestTemplate restTemplate;
    
    public TariffCalculatorClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Get calculation result by calculation ID
     * Note: This is a placeholder - actual implementation depends on tariff-calculator API
     */
    public Map<String, Object> getCalculationResult(String calculationId) {
        try {
            String url = tariffCalculatorUrl + "/api/tariff/history/" + calculationId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            throw new com.example.session.exception.DataAccessException(
                "Failed to fetch calculation from tariff-calculator service: " + e.getMessage(), e);
        }
    }
}

