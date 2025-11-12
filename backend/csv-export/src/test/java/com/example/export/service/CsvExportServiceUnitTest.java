package com.example.export.service;

import com.example.session.dto.CalculationHistoryDto;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvExportServiceUnitTest {

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private CsvExportService csvExportService;

    private CalculationHistoryDto testCalculation;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        testCalculation = new CalculationHistoryDto(
                "TestProduct", "Country1", "Country2", 10.0, "kg",
                1000.0, 5.0, 50.0, 1050.0, "AHS"
        );
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(httpServletResponse.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testExportToCsv_Success() throws IOException {
        // Arrange
        List<CalculationHistoryDto> cartItems = List.of(testCalculation);

        // Act
        csvExportService.exportToCsv(cartItems, httpServletResponse);

        // Assert
        verify(httpServletResponse).setContentType("text/csv");
        verify(httpServletResponse).setHeader(eq("Content-Disposition"), anyString());
        verify(httpServletResponse).getWriter();
        
        String csvContent = stringWriter.toString();
        assertTrue(csvContent.contains("ID,Product"));
        assertTrue(csvContent.contains("TestProduct"));
    }

    @Test
    void testExportToCsv_EmptyCart() throws IOException {
        // Arrange
        List<CalculationHistoryDto> emptyCart = new ArrayList<>();

        // Act
        csvExportService.exportToCsv(emptyCart, httpServletResponse);

        // Assert
        String csvContent = stringWriter.toString();
        assertTrue(csvContent.contains("Error: Export cart is empty"));
    }

    @Test
    void testExportToCsv_NullCart() throws IOException {
        // Act
        csvExportService.exportToCsv(null, httpServletResponse);

        // Assert
        String csvContent = stringWriter.toString();
        assertTrue(csvContent.contains("Error: Export cart is empty"));
    }

    @Test
    void testExportToCsv_MultipleItems() throws IOException {
        // Arrange
        CalculationHistoryDto calc2 = new CalculationHistoryDto(
                "Product2", "Country2", "Country3", 20.0, "liters",
                2000.0, 7.0, 140.0, 2140.0, "MFN"
        );
        List<CalculationHistoryDto> cartItems = List.of(testCalculation, calc2);

        // Act
        csvExportService.exportToCsv(cartItems, httpServletResponse);

        // Assert
        String csvContent = stringWriter.toString();
        String[] lines = csvContent.split("\n");
        assertEquals(3, lines.length); // Header + 2 data rows
        assertTrue(csvContent.contains("TestProduct"));
        assertTrue(csvContent.contains("Product2"));
    }

    @Test
    void testExportToCsv_EscapesSpecialCharacters() throws IOException {
        // Arrange
        CalculationHistoryDto calcWithComma = new CalculationHistoryDto(
                "Product, with comma", "Country1", "Country2", 10.0, "kg",
                1000.0, 5.0, 50.0, 1050.0, "AHS"
        );
        List<CalculationHistoryDto> cartItems = List.of(calcWithComma);

        // Act
        csvExportService.exportToCsv(cartItems, httpServletResponse);

        // Assert
        String csvContent = stringWriter.toString();
        assertTrue(csvContent.contains("\"Product, with comma\""));
    }

    @Test
    void testExportToCsv_HandlesIOException() throws IOException {
        // Arrange
        when(httpServletResponse.getWriter()).thenThrow(new IOException("Write error"));
        List<CalculationHistoryDto> cartItems = List.of(testCalculation);

        // Act & Assert
        assertThrows(com.example.export.exception.ExportException.class, () -> {
            csvExportService.exportToCsv(cartItems, httpServletResponse);
        });
    }
}

