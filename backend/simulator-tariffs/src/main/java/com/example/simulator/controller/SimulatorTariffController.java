package com.example.simulator.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.simulator.dto.TariffDefinitionsResponse;
import com.example.simulator.service.SessionTariffService;
import com.example.simulator.config.SecurityContextUtil;

import jakarta.servlet.http.HttpSession;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// routes session-based tariff definition requests for simulator mode
@RestController
@Tag(name = "Simulator Tariff Definitions", description = "API endpoints for session-based simulator tariff definitions")
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SimulatorTariffController {
    
    private final SessionTariffService sessionTariffService;

    public SimulatorTariffController(SessionTariffService sessionTariffService) {
        this.sessionTariffService = sessionTariffService;
    }

    // GET /api/tariff-definitions/user -> get user-defined tariff definitions from session
    @Operation(summary = "Retrieve only user-defined tariff definitions (session-based for simulator mode)")
    @GetMapping("/tariff-definitions/user")
    public ResponseEntity<TariffDefinitionsResponse> getUserTariffDefinitions(HttpSession session) {
        // get session-based tariffs (for simulator mode)
        List<TariffDefinitionsResponse.TariffDefinitionDto> sessionTariffs = 
            sessionTariffService.getTariffDefinitions(session);
        
        // if admin, also include database tariffs
        // note: in microservices, this would call global-tariffs service via HTTP
        if (SecurityContextUtil.isAdmin()) {
            // TODO: call global-tariffs service API: GET /api/tariff-definitions/modified
            // for now, return only session tariffs
        }
        
        // regular users only see their session tariffs
        return ResponseEntity.ok(new TariffDefinitionsResponse(true, sessionTariffs));
    }

    // POST /api/tariff-definitions/user -> add new user-defined tariff definition to session
    @Operation(summary = "Add a new user-defined tariff definition (regular users save to session for simulator mode)")
    @PostMapping("/tariff-definitions/user")
    public ResponseEntity<TariffDefinitionsResponse> addUserTariffDefinition(
            @RequestBody TariffDefinitionsResponse.TariffDefinitionDto dto,
            HttpSession session) {
        if (dto == null) {
            throw new com.example.simulator.exception.BadRequestException("Tariff definition data is required");
        }
        
        // save to session (for simulator mode) - API Gateway routes admin requests to /modified endpoint
        TariffDefinitionsResponse.TariffDefinitionDto saved = sessionTariffService.saveTariffDefinition(session, dto);
        return ResponseEntity.ok(new TariffDefinitionsResponse(true, List.of(saved)));
    }

    // PUT /api/tariff-definitions/user/{id} -> update existing user-defined tariff definition
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
        
        // update in session - API Gateway routes admin requests to /modified endpoint
        TariffDefinitionsResponse.TariffDefinitionDto updated = sessionTariffService.updateTariffDefinition(session, id, dto);
        return ResponseEntity.ok(new TariffDefinitionsResponse(true, List.of(updated)));
    }

    // DELETE /api/tariff-definitions/user/{id} -> delete user-defined tariff definition from session
    @Operation(summary = "Delete a user-defined tariff definition (regular users delete from session)")
    @DeleteMapping("/tariff-definitions/user/{id}")
    public ResponseEntity<?> deleteUserTariffDefinition(@PathVariable String id, HttpSession session) {
        if (id == null || id.trim().isEmpty()) {
            throw new com.example.simulator.exception.BadRequestException("Tariff definition ID is required");
        }
        
        // delete from session - API Gateway routes admin requests to /modified endpoint
        sessionTariffService.deleteTariffDefinition(session, id);
        return ResponseEntity.ok().build();
    }
}

