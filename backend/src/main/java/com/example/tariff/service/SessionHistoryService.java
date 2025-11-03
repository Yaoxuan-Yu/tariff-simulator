package com.example.tariff.service;

import com.example.tariff.dto.CalculationHistoryDto;
import com.example.tariff.dto.TariffResponse;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Service
public class SessionHistoryService {
    
    private static final String HISTORY_SESSION_KEY = "CALCULATION_HISTORY";

    // Save calculation to session history (auto-called after every calculation)
    public CalculationHistoryDto saveCalculation(HttpSession session, TariffResponse tariffResponse) {
        try {
            if (!tariffResponse.isSuccess() || tariffResponse.getData() == null) {
                return null;
            }

            TariffResponse.TariffCalculationData data = tariffResponse.getData();
            double tariffAmount = data.getTotalCost() - data.getProductCost();

            CalculationHistoryDto history = new CalculationHistoryDto(
                data.getProduct(),
                data.getBrand(),
                data.getExportingFrom(),
                data.getImportingTo(),
                data.getQuantity(),
                data.getUnit(),
                data.getProductCost(),
                data.getTariffRate(),
                tariffAmount,
                data.getTotalCost(),
                data.getTariffType()
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
            throw new com.example.tariff.exception.DataAccessException("Failed to save calculation to history", e);
        }
    }

    // Get all calculations from history
    public List<CalculationHistoryDto> getAllHistory(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CalculationHistoryDto> historyList = (List<CalculationHistoryDto>) session.getAttribute(HISTORY_SESSION_KEY);
        return historyList != null ? historyList : new ArrayList<>();
    }

    // Get specific calculation from history by ID
    public CalculationHistoryDto getCalculationById(HttpSession session, String calculationId) {
        List<CalculationHistoryDto> historyList = getAllHistory(session);
        return historyList.stream()
                .filter(h -> h.getId().equals(calculationId))
                .findFirst()
                .orElse(null);
    }

    // Clear entire session history
    public void clearHistory(HttpSession session) {
        session.removeAttribute(HISTORY_SESSION_KEY);
    }
}
