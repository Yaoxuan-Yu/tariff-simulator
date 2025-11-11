package com.example.product.service;

import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}

