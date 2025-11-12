package com.example.session.service;

import com.example.session.dto.CalculationHistoryDto;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.HashOperations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionHistoryServiceUnitTest {

    @Mock
    private HttpSession httpSession;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, String, Object> hashOperations;

    @InjectMocks
    private SessionHistoryService sessionHistoryService;

    private Map<String, Object> calculationData;

    @BeforeEach
    void setUp() {
        calculationData = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("product", "TestProduct");
        data.put("exportingFrom", "Country1");
        data.put("importingTo", "Country2");
        data.put("quantity", 10.0);
        data.put("unit", "kg");
        data.put("productCost", 1000.0);
        data.put("totalCost", 1050.0);
        data.put("tariffRate", 5.0);
        data.put("tariffType", "AHS");
        calculationData.put("data", data);
    }

    @Test
    void testSaveCalculation_Success() {
        // Arrange
        when(httpSession.getAttribute("CALCULATION_HISTORY")).thenReturn(null);

        // Act
        CalculationHistoryDto result = sessionHistoryService.saveCalculation(httpSession, calculationData);

        // Assert
        assertNotNull(result);
        assertEquals("TestProduct", result.getProduct());
        assertEquals("Country1", result.getExportingFrom());
        assertEquals("Country2", result.getImportingTo());
        assertEquals(10.0, result.getQuantity());
        assertEquals(1000.0, result.getProductCost());
        assertEquals(50.0, result.getTariffAmount()); // totalCost - productCost
        assertEquals(5.0, result.getTariffRate());
        verify(httpSession).setAttribute(eq("CALCULATION_HISTORY"), anyList());
    }

    @Test
    void testSaveCalculation_AddsToExistingHistory() {
        // Arrange
        List<CalculationHistoryDto> existingHistory = new ArrayList<>();
        CalculationHistoryDto existing = new CalculationHistoryDto("OldProduct", "C1", "C2", 5.0, "kg", 500.0, 2.0, 10.0, 510.0, "MFN");
        existingHistory.add(existing);
        when(httpSession.getAttribute("CALCULATION_HISTORY")).thenReturn(existingHistory);

        // Act
        CalculationHistoryDto result = sessionHistoryService.saveCalculation(httpSession, calculationData);

        // Assert
        assertNotNull(result);
        verify(httpSession).setAttribute(eq("CALCULATION_HISTORY"), argThat(list -> {
            @SuppressWarnings("unchecked")
            List<CalculationHistoryDto> history = (List<CalculationHistoryDto>) list;
            return history.size() == 2 && history.get(0).getProduct().equals("TestProduct");
        }));
    }

    @Test
    void testSaveCalculation_LimitsHistoryTo100() {
        // Arrange
        List<CalculationHistoryDto> largeHistory = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeHistory.add(new CalculationHistoryDto("Product" + i, "C1", "C2", 1.0, "kg", 100.0, 1.0, 1.0, 101.0, "AHS"));
        }
        when(httpSession.getAttribute("CALCULATION_HISTORY")).thenReturn(largeHistory);

        // Act
        sessionHistoryService.saveCalculation(httpSession, calculationData);

        // Assert
        verify(httpSession).setAttribute(eq("CALCULATION_HISTORY"), argThat(list -> {
            @SuppressWarnings("unchecked")
            List<CalculationHistoryDto> history = (List<CalculationHistoryDto>) list;
            return history.size() == 100; // Should still be 100, oldest removed
        }));
    }

    @Test
    void testSaveCalculation_MissingData() {
        // Arrange
        Map<String, Object> invalidData = new HashMap<>();

        // Act
        CalculationHistoryDto result = sessionHistoryService.saveCalculation(httpSession, invalidData);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetCalculationHistory_Empty() {
        // Arrange
        when(httpSession.getAttribute("CALCULATION_HISTORY")).thenReturn(null);

        // Act
        List<CalculationHistoryDto> result = sessionHistoryService.getCalculationHistory(httpSession);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCalculationHistory_WithData() {
        // Arrange
        List<CalculationHistoryDto> history = new ArrayList<>();
        history.add(new CalculationHistoryDto("Product1", "C1", "C2", 10.0, "kg", 1000.0, 5.0, 50.0, 1050.0, "AHS"));
        when(httpSession.getAttribute("CALCULATION_HISTORY")).thenReturn(history);

        // Act
        List<CalculationHistoryDto> result = sessionHistoryService.getCalculationHistory(httpSession);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Product1", result.get(0).getProduct());
    }

    @Test
    void testGetCalculationById_Found() {
        // Arrange
        CalculationHistoryDto calc1 = new CalculationHistoryDto("Product1", "C1", "C2", 10.0, "kg", 1000.0, 5.0, 50.0, 1050.0, "AHS");
        CalculationHistoryDto calc2 = new CalculationHistoryDto("Product2", "C1", "C2", 20.0, "kg", 2000.0, 5.0, 100.0, 2100.0, "AHS");
        List<CalculationHistoryDto> history = List.of(calc1, calc2);
        when(httpSession.getAttribute("CALCULATION_HISTORY")).thenReturn(history);

        // Act
        CalculationHistoryDto result = sessionHistoryService.getCalculationById(httpSession, calc1.getId());

        // Assert
        assertNotNull(result);
        assertEquals("Product1", result.getProduct());
    }

    @Test
    void testGetCalculationById_NotFound() {
        // Arrange
        List<CalculationHistoryDto> history = new ArrayList<>();
        when(httpSession.getAttribute("CALCULATION_HISTORY")).thenReturn(history);

        // Act
        CalculationHistoryDto result = sessionHistoryService.getCalculationById(httpSession, "non-existent-id");

        // Assert
        assertNull(result);
    }

    @Test
    void testClearCalculationHistory() {
        // Act
        sessionHistoryService.clearCalculationHistory(httpSession);

        // Assert
        verify(httpSession).removeAttribute("CALCULATION_HISTORY");
    }

    @Test
    void testGetCalculationByIdFromSession_WithRedis() {
        // Arrange
        String sessionId = "test-session-123";
        String calculationId = "calc-123";
        CalculationHistoryDto calc = new CalculationHistoryDto("Product1", "C1", "C2", 10.0, "kg", 1000.0, 5.0, 50.0, 1050.0, "AHS");
        List<CalculationHistoryDto> history = List.of(calc);
        
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get(anyString(), anyString())).thenReturn(history);

        // Act
        CalculationHistoryDto result = sessionHistoryService.getCalculationByIdFromSession(sessionId, calculationId);

        // Assert
        assertNotNull(result);
        assertEquals("Product1", result.getProduct());
    }

    @Test
    void testGetCalculationByIdFromSession_NoRedis() {
        // Arrange
        SessionHistoryService service = new SessionHistoryService();
        // redisTemplate is null

        // Act
        CalculationHistoryDto result = service.getCalculationByIdFromSession("session-id", "calc-id");

        // Assert
        assertNull(result);
    }
}

