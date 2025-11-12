package com.example.export.controller;

import com.example.export.service.CsvExportService;
import com.example.export.service.ExportCartService;
import com.example.session.dto.CalculationHistoryDto;
import jakarta.servlet.http.HttpServletResponse;
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
class ExportCartControllerUnitTest {

    @Mock
    private ExportCartService exportCartService;

    @Mock
    private CsvExportService csvExportService;

    @Mock
    private HttpSession httpSession;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private ExportCartController controller;

    private CalculationHistoryDto testCalculation;

    @BeforeEach
    void setUp() {
        testCalculation = new CalculationHistoryDto(
                "TestProduct", "Country1", "Country2", 10.0, "kg",
                1000.0, 5.0, 50.0, 1050.0, "AHS"
        );
    }

    @Test
    void testGetCart_Success() {
        // Arrange
        List<CalculationHistoryDto> mockCart = List.of(testCalculation);
        when(exportCartService.getCart(httpSession)).thenReturn(mockCart);

        // Act
        ResponseEntity<List<CalculationHistoryDto>> response = controller.getCart(httpSession);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(exportCartService).getCart(httpSession);
    }

    @Test
    void testGetCart_Empty() {
        // Arrange
        when(exportCartService.getCart(httpSession)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<CalculationHistoryDto>> response = controller.getCart(httpSession);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testAddToCart_Success() {
        // Arrange
        String calculationId = "calc-123";
        doNothing().when(exportCartService).addToCart(httpSession, calculationId);

        // Act
        ResponseEntity<?> response = controller.addToCart(calculationId, httpSession);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(exportCartService).addToCart(httpSession, calculationId);
    }

    @Test
    void testAddToCart_InvalidId() {
        // Act & Assert
        assertThrows(com.example.export.exception.BadRequestException.class, () -> {
            controller.addToCart("", httpSession);
        });
    }

    @Test
    void testAddToCart_NotFound() {
        // Arrange
        String calculationId = "non-existent";
        doThrow(new com.example.export.exception.NotFoundException("Not found"))
                .when(exportCartService).addToCart(httpSession, calculationId);

        // Act
        ResponseEntity<?> response = controller.addToCart(calculationId, httpSession);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testRemoveFromCart_Success() {
        // Arrange
        String calculationId = "calc-123";
        doNothing().when(exportCartService).removeFromCart(httpSession, calculationId);

        // Act
        ResponseEntity<?> response = controller.removeFromCart(calculationId, httpSession);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(exportCartService).removeFromCart(httpSession, calculationId);
    }

    @Test
    void testClearCart() {
        // Arrange
        doNothing().when(exportCartService).clearCart(httpSession);

        // Act
        ResponseEntity<?> response = controller.clearCart(httpSession);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(exportCartService).clearCart(httpSession);
    }

    @Test
    void testExportCartAsCsv_Success() throws Exception {
        // Arrange
        List<CalculationHistoryDto> mockCart = List.of(testCalculation);
        when(exportCartService.getCart(httpSession)).thenReturn(mockCart);
        doNothing().when(csvExportService).exportToCsv(anyList(), any());

        // Act
        controller.exportCartAsCsv(httpSession, httpServletResponse);

        // Assert
        verify(exportCartService).getCart(httpSession);
        verify(csvExportService).exportToCsv(mockCart, httpServletResponse);
    }

    @Test
    void testExportCartAsCsv_EmptyCart() {
        // Arrange
        when(exportCartService.getCart(httpSession)).thenReturn(new ArrayList<>());

        // Act
        controller.exportCartAsCsv(httpSession, httpServletResponse);

        // Assert
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_NO_CONTENT);
        verify(csvExportService, never()).exportToCsv(anyList(), any());
    }
}

