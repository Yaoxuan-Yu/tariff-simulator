package com.example.tariff.model;

import com.example.tariff.dto.TariffResponse;
import java.util.ArrayList;
import java.util.List;

public class ExportCart {
private final List<TariffResponse> items = new ArrayList<>();

    // Add a tariff calculation to cart
    public void addItem(TariffResponse tariffResponse) {
        if (tariffResponse != null && tariffResponse.isSuccess()) {
            items.add(tariffResponse);
        }
    }

    // Get all items
    public List<TariffResponse> getItems() {
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
