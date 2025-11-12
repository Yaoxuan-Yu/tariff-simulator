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

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class InsightsRoutingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private WireMockServer tradeInsightsMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tradeInsightsMock = new WireMockServer(8088);
        tradeInsightsMock.start();
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testSearchNews_Success() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", "trade");
        requestBody.put("country", "USA");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("articles", java.util.Collections.emptyList());

        tradeInsightsMock.stubFor(post(urlEqualTo("/api/news/search"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        mockMvc.perform(post("/api/news/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        tradeInsightsMock.verify(postRequestedFor(urlEqualTo("/api/news/search")));
    }

    @Test
    void testSearchAgreements_Success() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("country", "USA");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("agreements", java.util.Collections.emptyList());

        tradeInsightsMock.stubFor(post(urlEqualTo("/api/agreements/search"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        mockMvc.perform(post("/api/agreements/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        tradeInsightsMock.verify(postRequestedFor(urlEqualTo("/api/agreements/search")));
    }

    @Test
    void testSearchTradeInsights_Success() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", "ASEAN");
        requestBody.put("country", "Singapore");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("news", java.util.Collections.emptyList());
        mockResponse.put("agreements", java.util.Collections.emptyList());

        tradeInsightsMock.stubFor(post(urlEqualTo("/api/trade-insights/search"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        mockMvc.perform(post("/api/trade-insights/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        tradeInsightsMock.verify(postRequestedFor(urlEqualTo("/api/trade-insights/search")));
    }
}

