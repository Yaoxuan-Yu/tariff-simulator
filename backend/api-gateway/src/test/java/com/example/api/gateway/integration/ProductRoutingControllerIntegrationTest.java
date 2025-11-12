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

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductRoutingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @LocalServerPort
    private int port;

    private WireMockServer productServiceMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productServiceMock = new WireMockServer(8084);
        productServiceMock.start();
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testGetProducts_Success() throws Exception {
        List<String> mockProducts = Arrays.asList("Product1", "Product2", "Product3");
        
        productServiceMock.stubFor(get(urlEqualTo("/api/products"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockProducts))));

        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("Product1"))
                .andExpect(jsonPath("$[1]").value("Product2"))
                .andExpect(jsonPath("$[2]").value("Product3"));

        productServiceMock.verify(getRequestedFor(urlEqualTo("/api/products")));
    }

    @Test
    void testGetCountries_Success() throws Exception {
        List<String> mockCountries = Arrays.asList("Country1", "Country2", "Country3");
        
        productServiceMock.stubFor(get(urlEqualTo("/api/countries"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockCountries))));

        mockMvc.perform(get("/api/countries")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("Country1"));

        productServiceMock.verify(getRequestedFor(urlEqualTo("/api/countries")));
    }

    @Test
    void testGetProducts_ServiceUnavailable() throws Exception {
        productServiceMock.stubFor(get(urlEqualTo("/api/products"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("Service Unavailable")));

        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testGetProducts_WithQueryParameters() throws Exception {
        List<String> mockProducts = Arrays.asList("Product1");
        
        productServiceMock.stubFor(get(urlMatching("/api/products\\?.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockProducts))));

        mockMvc.perform(get("/api/products")
                        .param("filter", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        productServiceMock.verify(getRequestedFor(urlMatching("/api/products\\?.*")));
    }
}

