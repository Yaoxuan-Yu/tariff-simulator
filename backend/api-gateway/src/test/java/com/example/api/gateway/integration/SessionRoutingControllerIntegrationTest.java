package com.example.api.gateway.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.github.tomakehurst.wiremock.WireMockServer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SessionRoutingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private WireMockServer sessionManagementMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        sessionManagementMock = new WireMockServer(8082);
        sessionManagementMock.start();
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testGetCalculationHistory_Success() throws Exception {
        List<Map<String, Object>> mockHistory = Arrays.asList(
                createHistoryItem("1", "Product1", 10.0),
                createHistoryItem("2", "Product2", 20.0)
        );

        sessionManagementMock.stubFor(get(urlEqualTo("/api/tariff/history"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockHistory))));

        mockMvc.perform(get("/api/tariff/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("1"));

        sessionManagementMock.verify(getRequestedFor(urlEqualTo("/api/tariff/history")));
    }

    @Test
    void testSaveCalculation_Success() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> calculationData = new HashMap<>();
        calculationData.put("product", "Product1");
        calculationData.put("quantity", 10);
        requestBody.put("calculationData", calculationData);

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("id", "123");
        mockResponse.put("success", true);

        sessionManagementMock.stubFor(post(urlEqualTo("/api/tariff/history/save"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        mockMvc.perform(post("/api/tariff/history/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        sessionManagementMock.verify(postRequestedFor(urlEqualTo("/api/tariff/history/save")));
    }

    @Test
    void testClearCalculationHistory_Success() throws Exception {
        sessionManagementMock.stubFor(delete(urlEqualTo("/api/tariff/history/clear"))
                .willReturn(aResponse()
                        .withStatus(200)));

        mockMvc.perform(delete("/api/tariff/history/clear")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        sessionManagementMock.verify(deleteRequestedFor(urlEqualTo("/api/tariff/history/clear")));
    }

    private Map<String, Object> createHistoryItem(String id, String product, Double cost) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("product", product);
        item.put("cost", cost);
        return item;
    }
}

