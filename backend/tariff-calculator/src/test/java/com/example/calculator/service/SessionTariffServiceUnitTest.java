package com.example.calculator.service;

import com.example.calculator.dto.TariffDefinitionsResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionTariffServiceUnitTest {

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private SessionTariffService sessionTariffService;

    @Test
    void testGetTariffDefinitions_Empty() {
        // Arrange
        when(httpSession.getAttribute("SESSION_USER_TARIFFS")).thenReturn(null);

        // Act
        List<TariffDefinitionsResponse.TariffDefinitionDto> result = sessionTariffService.getTariffDefinitions(httpSession);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTariffDefinitions_WithData() {
        // Arrange
        List<Map<String, Object>> sessionTariffs = new ArrayList<>();
        sessionTariffs.add(Map.of(
                "id", "test-id",
                "product", "TestProduct",
                "exportingFrom", "Country1",
                "importingTo", "Country2",
                "type", "AHS",
                "rate", 5.0,
                "effectiveDate", "2024-01-01",
                "expirationDate", "2024-12-31"
        ));
        when(httpSession.getAttribute("SESSION_USER_TARIFFS")).thenReturn(sessionTariffs);

        // Act
        List<TariffDefinitionsResponse.TariffDefinitionDto> result = sessionTariffService.getTariffDefinitions(httpSession);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TestProduct", result.get(0).getProduct());
    }
}

