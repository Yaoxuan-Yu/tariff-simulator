package com.example.tariffs.integration;

import com.example.tariffs.entity.Product;
import com.example.tariffs.entity.Tariff;
import com.example.tariffs.entity.TariffId;
import com.example.tariffs.repository.ProductRepository;
import com.example.tariffs.repository.TariffRepository;
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
public class GlobalTariffControllerIntegrationTest {

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
        // Create test product
        Product product = new Product();
        product.setId(1L);
        product.setName("TestProduct");
        product.setHsCode("123456");
        product.setCost(100.0);
        product.setUnit("kg");
        productRepository.save(product);

        // Create test tariff
        Tariff tariff = new Tariff();
        tariff.setCountry("Country1");
        tariff.setPartner("Country2");
        tariff.setAhsWeighted(5.0);
        tariff.setMfnWeighted(10.0);
        tariffRepository.save(tariff);
    }

    @Test
    void testGetTariffDefinitions_Success() throws Exception {
        mockMvc.perform(get("/api/tariff-definitions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tariffs").isArray());
    }

    @Test
    void testGetGlobalTariffDefinitions_Success() throws Exception {
        mockMvc.perform(get("/api/tariff-definitions/global")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tariffs").isArray());
    }

    @Test
    void testGetModifiedTariffDefinitions_Success() throws Exception {
        mockMvc.perform(get("/api/tariff-definitions/modified")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testAddModifiedTariffDefinition_Success() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("product", "TestProduct");
        requestBody.put("exportingFrom", "Country2");
        requestBody.put("importingTo", "Country1");
        requestBody.put("type", "AHS");
        requestBody.put("rate", 5.0);
        requestBody.put("effectiveDate", "2024-01-01");
        requestBody.put("expirationDate", "2024-12-31");

        mockMvc.perform(post("/api/tariff-definitions/modified")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testUpdateModifiedTariffDefinition_Success() throws Exception {
        // First add a tariff definition
        Map<String, Object> addRequestBody = new HashMap<>();
        addRequestBody.put("product", "TestProduct");
        addRequestBody.put("exportingFrom", "Country2");
        addRequestBody.put("importingTo", "Country1");
        addRequestBody.put("type", "AHS");
        addRequestBody.put("rate", 5.0);

        String response = mockMvc.perform(post("/api/tariff-definitions/modified")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequestBody)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract ID from response
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> tariffs = (java.util.List<Map<String, Object>>) responseMap.get("tariffs");
        String id = (String) tariffs.get(0).get("id");

        // Update the tariff definition
        Map<String, Object> updateRequestBody = new HashMap<>();
        updateRequestBody.put("rate", 7.0);

        mockMvc.perform(put("/api/tariff-definitions/modified/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteModifiedTariffDefinition_Success() throws Exception {
        // First add a tariff definition
        Map<String, Object> addRequestBody = new HashMap<>();
        addRequestBody.put("product", "TestProduct");
        addRequestBody.put("exportingFrom", "Country2");
        addRequestBody.put("importingTo", "Country1");
        addRequestBody.put("type", "AHS");
        addRequestBody.put("rate", 5.0);

        String response = mockMvc.perform(post("/api/tariff-definitions/modified")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequestBody)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> tariffs = (java.util.List<Map<String, Object>>) responseMap.get("tariffs");
        String id = (String) tariffs.get(0).get("id");

        // Delete the tariff definition
        mockMvc.perform(delete("/api/tariff-definitions/modified/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testAddModifiedTariffDefinition_MissingData() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        // Missing required fields

        mockMvc.perform(post("/api/tariff-definitions/modified")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }
}

