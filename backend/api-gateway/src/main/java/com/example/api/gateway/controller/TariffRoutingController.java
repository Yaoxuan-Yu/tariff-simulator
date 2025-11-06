package com.example.api.gateway.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.api.gateway.service.RoutingService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TariffRoutingController {
    private final RoutingService routingService;

    public TariffRoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @GetMapping("/tariff")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> calculateTariff(HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getTariffCalculatorUrl(), 
            "/api/tariff", 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUrl, HttpMethod.GET, entity, Object.class);
        return (ResponseEntity<Map<String, Object>>) response;
    }

    @GetMapping("/tariff-definitions/global")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> getGlobalTariffDefinitions(HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getGlobalTariffsUrl(), 
            "/api/tariff-definitions/global", 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUrl, HttpMethod.GET, entity, Object.class);
        return (ResponseEntity<Map<String, Object>>) response;
    }

    @GetMapping("/tariff-definitions/user")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> getUserTariffDefinitions(HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getSimulatorTariffsUrl(), 
            "/api/tariff-definitions/user", 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUrl, HttpMethod.GET, entity, Object.class);
        return (ResponseEntity<Map<String, Object>>) response;
    }

    @PostMapping("/tariff-definitions/user")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> addUserTariffDefinition(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, body);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getSimulatorTariffsUrl(), 
            "/api/tariff-definitions/user", 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUrl, HttpMethod.POST, entity, Object.class);
        return (ResponseEntity<Map<String, Object>>) response;
    }

    @PutMapping("/tariff-definitions/user/{id}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> updateUserTariffDefinition(
            @PathVariable String id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, body);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getSimulatorTariffsUrl(), 
            "/api/tariff-definitions/user/" + id, 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUrl, HttpMethod.PUT, entity, Object.class);
        return (ResponseEntity<Map<String, Object>>) response;
    }

    @DeleteMapping("/tariff-definitions/user/{id}")
    public ResponseEntity<?> deleteUserTariffDefinition(
            @PathVariable String id,
            HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getSimulatorTariffsUrl(), 
            "/api/tariff-definitions/user/" + id, 
            queryString
        );
        return routingService.forwardRequest(targetUrl, HttpMethod.DELETE, entity, Object.class);
    }

    @GetMapping("/tariff-definitions/modified")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> getModifiedTariffDefinitions(HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getGlobalTariffsUrl(), 
            "/api/tariff-definitions/modified", 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUrl, HttpMethod.GET, entity, Object.class);
        return (ResponseEntity<Map<String, Object>>) response;
    }

    @PostMapping("/tariff-definitions/modified")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> addModifiedTariffDefinition(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, body);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getGlobalTariffsUrl(), 
            "/api/tariff-definitions/modified", 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUrl, HttpMethod.POST, entity, Object.class);
        return (ResponseEntity<Map<String, Object>>) response;
    }

    @PutMapping("/tariff-definitions/modified/{id}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> updateModifiedTariffDefinition(
            @PathVariable String id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, body);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getGlobalTariffsUrl(), 
            "/api/tariff-definitions/modified/" + id, 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUrl, HttpMethod.PUT, entity, Object.class);
        return (ResponseEntity<Map<String, Object>>) response;
    }

    @DeleteMapping("/tariff-definitions/modified/{id}")
    public ResponseEntity<?> deleteModifiedTariffDefinition(
            @PathVariable String id,
            HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getGlobalTariffsUrl(), 
            "/api/tariff-definitions/modified/" + id, 
            queryString
        );
        return routingService.forwardRequest(targetUrl, HttpMethod.DELETE, entity, Object.class);
    }

    @GetMapping("/tariff-definitions/export")
    public void exportTariffDefinitions(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getGlobalTariffsUrl(), 
            "/api/tariff-definitions/export", 
            queryString
        );
        
        org.springframework.http.ResponseEntity<byte[]> responseEntity = routingService.forwardRequest(
            targetUrl, 
            HttpMethod.GET, 
            entity, 
            byte[].class
        );
        
        if (responseEntity.getBody() != null) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=tariff-definitions.csv");
            response.getOutputStream().write(responseEntity.getBody());
            response.getOutputStream().flush();
        }
    }
}

