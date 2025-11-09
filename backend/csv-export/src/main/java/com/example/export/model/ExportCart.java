package com.example.export.model;

import com.example.session.dto.CalculationHistoryDto;
import java.util.ArrayList;
import java.util.List;

public class ExportCart {
private final List<CalculationHistoryDto> items = new ArrayList<>();

    // Add a tariff calculation to cart
    public void addItem(CalculationHistoryDto calculation) {
        if (calculation != null) {
            items.add(calculation);
        }
    }

    // Get all items
    public List<CalculationHistoryDto> getItems() {
        return new ArrayList<>(items);
    }

    // Remove by index
    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }

    // Clear entire cart
    public void clear() {
        items.clear();
    }

    // Check if cart is empty
    public boolean isEmpty() {
        return items.isEmpty();
    }    
}

