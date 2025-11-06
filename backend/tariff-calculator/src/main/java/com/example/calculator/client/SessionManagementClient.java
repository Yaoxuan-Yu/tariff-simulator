package com.example.calculator.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP client for communicating with session-management service
 */
@Component
public class SessionManagementClient {
    
    @Value("${services.session-management.url:http://localhost:8082}")
    private String sessionManagementUrl;
    
    private final RestTemplate restTemplate;
    
    public SessionManagementClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Save calculation to session history via HTTP call to session-management service
     * Note: Session is shared via cookies or session ID in header
     */
    public void saveCalculation(String sessionId, Map<String, Object> calculationData) {
        try {
            String url = sessionManagementUrl + "/api/tariff/history/save";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Pass session ID in header for distributed session management
            headers.set("X-Session-Id", sessionId);
            
            // Create request body with calculation data
            Map<String, Object> requestBody = Map.of(
                "calculationData", calculationData
            );
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            restTemplate.postForEntity(url, requestEntity, Void.class);
        } catch (Exception e) {
            // Log error but don't fail the calculation
            System.err.println("Failed to save calculation to session history: " + e.getMessage());
        }
    }
}

