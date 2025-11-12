package com.example.tariffs.integration;

import com.example.tariffs.entity.Product;
import com.example.tariffs.entity.Tariff;
import com.example.tariffs.repository.ProductRepository;
import com.example.tariffs.repository.TariffRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class AdminDashboardControllerIntegrationTest {

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

    @BeforeEach
    void setUp() {
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

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product2");
        product2.setHsCode("789012");
        product2.setCost(200.0);
        product2.setUnit("liters");
        productRepository.save(product2);

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
    void testGetDashboardStats_Success() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTariffs").exists())
                .andExpect(jsonPath("$.totalProducts").exists())
                .andExpect(jsonPath("$.totalCountries").exists())
                .andExpect(jsonPath("$.totalCountryPairs").exists());
    }

    @Test
    void testGetAllCountries_Success() throws Exception {
        mockMvc.perform(get("/api/admin/countries")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetAllProducts_Success() throws Exception {
        mockMvc.perform(get("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("Product1"))
                .andExpect(jsonPath("$[1]").value("Product2"));
    }
}

