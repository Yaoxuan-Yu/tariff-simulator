package com.example.export.client;

import com.example.session.dto.CalculationHistoryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
     * Get calculation history by ID from session-management service
     * Pass session ID in header to ensure session sharing works across microservices
     */
    public CalculationHistoryDto getCalculationById(String sessionId, String calculationId) {
        try {
            // Pass session ID as URL parameter instead of cookie (works for service-to-service calls)
            String url = sessionManagementUrl + "/api/tariff/history/" + calculationId + "?sessionId=" + sessionId;
            
            System.out.println("🌐 Calling session-management: " + url);
            System.out.println("🔑 Session ID param: " + sessionId);
            
            ResponseEntity<CalculationHistoryDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                CalculationHistoryDto.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            throw new com.example.export.exception.DataAccessException(
                "Failed to fetch calculation from session-management service: " + e.getMessage(), e);
        }
    }
    
    /**
     * Remove calculation from session history by ID
     * Pass session ID as URL parameter for cross-service session sharing
     */
    public void removeCalculationById(String sessionId, String calculationId) {
        try {
            // Pass session ID as URL parameter
            String url = sessionManagementUrl + "/api/tariff/history/" + calculationId + "?sessionId=" + sessionId;
            
            System.out.println("🗑️ Calling session-management to remove: " + url);
            
            restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                null,
                Void.class
            );
        } catch (Exception e) {
            throw new com.example.export.exception.DataAccessException(
                "Failed to remove calculation from session-management service: " + e.getMessage(), e);
        }
    }
}

