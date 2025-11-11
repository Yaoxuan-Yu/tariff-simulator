package com.example.session.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;


@Schema(description = "Stores a user's calculation history within their active session")
public class SessionHistory {

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

