package com.example.simulator.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.simulator.dto.TariffDefinitionsResponse;
import com.example.simulator.service.SessionTariffService;
// Note: TariffService for admin operations should call global-tariffs service via API
// For now, keeping direct dependency but should be refactored to use HTTP client
import com.example.simulator.config.SecurityContextUtil;

import jakarta.servlet.http.HttpSession;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Simulator Tariff Definitions", description = "API endpoints for session-based simulator tariff definitions")
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SimulatorTariffController {
    private final SessionTariffService sessionTariffService;
    // Note: In microservices, admin operations should call global-tariffs service via HTTP
    // For now, this is commented out - admin operations would need to be handled differently
    // private final TariffService tariffService;

    public SimulatorTariffController(
            SessionTariffService sessionTariffService) {
        this.sessionTariffService = sessionTariffService;
        // this.tariffService = tariffService;
    }

    @Operation(summary = "Retrieve only user-defined tariff definitions (session-based for simulator mode)")
    @GetMapping("/tariff-definitions/user")
    public ResponseEntity<TariffDefinitionsResponse> getUserTariffDefinitions(HttpSession session) {
        // Get session-based tariffs (for simulator mode)
        List<TariffDefinitionsResponse.TariffDefinitionDto> sessionTariffs = 
            sessionTariffService.getTariffDefinitions(session);
        
        // If admin, also include database tariffs
        // Note: In microservices, this would call global-tariffs service via HTTP
        if (SecurityContextUtil.isAdmin()) {
            // TODO: Call global-tariffs service API: GET /api/tariff-definitions/modified
            // For now, return only session tariffs
            // TariffDefinitionsResponse dbResponse = callGlobalTariffsService();
            // List<TariffDefinitionsResponse.TariffDefinitionDto> allTariffs = new java.util.ArrayList<>(sessionTariffs);
            // if (dbResponse != null && dbResponse.getData() != null) {
            //     allTariffs.addAll(dbResponse.getData());
            // }
            // return ResponseEntity.ok(new TariffDefinitionsResponse(true, allTariffs));
        }
        
        // Regular users only see their session tariffs
        return ResponseEntity.ok(new TariffDefinitionsResponse(true, sessionTariffs));
    }

    @Operation(summary = "Add a new user-defined tariff definition (regular users save to session for simulator mode)")
    @PostMapping("/tariff-definitions/user")
    public ResponseEntity<TariffDefinitionsResponse> addUserTariffDefinition(
            @RequestBody TariffDefinitionsResponse.TariffDefinitionDto dto,
            HttpSession session) {
        if (dto == null) {
            throw new com.example.simulator.exception.BadRequestException("Tariff definition data is required");
        }
        
        // Save to session (for simulator mode) - API Gateway routes admin requests to /modified endpoint
        TariffDefinitionsResponse.TariffDefinitionDto saved = sessionTariffService.saveTariffDefinition(session, dto);
        return ResponseEntity.ok(new TariffDefinitionsResponse(true, List.of(saved)));
    }

    @Operation(summary = "Update an existing user-defined tariff definition (regular users update session)")
    @PutMapping("/tariff-definitions/user/{id}")
    public ResponseEntity<TariffDefinitionsResponse> updateUserTariffDefinition(
            @PathVariable String id,
            @RequestBody TariffDefinitionsResponse.TariffDefinitionDto dto,
            HttpSession session) {
        if (dto == null) {
            throw new com.example.simulator.exception.BadRequestException("Tariff definition data is required");
        }
        if (id == null || id.trim().isEmpty()) {
            throw new com.example.simulator.exception.BadRequestException("Tariff definition ID is required");
        }
        
        // Update in session - API Gateway routes admin requests to /modified endpoint
        TariffDefinitionsResponse.TariffDefinitionDto updated = sessionTariffService.updateTariffDefinition(session, id, dto);
        return ResponseEntity.ok(new TariffDefinitionsResponse(true, List.of(updated)));
    }

    @Operation(summary = "Delete a user-defined tariff definition (regular users delete from session)")
    @DeleteMapping("/tariff-definitions/user/{id}")
    public ResponseEntity<?> deleteUserTariffDefinition(@PathVariable String id, HttpSession session) {
        if (id == null || id.trim().isEmpty()) {
            throw new com.example.simulator.exception.BadRequestException("Tariff definition ID is required");
        }
        
        // Delete from session - API Gateway routes admin requests to /modified endpoint
        sessionTariffService.deleteTariffDefinition(session, id);
        return ResponseEntity.ok().build();
    }
}

