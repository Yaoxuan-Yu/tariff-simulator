package com.example.simulator.controller;

import com.example.simulator.dto.TariffDefinitionsResponse;
import com.example.simulator.service.SessionTariffService;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulatorTariffControllerUnitTest {

    @Mock
    private SessionTariffService sessionTariffService;

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private SimulatorTariffController controller;

    private TariffDefinitionsResponse.TariffDefinitionDto testTariff;

    @BeforeEach
    void setUp() {
        testTariff = new TariffDefinitionsResponse.TariffDefinitionDto();
        testTariff.setId("test-id");
        testTariff.setProduct("TestProduct");
        testTariff.setExportingFrom("Country1");
        testTariff.setImportingTo("Country2");
        testTariff.setType("AHS");
        testTariff.setRate(5.0);
    }

    @Test
    void testGetUserTariffDefinitions_Success() {
        // Arrange
        List<TariffDefinitionsResponse.TariffDefinitionDto> mockTariffs = List.of(testTariff);
        when(sessionTariffService.getTariffDefinitions(httpSession)).thenReturn(mockTariffs);

        // Act
        ResponseEntity<TariffDefinitionsResponse> response = controller.getUserTariffDefinitions(httpSession);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getTariffs().size());
        verify(sessionTariffService).getTariffDefinitions(httpSession);
    }

    @Test
    void testAddUserTariffDefinition_Success() {
        // Arrange
        when(sessionTariffService.saveTariffDefinition(httpSession, testTariff)).thenReturn(testTariff);

        // Act
        ResponseEntity<TariffDefinitionsResponse> response = controller.addUserTariffDefinition(testTariff, httpSession);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(sessionTariffService).saveTariffDefinition(httpSession, testTariff);
    }

    @Test
    void testAddUserTariffDefinition_MissingData() {
        // Arrange
        TariffDefinitionsResponse.TariffDefinitionDto nullDto = null;

        // Act & Assert
        assertThrows(com.example.simulator.exception.BadRequestException.class, () -> {
            controller.addUserTariffDefinition(nullDto, httpSession);
        });
    }

    @Test
    void testUpdateUserTariffDefinition_Success() {
        // Arrange
        String id = "test-id";
        when(sessionTariffService.updateTariffDefinition(httpSession, id, testTariff)).thenReturn(testTariff);

        // Act
        ResponseEntity<TariffDefinitionsResponse> response = controller.updateUserTariffDefinition(id, testTariff, httpSession);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(sessionTariffService).updateTariffDefinition(httpSession, id, testTariff);
    }

    @Test
    void testUpdateUserTariffDefinition_InvalidId() {
        // Arrange
        String id = "";

        // Act & Assert
        assertThrows(com.example.simulator.exception.BadRequestException.class, () -> {
            controller.updateUserTariffDefinition(id, testTariff, httpSession);
        });
    }

    @Test
    void testDeleteUserTariffDefinition_Success() {
        // Arrange
        String id = "test-id";
        doNothing().when(sessionTariffService).deleteTariffDefinition(httpSession, id);

        // Act
        ResponseEntity<?> response = controller.deleteUserTariffDefinition(id, httpSession);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(sessionTariffService).deleteTariffDefinition(httpSession, id);
    }

    @Test
    void testDeleteUserTariffDefinition_InvalidId() {
        // Act & Assert
        assertThrows(com.example.simulator.exception.BadRequestException.class, () -> {
            controller.deleteUserTariffDefinition("", httpSession);
        });
    }
}

