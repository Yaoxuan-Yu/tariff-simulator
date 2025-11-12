package com.example.product.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import com.example.product.client.GlobalTariffsClient;

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

    // TODO: Add tests for getAllCountries method (calls globalTariffsClient)
    // TODO: Add tests for getAllPartners method (calls globalTariffsClient)
    
    @Test
    void getAllProducts_Success() {
        when(productRepository.findDistinctProducts())
            .thenReturn(List.of("Product 1", "Product 2", "Product 3"));

        List<String> products = productService.getAllProducts();

        assertNotNull(products);
        assertEquals(3, products.size());
        assertTrue(products.contains("Product 1"));
    }

}

