package com.example.api.gateway.integration;

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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TariffRoutingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @LocalServerPort
    private int port;

    private WireMockServer tariffCalculatorMock;
    private WireMockServer globalTariffsMock;
    private WireMockServer simulatorTariffsMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tariffCalculatorMock = new WireMockServer(8081);
        globalTariffsMock = new WireMockServer(8083);
        simulatorTariffsMock = new WireMockServer(8086);
        
        tariffCalculatorMock.start();
        globalTariffsMock.start();
        simulatorTariffsMock.start();
        
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testCalculateTariff_Success() throws Exception {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("tariffRate", 5.0);
        data.put("totalCost", 105.0);
        mockResponse.put("data", data);

        tariffCalculatorMock.stubFor(get(urlMatching("/api/tariff\\?.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        mockMvc.perform(get("/api/tariff")
                        .param("product", "TestProduct")
                        .param("exportingFrom", "Country1")
                        .param("importingTo", "Country2")
                        .param("quantity", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tariffRate").value(5.0));

        tariffCalculatorMock.verify(getRequestedFor(urlMatching("/api/tariff\\?.*")));
    }

    @Test
    void testGetGlobalTariffDefinitions_Success() throws Exception {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("tariffs", java.util.Collections.emptyList());

        globalTariffsMock.stubFor(get(urlEqualTo("/api/tariff-definitions/global"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        mockMvc.perform(get("/api/tariff-definitions/global")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        globalTariffsMock.verify(getRequestedFor(urlEqualTo("/api/tariff-definitions/global")));
    }

    @Test
    void testGetUserTariffDefinitions_Success() throws Exception {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("tariffs", java.util.Collections.emptyList());

        simulatorTariffsMock.stubFor(get(urlEqualTo("/api/tariff-definitions/user"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        mockMvc.perform(get("/api/tariff-definitions/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        simulatorTariffsMock.verify(getRequestedFor(urlEqualTo("/api/tariff-definitions/user")));
    }

    @Test
    void testAddUserTariffDefinition_Success() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("product", "TestProduct");
        requestBody.put("exportingFrom", "Country1");
        requestBody.put("importingTo", "Country2");
        requestBody.put("rate", 5.0);

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);

        simulatorTariffsMock.stubFor(post(urlEqualTo("/api/tariff-definitions/user"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        mockMvc.perform(post("/api/tariff-definitions/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        simulatorTariffsMock.verify(postRequestedFor(urlEqualTo("/api/tariff-definitions/user")));
    }

    @Test
    void testUpdateUserTariffDefinition_Success() throws Exception {
        String tariffId = "123";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("rate", 7.0);

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);

        simulatorTariffsMock.stubFor(put(urlEqualTo("/api/tariff-definitions/user/" + tariffId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        mockMvc.perform(put("/api/tariff-definitions/user/" + tariffId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        simulatorTariffsMock.verify(putRequestedFor(urlEqualTo("/api/tariff-definitions/user/" + tariffId)));
    }

    @Test
    void testDeleteUserTariffDefinition_Success() throws Exception {
        String tariffId = "123";

        simulatorTariffsMock.stubFor(delete(urlEqualTo("/api/tariff-definitions/user/" + tariffId))
                .willReturn(aResponse()
                        .withStatus(200)));

        mockMvc.perform(delete("/api/tariff-definitions/user/" + tariffId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        simulatorTariffsMock.verify(deleteRequestedFor(urlEqualTo("/api/tariff-definitions/user/" + tariffId)));
    }

    @Test
    void testGetModifiedTariffDefinitions_Success() throws Exception {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);

        globalTariffsMock.stubFor(get(urlEqualTo("/api/tariff-definitions/modified"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        mockMvc.perform(get("/api/tariff-definitions/modified")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        globalTariffsMock.verify(getRequestedFor(urlEqualTo("/api/tariff-definitions/modified")));
    }

    @Test
    void testAddModifiedTariffDefinition_Success() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("product", "TestProduct");
        requestBody.put("rate", 5.0);

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);

        globalTariffsMock.stubFor(post(urlEqualTo("/api/tariff-definitions/modified"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        mockMvc.perform(post("/api/tariff-definitions/modified")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        globalTariffsMock.verify(postRequestedFor(urlEqualTo("/api/tariff-definitions/modified")));
    }

    @Test
    void testGetAdminDashboardStats_Success() throws Exception {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("totalTariffs", 100);
        mockResponse.put("totalProducts", 50);

        globalTariffsMock.stubFor(get(urlEqualTo("/api/admin/dashboard/stats"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        mockMvc.perform(get("/api/admin/dashboard/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTariffs").value(100));

        globalTariffsMock.verify(getRequestedFor(urlEqualTo("/api/admin/dashboard/stats")));
    }
}

