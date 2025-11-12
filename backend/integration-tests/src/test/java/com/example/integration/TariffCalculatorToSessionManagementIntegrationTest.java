package com.example.integration;

import com.example.calculator.client.SessionManagementClient;
import com.example.calculator.dto.TariffResponse;
import com.example.calculator.service.TariffService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: Tariff Calculator -> Session Management Service
 * Tests that tariff calculator successfully calls session management to save calculations
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class TariffCalculatorToSessionManagementIntegrationTest {

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
        registry.add("services.session-management.url", () -> "http://localhost:8082");
    }

    @LocalServerPort
    private int tariffCalculatorPort;

    @Autowired
    private TariffService tariffService;

    private WireMockServer sessionManagementMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        sessionManagementMock = new WireMockServer(8082);
        sessionManagementMock.start();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testTariffCalculatorCallsSessionManagementAfterCalculation() {
        // Setup: Mock Session Management service
        String mockSessionResponse = """
            {
                "id": "calc-123",
                "data": {
                    "product": "TestProduct",
                    "quantity": 10.0
                }
            }
            """;

        sessionManagementMock.stubFor(post(urlEqualTo("/api/tariff/history/save"))
                .withHeader("X-Session-Id", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockSessionResponse)));

        // Test: Perform tariff calculation (which should trigger session save)
        TariffResponse response = tariffService.calculate(
                "TestProduct",
                "Country1",
                "Country2",
                10.0,
                null,
                "USD"
        );

        // Verify: Session Management was called
        assertTrue(response.isSuccess());
        sessionManagementMock.verify(postRequestedFor(urlEqualTo("/api/tariff/history/save"))
                .withHeader("X-Session-Id", matching(".*")));
    }

    @Test
    void testTariffCalculatorHandlesSessionManagementFailure() {
        // Setup: Mock Session Management service failure
        sessionManagementMock.stubFor(post(urlEqualTo("/api/tariff/history/save"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Service Unavailable")));

        // Test: Calculation should still succeed even if session save fails
        TariffResponse response = tariffService.calculate(
                "TestProduct",
                "Country1",
                "Country2",
                10.0,
                null,
                "USD"
        );

        // Verify: Calculation succeeded despite session management failure
        assertTrue(response.isSuccess());
        sessionManagementMock.verify(postRequestedFor(urlEqualTo("/api/tariff/history/save")));
    }
}

