package com.example.export.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.session.dto.CalculationHistoryDto;

import jakarta.servlet.http.HttpSession;

// manages export cart entries stored in the user's http session
@Service
public class ExportCartService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExportCartService.class);
    private static final String CART_SESSION_KEY = "EXPORT_CART"; // session attribute key

    private final com.example.export.client.SessionManagementClient sessionManagementClient;

    public ExportCartService(com.example.export.client.SessionManagementClient sessionManagementClient) {
        this.sessionManagementClient = sessionManagementClient;
    }
    
    // add calculation to cart (moves item from session history)
    public void addToCart(String calculationId, HttpSession session) {
        // call session-management to fetch calculation by id
        log.debug("Fetching calculation {} for session {}", calculationId, session.getId());
        CalculationHistoryDto calculation = sessionManagementClient.getCalculationById(session.getId(), calculationId);
        
        if (calculation == null) {
            throw new com.example.export.exception.NotFoundException("Calculation not found in history");
        }

        @SuppressWarnings("unchecked")
        List<CalculationHistoryDto> cart = (List<CalculationHistoryDto>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
        }

        // prevent duplicates in cart
        if (cart.stream().anyMatch(c -> c.getId().equals(calculationId))) {
            throw new com.example.export.exception.BadRequestException("Item already in cart");
        }

        cart.add(calculation);
        session.setAttribute(CART_SESSION_KEY, cart);
        
        // remove from history after adding to cart
        try {
            sessionManagementClient.removeCalculationById(session.getId(), calculationId);
            log.debug("Removed calculation {} from session history for {}", calculationId, session.getId());
        } catch (Exception e) {
            log.warn("Failed to remove calculation {} from history for {}: {}", calculationId, session.getId(), e.getMessage());
            // don't bubble up – export cart still contains the calculation
        }
    }

    // remove individual calculation from cart
    public void removeFromCart(String calculationId, HttpSession session) {
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

    // get current cart snapshot
    public List<CalculationHistoryDto> getCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CalculationHistoryDto> cart = (List<CalculationHistoryDto>) session.getAttribute(CART_SESSION_KEY);
        return cart != null ? cart : new ArrayList<>();
    }

    // remove cart from session
    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }
}

