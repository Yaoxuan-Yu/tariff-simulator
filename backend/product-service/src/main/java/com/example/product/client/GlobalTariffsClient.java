package com.example.product.client;

import com.example.product.dto.TariffDefinitionsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HTTP client for communicating with global-tariffs service
 */
@Component
public class GlobalTariffsClient {
    
    @Value("${services.global-tariffs.url:http://localhost:8083}")
    private String globalTariffsUrl;
    
    private final RestTemplate restTemplate;
    
    public GlobalTariffsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Get all countries from global-tariffs service
     */
    public List<String> getAllCountries() {
        try {
            // Get tariff definitions and extract unique countries
            String url = globalTariffsUrl + "/api/tariff-definitions/global";
            ResponseEntity<TariffDefinitionsResponse> response = restTemplate.getForEntity(
                url, 
                TariffDefinitionsResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                TariffDefinitionsResponse tariffDefinitions = response.getBody();
                if (tariffDefinitions.getData() != null) {
                    // Extract unique importing countries
                    Set<String> countries = tariffDefinitions.getData().stream()
                        .map(TariffDefinitionsResponse.TariffDefinitionDto::getImportingTo)
                        .filter(country -> country != null && !country.isEmpty())
                        .collect(Collectors.toSet());
                    return countries.stream().sorted().collect(Collectors.toList());
                }
            }
            return List.of();
        } catch (Exception e) {
            throw new com.example.product.exception.DataAccessException(
                "Failed to fetch countries from global-tariffs service: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all partners (exporting countries) from global-tariffs service
     */
    public List<String> getAllPartners() {
        try {
            // Get tariff definitions and extract unique partners
            String url = globalTariffsUrl + "/api/tariff-definitions/global";
            ResponseEntity<TariffDefinitionsResponse> response = restTemplate.getForEntity(
                url, 
                TariffDefinitionsResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                TariffDefinitionsResponse tariffDefinitions = response.getBody();
                if (tariffDefinitions.getData() != null) {
                    // Extract unique exporting countries
                    Set<String> partners = tariffDefinitions.getData().stream()
                        .map(TariffDefinitionsResponse.TariffDefinitionDto::getExportingFrom)
                        .filter(partner -> partner != null && !partner.isEmpty())
                        .collect(Collectors.toSet());
                    return partners.stream().sorted().collect(Collectors.toList());
                }
            }
            return List.of();
        } catch (Exception e) {
            throw new com.example.product.exception.DataAccessException(
                "Failed to fetch partners from global-tariffs service: " + e.getMessage(), e);
        }
    }
}

