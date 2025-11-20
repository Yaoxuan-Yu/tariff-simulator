package com.example.export.service;

import com.example.session.dto.CalculationHistoryDto;
import com.example.export.exception.BadRequestException;
import com.example.export.exception.NotFoundException;
import com.example.export.client.SessionManagementClient;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExportCartServiceTest {

    @Mock
    private HttpSession session;

    @Mock
    private SessionManagementClient sessionManagementClient;

    @InjectMocks
    private ExportCartService exportCartService;

    private CalculationHistoryDto testCalculation;
    private String testCalculationId; // Assume calculation IDs are strings (adjust if UUID/Long)
    private String testSessionId;

    @BeforeEach
    public void setUp() {
        testCalculationId = UUID.randomUUID().toString(); // Generate unique ID for tests
        testSessionId = UUID.randomUUID().toString(); // Generate unique session ID for tests
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
    }

    // Test 1: addToCart - valid calculation ID (fetched from client) is added to cart
    @Test
    public void addToCart_ValidCalculationId_AddsToCart() {
        // Arrange: Mock client to return valid calculation
        when(session.getId()).thenReturn(testSessionId);
        when(sessionManagementClient.getCalculationById(eq(testSessionId), eq(testCalculationId))).thenReturn(testCalculation);
        when(session.getAttribute("exportCart")).thenReturn(new ArrayList<CalculationHistoryDto>()); // Empty cart

        // Act
        exportCartService.addToCart(testCalculationId, session);

        // Assert: Cart now contains the test calculation
        List<CalculationHistoryDto> updatedCart = (List<CalculationHistoryDto>) session.getAttribute("exportCart");
        assertNotNull(updatedCart, "Cart should not be null after add");
        assertEquals(1, updatedCart.size(), "Cart should have 1 item after add");
        assertEquals(testCalculation.getProductName(), updatedCart.get(0).getProductName(), "Added item does not match test calculation");

        // Verify mocks
        verify(sessionManagementClient, times(1)).getCalculationById(testSessionId, testCalculationId);
        verify(session, times(2)).getAttribute("exportCart"); // Once to get, once to verify (implicit)
        verify(session, times(1)).setAttribute(eq("exportCart"), any(List.class));
    }

    // Test 2: addToCart - duplicate calculation ID throws BadRequestException
    @Test
    public void addToCart_DuplicateCalculationId_ThrowsBadRequest() {
        // Arrange: Cart already contains the test calculation
        when(session.getId()).thenReturn(testSessionId);
        List<CalculationHistoryDto> existingCart = new ArrayList<>();
        existingCart.add(testCalculation);
        when(session.getAttribute("exportCart")).thenReturn(existingCart);
        when(sessionManagementClient.getCalculationById(eq(testSessionId), eq(testCalculationId))).thenReturn(testCalculation);

        // Act + Assert: Duplicate throws exception
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportCartService.addToCart(testCalculationId, session);
        });

        assertEquals("Calculation already exists in cart", exception.getMessage(), "Duplicate error message incorrect");
        verify(sessionManagementClient, times(1)).getCalculationById(testSessionId, testCalculationId);
        verify(session, never()).setAttribute(any(), any()); // No new item added
    }

    // Test 3: addToCart - non-existent calculation ID throws NotFoundException
    @Test
    public void addToCart_NonExistentCalculationId_ThrowsNotFound() {
        // Arrange: Client returns null (calculation not found)
        when(session.getId()).thenReturn(testSessionId);
        when(sessionManagementClient.getCalculationById(eq(testSessionId), eq(testCalculationId))).thenReturn(null);
        when(session.getAttribute("exportCart")).thenReturn(new ArrayList<>());

        // Act + Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            exportCartService.addToCart(testCalculationId, session);
        });

        assertEquals("Calculation not found with ID: " + testCalculationId, exception.getMessage(), "NotFound error message incorrect");
        verify(sessionManagementClient, times(1)).getCalculationById(testSessionId, testCalculationId);
        verify(session, never()).setAttribute(any(), any()); // No item added
    }

    // Test 4: removeFromCart - existing calculation ID is removed from cart
    @Test
    public void removeFromCart_ExistingCalculationId_RemovesFromCart() {
        // Arrange: Cart contains the test calculation
        List<CalculationHistoryDto> existingCart = new ArrayList<>();
        existingCart.add(testCalculation);
        when(session.getAttribute("exportCart")).thenReturn(existingCart);

        // Act
        exportCartService.removeFromCart(testCalculationId, session);

        // Assert: Cart is now empty
        List<CalculationHistoryDto> updatedCart = (List<CalculationHistoryDto>) session.getAttribute("exportCart");
        assertTrue(updatedCart.isEmpty(), "Cart should be empty after removal");
        verify(session, times(1)).setAttribute(eq("exportCart"), eq(updatedCart));
    }

    // Test 5: removeFromCart - non-existent calculation ID throws NotFoundException
    @Test
    public void removeFromCart_NonExistentCalculationId_ThrowsNotFound() {
        // Arrange: Cart does NOT contain the calculation
        List<CalculationHistoryDto> existingCart = new ArrayList<>();
        when(session.getAttribute("exportCart")).thenReturn(existingCart);

        // Act + Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            exportCartService.removeFromCart(testCalculationId, session);
        });

        assertEquals("Calculation not found in cart", exception.getMessage(), "Removal NotFound error message incorrect");
        verify(session, never()).setAttribute(any(), any()); // Cart unchanged
    }

    // Test 6: getCart - returns existing cart (non-empty)
    @Test
    public void getCart_NonEmptyCart_ReturnsCartItems() {
        // Arrange: Cart has test calculation
        List<CalculationHistoryDto> existingCart = new ArrayList<>();
        existingCart.add(testCalculation);
        when(session.getAttribute("exportCart")).thenReturn(existingCart);

        // Act
        List<CalculationHistoryDto> result = exportCartService.getCart(session);

        // Assert: Returned cart matches existing cart
        assertNotNull(result, "getCart should return non-null list");
        assertEquals(1, result.size(), "Returned cart size incorrect");
        assertEquals(testCalculation.getProductName(), result.get(0).getProductName(), "Returned cart items incorrect");
        verify(session, times(1)).getAttribute("exportCart");
    }

    // Test 7: getCart - empty cart returns empty list (no null)
    @Test
    public void getCart_EmptyCart_ReturnsEmptyList() {
        // Arrange: Session has no cart attribute (or cart is empty)
        when(session.getAttribute("exportCart")).thenReturn(null); // No cart initialized

        // Act
        List<CalculationHistoryDto> result = exportCartService.getCart(session);

        // Assert: Returns empty list (avoids NPE)
        assertNotNull(result, "getCart should not return null for empty cart");
        assertTrue(result.isEmpty(), "getCart should return empty list when no cart exists");
        verify(session, times(1)).getAttribute("exportCart");
    }

    // Test 8: clearCart - empties the existing cart
    @Test
    public void clearCart_NonEmptyCart_EmptiesCart() {
        // Arrange: Cart has test calculation
        List<CalculationHistoryDto> existingCart = new ArrayList<>();
        existingCart.add(testCalculation);
        when(session.getAttribute("exportCart")).thenReturn(existingCart);

        // Act
        exportCartService.clearCart(session);

        // Assert: Cart is empty
        List<CalculationHistoryDto> updatedCart = (List<CalculationHistoryDto>) session.getAttribute("exportCart");
        assertTrue(updatedCart.isEmpty(), "Cart should be empty after clear");
        verify(session, times(1)).setAttribute(eq("exportCart"), eq(new ArrayList<>()));
    }

    // Test 9: clearCart - empty cart has no side effects
    @Test
    public void clearCart_EmptyCart_NoChanges() {
        // Arrange: Cart is empty
        List<CalculationHistoryDto> emptyCart = new ArrayList<>();
        when(session.getAttribute("exportCart")).thenReturn(emptyCart);

        // Act
        exportCartService.clearCart(session);

        // Assert: Cart remains empty (no unnecessary setAttribute)
        verify(session, times(1)).getAttribute("exportCart");
        verify(session, never()).setAttribute(any(), any()); // No setAttribute called
    }
}