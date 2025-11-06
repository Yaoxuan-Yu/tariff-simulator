package com.example.session.model;

// Note: TariffResponse is in tariff-calculator service
// In a microservices architecture, this model would either:
// 1. Use CalculationHistoryDto instead (which is already in session-management)
// 2. Or call tariff-calculator service to get TariffResponse
// For now, keeping reference commented out
// import com.example.calculator.dto.TariffResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;


@Schema(description = "Stores a user's calculation history within their active session")
public class SessionHistory {

    // Note: Using CalculationHistoryDto instead of TariffResponse for session management
    // This avoids cross-service dependencies
    @Schema(description = "List of all tariff calculations performed by the user in this session")
    private final List<com.example.session.dto.CalculationHistoryDto> history = new ArrayList<>();

    public void addCalculation(com.example.session.dto.CalculationHistoryDto calculation) {
        history.add(calculation);
    }

    public List<com.example.session.dto.CalculationHistoryDto> getHistory() {
        return new ArrayList<>(history);
    }

    public boolean isEmpty() {
        return history.isEmpty();
    }

    public void clear() {
        history.clear();
    }   
}

