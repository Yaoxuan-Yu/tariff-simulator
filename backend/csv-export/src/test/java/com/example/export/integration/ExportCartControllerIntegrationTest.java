package com.example.export.integration;

import com.example.session.dto.CalculationHistoryDto;
import com.example.export.service.ExportCartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import com.github.tomakehurst.wiremock.WireMockServer;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class ExportCartControllerIntegrationTest {

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Container
    static GenericContainer<?> sessionManagement = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExportCartService exportCartService;

    private WireMockServer sessionManagementMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        sessionManagementMock = new WireMockServer(8082);
        sessionManagementMock.start();
        objectMapper = new ObjectMapper();

        // Mock session management service
        Map<String, Object> mockCalculation = new HashMap<>();
        mockCalculation.put("id", "calc-1");
        mockCalculation.put("product", "TestProduct");
        mockCalculation.put("quantity", 10.0);

        sessionManagementMock.stubFor(get(urlMatching("/api/tariff/history.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"calc-1\",\"data\":{\"product\":\"TestProduct\",\"quantity\":10.0}}]")));
    }

    @Test
    void testGetCart_Empty() throws Exception {
        mockMvc.perform(get("/api/export-cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testAddToCart_Success() throws Exception {
        String calculationId = "calc-1";

        mockMvc.perform(post("/api/export-cart/add/" + calculationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCart_AfterAdd() throws Exception {
        String calculationId = "calc-1";

        // Add to cart
        mockMvc.perform(post("/api/export-cart/add/" + calculationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Get cart
        mockMvc.perform(get("/api/export-cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testRemoveFromCart_Success() throws Exception {
        String calculationId = "calc-1";

        // Add to cart first
        mockMvc.perform(post("/api/export-cart/add/" + calculationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Remove from cart
        mockMvc.perform(delete("/api/export-cart/remove/" + calculationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testClearCart_Success() throws Exception {
        String calculationId = "calc-1";

        // Add to cart first
        mockMvc.perform(post("/api/export-cart/add/" + calculationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Clear cart
        mockMvc.perform(delete("/api/export-cart/clear")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify cart is empty
        mockMvc.perform(get("/api/export-cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testExportCartAsCsv_Success() throws Exception {
        String calculationId = "calc-1";

        // Add to cart first
        mockMvc.perform(post("/api/export-cart/add/" + calculationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Export as CSV
        mockMvc.perform(get("/api/export-cart/export")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=export.csv"));
    }

    @Test
    void testExportCartAsCsv_EmptyCart() throws Exception {
        mockMvc.perform(get("/api/export-cart/export")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}

