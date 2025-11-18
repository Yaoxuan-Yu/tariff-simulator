package com.example.product.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.List;

import com.example.product.client.GlobalTariffsClient;
import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private GlobalTariffsClient globalTariffsClient;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    public void setUp() {
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setCost(10.0);
        testProduct.setUnit("piece");
    }

    @Test
    void getAllProducts_Success() {
        when(productRepository.findDistinctProducts())
            .thenReturn(List.of("Product 1", "Product 2", "Product 3"));

        List<String> products = productService.getAllProducts();

        assertNotNull(products);
        assertEquals(3, products.size());
        assertTrue(products.contains("Product 1"));
    }

    @Test
    void getAllCountries_Success() {
        when(globalTariffsClient.getAllCountries())
            .thenReturn(List.of("Singapore", "China"));

        List<String> countries = productService.getAllCountries();

        assertNotNull(countries);
        assertEquals(2, countries.size());
        assertTrue(countries.contains("Singapore"));
    }

    @Test
    void getAllPartners_Success() {
        when(globalTariffsClient.getAllPartners())
            .thenReturn(List.of("China", "India"));

        List<String> partners = productService.getAllPartners();

        assertNotNull(partners);
        assertEquals(2, partners.size());
        assertTrue(partners.contains("India"));
    }
}
