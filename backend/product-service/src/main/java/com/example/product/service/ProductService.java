package com.example.product.service;

import com.example.product.dto.BrandInfo;
import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final com.example.product.client.GlobalTariffsClient globalTariffsClient;

    public ProductService(ProductRepository productRepository, 
                         com.example.product.client.GlobalTariffsClient globalTariffsClient) {
        this.productRepository = productRepository;
        this.globalTariffsClient = globalTariffsClient;
    }

    /**
     * Get all countries from global-tariffs service via HTTP
     */
    public List<String> getAllCountries() {
        return globalTariffsClient.getAllCountries();
    }

    /**
     * Get all partners (exporting countries) from global-tariffs service via HTTP
     */
    public List<String> getAllPartners() {
        return globalTariffsClient.getAllPartners();
    }

    public List<String> getAllProducts() {
        return productRepository.findDistinctProducts();
    }

    public List<BrandInfo> getBrandsByProduct(String product) {
        if (product == null || product.trim().isEmpty()) {
            throw new com.example.product.exception.ValidationException("Product name cannot be null or empty");
        }
        
        try {
            List<Product> products = productRepository.findByName(product);
            if (products.isEmpty()) {
                throw new com.example.product.exception.NotFoundException("No products found for: " + product);
            }
            
            return products.stream()
                    .map(p -> new BrandInfo(p.getBrand(), p.getCost(), p.getUnit()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            if (e instanceof com.example.product.exception.NotFoundException) {
                throw e;
            }
            throw new com.example.product.exception.DataAccessException("Failed to retrieve brands for product: " + product, e);
        }
    }
}

