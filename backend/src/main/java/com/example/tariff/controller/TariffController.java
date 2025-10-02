package com.example.tariff.controller;
import com.example.tariff.dto.TariffResponse;
import com.example.tariff.dto.BrandInfo;
import com.example.tariff.dto.TariffDefinitionsResponse;
import com.example.tariff.service.TariffService;
import com.example.tariff.service.CsvExportService;
import com.example.tariff.service.ModeManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
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
            @RequestParam(required = false) String userTariffId
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

    @Operation(summary = "Export tariff calculation results as CSV file (feature yet to be implemented)")
    @GetMapping("/export")
    public void exportTariffAsCSV(
            @RequestParam String product,
            @RequestParam String brand,
            @RequestParam String exportingFrom,
            @RequestParam String importingTo,
            @RequestParam double quantity,
            @RequestParam(required = false) String customCost,
            HttpServletResponse response
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
        TariffResponse tariffResponse = tariffService.calculate(product, brand, exportingFrom, importingTo, quantity, customCost);
        csvExportService.exportSingleTariffAsCSV(tariffResponse, response);
    }
}