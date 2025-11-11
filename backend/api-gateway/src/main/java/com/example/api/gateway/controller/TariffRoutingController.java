package com.example.api.gateway.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.api.gateway.service.RoutingService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

// routes tariff calculation and definition requests to downstream services
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TariffRoutingController {
    private final RoutingService routingService;

    public TariffRoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    // GET /api/tariff -> call tariff-calculator service
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

    // GET /api/tariff-definitions/global -> global definitions from global-tariffs
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

    // GET /api/tariff-definitions/user -> session tariffs from simulator service
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

    // POST /api/tariff-definitions/user -> add session tariff definition
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

    // PUT /api/tariff-definitions/user/{id} -> update session tariff definition
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

    // DELETE /api/tariff-definitions/user/{id} -> delete session tariff definition
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

    // GET /api/tariff-definitions/modified -> fetch admin-modified definitions
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

    // POST /api/tariff-definitions/modified -> create admin definition
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

    // PUT /api/tariff-definitions/modified/{id} -> update admin definition
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

    // DELETE /api/tariff-definitions/modified/{id} -> delete admin definition
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

    // GET /api/tariff-definitions/export -> proxy csv export
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

    // Admin Dashboard Endpoints
    // GET /api/admin/dashboard/stats -> aggregate stats for admin UI
    @GetMapping("/admin/dashboard/stats")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> getAdminDashboardStats(HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getGlobalTariffsUrl(), 
            "/api/admin/dashboard/stats", 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUrl, HttpMethod.GET, entity, Object.class);
        return (ResponseEntity<Map<String, Object>>) response;
    }

    // GET /api/admin/countries -> list countries for admin dashboard
    @GetMapping("/admin/countries")
    @SuppressWarnings("unchecked")
    public ResponseEntity<java.util.List<String>> getAdminCountries(HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getGlobalTariffsUrl(), 
            "/api/admin/countries", 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUrl, HttpMethod.GET, entity, Object.class);
        return (ResponseEntity<java.util.List<String>>) response;
    }

    // GET /api/admin/products -> list products for admin dashboard
    @GetMapping("/admin/products")
    @SuppressWarnings("unchecked")
    public ResponseEntity<java.util.List<String>> getAdminProducts(HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getGlobalTariffsUrl(), 
            "/api/admin/products", 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUrl, HttpMethod.GET, entity, Object.class);
        return (ResponseEntity<java.util.List<String>>) response;
    }
}

