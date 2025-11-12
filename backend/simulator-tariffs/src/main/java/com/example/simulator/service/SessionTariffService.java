package com.example.simulator.service;

import com.example.simulator.dto.TariffDefinitionsResponse;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// service for managing user-defined tariff definitions stored in HTTP session
@Service
public class SessionTariffService {
    
    private static final String SESSION_TARIFFS_KEY = "SESSION_USER_TARIFFS";
    private static final String MAP_KEY_ID = "id";
    private static final String MAP_KEY_PRODUCT = "product";
    private static final String MAP_KEY_EXPORTING_FROM = "exportingFrom";
    private static final String MAP_KEY_IMPORTING_TO = "importingTo";
    private static final String MAP_KEY_TYPE = "type";
    private static final String MAP_KEY_RATE = "rate";
    private static final String MAP_KEY_EFFECTIVE_DATE = "effectiveDate";
    private static final String MAP_KEY_EXPIRATION_DATE = "expirationDate";

    // get raw session tariff data as list of maps
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

    // convert object to session map format
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

    // convert DTO to session map format
    private java.util.Map<String, Object> toSessionMap(TariffDefinitionsResponse.TariffDefinitionDto dto) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put(MAP_KEY_ID, dto.getId());
        map.put(MAP_KEY_PRODUCT, dto.getProduct());
        map.put(MAP_KEY_EXPORTING_FROM, dto.getExportingFrom());
        map.put(MAP_KEY_IMPORTING_TO, dto.getImportingTo());
        map.put(MAP_KEY_TYPE, dto.getType());
        map.put(MAP_KEY_RATE, dto.getRate());
        map.put(MAP_KEY_EFFECTIVE_DATE, dto.getEffectiveDate());
        map.put(MAP_KEY_EXPIRATION_DATE, dto.getExpirationDate());
        return map;
    }

    // convert session map to DTO
    private TariffDefinitionsResponse.TariffDefinitionDto fromSessionMap(java.util.Map<String, Object> map) {
        return new TariffDefinitionsResponse.TariffDefinitionDto(
                (String) map.get(MAP_KEY_ID),
                (String) map.get(MAP_KEY_PRODUCT),
                (String) map.get(MAP_KEY_EXPORTING_FROM),
                (String) map.get(MAP_KEY_IMPORTING_TO),
                (String) map.get(MAP_KEY_TYPE),
                map.get(MAP_KEY_RATE) instanceof Number ? ((Number) map.get(MAP_KEY_RATE)).doubleValue() : Double.parseDouble(String.valueOf(map.get(MAP_KEY_RATE))),
                (String) map.get(MAP_KEY_EFFECTIVE_DATE),
                (String) map.get(MAP_KEY_EXPIRATION_DATE)
        );
    }

    // convert list of session maps to list of DTOs
    private List<TariffDefinitionsResponse.TariffDefinitionDto> toDtoList(List<java.util.Map<String, Object>> sessionTariffs) {
        List<TariffDefinitionsResponse.TariffDefinitionDto> dtos = new ArrayList<>();
        for (java.util.Map<String, Object> entry : sessionTariffs) {
            dtos.add(fromSessionMap(entry));
        }
        return dtos;
    }

    // persist session tariffs to HTTP session
    private void persistSessionTariffs(HttpSession session, List<java.util.Map<String, Object>> sessionTariffs) {
        session.setAttribute(SESSION_TARIFFS_KEY, new ArrayList<>(sessionTariffs));
    }

    // save tariff definition to session (for simulator mode) - upserts if ID exists
    public TariffDefinitionsResponse.TariffDefinitionDto saveTariffDefinition(
            HttpSession session, 
            TariffDefinitionsResponse.TariffDefinitionDto dto) {
        try {
            // generate a unique ID if not provided
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
            
            // check if tariff with same ID exists, update it; otherwise add new
            boolean found = false;
            for (int i = 0; i < sessionTariffs.size(); i++) {
                if (dto.getId().equals(sessionTariffs.get(i).get(MAP_KEY_ID))) {
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

    // get all tariff definitions from session
    public List<TariffDefinitionsResponse.TariffDefinitionDto> getTariffDefinitions(HttpSession session) {
        return toDtoList(getSessionTariffsRaw(session));
    }

    // get specific tariff definition by ID from session
    @SuppressWarnings("unchecked")
    public TariffDefinitionsResponse.TariffDefinitionDto getTariffDefinitionById(HttpSession session, String id) {
        List<java.util.Map<String, Object>> sessionTariffs = getSessionTariffsRaw(session);
        return sessionTariffs.stream()
                .filter(t -> id.equals(t.get(MAP_KEY_ID)))
                .findFirst()
                .map(this::fromSessionMap)
                .orElse(null);
    }

    // update tariff definition in session
    public TariffDefinitionsResponse.TariffDefinitionDto updateTariffDefinition(
            HttpSession session, 
            String id, 
            TariffDefinitionsResponse.TariffDefinitionDto dto) {
        List<java.util.Map<String, Object>> sessionTariffs = getSessionTariffsRaw(session);
        
        for (int i = 0; i < sessionTariffs.size(); i++) {
            if (id.equals(sessionTariffs.get(i).get(MAP_KEY_ID))) {
                // update with new data but keep the same ID
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

    // delete tariff definition from session
    public void deleteTariffDefinition(HttpSession session, String id) {
        List<java.util.Map<String, Object>> sessionTariffs = getSessionTariffsRaw(session);
        
        boolean removed = sessionTariffs.removeIf(t -> id.equals(t.get(MAP_KEY_ID)));
        
        if (!removed) {
            throw new com.example.simulator.exception.NotFoundException("Tariff definition not found in session: " + id);
        }
        
        persistSessionTariffs(session, sessionTariffs);
    }

    // clear all tariff definitions from session
    public void clearTariffDefinitions(HttpSession session) {
        session.removeAttribute(SESSION_TARIFFS_KEY);
    }
}

