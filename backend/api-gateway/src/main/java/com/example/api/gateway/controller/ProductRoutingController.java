package com.example.api.gateway.controller;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.gateway.service.RoutingService;

import jakarta.servlet.http.HttpServletRequest;

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
}

