package com.example.calculator.controller;

import com.example.calculator.client.SessionManagementClient;
import com.example.calculator.dto.TariffResponse;
import com.example.calculator.service.TariffService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TariffCalculationControllerUnitTest {

    @Mock
    private TariffService tariffService;

    @Mock
    private SessionManagementClient sessionManagementClient;

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private TariffCalculationController controller;

    private TariffResponse mockResponse;

    @BeforeEach
    void setUp() {
        TariffResponse.TariffCalculationData data = new TariffResponse.TariffCalculationData(
                "TestProduct", "Country1", "Country2", 10.0, "kg",
                1000.0, 1050.0, 5.0, "AHS", null, "USD"
        );
        mockResponse = new TariffResponse(true, data);
    }

    @Test
    void testCalculateTariff_Success() {
        // Arrange
        when(httpSession.getId()).thenReturn("test-session-123");
        when(tariffService.calculate(anyString(), anyString(), anyString(), anyDouble(), anyString(), anyString()))
                .thenReturn(mockResponse);
        doNothing().when(sessionManagementClient).saveCalculation(anyString(), any());

        // Act
        ResponseEntity<TariffResponse> response = controller.calculateTariff(
                "TestProduct", "Country1", "Country2", 10.0, null, "USD", null, null, httpSession
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(tariffService).calculate("TestProduct", "Country1", "Country2", 10.0, null, "USD");
    }

    @Test
    void testCalculateTariff_WithUserMode() {
        // Arrange
        when(httpSession.getId()).thenReturn("test-session-123");
        when(tariffService.calculateWithMode(anyString(), anyString(), anyString(), anyDouble(),
                anyString(), anyString(), anyString(), anyString(), any(HttpSession.class)))
                .thenReturn(mockResponse);
        doNothing().when(sessionManagementClient).saveCalculation(anyString(), any());

        // Act
        ResponseEntity<TariffResponse> response = controller.calculateTariff(
                "TestProduct", "Country1", "Country2", 10.0, null, "USD", "user", null, httpSession
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tariffService).calculateWithMode(eq("TestProduct"), eq("Country1"), eq("Country2"),
                eq(10.0), isNull(), eq("USD"), eq("user"), isNull(), eq(httpSession));
    }

    @Test
    void testCalculateTariff_InvalidProduct() {
        // Act & Assert
        assertThrows(com.example.calculator.exception.BadRequestException.class, () -> {
            controller.calculateTariff("", "Country1", "Country2", 10.0, null, "USD", null, null, httpSession);
        });
    }

    @Test
    void testCalculateTariff_InvalidQuantity() {
        // Act & Assert
        assertThrows(com.example.calculator.exception.BadRequestException.class, () -> {
            controller.calculateTariff("TestProduct", "Country1", "Country2", 0.0, null, "USD", null, null, httpSession);
        });
    }

    @Test
    void testCalculateTariff_InvalidCustomCost() {
        // Act & Assert
        assertThrows(com.example.calculator.exception.BadRequestException.class, () -> {
            controller.calculateTariff("TestProduct", "Country1", "Country2", 10.0, "invalid", "USD", null, null, httpSession);
        });
    }

    @Test
    void testCalculateTariff_NegativeCustomCost() {
        // Act & Assert
        assertThrows(com.example.calculator.exception.BadRequestException.class, () -> {
            controller.calculateTariff("TestProduct", "Country1", "Country2", 10.0, "-100", "USD", null, null, httpSession);
        });
    }
}

