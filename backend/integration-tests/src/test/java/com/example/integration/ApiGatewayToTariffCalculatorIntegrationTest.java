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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test: API Gateway -> Tariff Calculator Service
 * Tests the actual HTTP communication between API Gateway and Tariff Calculator
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApiGatewayToTariffCalculatorIntegrationTest {

    @Autowired
    private MockMvc apiGatewayMockMvc;

    @LocalServerPort
    private int apiGatewayPort;

    private WireMockServer tariffCalculatorMock;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tariffCalculatorMock = new WireMockServer(8081);
        tariffCalculatorMock.start();
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testApiGatewayForwardsTariffCalculationRequest() throws Exception {
        // Setup: Mock Tariff Calculator service response
        String mockResponse = """
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
                        .withBody(mockResponse)));

        // Test: Call API Gateway, which should forward to Tariff Calculator
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

        // Verify: Tariff Calculator was called with correct parameters
        tariffCalculatorMock.verify(getRequestedFor(urlMatching("/api/tariff\\?product=TestProduct.*")));
    }

    @Test
    void testApiGatewayHandlesTariffCalculatorError() throws Exception {
        // Setup: Mock Tariff Calculator service error
        tariffCalculatorMock.stubFor(get(urlMatching("/api/tariff\\?.*"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        // Test: API Gateway should forward the error
        apiGatewayMockMvc.perform(get("http://localhost:" + apiGatewayPort + "/api/tariff")
                        .param("product", "TestProduct")
                        .param("exportingFrom", "Country1")
                        .param("importingTo", "Country2")
                        .param("quantity", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }
}

