package com.example.calculator.integration;

import com.example.calculator.entity.Product;
import com.example.calculator.entity.Tariff;
import com.example.calculator.entity.TariffId;
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
import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class TariffCalculationControllerIntegrationTest {

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

    private WireMockServer sessionManagementMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        sessionManagementMock = new WireMockServer(8082);
        sessionManagementMock.start();
        objectMapper = new ObjectMapper();
        
        // Setup test data
        setupTestData();
        
        // Mock session management service
        sessionManagementMock.stubFor(post(urlMatching("/api/tariff/history/save.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true}")));
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
        tariff.setCountry("Country2");
        tariff.setPartner("Country1");
        tariff.setAhsWeighted(5.0);
        tariff.setMfnWeighted(10.0);
        tariffRepository.save(tariff);
    }

    @Test
    void testCalculateTariff_Success() throws Exception {
        mockMvc.perform(get("/api/tariff")
                        .param("product", "TestProduct")
                        .param("exportingFrom", "Country1")
                        .param("importingTo", "Country2")
                        .param("quantity", "10")
                        .param("currency", "USD")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.product").value("TestProduct"))
                .andExpect(jsonPath("$.data.exportingFrom").value("Country1"))
                .andExpect(jsonPath("$.data.importingTo").value("Country2"))
                .andExpect(jsonPath("$.data.quantity").value(10.0))
                .andExpect(jsonPath("$.data.tariffRate").exists());
    }

    @Test
    void testCalculateTariff_WithCustomCost() throws Exception {
        mockMvc.perform(get("/api/tariff")
                        .param("product", "TestProduct")
                        .param("exportingFrom", "Country1")
                        .param("importingTo", "Country2")
                        .param("quantity", "10")
                        .param("customCost", "150.0")
                        .param("currency", "USD")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productCost").value(150.0));
    }

    @Test
    void testCalculateTariff_MissingProduct() throws Exception {
        mockMvc.perform(get("/api/tariff")
                        .param("exportingFrom", "Country1")
                        .param("importingTo", "Country2")
                        .param("quantity", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCalculateTariff_InvalidQuantity() throws Exception {
        mockMvc.perform(get("/api/tariff")
                        .param("product", "TestProduct")
                        .param("exportingFrom", "Country1")
                        .param("importingTo", "Country2")
                        .param("quantity", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCalculateTariff_WithUserMode() throws Exception {
        mockMvc.perform(get("/api/tariff")
                        .param("product", "TestProduct")
                        .param("exportingFrom", "Country1")
                        .param("importingTo", "Country2")
                        .param("quantity", "10")
                        .param("mode", "user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

