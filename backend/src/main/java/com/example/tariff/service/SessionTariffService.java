package com.example.tariff.service;

import com.example.tariff.dto.TariffDefinitionsResponse;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SessionTariffService {
    
    private static final String SESSION_TARIFFS_KEY = "SESSION_USER_TARIFFS";

    // Save tariff definition to session (for simulator mode)
    public TariffDefinitionsResponse.TariffDefinitionDto saveTariffDefinition(
            HttpSession session, 
            TariffDefinitionsResponse.TariffDefinitionDto dto) {
        try {
            // Generate a unique ID if not provided
            if (dto.getId() == null || dto.getId().trim().isEmpty()) {
                String id = UUID.randomUUID().toString();
                dto = new TariffDefinitionsResponse.TariffDefinitionDto(
                    id,
                    dto.getProduct(),
                    dto.getExportingFrom(),
                    dto.getImportingTo(),
                    dto.getType(),
                    dto.getRate(),
                    dto.getEffectiveDate(),
                    dto.getExpirationDate()
                );
            }

            List<TariffDefinitionsResponse.TariffDefinitionDto> sessionTariffs = getTariffDefinitions(session);
            
            // Check if tariff with same ID exists, update it; otherwise add new
            boolean found = false;
            for (int i = 0; i < sessionTariffs.size(); i++) {
                if (sessionTariffs.get(i).getId().equals(dto.getId())) {
                    sessionTariffs.set(i, dto);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                sessionTariffs.add(dto);
            }

            session.setAttribute(SESSION_TARIFFS_KEY, sessionTariffs);
            return dto;
        } catch (Exception e) {
            throw new com.example.tariff.exception.DataAccessException("Failed to save tariff definition to session", e);
        }
    }

    // Get all tariff definitions from session
    public List<TariffDefinitionsResponse.TariffDefinitionDto> getTariffDefinitions(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<TariffDefinitionsResponse.TariffDefinitionDto> sessionTariffs = 
            (List<TariffDefinitionsResponse.TariffDefinitionDto>) session.getAttribute(SESSION_TARIFFS_KEY);
        return sessionTariffs != null ? new ArrayList<>(sessionTariffs) : new ArrayList<>();
    }

    // Get specific tariff definition by ID
    public TariffDefinitionsResponse.TariffDefinitionDto getTariffDefinitionById(HttpSession session, String id) {
        List<TariffDefinitionsResponse.TariffDefinitionDto> sessionTariffs = getTariffDefinitions(session);
        return sessionTariffs.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // Update tariff definition in session
    public TariffDefinitionsResponse.TariffDefinitionDto updateTariffDefinition(
            HttpSession session, 
            String id, 
            TariffDefinitionsResponse.TariffDefinitionDto dto) {
        List<TariffDefinitionsResponse.TariffDefinitionDto> sessionTariffs = getTariffDefinitions(session);
        
        for (int i = 0; i < sessionTariffs.size(); i++) {
            if (sessionTariffs.get(i).getId().equals(id)) {
                // Update with new data but keep the same ID
                TariffDefinitionsResponse.TariffDefinitionDto updated = new TariffDefinitionsResponse.TariffDefinitionDto(
                    id,
                    dto.getProduct(),
                    dto.getExportingFrom(),
                    dto.getImportingTo(),
                    dto.getType(),
                    dto.getRate(),
                    dto.getEffectiveDate(),
                    dto.getExpirationDate()
                );
                sessionTariffs.set(i, updated);
                session.setAttribute(SESSION_TARIFFS_KEY, sessionTariffs);
                return updated;
            }
        }
        
        throw new com.example.tariff.exception.NotFoundException("Tariff definition not found in session: " + id);
    }

    // Delete tariff definition from session
    public void deleteTariffDefinition(HttpSession session, String id) {
        List<TariffDefinitionsResponse.TariffDefinitionDto> sessionTariffs = getTariffDefinitions(session);
        
        boolean removed = sessionTariffs.removeIf(t -> t.getId().equals(id));
        
        if (!removed) {
            throw new com.example.tariff.exception.NotFoundException("Tariff definition not found in session: " + id);
        }
        
        session.setAttribute(SESSION_TARIFFS_KEY, sessionTariffs);
    }

    // Clear all tariff definitions from session
    public void clearTariffDefinitions(HttpSession session) {
        session.removeAttribute(SESSION_TARIFFS_KEY);
    }
}

