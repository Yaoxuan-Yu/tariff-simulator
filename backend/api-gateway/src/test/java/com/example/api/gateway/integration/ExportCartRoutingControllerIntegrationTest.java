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
public class ExportCartRoutingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private WireMockServer csvExportMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        csvExportMock = new WireMockServer(8085);
        csvExportMock.start();
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testGetCart_Success() throws Exception {
        List<Map<String, Object>> mockCart = Arrays.asList(
                createCartItem("1", "Product1", 10.0),
                createCartItem("2", "Product2", 20.0)
        );

        csvExportMock.stubFor(get(urlEqualTo("/api/export-cart"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockCart))));

        mockMvc.perform(get("/api/export-cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("1"));

        csvExportMock.verify(getRequestedFor(urlEqualTo("/api/export-cart")));
    }

    @Test
    void testAddToCart_Success() throws Exception {
        String calculationId = "123";

        csvExportMock.stubFor(post(urlEqualTo("/api/export-cart/add/" + calculationId))
                .willReturn(aResponse()
                        .withStatus(200)));

        mockMvc.perform(post("/api/export-cart/add/" + calculationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        csvExportMock.verify(postRequestedFor(urlEqualTo("/api/export-cart/add/" + calculationId)));
    }

    @Test
    void testRemoveFromCart_Success() throws Exception {
        String calculationId = "123";

        csvExportMock.stubFor(delete(urlEqualTo("/api/export-cart/remove/" + calculationId))
                .willReturn(aResponse()
                        .withStatus(200)));

        mockMvc.perform(delete("/api/export-cart/remove/" + calculationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        csvExportMock.verify(deleteRequestedFor(urlEqualTo("/api/export-cart/remove/" + calculationId)));
    }

    @Test
    void testClearCart_Success() throws Exception {
        csvExportMock.stubFor(delete(urlEqualTo("/api/export-cart/clear"))
                .willReturn(aResponse()
                        .withStatus(200)));

        mockMvc.perform(delete("/api/export-cart/clear")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        csvExportMock.verify(deleteRequestedFor(urlEqualTo("/api/export-cart/clear")));
    }

    @Test
    void testExportCart_Success() throws Exception {
        String csvContent = "id,product,cost\n1,Product1,10.0\n";

        csvExportMock.stubFor(get(urlEqualTo("/api/export-cart/export"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/csv")
                        .withHeader("Content-Disposition", "attachment; filename=export.csv")
                        .withBody(csvContent)));

        mockMvc.perform(get("/api/export-cart/export")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"));

        csvExportMock.verify(getRequestedFor(urlEqualTo("/api/export-cart/export")));
    }

    private Map<String, Object> createCartItem(String id, String product, Double cost) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("product", product);
        item.put("cost", cost);
        return item;
    }
}

