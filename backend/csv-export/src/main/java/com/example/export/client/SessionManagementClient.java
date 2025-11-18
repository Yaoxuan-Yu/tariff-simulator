package com.example.export.client;

import com.example.session.dto.CalculationHistoryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

// http client that talks to session-management service
@Component
public class SessionManagementClient {
    
    @Value("${services.session-management.url:http://localhost:8082}")
    private String sessionManagementUrl;
    
    private final RestTemplate restTemplate;
    
    public SessionManagementClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SessionManagementClient.class);
    
    // fetch calculation details by id (service-to-service call)
    public CalculationHistoryDto getCalculationById(String sessionId) {
        try {
            // pass session ID as query param (works across services)
            String url = sessionManagementUrl + "/api/tariff/history/" + calculationId + "?sessionId=" + sessionId;
            
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
    
    // remove calculation from session history (used when item moves into export cart)
    public void removeCalculationById(String sessionId, String calculationId) {
        try {
            // pass session ID as query param
            String url = sessionManagementUrl + "/api/tariff/history/" + calculationId + "?sessionId=" + sessionId;
            
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

