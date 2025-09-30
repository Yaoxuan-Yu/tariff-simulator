package com.example.tariff.controller;
import com.example.tariff.dto.TariffResponse;
import com.example.tariff.dto.BrandInfo;
import com.example.tariff.dto.TariffDefinitionsResponse;
import com.example.tariff.service.TariffService;
import com.example.tariff.service.CsvExportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")

public class TariffController {
    private final TariffService tariffService;
    private final CsvExportService csvExportService;
    public TariffController(TariffService tariffService, CsvExportService csvExportService) {
        this.tariffService = tariffService;
        this.csvExportService = csvExportService;
    }
    @GetMapping("/tariff")
    public ResponseEntity<TariffResponse> calculateTariff(
            @RequestParam String product,
            @RequestParam String brand,
            @RequestParam String exportingFrom,
            @RequestParam String importingTo,
            @RequestParam double quantity,
            @RequestParam(required = false) String customCost
    ) {
        TariffResponse response = tariffService.calculate(product, brand, exportingFrom, importingTo, quantity, customCost);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/countries")
    public ResponseEntity<List<String>> getAllCountries() {
        return ResponseEntity.ok(tariffService.getAllCountries());
    }
    @GetMapping("/partners")
    public ResponseEntity<List<String>> getAllPartners() {
        return ResponseEntity.ok(tariffService.getAllPartners());
    }
    @GetMapping("/products")
    public ResponseEntity<List<String>> getAllProducts() {
        return ResponseEntity.ok(tariffService.getAllProducts());
    }
    @GetMapping("/brands")
    public ResponseEntity<List<BrandInfo>> getBrandsByProduct(@RequestParam String product) {
        return ResponseEntity.ok(tariffService.getBrandsByProduct(product));
    }
    @GetMapping("/tariff-definitions")
    public ResponseEntity<TariffDefinitionsResponse> getTariffDefinitions() {
        TariffDefinitionsResponse response = tariffService.getTariffDefinitions();
        return ResponseEntity.ok(response);
    }

    // Separate endpoints for global vs user-defined tariff definitions
    @GetMapping("/tariff-definitions/global")
    public ResponseEntity<TariffDefinitionsResponse> getGlobalTariffDefinitions() {
        return ResponseEntity.ok(tariffService.getGlobalTariffDefinitions());
    }

    @GetMapping("/tariff-definitions/user")
    public ResponseEntity<TariffDefinitionsResponse> getUserTariffDefinitions() {
        return ResponseEntity.ok(tariffService.getUserTariffDefinitions());
    }

    @PostMapping("/tariff-definitions/user")
    public ResponseEntity<TariffDefinitionsResponse> addUserTariffDefinition(@RequestBody TariffDefinitionsResponse.TariffDefinitionDto dto) {
        return ResponseEntity.ok(tariffService.addUserTariffDefinition(dto));
    }

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
        TariffResponse tariffResponse = tariffService.calculate(product, brand, exportingFrom, importingTo, quantity, customCost);
        csvExportService.exportSingleTariffAsCSV(tariffResponse, response);
    }
}