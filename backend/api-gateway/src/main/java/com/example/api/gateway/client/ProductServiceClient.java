package com.example.api.gateway.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;

/**
 * HTTP client for communicating with product-service
 */
@Component
public class ProductServiceClient {
    
    @Value("${services.product-service.url:http://localhost:8084}")
    private String productServiceUrl;
    
    private final RestTemplate restTemplate;
    
    public ProductServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Get all products
     */
    public ResponseEntity<List<String>> getAllProducts() {
        String url = productServiceUrl + "/api/products";
        return restTemplate.exchange(
            url, 
            HttpMethod.GET, 
            null, 
            new ParameterizedTypeReference<List<String>>() {}
        );
    }
    
    /**
     * Get all countries
     */
    public ResponseEntity<List<String>> getAllCountries() {
        String url = productServiceUrl + "/api/countries";
        return restTemplate.exchange(
            url, 
            HttpMethod.GET, 
            null, 
            new ParameterizedTypeReference<List<String>>() {}
        );
    }
}

