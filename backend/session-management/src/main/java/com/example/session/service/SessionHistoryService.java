package com.example.session.service;

import com.example.session.dto.CalculationHistoryDto;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SessionHistoryService {
    
    private static final String HISTORY_SESSION_KEY = "CALCULATION_HISTORY";

    /**
     * Save calculation to session history
     * Accepts calculation data as Map (from HTTP response) to avoid direct dependency on tariff-calculator DTO
     */
    public CalculationHistoryDto saveCalculation(HttpSession session, Map<String, Object> calculationData) {
        try {
            // Extract data from Map (from HTTP response)
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) calculationData.get("data");
            
            if (data == null) {
                return null;
            }

            double productCost = ((Number) data.getOrDefault("productCost", 0.0)).doubleValue();
            double totalCost = ((Number) data.getOrDefault("totalCost", 0.0)).doubleValue();
            double tariffAmount = totalCost - productCost;
            double tariffRate = ((Number) data.getOrDefault("tariffRate", 0.0)).doubleValue();

            CalculationHistoryDto history = new CalculationHistoryDto(
                (String) data.getOrDefault("product", ""),
                (String) data.getOrDefault("brand", ""),
                (String) data.getOrDefault("exportingFrom", ""),
                (String) data.getOrDefault("importingTo", ""),
                ((Number) data.getOrDefault("quantity", 0.0)).doubleValue(),
                (String) data.getOrDefault("unit", ""),
                productCost,
                tariffRate,
                tariffAmount,
                totalCost,
                (String) data.getOrDefault("tariffType", "")
            );

            @SuppressWarnings("unchecked")
            List<CalculationHistoryDto> historyList = (List<CalculationHistoryDto>) session.getAttribute(HISTORY_SESSION_KEY);
            if (historyList == null) {
                historyList = new ArrayList<>();
            }

            historyList.add(0, history); // Most recent first
            if (historyList.size() > 100) { // Keep only last 100
                historyList.remove(historyList.size() - 1);
            }

            session.setAttribute(HISTORY_SESSION_KEY, historyList);
            return history;
        } catch (Exception e) {
            throw new com.example.session.exception.DataAccessException("Failed to save calculation to history", e);
        }
    }

    // Get all calculations from history
    public List<CalculationHistoryDto> getCalculationHistory(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CalculationHistoryDto> historyList = (List<CalculationHistoryDto>) session.getAttribute(HISTORY_SESSION_KEY);
        return historyList != null ? new ArrayList<>(historyList) : new ArrayList<>();
    }

    // Get specific calculation from history by ID
    public CalculationHistoryDto getCalculationById(HttpSession session, String calculationId) {
        List<CalculationHistoryDto> historyList = getCalculationHistory(session);
        return historyList.stream()
                .filter(h -> h.getId().equals(calculationId))
                .findFirst()
                .orElse(null);
    }

    // Clear entire session history
    public void clearCalculationHistory(HttpSession session) {
        session.removeAttribute(HISTORY_SESSION_KEY);
    }
}

