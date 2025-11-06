package com.example.api.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpClientError(HttpClientErrorException ex) {
        // Try to parse the response body if it's JSON
        try {
            String responseBody = ex.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isEmpty()) {
                // Try to parse as JSON
                try {
                    Map<String, Object> errorMap = objectMapper.readValue(responseBody, Map.class);
                    return new ResponseEntity<>(errorMap, ex.getStatusCode());
                } catch (Exception e) {
                    // If not JSON, create a simple error map
                    Map<String, Object> error = new HashMap<>();
                    error.put("message", responseBody);
                    return new ResponseEntity<>(error, ex.getStatusCode());
                }
            }
        } catch (Exception e) {
            // If parsing fails, continue with default error
        }
        
        // Default error response
        Map<String, Object> error = new HashMap<>();
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        error.put("timestamp", java.time.Instant.now().toString());
        error.put("status", ex.getStatusCode().value());
        error.put("error", status.getReasonPhrase());
        error.put("message", ex.getMessage());
        
        return new ResponseEntity<>(error, ex.getStatusCode());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpServerError(HttpServerErrorException ex) {
        // Try to parse the response body if it's JSON
        try {
            String responseBody = ex.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isEmpty()) {
                // Try to parse as JSON
                try {
                    Map<String, Object> errorMap = objectMapper.readValue(responseBody, Map.class);
                    return new ResponseEntity<>(errorMap, ex.getStatusCode());
                } catch (Exception e) {
                    // If not JSON, create a simple error map
                    Map<String, Object> error = new HashMap<>();
                    error.put("message", responseBody);
                    return new ResponseEntity<>(error, ex.getStatusCode());
                }
            }
        } catch (Exception e) {
            // If parsing fails, continue with default error
        }
        
        // Default error response
        Map<String, Object> error = new HashMap<>();
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        error.put("timestamp", java.time.Instant.now().toString());
        error.put("status", ex.getStatusCode().value());
        error.put("error", status.getReasonPhrase());
        error.put("message", ex.getMessage());
        
        return new ResponseEntity<>(error, ex.getStatusCode());
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> handleRestClientError(RestClientException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", java.time.Instant.now().toString());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Service Unavailable");
        error.put("message", "Backend service is not available: " + ex.getMessage());
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", java.time.Instant.now().toString());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Internal Server Error");
        error.put("message", ex.getMessage());
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

