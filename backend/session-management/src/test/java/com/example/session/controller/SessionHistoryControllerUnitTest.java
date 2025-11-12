package com.example.session.controller;

import com.example.session.dto.CalculationHistoryDto;
import com.example.session.service.SessionHistoryService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionHistoryControllerUnitTest {

    @Mock
    private SessionHistoryService sessionHistoryService;

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private SessionHistoryController controller;

    private CalculationHistoryDto mockCalculation;

    @BeforeEach
    void setUp() {
        mockCalculation = new CalculationHistoryDto(
                "TestProduct", "Country1", "Country2", 10.0, "kg",
                1000.0, 5.0, 50.0, 1050.0, "AHS"
        );
    }

    @Test
    void testSaveCalculation_Success() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> calculationData = new HashMap<>();
        calculationData.put("product", "TestProduct");
        calculationData.put("quantity", 10.0);
        requestBody.put("calculationData", calculationData);
        
        when(sessionHistoryService.saveCalculation(eq(httpSession), any())).thenReturn(mockCalculation);

        // Act
        ResponseEntity<?> response = controller.saveCalculation(requestBody, httpSession);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(sessionHistoryService).saveCalculation(eq(httpSession), any());
    }

    @Test
    void testSaveCalculation_MissingData() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        // Missing calculationData

        // Act & Assert
        assertThrows(com.example.session.exception.BadRequestException.class, () -> {
            controller.saveCalculation(requestBody, httpSession);
        });
    }

    @Test
    void testGetCalculationHistory_Success() {
        // Arrange
        List<CalculationHistoryDto> mockHistory = List.of(mockCalculation);
        when(sessionHistoryService.getCalculationHistory(httpSession)).thenReturn(mockHistory);

        // Act
        ResponseEntity<List<CalculationHistoryDto>> response = controller.getCalculationHistory(httpSession);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(sessionHistoryService).getCalculationHistory(httpSession);
    }

    @Test
    void testGetCalculationById_Success() {
        // Arrange
        String calculationId = mockCalculation.getId();
        when(sessionHistoryService.getCalculationById(httpSession, calculationId)).thenReturn(mockCalculation);

        // Act
        ResponseEntity<CalculationHistoryDto> response = controller.getCalculationById(calculationId, null, httpSession);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("TestProduct", response.getBody().getProduct());
        verify(sessionHistoryService).getCalculationById(httpSession, calculationId);
    }

    @Test
    void testGetCalculationById_WithSessionId() {
        // Arrange
        String calculationId = mockCalculation.getId();
        String sessionId = "other-session-123";
        when(sessionHistoryService.getCalculationByIdFromSession(sessionId, calculationId)).thenReturn(mockCalculation);

        // Act
        ResponseEntity<CalculationHistoryDto> response = controller.getCalculationById(calculationId, sessionId, httpSession);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(sessionHistoryService).getCalculationByIdFromSession(sessionId, calculationId);
    }

    @Test
    void testGetCalculationById_NotFound() {
        // Arrange
        when(sessionHistoryService.getCalculationById(httpSession, "non-existent")).thenReturn(null);

        // Act & Assert
        assertThrows(com.example.session.exception.NotFoundException.class, () -> {
            controller.getCalculationById("non-existent", null, httpSession);
        });
    }

    @Test
    void testClearCalculationHistory() {
        // Arrange
        doNothing().when(sessionHistoryService).clearCalculationHistory(httpSession);

        // Act
        ResponseEntity<?> response = controller.clearCalculationHistory(httpSession);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(sessionHistoryService).clearCalculationHistory(httpSession);
    }

    @Test
    void testRemoveCalculationById_Success() {
        // Arrange
        String calculationId = "calc-123";
        doNothing().when(sessionHistoryService).removeCalculationByIdFromSession(anyString(), anyString());

        // Act
        ResponseEntity<?> response = controller.removeCalculationById(calculationId, null, httpSession);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

