package com.example.product.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.example.product.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    void getAllCountries_ReturnsList() throws Exception {
        when(productService.getAllCountries()).thenReturn(List.of("Singapore", "China"));

        mockMvc.perform(get("/api/countries"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(2))
               .andExpect(jsonPath("$[0]").value("Singapore"))
               .andExpect(jsonPath("$[1]").value("China"));
    }

    @Test
    void getAllPartners_ReturnsList() throws Exception {
        when(productService.getAllPartners()).thenReturn(List.of("China", "India"));

        mockMvc.perform(get("/api/partners"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(2))
               .andExpect(jsonPath("$[0]").value("China"))
               .andExpect(jsonPath("$[1]").value("India"));
    }

    @Test
    void getAllProducts_ReturnsList() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of("Product 1", "Product 2"));

        mockMvc.perform(get("/api/products"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(2))
               .andExpect(jsonPath("$[0]").value("Product 1"))
               .andExpect(jsonPath("$[1]").value("Product 2"));
    }
}
