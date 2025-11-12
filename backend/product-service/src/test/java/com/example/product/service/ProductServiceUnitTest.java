package com.example.product.service;

import com.example.product.client.GlobalTariffsClient;
import com.example.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private GlobalTariffsClient globalTariffsClient;

    @InjectMocks
    private ProductService productService;

    @Test
    void testGetAllProducts() {
        // Arrange
        when(productRepository.findDistinctProducts()).thenReturn(List.of("Product1", "Product2", "Product3"));

        // Act
        List<String> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Product1", result.get(0));
        verify(productRepository).findDistinctProducts();
    }

    @Test
    void testGetAllProducts_Empty() {
        // Arrange
        when(productRepository.findDistinctProducts()).thenReturn(List.of());

        // Act
        List<String> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllCountries() {
        // Arrange
        when(globalTariffsClient.getAllCountries()).thenReturn(List.of("Singapore", "Malaysia", "Thailand"));

        // Act
        List<String> result = productService.getAllCountries();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Singapore", result.get(0));
        verify(globalTariffsClient).getAllCountries();
    }

    @Test
    void testGetAllPartners() {
        // Arrange
        when(globalTariffsClient.getAllPartners()).thenReturn(List.of("China", "Japan", "USA"));

        // Act
        List<String> result = productService.getAllPartners();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("China", result.get(0));
        verify(globalTariffsClient).getAllPartners();
    }

    @Test
    void testGetAllCountries_ClientThrowsException() {
        // Arrange
        when(globalTariffsClient.getAllCountries())
                .thenThrow(new com.example.product.exception.DataAccessException("Service unavailable"));

        // Act & Assert
        assertThrows(com.example.product.exception.DataAccessException.class, () -> {
            productService.getAllCountries();
        });
    }
}

