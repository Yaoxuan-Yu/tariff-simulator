package com.example.tariff.controller;
import com.example.tariff.dto.TariffResponse;
import com.example.tariff.model.SessionHistory;
import com.example.tariff.model.ExportCart;
import com.example.tariff.dto.BrandInfo;
import com.example.tariff.dto.TariffDefinitionsResponse;
import com.example.tariff.service.TariffService;
import com.example.tariff.service.CsvExportService;
import com.example.tariff.service.ModeManager;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

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
    public TariffController(TariffService tariffService, CsvExportService csvExportService, ModeManager modeManager) {
        this.tariffService = tariffService;
        this.csvExportService = csvExportService;
        this.modeManager = modeManager;
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
        if (mode != null && mode.equalsIgnoreCase("user")) {
            modeManager.useSimulatorMode();
        } else {
            modeManager.useGlobalMode();
        }
        TariffResponse response = modeManager.calculate(
            importingTo,
            exportingFrom,
            product,
            brand,
            quantity,
            customCost
        );

        // Retrieve or initialize user's session history and add current calculation.
        SessionHistory sessionHistory = (SessionHistory) session.getAttribute("tariffHistory");
        if (sessionHistory == null) {
            sessionHistory = new SessionHistory();
        }

        // Add calculation and persist back to session
        sessionHistory.addCalculation(response);
        session.setAttribute("tariffHistory", sessionHistory);

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

    @Operation(summary = "Retrieve only user-defined tariff definitions")
    @GetMapping("/tariff-definitions/user")
    public ResponseEntity<TariffDefinitionsResponse> getUserTariffDefinitions() {
        return ResponseEntity.ok(tariffService.getUserTariffDefinitions());
    }

    @Operation(summary = "Add a new user-defined tariff definition")
    @PostMapping("/tariff-definitions/user")
    public ResponseEntity<TariffDefinitionsResponse> addUserTariffDefinition(@RequestBody TariffDefinitionsResponse.TariffDefinitionDto dto) {
        if (dto == null) {
            throw new com.example.tariff.exception.BadRequestException("Tariff definition data is required");
        }
        return ResponseEntity.ok(tariffService.addUserTariffDefinition(dto));
    }

    @Operation(summary = "Retrieve all session-based tariff calculations")
    @GetMapping("/tariff/history")
    public ResponseEntity<?> getHistory(HttpSession session) {
        SessionHistory sessionHistory = (SessionHistory) session.getAttribute("tariffHistory");

        if (sessionHistory == null || sessionHistory.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No tariff history found for this session.");
        }

        return ResponseEntity.ok(sessionHistory.getHistory());
    }

    @Operation(summary = "Export tariff calculation results as CSV file (feature yet to be implemented)")
    @GetMapping("/export")
    public void exportHistoryAsCSV(
            HttpSession session,
            HttpServletResponse response
    ) throws IOException {
        SessionHistory sessionHistory = (SessionHistory) session.getAttribute("tariffHistory");

        if (sessionHistory == null || sessionHistory.isEmpty()) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
            response.getWriter().write("No session history to export");
            return;
        }

        csvExportService.exportAsCSV(sessionHistory.getHistory(), response);
    }    
    

    @Operation(summary = "Clear the current user's session tariff history")
    @DeleteMapping("/tariff/history")
    public ResponseEntity<String> clearHistory(HttpSession session) {
        SessionHistory sessionHistory = (SessionHistory) session.getAttribute("tariffHistory");

        if (sessionHistory != null) {
            sessionHistory.clear();
            session.setAttribute("tariffHistory", sessionHistory);
        }

        return ResponseEntity.ok("Tariff history cleared successfully.");
    }

    @Operation(summary = "Add a tariff calculation to export cart")
    @PostMapping("/export-cart/add")
    public ResponseEntity<String> addToExportCart(
            @RequestBody TariffResponse tariffResponse,
            HttpSession session
    ) {
        ExportCart exportCart = (ExportCart) session.getAttribute("exportCart");
        if (exportCart == null) {
            exportCart = new ExportCart();
        }
        exportCart.addItem(tariffResponse);
        session.setAttribute("exportCart", exportCart);

        return ResponseEntity.ok("Calculation added to export cart.");
    }

    @Operation(summary = "View items in the export cart")
    @GetMapping("/export-cart")
    public ResponseEntity<List<TariffResponse>> viewExportCart(HttpSession session) {
        ExportCart exportCart = (ExportCart) session.getAttribute("exportCart");
        if (exportCart == null || exportCart.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(exportCart.getItems());
    }

    @Operation(summary = "Remove an item from the export cart by index")
    @DeleteMapping("/export-cart/{index}")
    public ResponseEntity<String> removeFromExportCart(@PathVariable int index, HttpSession session) {
        ExportCart exportCart = (ExportCart) session.getAttribute("exportCart");
        if (exportCart == null || exportCart.isEmpty()) {
            return ResponseEntity.badRequest().body("Export cart is empty.");
        }
        exportCart.removeItem(index);
        session.setAttribute("exportCart", exportCart);
        return ResponseEntity.ok("Item removed from export cart.");
    }

    @Operation(summary = "Export all items in the export cart as CSV")
    @GetMapping("/export-cart/export")
    public void exportCartAsCsv(HttpSession session, HttpServletResponse response) throws IOException {
        ExportCart exportCart = (ExportCart) session.getAttribute("exportCart");

        if (exportCart == null || exportCart.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            response.getWriter().write("No items in export cart to export.");
            return;
        }

        csvExportService.exportAsCSV(exportCart.getItems(), response);
    }

}