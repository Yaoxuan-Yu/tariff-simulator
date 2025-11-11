package com.example.tariffs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.tariffs.service.TariffService;
import com.example.tariffs.repository.TariffRepository;
import com.example.tariffs.repository.ProductRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

// admin endpoints that expose aggregate stats and reference data
@RestController
@Tag(name = "Admin Dashboard", description = "Admin-only endpoints for dashboard statistics and management")
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminDashboardController {
    private final TariffService tariffService;
    private final TariffRepository tariffRepository;
    private final ProductRepository productRepository;

    public AdminDashboardController(TariffService tariffService, 
                                   TariffRepository tariffRepository,
                                   ProductRepository productRepository) {
        this.tariffService = tariffService;
        this.tariffRepository = tariffRepository;
        this.productRepository = productRepository;
    }

    @Operation(summary = "Get admin dashboard statistics")
    // GET /api/admin/dashboard/stats -> counts for dashboard widgets
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Count tariffs
        long totalTariffs = tariffRepository.count();
        stats.put("totalTariffs", totalTariffs);
        
        // Count products
        long totalProducts = productRepository.count();
        stats.put("totalProducts", totalProducts);
        
        // Count countries
        long totalCountries = tariffService.getAllCountries().size();
        stats.put("totalCountries", totalCountries);
        
        // Count country pairs (unique tariff relationships)
        stats.put("totalCountryPairs", totalTariffs);
        
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get all available countries")
    // GET /api/admin/countries -> distinct country list
    @GetMapping("/countries")
    public ResponseEntity<java.util.List<String>> getAllCountries() {
        return ResponseEntity.ok(tariffService.getAllCountries());
    }

    @Operation(summary = "Get all available products")
    // GET /api/admin/products -> distinct product list
    @GetMapping("/products")
    public ResponseEntity<java.util.List<String>> getAllProducts() {
        java.util.List<String> products = productRepository.findDistinctProducts();
        return ResponseEntity.ok(products);
    }
}

