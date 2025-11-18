package com.example.export.controller;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.export.service.CsvExportService;
import com.example.session.dto.CalculationHistoryDto;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class CsvExportServiceTest {

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CsvExportService csvExportService;

    private List<CalculationHistoryDto> testCartItems;
    private CalculationHistoryDto itemWithSpecialChars; // For escaping tests

    @BeforeEach
    public void setUp() {
        // Regular test items
        testCartItems = new ArrayList<>();
        CalculationHistoryDto calc1 = new CalculationHistoryDto(
            "Product 1",
            "Singapore",
            "China",
            2.0,
            "piece",
            20.0,
            15.0,
            3.0,
            23.0,
            "MFN"
        );
        testCartItems.add(calc1);

        // Item with commas, quotes, newlines (for escaping tests)
        itemWithSpecialChars = new CalculationHistoryDto(
            "Product, \"Two\"", // Comma + quote
            "New\nYork",       // Newline
            "China, Hong Kong",// Comma
            5.0,
            "box",
            100.0,
            80.0,
            10.0,
            95.0,
            "FTA"
        );
    }

    // Test 1: CSV header generation (matches DTO fields)
    @Test
    public void exportToCsv_ValidItems_GeneratesCorrectHeader() throws IOException {
        // Arrange: Capture the Writer from response
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(stringWriter));

        // Act
        csvExportService.exportToCsv(testCartItems, response);

        // Assert: First line is the correct header
        String csvOutput = stringWriter.toString();
        String firstLine = csvOutput.split("\n")[0].trim();
        String expectedHeader = "productName,originCountry,destinationCountry,quantity,unit,unitPrice,dutyAmount,taxAmount,totalAmount,tariffScheme";
        assertEquals(expectedHeader, firstLine, "CSV header does not match expected fields");
    }

    // Test 2: CSV data row formatting (matches DTO values)
    @Test
    public void exportToCsv_ValidItems_GeneratesCorrectDataRows() throws IOException {
        // Arrange
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(stringWriter));

        // Act
        csvExportService.exportToCsv(testCartItems, response);

        // Assert: Second line (data row) matches test item values
        String[] csvLines = stringWriter.toString().split("\n");
        String dataRow = csvLines[1].trim();
        String expectedData = "Product 1,Singapore,China,2.0,piece,20.0,15.0,3.0,23.0,MFN";
        assertEquals(expectedData, dataRow, "CSV data row does not match test item values");
    }

    // Test 3: CSV escaping (handles commas, quotes, newlines)
    @Test
    public void exportToCsv_ItemsWithSpecialChars_EscapesCorrectly() throws IOException {
        // Arrange: Add item with special characters
        List<CalculationHistoryDto> itemsWithSpecialChars = new ArrayList<>();
        itemsWithSpecialChars.add(itemWithSpecialChars);

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(stringWriter));

        // Act
        csvExportService.exportToCsv(itemsWithSpecialChars, response);

        // Assert: Special characters are escaped (SuperCSV handles this by default)
        String[] csvLines = stringWriter.toString().split("\n");
        String dataRow = csvLines[1].trim();

        // Expected escaped values (SuperCSV wraps fields with special chars in quotes)
        String expectedProductName = "\"Product, \"\"Two\"\"\""; // Escapes quote with double quote
        String expectedOrigin = "\"New\nYork\"";                // Wraps newline in quotes
        String expectedDestination = "\"China, Hong Kong\"";     // Wraps comma in quotes

        assertTrue(dataRow.startsWith(expectedProductName), "Product name special chars not escaped");
        assertTrue(dataRow.contains(expectedOrigin), "Origin country newline not escaped");
        assertTrue(dataRow.contains(expectedDestination), "Destination country comma not escaped");
    }

    // Test 4: Empty cart handling (header only, no data rows)
    @Test
    public void exportToCsv_EmptyCart_GeneratesOnlyHeader() throws IOException {
        // Arrange: Empty item list
        List<CalculationHistoryDto> emptyCart = new ArrayList<>();
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(stringWriter));

        // Act
        csvExportService.exportToCsv(emptyCart, response);

        // Assert: Only header exists (no data rows)
        String[] csvLines = stringWriter.toString().split("\n");
        assertEquals(1, csvLines.length, "Empty cart should generate only CSV header");
        String header = csvLines[0].trim();
        String expectedHeader = "productName,originCountry,destinationCountry,quantity,unit,unitPrice,dutyAmount,taxAmount,totalAmount,tariffScheme";
        assertEquals(expectedHeader, header, "Empty cart CSV header is incorrect");
    }

    // Test 5: IOException handling (propagates exception when response writer fails)
    @Test
    public void exportToCsv_IOExceptionThrown_PropagatesException() throws IOException {
        // Arrange: Mock response.getWriter() to throw IOException
        when(response.getWriter()).thenThrow(new IOException("Failed to get writer"));

        // Act + Assert: Exception is propagated (service doesn't swallow it)
        IOException exception = assertThrows(IOException.class, () -> {
            csvExportService.exportToCsv(testCartItems, response);
        });

        assertEquals("Failed to get writer", exception.getMessage(), "IOException message does not match");
        verify(response, times(1)).getWriter(); // Verify writer was called once
    }

    // Test 6: Response headers are set correctly (Content-Type, Content-Disposition)
    @Test
    public void exportToCsv_ValidItems_SetsCorrectResponseHeaders() throws IOException {
        // Arrange
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(stringWriter));

        // Act
        csvExportService.exportToCsv(testCartItems, response);

        // Assert: Response headers for CSV download are set
        verify(response, times(1)).setContentType("text/csv");
        verify(response, times(1)).setCharacterEncoding("UTF-8");
        verify(response, times(1)).setHeader(
            "Content-Disposition",
            "attachment; filename=\"export-cart.csv\""
        );
    }
}