package com.example.export.client;

import com.example.session.dto.CalculationHistoryDto;
import com.example.export.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionManagementClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SessionManagementClient sessionManagementClient;

    private String testSessionId;
    private String testCalculationId;
    private CalculationHistoryDto testCalculation;

    @BeforeEach
    public void setUp() {
        testSessionId = "test-session-123";
        testCalculationId = "calc-123";
        testCalculation = new CalculationHistoryDto(
            "Test Product", "Singapore", "China", 2.0, "piece",
            20.0, 15.0, 3.0, 23.0, "MFN"
        );
    }

    @Test
    public void getCalculationById_Success_ReturnsCalculation() {
        // Arrange
        ResponseEntity<CalculationHistoryDto> response = new ResponseEntity<>(testCalculation, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(CalculationHistoryDto.class)))
            .thenReturn(response);

        // Act
        CalculationHistoryDto result = sessionManagementClient.getCalculationById(testSessionId, testCalculationId);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getProductName());
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(CalculationHistoryDto.class));
    }

    @Test
    public void getCalculationById_NotFound_ReturnsNull() {
        // Arrange
        ResponseEntity<CalculationHistoryDto> response = new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        when(restTemplate.exchange(anyString(), any(), any(), eq(CalculationHistoryDto.class)))
            .thenReturn(response);

        // Act
        CalculationHistoryDto result = sessionManagementClient.getCalculationById(testSessionId, testCalculationId);

        // Assert
        assertNull(result);
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(CalculationHistoryDto.class));
    }

    @Test
    public void getCalculationById_Exception_ThrowsDataAccessException() {
        // Arrange
        when(restTemplate.exchange(anyString(), any(), any(), eq(CalculationHistoryDto.class)))
            .thenThrow(new RestClientException("Connection failed"));

        // Act & Assert
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            sessionManagementClient.getCalculationById(testSessionId, testCalculationId);
        });

        assertTrue(exception.getMessage().contains("Failed to fetch calculation"));
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(CalculationHistoryDto.class));
    }

    @Test
    public void removeCalculationById_Success_NoException() {
        // Arrange
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
            .thenReturn(response);

        // Act & Assert - should not throw
        assertDoesNotThrow(() -> {
            sessionManagementClient.removeCalculationById(testSessionId, testCalculationId);
        });

        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(Void.class));
    }

    @Test
    public void removeCalculationById_Exception_ThrowsDataAccessException() {
        // Arrange
        when(restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
            .thenThrow(new RestClientException("Service unavailable"));

        // Act & Assert
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            sessionManagementClient.removeCalculationById(testSessionId, testCalculationId);
        });

        assertTrue(exception.getMessage().contains("Failed to remove calculation"));
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(Void.class));
    }
}

