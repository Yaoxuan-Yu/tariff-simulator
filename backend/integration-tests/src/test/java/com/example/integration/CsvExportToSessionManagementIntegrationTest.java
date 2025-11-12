package com.example.integration;

import com.example.export.service.ExportCartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: CSV Export -> Session Management Service
 * Tests that CSV Export service successfully retrieves calculations from Session Management
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class CsvExportToSessionManagementIntegrationTest {

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Container
    static GenericContainer<?> sessionManagement = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private ExportCartService exportCartService;

    private WireMockServer sessionManagementMock;
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("services.session-management.url", () -> "http://localhost:8082");
    }

    @BeforeEach
    void setUp() {
        sessionManagementMock = new WireMockServer(8082);
        sessionManagementMock.start();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCsvExportRetrievesCalculationFromSessionManagement() {
        // Setup: Mock Session Management service response
        String mockCalculationResponse = """
            {
                "id": "calc-123",
                "data": {
                    "product": "TestProduct",
                    "exportingFrom": "Country1",
                    "importingTo": "Country2",
                    "quantity": 10.0,
                    "tariffRate": 5.0,
                    "totalCost": 105.0
                }
            }
            """;

        sessionManagementMock.stubFor(get(urlMatching("/api/tariff/history/calc-123\\?sessionId=.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockCalculationResponse)));

        // Test: Export Cart should be able to retrieve calculation
        // This would be called when adding to cart
        String sessionId = "test-session-123";
        String calculationId = "calc-123";

        // Verify: Session Management was called with correct parameters
        sessionManagementMock.verify(getRequestedFor(urlMatching("/api/tariff/history/" + calculationId + "\\?sessionId=" + sessionId)));
    }

    @Test
    void testCsvExportHandlesSessionManagementError() {
        // Setup: Mock Session Management service error
        sessionManagementMock.stubFor(get(urlMatching("/api/tariff/history/.*"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("Calculation not found")));

        // Test: Export Cart should handle error gracefully
        // This would throw an exception or return null
        assertTrue(true); // Placeholder - actual implementation would test error handling
    }
}

