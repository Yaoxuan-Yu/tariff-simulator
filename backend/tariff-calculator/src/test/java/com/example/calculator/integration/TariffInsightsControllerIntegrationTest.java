package com.example.calculator.integration;

import com.example.calculator.entity.Product;
import com.example.calculator.entity.Tariff;
import com.example.calculator.repository.ProductRepository;
import com.example.calculator.repository.TariffRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class TariffInsightsControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TariffRepository tariffRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        setupTestData();
    }

    private void setupTestData() {
        // Create test products
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Product1");
        product1.setHsCode("123456");
        product1.setCost(100.0);
        product1.setUnit("kg");
        productRepository.save(product1);

        // Create test tariffs
        Tariff tariff1 = new Tariff();
        tariff1.setCountry("Country1");
        tariff1.setPartner("Country2");
        tariff1.setAhsWeighted(5.0);
        tariff1.setMfnWeighted(10.0);
        tariffRepository.save(tariff1);

        Tariff tariff2 = new Tariff();
        tariff2.setCountry("Country2");
        tariff2.setPartner("Country1");
        tariff2.setAhsWeighted(3.0);
        tariff2.setMfnWeighted(8.0);
        tariffRepository.save(tariff2);
    }

    @Test
    void testGetAllTariffs_Success() throws Exception {
        mockMvc.perform(get("/api/tariffs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetTariffsByCountry_Success() throws Exception {
        mockMvc.perform(get("/api/tariffs/country/Country1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetTariffByCountryAndPartner_Success() throws Exception {
        mockMvc.perform(get("/api/tariffs/Country1/Country2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value("Country1"))
                .andExpect(jsonPath("$.partner").value("Country2"));
    }

    @Test
    void testCompareTariffs_Success() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("product", "Product1");
        requestBody.put("exportingFrom", "Country2");
        requestBody.put("importingToCountries", java.util.Arrays.asList("Country1"));
        requestBody.put("quantity", 10.0);

        mockMvc.perform(post("/api/tariffs/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comparisons").exists());
    }

    @Test
    void testGetTariffHistory_Success() throws Exception {
        mockMvc.perform(get("/api/tariffs/history")
                        .param("product", "Product1")
                        .param("exportingFrom", "Country2")
                        .param("importingTo", "Country1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history").exists());
    }

    @Test
    void testGetTariffTrends_Success() throws Exception {
        mockMvc.perform(get("/api/tariff-trends")
                        .param("importCountries", "Country1,Country2")
                        .param("exportCountries", "Country2")
                        .param("products", "Product1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetSupportedCurrencies_Success() throws Exception {
        mockMvc.perform(get("/api/tariffs/currencies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").exists())
                .andExpect(jsonPath("$.currency").isArray());
    }

    @Test
    void testGetExchangeRate_Success() throws Exception {
        mockMvc.perform(get("/api/tariffs/exchange-rate/USD")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.rate").exists());
    }
}

