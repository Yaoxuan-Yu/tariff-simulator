package com.example.tariff.controller;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.tariff.dto.BrandInfo;
import com.example.tariff.dto.CalculationHistoryDto;
import com.example.tariff.dto.TariffDefinitionsResponse;
import com.example.tariff.dto.TariffResponse;
import com.example.tariff.service.CsvExportService;
import com.example.tariff.service.ModeManager;
import com.example.tariff.service.SessionHistoryService;
import com.example.tariff.service.SessionTariffService;
import com.example.tariff.service.ExportCartService;
import com.example.tariff.config.SecurityContextUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import com.example.tariff.service.TariffService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name="Tariffs", description="A summary of all our API endpoints for tariff calculations and related data")
@RequestMapping("/api")
@CrossOrigin(origins = "*")

// this file exposes api endpoints for the calculations and other stuff
public class TariffController {
    private final TariffService tariffService;
    private final CsvExportService csvExportService;
    private final ModeManager modeManager;
    private final SessionHistoryService sessionHistoryService;
    private final SessionTariffService sessionTariffService;
    private final ExportCartService exportCartService;

    public TariffController(TariffService tariffService, CsvExportService csvExportService, 
                          ModeManager modeManager, SessionHistoryService sessionHistoryService,
                          SessionTariffService sessionTariffService, ExportCartService exportCartService) {
        this.tariffService = tariffService;
        this.csvExportService = csvExportService;
        this.modeManager = modeManager;
        this.sessionHistoryService = sessionHistoryService;
        this.sessionTariffService = sessionTariffService;
        this.exportCartService = exportCartService;
    }

    @Operation(summary = "Calculate tariff rates for importing products between countries")
    @GetMapping("/tariff")
    public ResponseEntity<TariffResponse> calculateTariff(
            @RequestParam String product,
            @RequestParam String brand,
            @RequestParam String exportingFrom,
            @RequestParam String importingTo,
            @RequestParam double quantity,
            @RequestParam(required = false) String customCost,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String userTariffId,
            HttpSession session
    ) {
        // Input validation
        if (product == null || product.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Product is required");
        }
        if (brand == null || brand.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Brand is required");
        }
        if (exportingFrom == null || exportingFrom.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Exporting country is required");
        }
        if (importingTo == null || importingTo.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Importing country is required");
        }
        if (quantity <= 0) {
            throw new com.example.tariff.exception.BadRequestException("Quantity must be greater than 0");
        }
        if (customCost != null && !customCost.trim().isEmpty()) {
            try {
                double cost = Double.parseDouble(customCost);
                if (cost < 0) {
                    throw new com.example.tariff.exception.BadRequestException("Custom cost cannot be negative");
                }
            } catch (NumberFormatException e) {
                throw new com.example.tariff.exception.BadRequestException("Invalid custom cost format");
            }
        }

        TariffResponse response;
        if (mode != null && mode.equalsIgnoreCase("user")) {
            // For simulator mode, call TariffService directly with session to use session-based tariffs
            response = tariffService.calculateWithMode(
                product,
                brand,
                exportingFrom,
                importingTo,
                quantity,
                customCost,
                "user",
                userTariffId,
                session
            );
        } else {
            // For global mode, use the mode manager
            modeManager.useGlobalMode();
            response = modeManager.calculate(
                importingTo,
                exportingFrom,
                product,
                brand,
                quantity,
                customCost
            );
        }

        // SAVE TO SESSION HISTORY
        if (response.isSuccess()) {
            try {
                sessionHistoryService.saveCalculation(session, response);
            } catch (Exception e) {
                System.err.println("Failed to save calculation to session history: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Retrieve list of all countries (exporting)")
    @GetMapping("/countries")
    public ResponseEntity<List<String>> getAllCountries() {
        return ResponseEntity.ok(tariffService.getAllCountries());
    }

    @Operation(summary = "Retrieve list of all countries (importing)")
    @GetMapping("/partners")
    public ResponseEntity<List<String>> getAllPartners() {
        return ResponseEntity.ok(tariffService.getAllPartners());
    }

    @Operation(summary = "Retrieve a list of all available products")
    @GetMapping("/products")
    public ResponseEntity<List<String>> getAllProducts() {
        return ResponseEntity.ok(tariffService.getAllProducts());
    }

    @Operation(summary = "Get available brands for a specific product")
    @GetMapping("/brands")
    public ResponseEntity<List<BrandInfo>> getBrandsByProduct(@RequestParam String product) {
        if (product == null || product.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Product is required");
        }
        return ResponseEntity.ok(tariffService.getBrandsByProduct(product));
    }

    @Operation(summary = "Retrieve all tariff definitions (both global and user-defined)")
    @GetMapping("/tariff-definitions")
    public ResponseEntity<TariffDefinitionsResponse> getTariffDefinitions() {
        TariffDefinitionsResponse response = tariffService.getTariffDefinitions();
        return ResponseEntity.ok(response);
    }

    // Separate endpoints for global vs user-defined tariff definitions
    @Operation(summary = "Retrieve only global/system tariff definitions that is currently stored in the database.")
    @GetMapping("/tariff-definitions/global")
    public ResponseEntity<TariffDefinitionsResponse> getGlobalTariffDefinitions() {
        return ResponseEntity.ok(tariffService.getGlobalTariffDefinitions());
    }

    @Operation(summary = "Retrieve only user-defined tariff definitions (session-based for simulator mode)")
    @GetMapping("/tariff-definitions/user")
    public ResponseEntity<TariffDefinitionsResponse> getUserTariffDefinitions(HttpSession session) {
        // Get session-based tariffs (for simulator mode)
        List<TariffDefinitionsResponse.TariffDefinitionDto> sessionTariffs = 
            sessionTariffService.getTariffDefinitions(session);
        
        // If admin, also include database tariffs
        if (SecurityContextUtil.isAdmin()) {
            TariffDefinitionsResponse dbResponse = tariffService.getUserTariffDefinitions();
            List<TariffDefinitionsResponse.TariffDefinitionDto> allTariffs = new java.util.ArrayList<>(sessionTariffs);
            if (dbResponse != null && dbResponse.getData() != null) {
                allTariffs.addAll(dbResponse.getData());
            }
            return ResponseEntity.ok(new TariffDefinitionsResponse(true, allTariffs));
        }
        
        // Regular users only see their session tariffs
        return ResponseEntity.ok(new TariffDefinitionsResponse(true, sessionTariffs));
    }

    @Operation(summary = "Add a new user-defined tariff definition (Admin saves to DB, regular users save to session for simulator mode)")
    @PostMapping("/tariff-definitions/user")
    public ResponseEntity<TariffDefinitionsResponse> addUserTariffDefinition(
            @RequestBody TariffDefinitionsResponse.TariffDefinitionDto dto,
            HttpSession session) {
        if (dto == null) {
            throw new com.example.tariff.exception.BadRequestException("Tariff definition data is required");
        }
        
        // If admin, save to database (permanent)
        if (SecurityContextUtil.isAdmin()) {
            return ResponseEntity.ok(tariffService.addAdminTariffDefinition(dto));
        }
        
        // Regular users save to session (for simulator mode)
        TariffDefinitionsResponse.TariffDefinitionDto saved = sessionTariffService.saveTariffDefinition(session, dto);
        return ResponseEntity.ok(new TariffDefinitionsResponse(true, List.of(saved)));
    }

    
    @Operation(summary = "Update an existing user-defined tariff definition (Admin updates DB, regular users update session)")
    @PutMapping("/tariff-definitions/user/{id}")
    public ResponseEntity<TariffDefinitionsResponse> updateUserTariffDefinition(
            @PathVariable String id,
            @RequestBody TariffDefinitionsResponse.TariffDefinitionDto dto,
            HttpSession session) {
        if (dto == null) {
            throw new com.example.tariff.exception.BadRequestException("Tariff definition data is required");
        }
        if (id == null || id.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Tariff definition ID is required");
        }
        
        // If admin, update in database
        if (SecurityContextUtil.isAdmin()) {
            return ResponseEntity.ok(tariffService.updateAdminTariffDefinition(id, dto));
        }
        
        // Regular users update in session
        TariffDefinitionsResponse.TariffDefinitionDto updated = sessionTariffService.updateTariffDefinition(session, id, dto);
        return ResponseEntity.ok(new TariffDefinitionsResponse(true, List.of(updated)));
    }

    @Operation(summary = "Delete a user-defined tariff definition (Admin deletes from DB, regular users delete from session)")
    @DeleteMapping("/tariff-definitions/user/{id}")
    public ResponseEntity<?> deleteUserTariffDefinition(@PathVariable String id, HttpSession session) {
        if (id == null || id.trim().isEmpty()) {
            throw new com.example.tariff.exception.BadRequestException("Tariff definition ID is required");
        }
        
        // If admin, delete from database
        if (SecurityContextUtil.isAdmin()) {
            tariffService.deleteAdminTariffDefinition(id);
            return ResponseEntity.ok().build();
        }
        
        // Regular users delete from session
        sessionTariffService.deleteTariffDefinition(session, id);
        return ResponseEntity.ok().build();
    }
        // added in by trisha for csv export cart 
        // ===== SESSION HISTORY ENDPOINTS =====

    @Operation(summary = "Get all calculations from session history")
    @GetMapping("/tariff/history")
    public ResponseEntity<List<CalculationHistoryDto>> getHistory(HttpSession session) {
        List<CalculationHistoryDto> history = sessionHistoryService.getAllHistory(session);
        if (history.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Clear all calculation history from session")
    @DeleteMapping("/tariff/history/clear")
    public ResponseEntity<String> clearHistory(HttpSession session) {
        sessionHistoryService.clearHistory(session);
        return ResponseEntity.ok("All calculations cleared from session");
    }

    // ===== EXPORT CART ENDPOINTS =====

    @Operation(summary = "Add calculation to export cart")
    @PostMapping("/export-cart/add/{calculationId}")
    public ResponseEntity<String> addToCart(@PathVariable String calculationId, HttpSession session) {
        exportCartService.addToCart(session, calculationId);
        return ResponseEntity.ok("Added to export cart");
    }

    @Operation(summary = "Remove calculation from export cart")
    @DeleteMapping("/export-cart/remove/{calculationId}")
    public ResponseEntity<String> removeFromCart(@PathVariable String calculationId, HttpSession session) {
        exportCartService.removeFromCart(session, calculationId);
        return ResponseEntity.ok("Removed from export cart");
    }

    @Operation(summary = "Get all items in export cart")
    @GetMapping("/export-cart")
    public ResponseEntity<List<CalculationHistoryDto>> getCart(HttpSession session) {
        List<CalculationHistoryDto> cart = exportCartService.getCart(session);
        if (cart.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(cart);
    }

    @Operation(summary = "Clear all items from export cart")
    @DeleteMapping("/export-cart/clear")
    public ResponseEntity<String> clearCart(HttpSession session) {
        exportCartService.clearCart(session);
        return ResponseEntity.ok("Export cart cleared");
    }

    @Operation(summary = "Export all items in cart as CSV")
    @GetMapping("/export-cart/export")
    public void exportCart(HttpSession session, HttpServletResponse response) {
        List<CalculationHistoryDto> cart = exportCartService.getCart(session);
        if (cart.isEmpty()) {
            throw new com.example.tariff.exception.NotFoundException("Export cart is empty");
        }
        csvExportService.exportCartAsCSV(cart, response);
    }

}