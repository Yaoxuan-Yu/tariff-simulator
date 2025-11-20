package com.example.tariffs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.tariffs.dto.TariffDefinitionsResponse;
import com.example.tariffs.service.TariffService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

// rest endpoints for tariff definitions (global + admin modified)
@RestController
@Tag(name = "Global Tariff Definitions", description = "API endpoints for global/system tariff definitions")
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GlobalTariffController {
    private final TariffService tariffService;

    public GlobalTariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    @Operation(summary = "Retrieve all tariff definitions (both global and user-defined)")
    // GET /api/tariff-definitions -> combined view of tariffs
    @GetMapping("/tariff-definitions")
    public ResponseEntity<TariffDefinitionsResponse> getTariffDefinitions() {
        TariffDefinitionsResponse response = tariffService.getTariffDefinitions();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retrieve only global/system tariff definitions that is currently stored in the database.")
    // GET /api/tariff-definitions/global -> view generated from database tariffs
    @GetMapping("/tariff-definitions/global")
    public ResponseEntity<TariffDefinitionsResponse> getGlobalTariffDefinitions() {
        return ResponseEntity.ok(tariffService.getGlobalTariffDefinitions());
    }

    @Operation(summary = "Retrieve modified tariff definitions")
    // GET /api/tariff-definitions/modified -> admin managed overrides
    @GetMapping("/tariff-definitions/modified")
    public ResponseEntity<TariffDefinitionsResponse> getModifiedTariffDefinitions() {
        TariffDefinitionsResponse response = tariffService.getUserTariffDefinitions();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retrieve user-defined tariff definitions")
    // GET /api/tariff-definitions/user -> user-defined tariffs (in-memory)
    @GetMapping("/tariff-definitions/user")
    public ResponseEntity<TariffDefinitionsResponse> getUserTariffDefinitions() {
        TariffDefinitionsResponse response = tariffService.getUserTariffDefinitions();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add a new tariff definition (saves to database)")
    // POST /api/tariff-definitions/modified -> create admin override
    @PostMapping("/tariff-definitions/modified")
    public ResponseEntity<TariffDefinitionsResponse> addModifiedTariffDefinition(
            @RequestBody Map<String, Object> requestBody) {
        if (requestBody == null) {
            throw new com.example.tariffs.exception.BadRequestException("Tariff definition data is required");
        }
        // Convert Map to DTO - simple conversion for now
        TariffDefinitionsResponse.TariffDefinitionDto dto = convertMapToDto(requestBody);
        return ResponseEntity.ok(tariffService.addAdminTariffDefinition(dto));
    }

    @Operation(summary = "Add a new user-defined tariff definition (stores in-memory)")
    // POST /api/tariff-definitions/user -> create user-defined tariff
    @PostMapping("/tariff-definitions/user")
    public ResponseEntity<TariffDefinitionsResponse> addUserTariffDefinition(
            @RequestBody Map<String, Object> requestBody) {
        if (requestBody == null) {
            throw new com.example.tariffs.exception.BadRequestException("Tariff definition data is required");
        }
        // Convert Map to DTO - simple conversion for now
        TariffDefinitionsResponse.TariffDefinitionDto dto = convertMapToDto(requestBody);
        // For user tariffs, we just add them to the in-memory list with their original ID
        tariffService.addUserTariffDefinition(dto);
        return ResponseEntity.ok(new TariffDefinitionsResponse(true, List.of(dto)));
    }

    @Operation(summary = "Update an existing tariff definition (updates database)")
    // PUT /api/tariff-definitions/modified/{id} -> update admin override
    @PutMapping("/tariff-definitions/modified/{id}")
    public ResponseEntity<TariffDefinitionsResponse> updateModifiedTariffDefinition(
            @PathVariable String id,
            @RequestBody Map<String, Object> requestBody) {
        if (requestBody == null) {
            throw new com.example.tariffs.exception.BadRequestException("Tariff definition data is required");
        }
        if (id == null || id.trim().isEmpty()) {
            throw new com.example.tariffs.exception.BadRequestException("Tariff definition ID is required");
        }
        TariffDefinitionsResponse.TariffDefinitionDto dto = convertMapToDto(requestBody);
        return ResponseEntity.ok(tariffService.updateAdminTariffDefinition(id, dto));
    }

    @Operation(summary = "Delete a tariff definition (deletes from database)")
    // DELETE /api/tariff-definitions/modified/{id} -> delete admin override (in-memory)
    @DeleteMapping("/tariff-definitions/modified/{id}")
    public ResponseEntity<?> deleteModifiedTariffDefinition(@PathVariable String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new com.example.tariffs.exception.BadRequestException("Tariff definition ID is required");
        }
        tariffService.deleteAdminTariffDefinition(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete a global tariff definition from database (admin only)")
    // DELETE /api/tariff-definitions/global -> delete from database using tariff data
    @DeleteMapping(value = "/tariff-definitions/global", consumes = "application/json")
    public ResponseEntity<?> deleteGlobalTariffDefinition(@RequestBody Map<String, Object> requestBody) {
        if (requestBody == null || requestBody.isEmpty()) {
            throw new com.example.tariffs.exception.BadRequestException("Tariff definition data is required");
        }
        TariffDefinitionsResponse.TariffDefinitionDto dto = convertMapToDto(requestBody);
        tariffService.deleteGlobalTariffDefinition(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete a user-defined tariff definition (removes from in-memory list)")
    // DELETE /api/tariff-definitions/user/{id} -> delete user-defined tariff
    @DeleteMapping("/tariff-definitions/user/{id}")
    public ResponseEntity<?> deleteUserTariffDefinition(@PathVariable String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new com.example.tariffs.exception.BadRequestException("Tariff definition ID is required");
        }
        tariffService.deleteUserTariffDefinition(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Export tariff definitions as CSV")
    // GET /api/tariff-definitions/export -> placeholder endpoint
    @GetMapping("/tariff-definitions/export")
    public void exportTariffDefinitions(jakarta.servlet.http.HttpServletResponse response) {
        // Export functionality to be implemented
        response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_NOT_IMPLEMENTED);
        try {
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Export functionality not yet implemented\"}");
        } catch (java.io.IOException e) {
            // Ignore
        }
    }

    // Helper method to convert Map to DTO
    private TariffDefinitionsResponse.TariffDefinitionDto convertMapToDto(Map<String, Object> map) {
        TariffDefinitionsResponse.TariffDefinitionDto dto = new TariffDefinitionsResponse.TariffDefinitionDto();
        if (map.containsKey("id")) dto.setId((String) map.get("id"));
        if (map.containsKey("product")) dto.setProduct((String) map.get("product"));
        if (map.containsKey("exportingFrom")) dto.setExportingFrom((String) map.get("exportingFrom"));
        if (map.containsKey("importingTo")) dto.setImportingTo((String) map.get("importingTo"));
        if (map.containsKey("type")) dto.setType((String) map.get("type"));
        if (map.containsKey("rate")) {
            Object rate = map.get("rate");
            if (rate instanceof Number) {
                dto.setRate(((Number) rate).doubleValue());
            } else if (rate instanceof String) {
                try {
                    dto.setRate(Double.parseDouble((String) rate));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        if (map.containsKey("effectiveDate")) dto.setEffectiveDate((String) map.get("effectiveDate"));
        if (map.containsKey("expirationDate")) dto.setExpirationDate((String) map.get("expirationDate"));
        return dto;
    }
}

