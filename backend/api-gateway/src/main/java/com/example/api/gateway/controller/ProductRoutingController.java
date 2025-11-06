package com.example.api.gateway.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.api.gateway.service.RoutingService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProductRoutingController {
    private final RoutingService routingService;

    public ProductRoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @GetMapping("/products")
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<String>> getProducts(HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getProductServiceUrl(), 
            "/api/products", 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUrl, HttpMethod.GET, entity, Object.class);
        return (ResponseEntity<List<String>>) response;
    }

    @GetMapping("/countries")
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<String>> getCountries(HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getProductServiceUrl(), 
            "/api/countries", 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUrl, HttpMethod.GET, entity, Object.class);
        return (ResponseEntity<List<String>>) response;
    }

    @GetMapping("/brands")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> getBrands(HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        java.net.URI targetUri = routingService.buildTargetUri(
            routingService.getProductServiceUrl(), 
            "/api/brands", 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUri, HttpMethod.GET, entity, Object.class);
        return response;
    }
}

