package com.example.simulator.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.example.simulator.dto.TariffDefinitionsResponse;
import com.example.simulator.service.SessionTariffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.HttpSession;

public class SessionTariffControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SessionTariffService service;

    @Mock
    private HttpSession session;

    @InjectMocks
    private SessionTariffController controller;

    private TariffDefinitionsResponse.TariffDefinitionDto testDto;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        testDto = new TariffDefinitionsResponse.TariffDefinitionDto(
                "test-id",
                "Test Product",
                "Singapore",
                "China",
                "AHS",
                5.0,
                "2024-01-01",
                "2025-01-01"
        );
    }

    @Test
    public void testGetTariffDefinitions() throws Exception {
        when(service.getTariffDefinitions(session)).thenReturn(List.of(testDto));

        mockMvc.perform(get("/tariffs")
                        .sessionAttr("tariffDefinitions", List.of(testDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("test-id"));
    }

    @Test
    public void testSaveTariffDefinition() throws Exception {
        String json = """
                {
                    "id": "test-id",
                    "product": "Test Product",
                    "country": "Singapore",
                    "partner": "China",
                    "type": "AHS",
                    "rate": 5.0,
                    "startDate": "2024-01-01",
                    "endDate": "2025-01-01"
                }
                """;

        mockMvc.perform(post("/tariffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .sessionAttr("tariffDefinitions", List.of()))
                .andExpect(status().isOk());
        verify(service).saveTariffDefinition(any(), any());
    }
}
