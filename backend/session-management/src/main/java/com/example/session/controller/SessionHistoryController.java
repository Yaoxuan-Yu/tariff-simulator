package com.example.session.controller;

import com.example.session.dto.CalculationHistoryDto;
import com.example.session.service.SessionHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Session History", description = "API endpoints for calculation session history")
@RequestMapping("/api/tariff")
@CrossOrigin(origins = "*")
public class SessionHistoryController {
    private final SessionHistoryService sessionHistoryService;

    public SessionHistoryController(SessionHistoryService sessionHistoryService) {
        this.sessionHistoryService = sessionHistoryService;
    }

    @Operation(summary = "Save calculation to session history")
    @PostMapping("/history/save")
    public ResponseEntity<?> saveCalculation(
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> calculationData = (Map<String, Object>) request.get("calculationData");
            
            if (calculationData == null) {
                throw new com.example.session.exception.BadRequestException("Calculation data is required");
            }
            
            CalculationHistoryDto history = sessionHistoryService.saveCalculation(session, calculationData);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            throw new com.example.session.exception.DataAccessException("Failed to save calculation", e);
        }
    }

    @Operation(summary = "Retrieve calculation history for the current session")
    @GetMapping("/history")
    public ResponseEntity<List<CalculationHistoryDto>> getCalculationHistory(HttpSession session) {
        List<CalculationHistoryDto> history = sessionHistoryService.getCalculationHistory(session);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Clear calculation history for the current session")
    @DeleteMapping("/history/clear")
    public ResponseEntity<?> clearCalculationHistory(HttpSession session) {
        sessionHistoryService.clearCalculationHistory(session);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get a specific calculation by ID from session history")
    @GetMapping("/history/{id}")
    public ResponseEntity<CalculationHistoryDto> getCalculationById(
            @PathVariable String id,
            @RequestParam(required = false) String sessionId,
            HttpSession session) {
        if (id == null || id.trim().isEmpty()) {
            throw new com.example.session.exception.BadRequestException("Calculation ID is required");
        }
        
        // If sessionId parameter is provided, use it to look up the calculation
        CalculationHistoryDto calculation;
        if (sessionId != null && !sessionId.isBlank()) {
            calculation = sessionHistoryService.getCalculationByIdFromSession(sessionId, id);
        } else {
            calculation = sessionHistoryService.getCalculationById(session, id);
        }

        if (calculation == null) {
            throw new com.example.session.exception.NotFoundException("Calculation not found in history");
        }
        return ResponseEntity.ok(calculation);
    }

    @Operation(summary = "Remove a specific calculation by ID from session history")
    @DeleteMapping("/history/{id}")
    public ResponseEntity<?> removeCalculationById(
            @PathVariable String id,
            @RequestParam(required = false) String sessionId,
            HttpSession session) {
        if (id == null || id.trim().isEmpty()) {
            throw new com.example.session.exception.BadRequestException("Calculation ID is required");
        }
        
        if (sessionId != null && !sessionId.isBlank()) {
            sessionHistoryService.removeCalculationByIdFromSession(sessionId, id);
        } else {
            @SuppressWarnings("unchecked")
            List<CalculationHistoryDto> historyList = (List<CalculationHistoryDto>) session.getAttribute("CALCULATION_HISTORY");
            if (historyList != null) {
                historyList.removeIf(h -> h.getId().equals(id));
                session.setAttribute("CALCULATION_HISTORY", historyList);
            }
        }
        return ResponseEntity.ok().build();
    }
}
