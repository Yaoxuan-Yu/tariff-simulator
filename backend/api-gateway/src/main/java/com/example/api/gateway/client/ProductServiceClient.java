package com.example.api.gateway.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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
     * Get brands for a specific product
     * Properly handles URL encoding/decoding of the product parameter
     */
    public ResponseEntity<List<Object>> getBrandsByProduct(String product) {
        // Build URL using UriComponentsBuilder to properly handle encoding
        // This ensures spaces are encoded as %20, not +
        String baseUrl = productServiceUrl + "/api/brands";
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl);
        
        if (product != null && !product.isEmpty()) {
            // UriComponentsBuilder.queryParam() properly encodes the value
            uriBuilder.queryParam("product", product);
        }
        
        // Build URI object directly - UriComponentsBuilder properly encodes spaces as %20
        URI targetUri = uriBuilder.build().toUri();
        
        // Make the request using the URI object directly
        return restTemplate.exchange(
            targetUri, 
            HttpMethod.GET, 
            null, 
            new ParameterizedTypeReference<List<Object>>() {}
        );
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

