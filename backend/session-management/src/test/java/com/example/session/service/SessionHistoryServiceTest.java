package com.example.session.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import jakarta.servlet.http.HttpSession;

public class SessionHistoryServiceTest {

    @Mock
    private HttpSession session;

    @InjectMocks
    private SessionHistoryService service;

    private Map<String, Object> testCalculationData;

    @BeforeEach
    public void setUp() {
        testCalculationData = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("product", "Test Product");
        data.put("exportingFrom", "Singapore");
        data.put("importingTo", "China");
        data.put("quantity", 2.0);
        data.put("unit", "piece");
        data.put("productCost", 20.0);
        data.put("totalCost", 23.0);
        data.put("tariffRate", 15.0);
        data.put("tariffType", "MFN (no FTA)");

        testCalculationData.put("success", true);
        testCalculationData.put("data", data);
    }

    @Test
    public void testSaveCalculation_addsToSessionHistory() {
        List<Map<String, Object>> history = new ArrayList<>();
        when(session.getAttribute("calculationHistory")).thenReturn(history);

        service.saveCalculation(session, testCalculationData);

        verify(session).setAttribute(eq("calculationHistory"), any());
        assertEquals(1, history.size());
        assertEquals("Test Product", ((Map<?, ?>) history.get(0).get("data")).get("product"));
    }

    @Test
    public void testGetCalculationHistory_returnsHistory() {
        List<Map<String, Object>> history = new ArrayList<>();
        history.add(testCalculationData);
        when(session.getAttribute("calculationHistory")).thenReturn(history);

        List<Map<String, Object>> result = service.getCalculationHistory(session);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testClearCalculationHistory_clearsSession() {
        service.clearCalculationHistory(session);
        verify(session).removeAttribute("calculationHistory");
    }

    @Test
    public void testGetCalculationById_returnsCorrectEntry() {
        List<Map<String, Object>> history = new ArrayList<>();
        Map<String, Object> entry = new HashMap<>(testCalculationData);
        entry.put("id", "123");
        history.add(entry);
        when(session.getAttribute("calculationHistory")).thenReturn(history);

        Map<String, Object> result = service.getCalculationById(session, "123");

        assertNotNull(result);
        assertEquals("123", result.get("id"));
    }
}
