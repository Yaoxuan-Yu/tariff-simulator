package com.example.export.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.export.dto.CalculationHistoryDto;
import com.example.export.service.ExportCartService;
import com.example.export.service.CsvExportService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Export Cart", description = "API endpoints for managing export cart and CSV export")
@RequestMapping("/api/export-cart")
@CrossOrigin(origins = "*")
public class ExportCartController {
    private final ExportCartService exportCartService;
    private final CsvExportService csvExportService;

    public ExportCartController(
            ExportCartService exportCartService,
            CsvExportService csvExportService) {
        this.exportCartService = exportCartService;
        this.csvExportService = csvExportService;
    }

    @Operation(summary = "Get all items in the export cart")
    @GetMapping
    public ResponseEntity<List<CalculationHistoryDto>> getCart(HttpSession session) {
        List<CalculationHistoryDto> cart = exportCartService.getCart(session);
        if (cart == null || cart.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(cart);
    }

    @Operation(summary = "Add a calculation to the export cart")
    @PostMapping("/add/{calculationId}")
    public ResponseEntity<?> addToCart(
            @PathVariable String calculationId,
            HttpSession session) {
        if (calculationId == null || calculationId.trim().isEmpty()) {
            throw new com.example.export.exception.BadRequestException("Calculation ID is required");
        }
        try {
            exportCartService.addToCart(session, calculationId);
            return ResponseEntity.ok().build();
        } catch (com.example.export.exception.NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (com.example.export.exception.BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Remove a calculation from the export cart")
    @DeleteMapping("/remove/{calculationId}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable String calculationId,
            HttpSession session) {
        if (calculationId == null || calculationId.trim().isEmpty()) {
            throw new com.example.export.exception.BadRequestException("Calculation ID is required");
        }
        try {
            exportCartService.removeFromCart(session, calculationId);
            return ResponseEntity.ok().build();
        } catch (com.example.export.exception.NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Clear the entire export cart")
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpSession session) {
        exportCartService.clearCart(session);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Export cart as CSV file")
    @GetMapping("/export")
    public void exportCartAsCsv(HttpSession session, HttpServletResponse response) {
        List<CalculationHistoryDto> cart = exportCartService.getCart(session);
        
        if (cart == null || cart.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        try {
            csvExportService.exportToCsv(cart, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println("Failed to export cart as CSV: " + e.getMessage());
        }
    }
}
