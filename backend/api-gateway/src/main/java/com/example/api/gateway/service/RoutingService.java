package com.example.api.gateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Service
public class RoutingService {
    private final RestTemplate restTemplate;

    @Value("${services.product-service.url:http://product-service:8084}")
    private String productServiceUrl;

    @Value("${services.global-tariffs.url:http://global-tariffs:8083}")
    private String globalTariffsUrl;

    @Value("${services.simulator-tariffs.url:http://simulator-tariffs:8086}")
    private String simulatorTariffsUrl;

    @Value("${services.tariff-calculator.url:http://tariff-calculator:8081}")
    private String tariffCalculatorUrl;

    @Value("${services.session-management.url:http://session-management:8082}")
    private String sessionManagementUrl;

    @Value("${services.csv-export.url:http://csv-export:8085}")
    private String csvExportUrl;

    public RoutingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
    
    // Getter methods for service URLs
    public String getProductServiceUrl() {
        return productServiceUrl;
    }
    
    public String getGlobalTariffsUrl() {
        return globalTariffsUrl;
    }
    
    public String getSimulatorTariffsUrl() {
        return simulatorTariffsUrl;
    }
    
    public String getTariffCalculatorUrl() {
        return tariffCalculatorUrl;
    }
    
    public String getSessionManagementUrl() {
        return sessionManagementUrl;
    }
    
    public String getCsvExportUrl() {
        return csvExportUrl;
    }

    public <T> ResponseEntity<T> forwardRequest(String targetUrl, HttpMethod method, 
            HttpEntity<?> requestEntity, Class<T> responseType) {
        // Use URI.create() to properly handle already-encoded URLs
        // This ensures RestTemplate doesn't double-encode query parameters
        return restTemplate.exchange(java.net.URI.create(targetUrl), method, requestEntity, responseType);
    }
    
    public <T> ResponseEntity<T> forwardRequest(URI targetUri, HttpMethod method, 
            HttpEntity<?> requestEntity, Class<T> responseType) {
        // Use the URI directly - already properly encoded by UriComponentsBuilder
        return restTemplate.exchange(targetUri, method, requestEntity, responseType);
    }
    
    @SuppressWarnings("unchecked")
    public <T> ResponseEntity<T> forwardRequestGeneric(String targetUrl, HttpMethod method, 
            HttpEntity<?> requestEntity) {
        // Use URI.create() to properly handle already-encoded URLs
        ResponseEntity<?> response = restTemplate.exchange(java.net.URI.create(targetUrl), method, requestEntity, Object.class);
        return (ResponseEntity<T>) response;
    }

    public HttpEntity<?> createHttpEntity(HttpServletRequest request, Object body) {
        HttpHeaders headers = new HttpHeaders();
        
        // Copy all headers from the original request (including Cookie header)
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Skip host header to avoid issues
            if (!headerName.equalsIgnoreCase("host")) {
                Enumeration<String> headerValues = request.getHeaders(headerName);
                while (headerValues.hasMoreElements()) {
                    headers.add(headerName, headerValues.nextElement());
                }
            }
        }
        
        // Explicitly forward cookies from the request
        // This is important because backend services (simulator-tariffs, csv-export) 
        // need session cookies to store user-specific data (tariffs, cart items)
        if (request.getCookies() != null) {
            StringBuilder cookieHeader = new StringBuilder();
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (cookieHeader.length() > 0) {
                    cookieHeader.append("; ");
                }
                cookieHeader.append(cookie.getName()).append("=").append(cookie.getValue());
            }
            if (cookieHeader.length() > 0) {
                // Add or update Cookie header to ensure cookies are forwarded
                headers.set("Cookie", cookieHeader.toString());
            }
        }
        
        if (body != null) {
            return new HttpEntity<>(body, headers);
        }
        return new HttpEntity<>(headers);
    }

    public String buildTargetUrl(String serviceUrl, String path, String queryString) {
        String url = serviceUrl + path;
        if (queryString != null && !queryString.isEmpty()) {
            url += "?" + queryString;
        }
        return url;
    }

    /**
     * Build target URL with proper encoding/decoding of query parameters.
     * This method properly handles URL-encoded query strings by decoding parameter values
     * and re-encoding them using UriComponentsBuilder.
     */
    public URI buildTargetUri(String serviceUrl, String path, String queryString) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(serviceUrl + path);
        
        if (queryString != null && !queryString.isEmpty()) {
            // Parse the query string and decode/encode properly
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    try {
                        // Decode the key and value
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        // UriComponentsBuilder will properly encode them again
                        uriBuilder.queryParam(key, value);
                    } catch (Exception e) {
                        // If decoding fails, use the original values
                        uriBuilder.queryParam(keyValue[0], keyValue[1]);
                    }
                } else if (keyValue.length == 1) {
                    // Parameter without value
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    uriBuilder.queryParam(key, "");
                }
            }
        }
        
        return uriBuilder.build().toUri();
    }
}

