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
        when(session.getAttribute("EXPORT_CART")).thenReturn(new ArrayList<CalculationHistoryDto>()); // Empty cart

        // Act
        exportCartService.addToCart(testCalculationId, session);

        // Assert: Cart now contains the test calculation
        List<CalculationHistoryDto> updatedCart = (List<CalculationHistoryDto>) session.getAttribute("EXPORT_CART");
        assertNotNull(updatedCart, "Cart should not be null after add");
        assertEquals(1, updatedCart.size(), "Cart should have 1 item after add");
        assertEquals(testCalculation.getProductName(), updatedCart.get(0).getProductName(), "Added item does not match test calculation");

        // Verify mocks
        verify(sessionManagementClient, times(1)).getCalculationById(testSessionId, testCalculationId);
        verify(session, atLeastOnce()).getAttribute("EXPORT_CART");
        verify(session, times(1)).setAttribute(eq("EXPORT_CART"), any(List.class));
    }

    // Test 2: addToCart - duplicate calculation ID throws BadRequestException
    @Test
    public void addToCart_DuplicateCalculationId_ThrowsBadRequest() {
        // Arrange: Cart already contains the test calculation with same ID
        when(session.getId()).thenReturn(testSessionId);
        testCalculation.setId(testCalculationId); // Set ID to match
        List<CalculationHistoryDto> existingCart = new ArrayList<>();
        existingCart.add(testCalculation);
        when(session.getAttribute("EXPORT_CART")).thenReturn(existingCart);
        when(sessionManagementClient.getCalculationById(eq(testSessionId), eq(testCalculationId))).thenReturn(testCalculation);

        // Act + Assert: Duplicate throws exception
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportCartService.addToCart(testCalculationId, session);
        });

        assertEquals("Item already in cart", exception.getMessage(), "Duplicate error message incorrect");
        verify(sessionManagementClient, times(1)).getCalculationById(testSessionId, testCalculationId);
        verify(session, never()).setAttribute(any(), any()); // No new item added
    }

    // Test 3: addToCart - non-existent calculation ID throws NotFoundException
    @Test
    public void addToCart_NonExistentCalculationId_ThrowsNotFound() {
        // Arrange: Client returns null (calculation not found)
        when(session.getId()).thenReturn(testSessionId);
        when(sessionManagementClient.getCalculationById(eq(testSessionId), eq(testCalculationId))).thenReturn(null);

        // Act + Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            exportCartService.addToCart(testCalculationId, session);
        });

        assertEquals("Calculation not found in history", exception.getMessage(), "NotFound error message incorrect");
        verify(sessionManagementClient, times(1)).getCalculationById(testSessionId, testCalculationId);
        verify(session, never()).setAttribute(any(), any()); // No item added
    }

    // Test 4: removeFromCart - existing calculation ID is removed from cart
    @Test
    public void removeFromCart_ExistingCalculationId_RemovesFromCart() {
        // Arrange: Cart contains the test calculation with matching ID
        testCalculation.setId(testCalculationId);
        List<CalculationHistoryDto> existingCart = new ArrayList<>();
        existingCart.add(testCalculation);
        when(session.getAttribute("EXPORT_CART")).thenReturn(existingCart);

        // Act
        exportCartService.removeFromCart(testCalculationId, session);

        // Assert: Cart is now empty
        verify(session, times(1)).setAttribute(eq("EXPORT_CART"), any(List.class));
    }

    // Test 5: removeFromCart - non-existent calculation ID throws NotFoundException
    @Test
    public void removeFromCart_NonExistentCalculationId_ThrowsNotFound() {
        // Arrange: Cart contains a different calculation (not the one being removed)
        CalculationHistoryDto otherCalculation = new CalculationHistoryDto(
            "Other Product", "USA", "Canada", 1.0, "piece", 10.0, 5.0, 0.5, 10.5, "MFN"
        );
        otherCalculation.setId("other-id");
        List<CalculationHistoryDto> existingCart = new ArrayList<>();
        existingCart.add(otherCalculation);
        when(session.getAttribute("EXPORT_CART")).thenReturn(existingCart);

        // Act + Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            exportCartService.removeFromCart(testCalculationId, session);
        });

        assertEquals("Item not found in cart", exception.getMessage(), "Removal NotFound error message incorrect");
        verify(session, never()).setAttribute(any(), any()); // Cart unchanged
    }

    // Test 6: getCart - returns existing cart (non-empty)
    @Test
    public void getCart_NonEmptyCart_ReturnsCartItems() {
        // Arrange: Cart has test calculation
        List<CalculationHistoryDto> existingCart = new ArrayList<>();
        existingCart.add(testCalculation);
        when(session.getAttribute("EXPORT_CART")).thenReturn(existingCart);

        // Act
        List<CalculationHistoryDto> result = exportCartService.getCart(session);

        // Assert: Returned cart matches existing cart
        assertNotNull(result, "getCart should return non-null list");
        assertEquals(1, result.size(), "Returned cart size incorrect");
        assertEquals(testCalculation.getProductName(), result.get(0).getProductName(), "Returned cart items incorrect");
        verify(session, times(1)).getAttribute("EXPORT_CART");
    }

    // Test 7: getCart - empty cart returns empty list (no null)
    @Test
    public void getCart_EmptyCart_ReturnsEmptyList() {
        // Arrange: Session has no cart attribute (or cart is empty)
        when(session.getAttribute("EXPORT_CART")).thenReturn(null); // No cart initialized

        // Act
        List<CalculationHistoryDto> result = exportCartService.getCart(session);

        // Assert: Returns empty list (avoids NPE)
        assertNotNull(result, "getCart should not return null for empty cart");
        assertTrue(result.isEmpty(), "getCart should return empty list when no cart exists");
        verify(session, times(1)).getAttribute("EXPORT_CART");
    }

    // Test 8: clearCart - empties the existing cart
    @Test
    public void clearCart_NonEmptyCart_EmptiesCart() {
        // Act
        exportCartService.clearCart(session);

        // Assert: Cart is cleared
        verify(session, times(1)).removeAttribute("EXPORT_CART");
    }

    // Test 9: clearCart - empty cart has no side effects
    @Test
    public void clearCart_EmptyCart_NoChanges() {
        // Act
        exportCartService.clearCart(session);

        // Assert: Cart is cleared
        verify(session, times(1)).removeAttribute("EXPORT_CART");
    }

    // Test 10: removeFromCart - null cart throws NotFoundException
    @Test
    public void removeFromCart_NullCart_ThrowsNotFoundException() {
        // Arrange: Cart is null
        when(session.getAttribute("EXPORT_CART")).thenReturn(null);

        // Act + Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            exportCartService.removeFromCart(testCalculationId, session);
        });

        assertEquals("Cart is empty", exception.getMessage());
        verify(session, never()).setAttribute(any(), any());
    }

    // Test 11: removeFromCart - empty cart throws NotFoundException
    @Test
    public void removeFromCart_EmptyCart_ThrowsNotFoundException() {
        // Arrange: Cart is empty list
        when(session.getAttribute("EXPORT_CART")).thenReturn(new ArrayList<>());

        // Act + Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            exportCartService.removeFromCart(testCalculationId, session);
        });

        assertEquals("Cart is empty", exception.getMessage());
        verify(session, never()).setAttribute(any(), any());
    }

    // Test 12: addToCart - removeCalculationById fails but doesn't throw
    @Test
    public void addToCart_RemoveFromHistoryFails_StillAddsToCart() {
        // Arrange: Client returns calculation but fails to remove from history
        when(session.getId()).thenReturn(testSessionId);
        when(sessionManagementClient.getCalculationById(eq(testSessionId), eq(testCalculationId))).thenReturn(testCalculation);
        when(session.getAttribute("EXPORT_CART")).thenReturn(new ArrayList<>());
        doThrow(new RuntimeException("Service unavailable"))
            .when(sessionManagementClient).removeCalculationById(eq(testSessionId), eq(testCalculationId));

        // Act - should not throw despite removal failure
        assertDoesNotThrow(() -> {
            exportCartService.addToCart(testCalculationId, session);
        });

        // Assert: Item still added to cart despite removal failure
        List<CalculationHistoryDto> updatedCart = (List<CalculationHistoryDto>) session.getAttribute("EXPORT_CART");
        assertNotNull(updatedCart);
        assertEquals(1, updatedCart.size());
        verify(sessionManagementClient, times(1)).getCalculationById(testSessionId, testCalculationId);
        verify(sessionManagementClient, times(1)).removeCalculationById(testSessionId, testCalculationId);
    }
}