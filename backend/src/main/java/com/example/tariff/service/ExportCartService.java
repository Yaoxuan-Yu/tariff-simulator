package com.example.tariff.service;

import com.example.tariff.dto.CalculationHistoryDto;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExportCartService {
    
    private static final String CART_SESSION_KEY = "EXPORT_CART";

    private final SessionHistoryService sessionHistoryService;

    public ExportCartService(SessionHistoryService sessionHistoryService) {
        this.sessionHistoryService = sessionHistoryService;
    }

    // Add calculation to export cart
    public void addToCart(HttpSession session, String calculationId) {
        CalculationHistoryDto calculation = sessionHistoryService.getCalculationById(session, calculationId);
        
        if (calculation == null) {
            throw new com.example.tariff.exception.NotFoundException("Calculation not found in history");
        }

        @SuppressWarnings("unchecked")
        List<CalculationHistoryDto> cart = (List<CalculationHistoryDto>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
        }

        // Prevent duplicates
        if (cart.stream().anyMatch(c -> c.getId().equals(calculationId))) {
            throw new com.example.tariff.exception.BadRequestException("Item already in cart");
        }

        cart.add(calculation);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    // Remove calculation from export cart
    public void removeFromCart(HttpSession session, String calculationId) {
        @SuppressWarnings("unchecked")
        List<CalculationHistoryDto> cart = (List<CalculationHistoryDto>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null || cart.isEmpty()) {
            throw new com.example.tariff.exception.NotFoundException("Cart is empty");
        }

        boolean removed = cart.removeIf(c -> c.getId().equals(calculationId));
        if (!removed) {
            throw new com.example.tariff.exception.NotFoundException("Item not found in cart");
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
