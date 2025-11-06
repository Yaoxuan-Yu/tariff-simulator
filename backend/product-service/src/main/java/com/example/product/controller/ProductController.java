package com.example.product.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.product.dto.BrandInfo;
import com.example.product.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Product Catalog", description = "API endpoints for products, brands, and countries")
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Retrieve list of all countries (exporting)")
    @GetMapping("/countries")
    public ResponseEntity<List<String>> getAllCountries() {
        return ResponseEntity.ok(productService.getAllCountries());
    }

    @Operation(summary = "Retrieve list of all countries (importing)")
    @GetMapping("/partners")
    public ResponseEntity<List<String>> getAllPartners() {
        return ResponseEntity.ok(productService.getAllPartners());
    }

    @Operation(summary = "Retrieve a list of all available products")
    @GetMapping("/products")
    public ResponseEntity<List<String>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Operation(summary = "Get available brands for a specific product")
    @GetMapping("/brands")
    public ResponseEntity<List<BrandInfo>> getBrandsByProduct(@RequestParam String product) {
        if (product == null || product.trim().isEmpty()) {
            throw new com.example.product.exception.BadRequestException("Product is required");
        }
        return ResponseEntity.ok(productService.getBrandsByProduct(product));
    }
}

