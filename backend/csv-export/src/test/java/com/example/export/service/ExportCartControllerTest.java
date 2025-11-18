package com.example.export.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.export.controller.ExportCartController;
import com.example.export.exception.BadRequestException;
import com.example.export.exception.NotFoundException;
import com.example.session.dto.CalculationHistoryDto;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
public class ExportCartControllerTest {

    @Mock
    private ExportCartService exportCartService;

    @Mock
    private CsvExportService csvExportService;

    @Mock
    private HttpSession session;

    @Mock
    private HttpServletResponse response; // Field: HttpServletResponse (mocked)

    @InjectMocks
    private ExportCartController exportCartController;

    private String testCalculationId;
    private CalculationHistoryDto testCalculation;
    private List<CalculationHistoryDto> testCartItems;

    @BeforeEach
    public void setUp() {
        testCalculationId = UUID.randomUUID().toString();
        testCalculation = new CalculationHistoryDto(
            "Test Product",
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
        testCartItems = new ArrayList<>();
        testCartItems.add(testCalculation);
    }

    // Test 1: GET /api/export-cart - returns cart items with 200 OK
    @Test
    public void getCart_ReturnsCartItems_200OK() {
        // Arrange: Service returns test cart items
        when(exportCartService.getCart(session)).thenReturn(testCartItems);

        // Act: Rename local variable to "cartResponse" (no conflict with field "response")
        ResponseEntity<List<CalculationHistoryDto>> cartResponse = exportCartController.getCart(session);

        // Assert
        assertEquals(HttpStatus.OK, cartResponse.getStatusCode(), "GET cart should return 200 OK");
        assertNotNull(cartResponse.getBody(), "Response body should not be null");
        assertEquals(1, cartResponse.getBody().size(), "Response body size incorrect");
        assertEquals(testCalculation.getProductName(), cartResponse.getBody().get(0).getProductName(), "Response items incorrect");
        verify(exportCartService, times(1)).getCart(session);
    }

    // Test 2: GET /api/export-cart - empty cart returns 200 OK with empty list
    @Test
    public void getCart_EmptyCart_ReturnsEmptyList_200OK() {
        // Arrange: Service returns empty list
        when(exportCartService.getCart(session)).thenReturn(new ArrayList<>());

        // Act: Rename local variable to "cartResponse"
        ResponseEntity<List<CalculationHistoryDto>> cartResponse = exportCartController.getCart(session);

        // Assert
        assertEquals(HttpStatus.OK, cartResponse.getStatusCode());
        assertNotNull(cartResponse.getBody());
        assertTrue(cartResponse.getBody() != null && cartResponse.getBody().isEmpty(), "Empty cart should return empty list");
        verify(exportCartService, times(1)).getCart(session);
    }

    // Test 3: POST /api/export-cart/add/{id} - valid ID (no exception = success)
    @Test
    public void addToCart_ValidId_ExecutesSuccessfully() {
        // Arrange: Service adds item without exception
        doNothing().when(exportCartService).addToCart(eq(testCalculationId), eq(session));

        // Act (void return: no assignment needed)
        exportCartController.addToCart(testCalculationId, session);

        // Assert: Verify service method was called (confirms success)
        verify(exportCartService, times(1)).addToCart(testCalculationId, session);
    }

    // Test 4: POST /api/export-cart/add/{id} - duplicate ID throws BadRequestException
    @Test
    public void addToCart_DuplicateId_ThrowsBadRequestException() {
        // Arrange: Service throws BadRequestException
        String errorMsg = "Calculation already exists in cart";
        doThrow(new BadRequestException(errorMsg)).when(exportCartService).addToCart(eq(testCalculationId), eq(session));

        // Act + Assert: Verify exception is thrown
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportCartController.addToCart(testCalculationId, session);
        });

        assertEquals(errorMsg, exception.getMessage(), "Duplicate error message incorrect");
        verify(exportCartService, times(1)).addToCart(testCalculationId, session);
    }

    // Test 5: POST /api/export-cart/add/{id} - non-existent ID throws NotFoundException
    @Test
    public void addToCart_NonExistentId_ThrowsNotFoundException() {
        // Arrange: Service throws NotFoundException
        String errorMsg = "Calculation not found with ID: " + testCalculationId;
        doThrow(new NotFoundException(errorMsg)).when(exportCartService).addToCart(eq(testCalculationId), eq(session));

        // Act + Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            exportCartController.addToCart(testCalculationId, session);
        });

        assertEquals(errorMsg, exception.getMessage(), "NotFound error message incorrect");
        verify(exportCartService, times(1)).addToCart(testCalculationId, session);
    }

    // Test 6: DELETE /api/export-cart/remove/{id} - existing ID (no exception = success)
    @Test
    public void removeFromCart_ExistingId_ExecutesSuccessfully() {
        // Arrange: Service removes item without exception
        doNothing().when(exportCartService).removeFromCart(eq(testCalculationId), eq(session));

        // Act (void return)
        exportCartController.removeFromCart(testCalculationId, session);

        // Assert: Verify service call
        verify(exportCartService, times(1)).removeFromCart(testCalculationId, session);
    }

    // Test 7: DELETE /api/export-cart/remove/{id} - non-existent ID throws NotFoundException
    @Test
    public void removeFromCart_NonExistentId_ThrowsNotFoundException() {
        // Arrange: Service throws NotFoundException
        String errorMsg = "Calculation not found in cart";
        doThrow(new NotFoundException(errorMsg)).when(exportCartService).removeFromCart(eq(testCalculationId), eq(session));

        // Act + Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            exportCartController.removeFromCart(testCalculationId, session);
        });

        assertEquals(errorMsg, exception.getMessage(), "Removal error message incorrect");
        verify(exportCartService, times(1)).removeFromCart(testCalculationId, session);
    }

    // Test 8: DELETE /api/export-cart/clear - executes successfully
    @Test
    public void clearCart_ExecutesSuccessfully() {
        // Arrange: Service clears cart without exception
        doNothing().when(exportCartService).clearCart(session);

        // Act (void return)
        exportCartController.clearCart(session);

        // Assert: Verify service call
        verify(exportCartService, times(1)).clearCart(session);
    }

    // Test 9: GET /api/export-cart/export - exports CSV successfully (no exception)
    @Test
    public void exportCartAsCsv_Success_ExecutesWithoutException() throws IOException {
        // Arrange: Service returns cart items; CSV service exports without error
        when(exportCartService.getCart(session)).thenReturn(testCartItems);
        doNothing().when(csvExportService).exportToCsv(eq(testCartItems), eq(response)); // Use field "response" (HttpServletResponse)

        // Act (void return: CSV is written to HttpServletResponse)
        exportCartController.exportCartAsCsv(session, response);

        // Assert: Verify service calls
        verify(exportCartService, times(1)).getCart(session);
        verify(csvExportService, times(1)).exportToCsv(testCartItems, response);
    }

    // Test 10: GET /api/export-cart/export - IOException during export is propagated
    @Test
    public void exportCartAsCsv_IOExceptionThrown_ThrowsException() throws IOException {
        // Arrange: CSV service throws IOException
        when(exportCartService.getCart(session)).thenReturn(testCartItems);
        String errorMsg = "Export failed";
        doThrow(new IOException(errorMsg)).when(csvExportService).exportToCsv(eq(testCartItems), eq(response));

        // Act + Assert
        IOException exception = assertThrows(IOException.class, () -> {
            exportCartController.exportCartAsCsv(session, response);
        });

        assertEquals(errorMsg, exception.getMessage(), "IO Exception message incorrect");
        verify(exportCartService, times(1)).getCart(session);
        verify(csvExportService, times(1)).exportToCsv(testCartItems, response);
    }

    // Test 11: GET /api/export-cart/export - empty cart exports CSV (no exception)
    @Test
    public void exportCartAsCsv_EmptyCart_ExecutesWithoutException() throws IOException {
        // Arrange: Empty cart; CSV service exports without error
        List<CalculationHistoryDto> emptyCart = new ArrayList<>();
        when(exportCartService.getCart(session)).thenReturn(emptyCart);
        doNothing().when(csvExportService).exportToCsv(eq(emptyCart), eq(response));

        // Act
        exportCartController.exportCartAsCsv(session, response);

        // Assert: Verify service calls
        verify(exportCartService, times(1)).getCart(session);
        verify(csvExportService, times(1)).exportToCsv(emptyCart, response);
    }
}