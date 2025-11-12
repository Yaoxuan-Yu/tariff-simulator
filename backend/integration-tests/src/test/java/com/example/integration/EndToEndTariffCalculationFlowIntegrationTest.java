package com.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End Integration Test: Complete Tariff Calculation Flow
 * Tests the full flow: API Gateway -> Tariff Calculator -> Session Management
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EndToEndTariffCalculationFlowIntegrationTest {

    @Autowired
    private MockMvc apiGatewayMockMvc;

    @LocalServerPort
    private int apiGatewayPort;

    private WireMockServer tariffCalculatorMock;
    private WireMockServer sessionManagementMock;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tariffCalculatorMock = new WireMockServer(8081);
        sessionManagementMock = new WireMockServer(8082);
        
        tariffCalculatorMock.start();
        sessionManagementMock.start();
        
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCompleteTariffCalculationFlow() throws Exception {
        // Step 1: Mock Tariff Calculator response
        String tariffResponse = """
            {
                "success": true,
                "data": {
                    "product": "TestProduct",
                    "exportingFrom": "Country1",
                    "importingTo": "Country2",
                    "quantity": 10.0,
                    "tariffRate": 5.0,
                    "totalCost": 105.0,
                    "currency": "USD"
                }
            }
            """;

        tariffCalculatorMock.stubFor(get(urlMatching("/api/tariff\\?.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(tariffResponse)));

        // Step 2: Mock Session Management response (for saving calculation)
        String sessionResponse = """
            {
                "id": "calc-123",
                "data": {
                    "product": "TestProduct",
                    "quantity": 10.0
                }
            }
            """;

        sessionManagementMock.stubFor(post(urlEqualTo("/api/tariff/history/save"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(sessionResponse)));

        // Step 3: Execute end-to-end flow through API Gateway
        apiGatewayMockMvc.perform(get("http://localhost:" + apiGatewayPort + "/api/tariff")
                        .param("product", "TestProduct")
                        .param("exportingFrom", "Country1")
                        .param("importingTo", "Country2")
                        .param("quantity", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.product").value("TestProduct"))
                .andExpect(jsonPath("$.data.tariffRate").value(5.0));

        // Step 4: Verify all services were called in sequence
        tariffCalculatorMock.verify(getRequestedFor(urlMatching("/api/tariff\\?.*")));
        // Note: Session Management call happens asynchronously in Tariff Calculator
        // In a real scenario, we'd verify this with a delay or async verification
    }

    @Test
    void testEndToEndFlowWithErrorHandling() throws Exception {
        // Setup: Tariff Calculator returns error
        tariffCalculatorMock.stubFor(get(urlMatching("/api/tariff\\?.*"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("Bad Request: Invalid parameters")));

        // Test: API Gateway should forward the error
        apiGatewayMockMvc.perform(get("http://localhost:" + apiGatewayPort + "/api/tariff")
                        .param("product", "")
                        .param("exportingFrom", "Country1")
                        .param("importingTo", "Country2")
                        .param("quantity", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        // Verify: Session Management should NOT be called when calculation fails
        sessionManagementMock.verify(0, postRequestedFor(urlEqualTo("/api/tariff/history/save")));
    }
}

