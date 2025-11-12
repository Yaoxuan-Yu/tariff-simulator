package com.example.product.service;

import com.example.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// provides product/country lookups and validates downstream responses
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final com.example.product.client.GlobalTariffsClient globalTariffsClient;

    public ProductService(ProductRepository productRepository,
                          com.example.product.client.GlobalTariffsClient globalTariffsClient) {
        this.productRepository = productRepository;
        this.globalTariffsClient = globalTariffsClient;
    }

    // list importing countries via global-tariffs service
    public List<String> getAllCountries() {
        return globalTariffsClient.getAllCountries();
    }

    // list exporting partners via global-tariffs service
    public List<String> getAllPartners() {
        return globalTariffsClient.getAllPartners();
    }

    // list local product names from repository
    public List<String> getAllProducts() {
        return productRepository.findDistinctProducts();
    }
}

