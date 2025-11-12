package com.example.product.controller;

import com.example.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerUnitTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController controller;

    @Test
    void testGetAllProducts_Success() {
        // Arrange
        List<String> mockProducts = List.of("Product1", "Product2", "Product3");
        when(productService.getAllProducts()).thenReturn(mockProducts);

        // Act
        ResponseEntity<List<String>> response = controller.getAllProducts();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertEquals("Product1", response.getBody().get(0));
        verify(productService).getAllProducts();
    }

    @Test
    void testGetAllCountries_Success() {
        // Arrange
        List<String> mockCountries = List.of("Singapore", "Malaysia", "Thailand");
        when(productService.getAllCountries()).thenReturn(mockCountries);

        // Act
        ResponseEntity<List<String>> response = controller.getAllCountries();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        verify(productService).getAllCountries();
    }

    @Test
    void testGetAllPartners_Success() {
        // Arrange
        List<String> mockPartners = List.of("China", "Japan", "USA");
        when(productService.getAllPartners()).thenReturn(mockPartners);

        // Act
        ResponseEntity<List<String>> response = controller.getAllPartners();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        verify(productService).getAllPartners();
    }
}

