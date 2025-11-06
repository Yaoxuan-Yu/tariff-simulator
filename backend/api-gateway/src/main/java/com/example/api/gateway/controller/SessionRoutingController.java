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
@RequestMapping("/api/tariff")
@CrossOrigin(origins = "*")
public class SessionRoutingController {
    private final RoutingService routingService;

    public SessionRoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @GetMapping("/history")
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<Map<String, Object>>> getCalculationHistory(HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getSessionManagementUrl(), 
            "/api/tariff/history", 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUrl, HttpMethod.GET, entity, Object.class);
        return (ResponseEntity<List<Map<String, Object>>>) response;
    }

    @PostMapping("/history/save")
    public ResponseEntity<?> saveCalculation(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, body);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getSessionManagementUrl(), 
            "/api/tariff/history/save", 
            queryString
        );
        return routingService.forwardRequest(targetUrl, HttpMethod.POST, entity, Object.class);
    }

    @DeleteMapping("/history/clear")
    public ResponseEntity<?> clearCalculationHistory(HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getSessionManagementUrl(), 
            "/api/tariff/history/clear", 
            queryString
        );
        return routingService.forwardRequest(targetUrl, HttpMethod.DELETE, entity, Object.class);
    }
}

