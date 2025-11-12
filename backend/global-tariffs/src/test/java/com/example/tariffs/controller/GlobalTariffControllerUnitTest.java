package com.example.tariffs.controller;

import com.example.tariffs.dto.TariffDefinitionsResponse;
import com.example.tariffs.service.TariffService;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalTariffControllerUnitTest {

    @Mock
    private TariffService tariffService;

    @InjectMocks
    private GlobalTariffController controller;

    private TariffDefinitionsResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new TariffDefinitionsResponse(true, new ArrayList<>());
    }

    @Test
    void testGetTariffDefinitions_Success() {
        // Arrange
        when(tariffService.getTariffDefinitions()).thenReturn(mockResponse);

        // Act
        ResponseEntity<TariffDefinitionsResponse> response = controller.getTariffDefinitions();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(tariffService).getTariffDefinitions();
    }

    @Test
    void testGetGlobalTariffDefinitions_Success() {
        // Arrange
        when(tariffService.getGlobalTariffDefinitions()).thenReturn(mockResponse);

        // Act
        ResponseEntity<TariffDefinitionsResponse> response = controller.getGlobalTariffDefinitions();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tariffService).getGlobalTariffDefinitions();
    }

    @Test
    void testGetModifiedTariffDefinitions_Success() {
        // Arrange
        when(tariffService.getUserTariffDefinitions()).thenReturn(mockResponse);

        // Act
        ResponseEntity<TariffDefinitionsResponse> response = controller.getModifiedTariffDefinitions();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tariffService).getUserTariffDefinitions();
    }

    @Test
    void testAddModifiedTariffDefinition_Success() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("product", "TestProduct");
        requestBody.put("exportingFrom", "Country1");
        requestBody.put("importingTo", "Country2");
        requestBody.put("type", "AHS");
        requestBody.put("rate", 5.0);
        when(tariffService.addAdminTariffDefinition(any())).thenReturn(mockResponse);

        // Act
        ResponseEntity<TariffDefinitionsResponse> response = controller.addModifiedTariffDefinition(requestBody);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tariffService).addAdminTariffDefinition(any());
    }

    @Test
    void testAddModifiedTariffDefinition_MissingData() {
        // Arrange
        Map<String, Object> requestBody = null;

        // Act & Assert
        assertThrows(com.example.tariffs.exception.BadRequestException.class, () -> {
            controller.addModifiedTariffDefinition(requestBody);
        });
    }

    @Test
    void testUpdateModifiedTariffDefinition_Success() {
        // Arrange
        String id = "Country2_Country1";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("rate", 7.0);
        when(tariffService.updateAdminTariffDefinition(eq(id), any())).thenReturn(mockResponse);

        // Act
        ResponseEntity<TariffDefinitionsResponse> response = controller.updateModifiedTariffDefinition(id, requestBody);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tariffService).updateAdminTariffDefinition(eq(id), any());
    }

    @Test
    void testUpdateModifiedTariffDefinition_InvalidId() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();

        // Act & Assert
        assertThrows(com.example.tariffs.exception.BadRequestException.class, () -> {
            controller.updateModifiedTariffDefinition("", requestBody);
        });
    }

    @Test
    void testDeleteModifiedTariffDefinition_Success() {
        // Arrange
        String id = "Country2_Country1";
        doNothing().when(tariffService).deleteAdminTariffDefinition(id);

        // Act
        ResponseEntity<?> response = controller.deleteModifiedTariffDefinition(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tariffService).deleteAdminTariffDefinition(id);
    }

    @Test
    void testDeleteModifiedTariffDefinition_InvalidId() {
        // Act & Assert
        assertThrows(com.example.tariffs.exception.BadRequestException.class, () -> {
            controller.deleteModifiedTariffDefinition("");
        });
    }
}

