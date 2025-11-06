package com.example.export.service;

import com.example.export.dto.CalculationHistoryDto;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExportCartService {
    
    private static final String CART_SESSION_KEY = "EXPORT_CART";
    
    private final com.example.export.client.SessionManagementClient sessionManagementClient;

    public ExportCartService(com.example.export.client.SessionManagementClient sessionManagementClient) {
        this.sessionManagementClient = sessionManagementClient;
    }
    
    /**
     * Add calculation to export cart by calling session-management service via HTTP
     */
    public void addToCart(HttpSession session, String calculationId) {
        // Call session-management service to get calculation by ID
        CalculationHistoryDto calculation = sessionManagementClient.getCalculationById(calculationId);
        
        if (calculation == null) {
            throw new com.example.export.exception.NotFoundException("Calculation not found in history");
        }

        @SuppressWarnings("unchecked")
        List<CalculationHistoryDto> cart = (List<CalculationHistoryDto>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
        }

        // Prevent duplicates
        if (cart.stream().anyMatch(c -> c.getId().equals(calculationId))) {
            throw new com.example.export.exception.BadRequestException("Item already in cart");
        }

        cart.add(calculation);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    // Remove calculation from export cart
    public void removeFromCart(HttpSession session, String calculationId) {
        @SuppressWarnings("unchecked")
        List<CalculationHistoryDto> cart = (List<CalculationHistoryDto>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null || cart.isEmpty()) {
            throw new com.example.export.exception.NotFoundException("Cart is empty");
        }

        boolean removed = cart.removeIf(c -> c.getId().equals(calculationId));
        if (!removed) {
            throw new com.example.export.exception.NotFoundException("Item not found in cart");
        }

        session.setAttribute(CART_SESSION_KEY, cart);
    }

    // Get all items in export cart
    public List<CalculationHistoryDto> getCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CalculationHistoryDto> cart = (List<CalculationHistoryDto>) session.getAttribute(CART_SESSION_KEY);
        return cart != null ? cart : new ArrayList<>();
    }

    // Clear entire export cart
    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }
}

