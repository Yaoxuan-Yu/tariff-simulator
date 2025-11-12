package com.example.simulator.service;

import com.example.simulator.dto.TariffDefinitionsResponse;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionTariffServiceUnitTest {

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private SessionTariffService sessionTariffService;

    private TariffDefinitionsResponse.TariffDefinitionDto testTariff;

    @BeforeEach
    void setUp() {
        testTariff = new TariffDefinitionsResponse.TariffDefinitionDto();
        testTariff.setId("test-id-1");
        testTariff.setProduct("TestProduct");
        testTariff.setExportingFrom("Country1");
        testTariff.setImportingTo("Country2");
        testTariff.setType("AHS");
        testTariff.setRate(5.0);
        testTariff.setEffectiveDate("2024-01-01");
        testTariff.setExpirationDate("2024-12-31");
    }

    @Test
    void testSaveTariffDefinition_NewTariff() {
        // Arrange
        when(httpSession.getAttribute("SESSION_USER_TARIFFS")).thenReturn(null);

        // Act
        TariffDefinitionsResponse.TariffDefinitionDto result = sessionTariffService.saveTariffDefinition(httpSession, testTariff);

        // Assert
        assertNotNull(result);
        assertEquals("test-id-1", result.getId());
        verify(httpSession).setAttribute(eq("SESSION_USER_TARIFFS"), anyList());
    }

    @Test
    void testSaveTariffDefinition_GeneratesIdIfMissing() {
        // Arrange
        testTariff.setId(null);
        when(httpSession.getAttribute("SESSION_USER_TARIFFS")).thenReturn(null);

        // Act
        TariffDefinitionsResponse.TariffDefinitionDto result = sessionTariffService.saveTariffDefinition(httpSession, testTariff);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertFalse(result.getId().isEmpty());
    }

    @Test
    void testSaveTariffDefinition_UpdatesExisting() {
        // Arrange
        List<Map<String, Object>> existingTariffs = new ArrayList<>();
        Map<String, Object> existing = Map.of(
                "id", "test-id-1",
                "product", "OldProduct",
                "exportingFrom", "Country1",
                "importingTo", "Country2",
                "type", "AHS",
                "rate", 3.0,
                "effectiveDate", "2024-01-01",
                "expirationDate", "2024-12-31"
        );
        existingTariffs.add(existing);
        when(httpSession.getAttribute("SESSION_USER_TARIFFS")).thenReturn(existingTariffs);

        // Act
        TariffDefinitionsResponse.TariffDefinitionDto result = sessionTariffService.saveTariffDefinition(httpSession, testTariff);

        // Assert
        assertNotNull(result);
        assertEquals("test-id-1", result.getId());
        assertEquals("TestProduct", result.getProduct()); // Updated
        verify(httpSession).setAttribute(eq("SESSION_USER_TARIFFS"), anyList());
    }

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
        List<Map<String, Object>> existingTariffs = new ArrayList<>();
        existingTariffs.add(Map.of(
                "id", "test-id-1",
                "product", "TestProduct",
                "exportingFrom", "Country1",
                "importingTo", "Country2",
                "type", "AHS",
                "rate", 5.0,
                "effectiveDate", "2024-01-01",
                "expirationDate", "2024-12-31"
        ));
        when(httpSession.getAttribute("SESSION_USER_TARIFFS")).thenReturn(existingTariffs);

        // Act
        List<TariffDefinitionsResponse.TariffDefinitionDto> result = sessionTariffService.getTariffDefinitions(httpSession);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TestProduct", result.get(0).getProduct());
    }

    @Test
    void testGetTariffDefinitionById_Found() {
        // Arrange
        List<Map<String, Object>> existingTariffs = new ArrayList<>();
        existingTariffs.add(Map.of(
                "id", "test-id-1",
                "product", "TestProduct",
                "exportingFrom", "Country1",
                "importingTo", "Country2",
                "type", "AHS",
                "rate", 5.0,
                "effectiveDate", "2024-01-01",
                "expirationDate", "2024-12-31"
        ));
        when(httpSession.getAttribute("SESSION_USER_TARIFFS")).thenReturn(existingTariffs);

        // Act
        TariffDefinitionsResponse.TariffDefinitionDto result = sessionTariffService.getTariffDefinitionById(httpSession, "test-id-1");

        // Assert
        assertNotNull(result);
        assertEquals("test-id-1", result.getId());
        assertEquals("TestProduct", result.getProduct());
    }

    @Test
    void testGetTariffDefinitionById_NotFound() {
        // Arrange
        when(httpSession.getAttribute("SESSION_USER_TARIFFS")).thenReturn(new ArrayList<>());

        // Act
        TariffDefinitionsResponse.TariffDefinitionDto result = sessionTariffService.getTariffDefinitionById(httpSession, "non-existent");

        // Assert
        assertNull(result);
    }

    @Test
    void testUpdateTariffDefinition_Success() {
        // Arrange
        List<Map<String, Object>> existingTariffs = new ArrayList<>();
        existingTariffs.add(Map.of(
                "id", "test-id-1",
                "product", "OldProduct",
                "exportingFrom", "Country1",
                "importingTo", "Country2",
                "type", "AHS",
                "rate", 3.0,
                "effectiveDate", "2024-01-01",
                "expirationDate", "2024-12-31"
        ));
        when(httpSession.getAttribute("SESSION_USER_TARIFFS")).thenReturn(existingTariffs);

        // Act
        TariffDefinitionsResponse.TariffDefinitionDto result = sessionTariffService.updateTariffDefinition(
                httpSession, "test-id-1", testTariff
        );

        // Assert
        assertNotNull(result);
        assertEquals("test-id-1", result.getId());
        assertEquals("TestProduct", result.getProduct()); // Updated
        assertEquals(5.0, result.getRate()); // Updated
        verify(httpSession).setAttribute(eq("SESSION_USER_TARIFFS"), anyList());
    }

    @Test
    void testUpdateTariffDefinition_NotFound() {
        // Arrange
        when(httpSession.getAttribute("SESSION_USER_TARIFFS")).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(com.example.simulator.exception.NotFoundException.class, () -> {
            sessionTariffService.updateTariffDefinition(httpSession, "non-existent", testTariff);
        });
    }

    @Test
    void testDeleteTariffDefinition_Success() {
        // Arrange
        List<Map<String, Object>> existingTariffs = new ArrayList<>();
        existingTariffs.add(Map.of(
                "id", "test-id-1",
                "product", "TestProduct",
                "exportingFrom", "Country1",
                "importingTo", "Country2",
                "type", "AHS",
                "rate", 5.0,
                "effectiveDate", "2024-01-01",
                "expirationDate", "2024-12-31"
        ));
        when(httpSession.getAttribute("SESSION_USER_TARIFFS")).thenReturn(existingTariffs);

        // Act
        assertDoesNotThrow(() -> {
            sessionTariffService.deleteTariffDefinition(httpSession, "test-id-1");
        });

        // Assert
        verify(httpSession).setAttribute(eq("SESSION_USER_TARIFFS"), anyList());
    }

    @Test
    void testDeleteTariffDefinition_NotFound() {
        // Arrange
        when(httpSession.getAttribute("SESSION_USER_TARIFFS")).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(com.example.simulator.exception.NotFoundException.class, () -> {
            sessionTariffService.deleteTariffDefinition(httpSession, "non-existent");
        });
    }

    @Test
    void testClearTariffDefinitions() {
        // Act
        sessionTariffService.clearTariffDefinitions(httpSession);

        // Assert
        verify(httpSession).removeAttribute("SESSION_USER_TARIFFS");
    }
}

