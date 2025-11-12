package com.example.product.service;

import com.example.product.dto.BrandInfo;
import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// provides product/country/brand lookups and validates downstream responses
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
    // load brand information for a given product (validated)
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
        } catch (com.example.product.exception.NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new com.example.product.exception.DataAccessException("Failed to retrieve brands for product: " + product, e);
        }
    }
}

