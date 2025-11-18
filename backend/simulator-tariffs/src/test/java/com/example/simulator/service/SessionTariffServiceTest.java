package com.example.simulator.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.simulator.dto.TariffDefinitionsResponse;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
public class SessionTariffServiceTest {

    @Mock
    private HttpSession session;

    @InjectMocks
    private SessionTariffService sessionTariffService;

    private TariffDefinitionsResponse.TariffDefinitionDto testTariffDto;

    @BeforeEach
    public void setUp() {
        testTariffDto = new TariffDefinitionsResponse.TariffDefinitionDto(
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
    public void testSaveTariffDefinition_withId() {
        when(session.getAttribute(anyString())).thenReturn(null);
        sessionTariffService.saveTariffDefinition(testTariffDto, session);

        List<TariffDefinitionsResponse.TariffDefinitionDto> saved =
                sessionTariffService.getTariffDefinitions(session);
        assertEquals(1, saved.size());
        assertEquals("test-id", saved.get(0).getId());
    }

    @Test
    public void testSaveTariffDefinition_withoutId_generatesUUID() {
        testTariffDto.setId(null);
        when(session.getAttribute(anyString())).thenReturn(null);

        sessionTariffService.saveTariffDefinition(testTariffDto, session);

        List<TariffDefinitionsResponse.TariffDefinitionDto> saved =
                sessionTariffService.getTariffDefinitions(session);
        assertNotNull(saved.get(0).getId());
        assertDoesNotThrow(() -> UUID.fromString(saved.get(0).getId()));
    }

    @Test
    public void testGetTariffDefinitionById_found() {
        when(session.getAttribute(anyString())).thenReturn(List.of(testTariffDto));
        TariffDefinitionsResponse.TariffDefinitionDto result =
                sessionTariffService.getTariffDefinitionById("test-id", session);
        assertEquals("test-id", result.getId());
    }

    @Test
    public void testGetTariffDefinitionById_notFound() {
        when(session.getAttribute(anyString())).thenReturn(List.of(testTariffDto));
        assertThrows(Exception.class, () ->
                sessionTariffService.getTariffDefinitionById("invalid-id", session));
    }

    @Test
    public void testUpdateTariffDefinition() {
        when(session.getAttribute(anyString())).thenReturn(List.of(testTariffDto));
        testTariffDto.setProduct("Updated Product");
        sessionTariffService.updateTariffDefinition("test-id", testTariffDto, session);

        TariffDefinitionsResponse.TariffDefinitionDto updated =
                sessionTariffService.getTariffDefinitionById("test-id", session);
        assertEquals("Updated Product", updated.getProduct());
    }

    @Test
    public void testDeleteTariffDefinition() {
        when(session.getAttribute(anyString())).thenReturn(List.of(testTariffDto));
        sessionTariffService.deleteTariffDefinition("test-id", session);

        List<TariffDefinitionsResponse.TariffDefinitionDto> saved =
                sessionTariffService.getTariffDefinitions(session);
        assertTrue(saved.isEmpty());
    }

    @Test
    public void testClearTariffDefinitions() {
        when(session.getAttribute(anyString())).thenReturn(List.of(testTariffDto));
        sessionTariffService.clearTariffDefinitions(session);
        verify(session).removeAttribute("tariffDefinitions");
    }
}
