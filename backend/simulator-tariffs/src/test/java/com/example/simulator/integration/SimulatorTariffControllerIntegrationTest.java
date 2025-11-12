package com.example.simulator.integration;

import com.example.simulator.dto.TariffDefinitionsResponse;
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
public class SimulatorTariffControllerIntegrationTest {

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetUserTariffDefinitions_Empty() throws Exception {
        mockMvc.perform(get("/api/tariff-definitions/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tariffs").isArray())
                .andExpect(jsonPath("$.tariffs").isEmpty());
    }

    @Test
    void testAddUserTariffDefinition_Success() throws Exception {
        TariffDefinitionsResponse.TariffDefinitionDto dto = new TariffDefinitionsResponse.TariffDefinitionDto();
        dto.setProduct("TestProduct");
        dto.setExportingFrom("Country1");
        dto.setImportingTo("Country2");
        dto.setType("AHS");
        dto.setRate(5.0);
        dto.setEffectiveDate("2024-01-01");
        dto.setExpirationDate("2024-12-31");

        mockMvc.perform(post("/api/tariff-definitions/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tariffs[0].product").value("TestProduct"));
    }

    @Test
    void testUpdateUserTariffDefinition_Success() throws Exception {
        // First add a tariff definition
        TariffDefinitionsResponse.TariffDefinitionDto addDto = new TariffDefinitionsResponse.TariffDefinitionDto();
        addDto.setProduct("TestProduct");
        addDto.setExportingFrom("Country1");
        addDto.setImportingTo("Country2");
        addDto.setType("AHS");
        addDto.setRate(5.0);

        String response = mockMvc.perform(post("/api/tariff-definitions/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract ID from response
        TariffDefinitionsResponse responseObj = objectMapper.readValue(response, TariffDefinitionsResponse.class);
        String id = responseObj.getTariffs().get(0).getId();

        // Update the tariff definition
        TariffDefinitionsResponse.TariffDefinitionDto updateDto = new TariffDefinitionsResponse.TariffDefinitionDto();
        updateDto.setRate(7.0);

        mockMvc.perform(put("/api/tariff-definitions/user/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteUserTariffDefinition_Success() throws Exception {
        // First add a tariff definition
        TariffDefinitionsResponse.TariffDefinitionDto addDto = new TariffDefinitionsResponse.TariffDefinitionDto();
        addDto.setProduct("TestProduct");
        addDto.setExportingFrom("Country1");
        addDto.setImportingTo("Country2");
        addDto.setType("AHS");
        addDto.setRate(5.0);

        String response = mockMvc.perform(post("/api/tariff-definitions/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TariffDefinitionsResponse responseObj = objectMapper.readValue(response, TariffDefinitionsResponse.class);
        String id = responseObj.getTariffs().get(0).getId();

        // Delete the tariff definition
        mockMvc.perform(delete("/api/tariff-definitions/user/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify it's deleted
        mockMvc.perform(get("/api/tariff-definitions/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tariffs").isEmpty());
    }

    @Test
    void testAddUserTariffDefinition_MissingData() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();

        mockMvc.perform(post("/api/tariff-definitions/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }
}

