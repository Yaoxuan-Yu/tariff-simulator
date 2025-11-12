package com.example.session.service;

import com.example.session.dto.CalculationHistoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// manages calculation history stored in http session (with optional redis lookup for cross-service)
@Service
public class SessionHistoryService {
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String HISTORY_SESSION_KEY = "CALCULATION_HISTORY";
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SessionHistoryService.class);


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
            
            // Set source field if provided, otherwise default to "global"
            String source = (String) data.getOrDefault("source", "global");
            history.setSource(source);

            @SuppressWarnings("unchecked")
            List<CalculationHistoryDto> historyList = (List<CalculationHistoryDto>) session.getAttribute(HISTORY_SESSION_KEY);
            if (historyList == null) {
                historyList = new ArrayList<>();
            }

            historyList.add(0, history); // Most recent first
            if (historyList.size() > 100) { // Keep only last 100
                historyList.remove(historyList.size() - 1);
                log.debug("History size > 100, trimming oldest entry for session {}", session.getId());
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
    
    // Get specific calculation from history by ID using session ID (for cross-service calls)
    public CalculationHistoryDto getCalculationByIdFromSession(String sessionId, String calculationId) {
        try {
            if (redisTemplate == null) {
                log.warn("RedisTemplate is not configured; cannot access session {}", sessionId);
                return null;
            }
            
            // Access Redis directly using the session ID
            String sessionKey = "spring:session:sessions:" + sessionId;
            Object historyObj = redisTemplate.opsForHash().get(sessionKey, "sessionAttr:" + HISTORY_SESSION_KEY);
            
            log.debug("Looking up session {} for calculation {}", sessionKey, calculationId);
            
            if (historyObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<CalculationHistoryDto> historyList = (List<CalculationHistoryDto>) historyObj;
                return historyList.stream()
                        .filter(h -> h.getId().equals(calculationId))
                        .findFirst()
                        .orElse(null);
            }
            return null;
        } catch (Exception e) {
            log.error("Error accessing Redis for session {}: {}", sessionId, e.getMessage(), e);
            return null;
        }
    }

    // Clear entire session history
    public void clearCalculationHistory(HttpSession session) {
        session.removeAttribute(HISTORY_SESSION_KEY);
    }
    
    // Remove specific calculation from history by ID using session ID (for cross-service calls)
    public void removeCalculationByIdFromSession(String sessionId, String calculationId) {
        try {
            if (redisTemplate == null) {
                log.warn("RedisTemplate is not configured; cannot remove calc {} from session {}", calculationId, sessionId);
                return;
            }
            
            // Access Redis directly using the session ID
            String sessionKey = "spring:session:sessions:" + sessionId;
            Object historyObj = redisTemplate.opsForHash().get(sessionKey, "sessionAttr:" + HISTORY_SESSION_KEY);
            
            log.debug("Removing calculation {} from session {}", calculationId, sessionKey);
            
            if (historyObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<CalculationHistoryDto> historyList = (List<CalculationHistoryDto>) historyObj;
                
                // Remove the calculation with matching ID
                boolean removed = historyList.removeIf(h -> h.getId().equals(calculationId));
                
                if (removed) {
                    // Save the updated list back to Redis
                    redisTemplate.opsForHash().put(sessionKey, "sessionAttr:" + HISTORY_SESSION_KEY, historyList);
                    log.debug("Calculation {} removed from session {}", calculationId, sessionKey);
                } else {
                    log.debug("Calculation {} not found in session {}", calculationId, sessionKey);
                }
            }
        } catch (Exception e) {
            log.error("Failed to remove calculation {} from session {}: {}", calculationId, sessionId, e.getMessage(), e);
        }
    }
}

