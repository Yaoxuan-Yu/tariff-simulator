package com.example.api.gateway.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.api.gateway.service.RoutingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

// routes export cart requests to the csv-export service
@RestController
@RequestMapping("/api/export-cart")
@CrossOrigin(origins = "*")
public class ExportCartRoutingController {
    private final RoutingService routingService;

    public ExportCartRoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    // GET /api/export-cart -> list cart entries
    @GetMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<Map<String, Object>>> getCart(HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getCsvExportUrl(), 
            "/api/export-cart", 
            queryString
        );
        ResponseEntity<?> response = routingService.forwardRequest(targetUrl, HttpMethod.GET, entity, Object.class);
        return (ResponseEntity<List<Map<String, Object>>>) response;
    }

    // POST /api/export-cart/add/{id} -> add calculation to cart
    @PostMapping("/add/{calculationId}")
    public ResponseEntity<?> addToCart(
            @PathVariable String calculationId,
            HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getCsvExportUrl(), 
            "/api/export-cart/add/" + calculationId, 
            queryString
        );
        return routingService.forwardRequest(targetUrl, HttpMethod.POST, entity, Object.class);
    }

    // DELETE /api/export-cart/remove/{id} -> remove item from cart
    @DeleteMapping("/remove/{calculationId}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable String calculationId,
            HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getCsvExportUrl(), 
            "/api/export-cart/remove/" + calculationId, 
            queryString
        );
        return routingService.forwardRequest(targetUrl, HttpMethod.DELETE, entity, Object.class);
    }

    // DELETE /api/export-cart/clear -> clear entire cart
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getCsvExportUrl(), 
            "/api/export-cart/clear", 
            queryString
        );
        return routingService.forwardRequest(targetUrl, HttpMethod.DELETE, entity, Object.class);
    }

    // GET /api/export-cart/export -> stream csv download
    @GetMapping("/export")
    public void exportCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String queryString = request.getQueryString();
        HttpEntity<?> entity = routingService.createHttpEntity(request, null);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getCsvExportUrl(), 
            "/api/export-cart/export", 
            queryString
        );
        
        ResponseEntity<byte[]> responseEntity = routingService.forwardRequest(
            targetUrl, 
            HttpMethod.GET, 
            entity, 
            byte[].class
        );
        
        if (responseEntity.getBody() != null) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=export.csv");
            response.getOutputStream().write(responseEntity.getBody());
            response.getOutputStream().flush();
        }
    }
}

