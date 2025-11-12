package com.example.session.integration;

import com.example.session.dto.CalculationHistoryDto;
import com.example.session.service.SessionHistoryService;
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

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class SessionHistoryControllerIntegrationTest {

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionHistoryService sessionHistoryService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetCalculationHistory_Empty() throws Exception {
        mockMvc.perform(get("/api/tariff/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testSaveCalculation_Success() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> calculationData = new HashMap<>();
        calculationData.put("product", "TestProduct");
        calculationData.put("exportingFrom", "Country1");
        calculationData.put("importingTo", "Country2");
        calculationData.put("quantity", 10.0);
        calculationData.put("tariffRate", 5.0);
        calculationData.put("totalCost", 105.0);
        requestBody.put("calculationData", calculationData);

        mockMvc.perform(post("/api/tariff/history/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.data.product").value("TestProduct"));
    }

    @Test
    void testGetCalculationHistory_AfterSave() throws Exception {
        // First save a calculation
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> calculationData = new HashMap<>();
        calculationData.put("product", "TestProduct");
        calculationData.put("quantity", 10.0);
        requestBody.put("calculationData", calculationData);

        mockMvc.perform(post("/api/tariff/history/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        // Then retrieve history
        mockMvc.perform(get("/api/tariff/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].data.product").value("TestProduct"));
    }

    @Test
    void testGetCalculationById_Success() throws Exception {
        // Save a calculation first
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> calculationData = new HashMap<>();
        calculationData.put("product", "TestProduct");
        requestBody.put("calculationData", calculationData);

        String response = mockMvc.perform(post("/api/tariff/history/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract ID from response
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        String id = (String) responseMap.get("id");

        // Get calculation by ID
        mockMvc.perform(get("/api/tariff/history/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.data.product").value("TestProduct"));
    }

    @Test
    void testClearCalculationHistory_Success() throws Exception {
        // Save a calculation first
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> calculationData = new HashMap<>();
        calculationData.put("product", "TestProduct");
        requestBody.put("calculationData", calculationData);

        mockMvc.perform(post("/api/tariff/history/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        // Clear history
        mockMvc.perform(delete("/api/tariff/history/clear")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify history is empty
        mockMvc.perform(get("/api/tariff/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testRemoveCalculationById_Success() throws Exception {
        // Save a calculation first
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> calculationData = new HashMap<>();
        calculationData.put("product", "TestProduct");
        requestBody.put("calculationData", calculationData);

        String response = mockMvc.perform(post("/api/tariff/history/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        String id = (String) responseMap.get("id");

        // Remove calculation by ID
        mockMvc.perform(delete("/api/tariff/history/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify calculation is removed
        mockMvc.perform(get("/api/tariff/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testSaveCalculation_MissingData() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();

        mockMvc.perform(post("/api/tariff/history/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }
}

