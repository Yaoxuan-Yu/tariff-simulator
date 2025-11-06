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

import com.example.product.dto.BrandInfo;
import com.example.product.entity.Product;
import com.example.product.exception.NotFoundException;
import com.example.product.exception.ValidationException;
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
        testProduct.setBrand("Test Brand");
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

    @Test
    void getBrandsByProduct_Success() {
        when(productRepository.findByName("Test Product"))
            .thenReturn(List.of(testProduct));

        List<BrandInfo> brands = productService.getBrandsByProduct("Test Product");

        assertNotNull(brands);
        assertEquals(1, brands.size());
        assertEquals("Test Brand", brands.get(0).getBrand());
        assertEquals(10.0, brands.get(0).getCost());
    }

    @Test
    void getBrandsByProduct_ProductNotFound() {
        when(productRepository.findByName("Non-existent Product"))
            .thenReturn(List.of());

        NotFoundException thrown = assertThrows(NotFoundException.class, () ->
            productService.getBrandsByProduct("Non-existent Product")
        );

        assertTrue(thrown.getMessage().contains("No products found"));
    }

    @Test
    void getBrandsByProduct_NullProduct() {
        ValidationException thrown = assertThrows(ValidationException.class, () ->
            productService.getBrandsByProduct(null)
        );

        assertTrue(thrown.getMessage().contains("Product name cannot be null"));
    }

    @Test
    void getBrandsByProduct_EmptyProduct() {
        ValidationException thrown = assertThrows(ValidationException.class, () ->
            productService.getBrandsByProduct("")
        );

        assertTrue(thrown.getMessage().contains("Product name cannot be null"));
    }
}

