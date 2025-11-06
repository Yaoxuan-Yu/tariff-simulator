package com.example.export.client;

import com.example.export.dto.CalculationHistoryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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
     */
    public CalculationHistoryDto getCalculationById(String calculationId) {
        try {
            String url = sessionManagementUrl + "/api/tariff/history/" + calculationId;
            ResponseEntity<CalculationHistoryDto> response = restTemplate.getForEntity(
                url, 
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
     * Get all calculation history from session-management service
     */
    public List<CalculationHistoryDto> getCalculationHistory() {
        try {
            String url = sessionManagementUrl + "/api/tariff/history";
            ResponseEntity<List<CalculationHistoryDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CalculationHistoryDto>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return List.of();
        } catch (Exception e) {
            throw new com.example.export.exception.DataAccessException(
                "Failed to fetch calculation history from session-management service: " + e.getMessage(), e);
        }
    }
}

