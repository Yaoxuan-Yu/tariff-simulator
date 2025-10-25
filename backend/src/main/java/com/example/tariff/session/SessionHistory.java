package com.example.tariff.session;

import com.example.tariff.dto.TariffResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;


@Schema(description = "Stores a user's calculation history within their active session")
public class SessionHistory {
    
    @Schema(description = "List of all tariff calculations performed by the user in this session")
    private final List<TariffResponse> history = new ArrayList<>();

    public void addCalculation(TariffResponse response) {
        history.add(response);
    }

    public List<TariffResponse> getHistory() {
        return new ArrayList<>(history);
    }

    public boolean isEmpty() {
        return history.isEmpty();
    }

    public void clear() {
        history.clear();
    }   
}
