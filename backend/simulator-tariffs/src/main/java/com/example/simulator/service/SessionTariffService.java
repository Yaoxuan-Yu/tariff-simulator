package com.example.simulator.service;

import com.example.simulator.dto.TariffDefinitionsResponse;
// Note: This service uses TariffDefinitionsResponse from simulator-tariffs package
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SessionTariffService {
    
    private static final String SESSION_TARIFFS_KEY = "SESSION_USER_TARIFFS";

    private List<java.util.Map<String, Object>> getSessionTariffsRaw(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Object> rawList = (List<Object>) session.getAttribute(SESSION_TARIFFS_KEY);
        List<java.util.Map<String, Object>> mapped = new ArrayList<>();
        if (rawList != null) {
            for (Object item : rawList) {
                mapped.add(toSessionMap(item));
            }
        }
        return mapped;
    }

    private java.util.Map<String, Object> toSessionMap(Object entry) {
        if (entry instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> existing = (java.util.Map<String, Object>) entry;
            return new java.util.HashMap<>(existing);
        }
        if (entry instanceof TariffDefinitionsResponse.TariffDefinitionDto dto) {
            return toSessionMap(dto);
        }
        return new java.util.HashMap<>();
    }

    private java.util.Map<String, Object> toSessionMap(TariffDefinitionsResponse.TariffDefinitionDto dto) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", dto.getId());
        map.put("product", dto.getProduct());
        map.put("exportingFrom", dto.getExportingFrom());
        map.put("importingTo", dto.getImportingTo());
        map.put("type", dto.getType());
        map.put("rate", dto.getRate());
        map.put("effectiveDate", dto.getEffectiveDate());
        map.put("expirationDate", dto.getExpirationDate());
        return map;
    }

    private TariffDefinitionsResponse.TariffDefinitionDto fromSessionMap(java.util.Map<String, Object> map) {
        return new TariffDefinitionsResponse.TariffDefinitionDto(
                (String) map.get("id"),
                (String) map.get("product"),
                (String) map.get("exportingFrom"),
                (String) map.get("importingTo"),
                (String) map.get("type"),
                map.get("rate") instanceof Number ? ((Number) map.get("rate")).doubleValue() : Double.parseDouble(String.valueOf(map.get("rate"))),
                (String) map.get("effectiveDate"),
                (String) map.get("expirationDate")
        );
    }

    private List<TariffDefinitionsResponse.TariffDefinitionDto> toDtoList(List<java.util.Map<String, Object>> sessionTariffs) {
        List<TariffDefinitionsResponse.TariffDefinitionDto> dtos = new ArrayList<>();
        for (java.util.Map<String, Object> entry : sessionTariffs) {
            dtos.add(fromSessionMap(entry));
        }
        return dtos;
    }

    private void persistSessionTariffs(HttpSession session, List<java.util.Map<String, Object>> sessionTariffs) {
        session.setAttribute(SESSION_TARIFFS_KEY, new ArrayList<>(sessionTariffs));
    }

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

            List<java.util.Map<String, Object>> sessionTariffs = getSessionTariffsRaw(session);
            
            // Check if tariff with same ID exists, update it; otherwise add new
            boolean found = false;
            for (int i = 0; i < sessionTariffs.size(); i++) {
                if (dto.getId().equals(sessionTariffs.get(i).get("id"))) {
                    sessionTariffs.set(i, toSessionMap(dto));
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                sessionTariffs.add(toSessionMap(dto));
            }

            persistSessionTariffs(session, sessionTariffs);
            return dto;
        } catch (Exception e) {
            throw new com.example.simulator.exception.DataAccessException("Failed to save tariff definition to session", e);
        }
    }

    // Get all tariff definitions from session
    public List<TariffDefinitionsResponse.TariffDefinitionDto> getTariffDefinitions(HttpSession session) {
        return toDtoList(getSessionTariffsRaw(session));
    }

    // Get specific tariff definition by ID
    @SuppressWarnings("Unchecked")
    public TariffDefinitionsResponse.TariffDefinitionDto getTariffDefinitionById(HttpSession session, String id) {
        List<java.util.Map<String, Object>> sessionTariffs = getSessionTariffsRaw(session);
        return sessionTariffs.stream()
                .filter(t -> id.equals(t.get("id")))
                .findFirst()
                .map(this::fromSessionMap)
                .orElse(null);
    }

    // Update tariff definition in session
    public TariffDefinitionsResponse.TariffDefinitionDto updateTariffDefinition(
            HttpSession session, 
            String id, 
            TariffDefinitionsResponse.TariffDefinitionDto dto) {
        List<java.util.Map<String, Object>> sessionTariffs = getSessionTariffsRaw(session);
        
        for (int i = 0; i < sessionTariffs.size(); i++) {
            if (id.equals(sessionTariffs.get(i).get("id"))) {
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
                sessionTariffs.set(i, toSessionMap(updated));
                persistSessionTariffs(session, sessionTariffs);
                return updated;
            }
        }
        
        throw new com.example.simulator.exception.NotFoundException("Tariff definition not found in session: " + id);
    }

    // Delete tariff definition from session
    public void deleteTariffDefinition(HttpSession session, String id) {
        List<java.util.Map<String, Object>> sessionTariffs = getSessionTariffsRaw(session);
        
        boolean removed = sessionTariffs.removeIf(t -> id.equals(t.get("id")));
        
        if (!removed) {
            throw new com.example.simulator.exception.NotFoundException("Tariff definition not found in session: " + id);
        }
        
        persistSessionTariffs(session, sessionTariffs);
    }

    // Clear all tariff definitions from session
    public void clearTariffDefinitions(HttpSession session) {
        session.removeAttribute(SESSION_TARIFFS_KEY);
    }
}

