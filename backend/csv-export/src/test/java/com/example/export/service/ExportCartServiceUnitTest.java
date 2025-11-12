package com.example.export.service;

import com.example.export.client.SessionManagementClient;
import com.example.session.dto.CalculationHistoryDto;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportCartServiceUnitTest {

    @Mock
    private HttpSession httpSession;

    @Mock
    private SessionManagementClient sessionManagementClient;

    @InjectMocks
    private ExportCartService exportCartService;

    private CalculationHistoryDto testCalculation;

    @BeforeEach
    void setUp() {
        testCalculation = new CalculationHistoryDto(
                "TestProduct", "Country1", "Country2", 10.0, "kg",
                1000.0, 5.0, 50.0, 1050.0, "AHS"
        );
    }

    @Test
    void testAddToCart_Success() {
        // Arrange
        String sessionId = "test-session-123";
        String calculationId = testCalculation.getId();
        when(httpSession.getId()).thenReturn(sessionId);
        when(httpSession.getAttribute("EXPORT_CART")).thenReturn(null);
        when(sessionManagementClient.getCalculationById(sessionId, calculationId))
                .thenReturn(testCalculation);

        // Act
        exportCartService.addToCart(httpSession, calculationId);

        // Assert
        verify(sessionManagementClient).getCalculationById(sessionId, calculationId);
        verify(httpSession).setAttribute(eq("EXPORT_CART"), anyList());
        verify(sessionManagementClient).removeCalculationById(sessionId, calculationId);
    }

    @Test
    void testAddToCart_CalculationNotFound() {
        // Arrange
        String sessionId = "test-session-123";
        String calculationId = "non-existent";
        when(httpSession.getId()).thenReturn(sessionId);
        when(sessionManagementClient.getCalculationById(sessionId, calculationId))
                .thenReturn(null);

        // Act & Assert
        assertThrows(com.example.export.exception.NotFoundException.class, () -> {
            exportCartService.addToCart(httpSession, calculationId);
        });
        verify(sessionManagementClient, never()).removeCalculationById(anyString(), anyString());
    }

    @Test
    void testAddToCart_DuplicateItem() {
        // Arrange
        String sessionId = "test-session-123";
        String calculationId = testCalculation.getId();
        List<CalculationHistoryDto> existingCart = new ArrayList<>();
        existingCart.add(testCalculation);
        
        when(httpSession.getId()).thenReturn(sessionId);
        when(httpSession.getAttribute("EXPORT_CART")).thenReturn(existingCart);
        when(sessionManagementClient.getCalculationById(sessionId, calculationId))
                .thenReturn(testCalculation);

        // Act & Assert
        assertThrows(com.example.export.exception.BadRequestException.class, () -> {
            exportCartService.addToCart(httpSession, calculationId);
        });
    }

    @Test
    void testAddToCart_HandlesRemoveFailure() {
        // Arrange
        String sessionId = "test-session-123";
        String calculationId = testCalculation.getId();
        when(httpSession.getId()).thenReturn(sessionId);
        when(httpSession.getAttribute("EXPORT_CART")).thenReturn(null);
        when(sessionManagementClient.getCalculationById(sessionId, calculationId))
                .thenReturn(testCalculation);
        doThrow(new RuntimeException("Remove failed"))
                .when(sessionManagementClient).removeCalculationById(sessionId, calculationId);

        // Act - Should not throw, just log warning
        assertDoesNotThrow(() -> {
            exportCartService.addToCart(httpSession, calculationId);
        });

        // Assert - Cart should still be updated
        verify(httpSession).setAttribute(eq("EXPORT_CART"), anyList());
    }

    @Test
    void testRemoveFromCart_Success() {
        // Arrange
        List<CalculationHistoryDto> cart = new ArrayList<>();
        cart.add(testCalculation);
        when(httpSession.getAttribute("EXPORT_CART")).thenReturn(cart);

        // Act
        exportCartService.removeFromCart(httpSession, testCalculation.getId());

        // Assert
        verify(httpSession).setAttribute(eq("EXPORT_CART"), argThat(list -> {
            @SuppressWarnings("unchecked")
            List<CalculationHistoryDto> updatedCart = (List<CalculationHistoryDto>) list;
            return updatedCart.isEmpty();
        }));
    }

    @Test
    void testRemoveFromCart_EmptyCart() {
        // Arrange
        when(httpSession.getAttribute("EXPORT_CART")).thenReturn(null);

        // Act & Assert
        assertThrows(com.example.export.exception.NotFoundException.class, () -> {
            exportCartService.removeFromCart(httpSession, "any-id");
        });
    }

    @Test
    void testRemoveFromCart_ItemNotFound() {
        // Arrange
        List<CalculationHistoryDto> cart = new ArrayList<>();
        when(httpSession.getAttribute("EXPORT_CART")).thenReturn(cart);

        // Act & Assert
        assertThrows(com.example.export.exception.NotFoundException.class, () -> {
            exportCartService.removeFromCart(httpSession, "non-existent-id");
        });
    }

    @Test
    void testGetCart_Empty() {
        // Arrange
        when(httpSession.getAttribute("EXPORT_CART")).thenReturn(null);

        // Act
        List<CalculationHistoryDto> result = exportCartService.getCart(httpSession);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCart_WithItems() {
        // Arrange
        List<CalculationHistoryDto> cart = new ArrayList<>();
        cart.add(testCalculation);
        when(httpSession.getAttribute("EXPORT_CART")).thenReturn(cart);

        // Act
        List<CalculationHistoryDto> result = exportCartService.getCart(httpSession);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TestProduct", result.get(0).getProduct());
    }

    @Test
    void testClearCart() {
        // Act
        exportCartService.clearCart(httpSession);

        // Assert
        verify(httpSession).removeAttribute("EXPORT_CART");
    }
}

