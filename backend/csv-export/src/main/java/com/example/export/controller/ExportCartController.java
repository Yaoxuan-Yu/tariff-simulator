package com.example.export.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.export.service.CsvExportService;
import com.example.export.service.ExportCartService;
import com.example.session.dto.CalculationHistoryDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// rest endpoints for managing the export cart & csv download
@RestController
@Tag(name = "Export Cart", description = "API endpoints for managing export cart and CSV export")
@RequestMapping("/api/export-cart")
@CrossOrigin(origins = "*")
public class ExportCartController {
    private final ExportCartService exportCartService;
    private final CsvExportService csvExportService;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExportCartController.class);

    public ExportCartController(
            ExportCartService exportCartService,
            CsvExportService csvExportService) {
        this.exportCartService = exportCartService;
        this.csvExportService = csvExportService;
    }

    // GET /api/export-cart -> return cart contents (204 when empty)
    @Operation(summary = "Get all items in the export cart")
    @GetMapping
    public ResponseEntity<List<CalculationHistoryDto>> getCart(HttpSession session) {
        List<CalculationHistoryDto> cart = exportCartService.getCart(session);
        if (cart == null || cart.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(cart);
    }

    // POST /api/export-cart/add/{calculationId} -> move calc into export cart
    @Operation(summary = "Add a calculation to the export cart")
    @PostMapping("/add/{calculationId}")
    public ResponseEntity<?> addToCart(
            @PathVariable String calculationId,
            HttpSession session) {
        if (calculationId == null || calculationId.trim().isEmpty()) {
            throw new com.example.export.exception.BadRequestException("Calculation ID is required");
        }
        try {
            exportCartService.addToCart(calculationId, session);
            return ResponseEntity.ok().build();
        } catch (com.example.export.exception.NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (com.example.export.exception.BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // DELETE /api/export-cart/remove/{calculationId} -> remove single calc
    @Operation(summary = "Remove a calculation from the export cart")
    @DeleteMapping("/remove/{calculationId}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable String calculationId,
            HttpSession session) {
        if (calculationId == null || calculationId.trim().isEmpty()) {
            throw new com.example.export.exception.BadRequestException("Calculation ID is required");
        }
        try {
            exportCartService.removeFromCart(calculationId, session);
            return ResponseEntity.ok().build();
        } catch (com.example.export.exception.NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // DELETE /api/export-cart/clear -> clear cart
    @Operation(summary = "Clear the entire export cart")
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpSession session) {
        exportCartService.clearCart(session);
        return ResponseEntity.ok().build();
    }

    // GET /api/export-cart/export -> stream csv
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
            log.error("Failed to export cart as CSV", e);
        }
    }
}
