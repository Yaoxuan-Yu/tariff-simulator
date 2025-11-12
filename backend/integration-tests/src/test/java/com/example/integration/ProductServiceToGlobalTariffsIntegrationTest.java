package com.example.integration;

import com.example.product.client.GlobalTariffsClient;
import com.example.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: Product Service -> Global Tariffs Service
 * Tests that Product Service successfully fetches country/partner data from Global Tariffs
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class ProductServiceToGlobalTariffsIntegrationTest {

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
        registry.add("services.global-tariffs.url", () -> "http://localhost:8083");
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private GlobalTariffsClient globalTariffsClient;

    private WireMockServer globalTariffsMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        globalTariffsMock = new WireMockServer(8083);
        globalTariffsMock.start();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testProductServiceFetchesCountriesFromGlobalTariffs() {
        // Setup: Mock Global Tariffs service response
        String mockTariffDefinitionsResponse = """
            {
                "success": true,
                "tariffs": [
                    {
                        "id": "1",
                        "product": "Product1",
                        "exportingFrom": "Country1",
                        "importingTo": "Country2",
                        "type": "AHS",
                        "rate": 5.0
                    },
                    {
                        "id": "2",
                        "product": "Product2",
                        "exportingFrom": "Country2",
                        "importingTo": "Country1",
                        "type": "MFN",
                        "rate": 10.0
                    }
                ]
            }
            """;

        globalTariffsMock.stubFor(get(urlEqualTo("/api/tariff-definitions/global"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockTariffDefinitionsResponse)));

        // Test: Product Service should fetch countries from Global Tariffs
        List<String> countries = globalTariffsClient.getAllCountries();

        // Verify: Countries were retrieved and parsed correctly
        assertNotNull(countries);
        assertTrue(countries.contains("Country1"));
        assertTrue(countries.contains("Country2"));
        
        // Verify: Global Tariffs was called
        globalTariffsMock.verify(getRequestedFor(urlEqualTo("/api/tariff-definitions/global")));
    }

    @Test
    void testProductServiceHandlesGlobalTariffsError() {
        // Setup: Mock Global Tariffs service error
        globalTariffsMock.stubFor(get(urlEqualTo("/api/tariff-definitions/global"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        // Test: Product Service should handle error gracefully
        assertThrows(Exception.class, () -> {
            globalTariffsClient.getAllCountries();
        });
    }
}

