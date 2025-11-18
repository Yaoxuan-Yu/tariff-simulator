package com.example.session.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.session.service.SessionHistoryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.HttpSession;

import java.util.*;

public class SessionHistoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SessionHistoryService service;

    @Mock
    private HttpSession session;

    @InjectMocks
    private SessionHistoryController controller;

    private Map<String, Object> testCalculationData;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        testCalculationData = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("product", "Test Product");
        testCalculationData.put("success", true);
        testCalculationData.put("data", data);
    }

    @Test
    public void testSaveCalculationEndpoint() throws Exception {
        doNothing().when(service).saveCalculation(eq(session), any());

        mockMvc.perform(post("/api/tariff/history/save")
                        .sessionAttr("calculationHistory", null)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":{\"product\":\"Test Product\"}}"))
                .andExpect(status().isOk());

        verify(service).saveCalculation(eq(session), any());
    }

    @Test
    public void testGetCalculationHistoryEndpoint() throws Exception {
        List<Map<String, Object>> history = new ArrayList<>();
        history.add(testCalculationData);
        when(service.getCalculationHistory(session)).thenReturn(history);

        mockMvc.perform(get("/api/tariff/history")
                        .sessionAttr("calculationHistory", history))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].success").value(true));
    }

    @Test
    public void testClearCalculationHistoryEndpoint() throws Exception {
        doNothing().when(service).clearCalculationHistory(session);

        mockMvc.perform(delete("/api/tariff/history/clear")
                        .sessionAttr("calculationHistory", null))
                .andExpect(status().isOk());

        verify(service).clearCalculationHistory(session);
    }
}
