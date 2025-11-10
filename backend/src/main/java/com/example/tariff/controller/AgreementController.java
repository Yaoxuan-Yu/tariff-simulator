package com.example.tariff.controller;

import com.example.tariff.dto.AgreementSearchRequest;
import com.example.tariff.dto.AgreementSearchResultDto;
import com.example.tariff.service.AgreementService;
import com.example.tariff.service.QueryLoggerService;
import com.example.tariff.entity.SearchType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/agreements")
@Validated
@Tag(name = "Trade Agreements", description = "API endpoints for searching trade agreements")
@CrossOrigin(origins = "*")
public class AgreementController {
    @Autowired
    private AgreementService agreementService;
    
    @Autowired
    private QueryLoggerService queryLoggerService;
    
    @Operation(summary = "Search for trade agreements by country")
    @PostMapping("/search")
    public ResponseEntity<AgreementSearchResultDto> searchAgreements(
            @Valid @RequestBody AgreementSearchRequest request,
            Authentication authentication) {
        
        try {
            if (request.getCountry() == null || request.getCountry().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            String userId = authentication != null ? authentication.getName() : "anonymous";
            
            Map<String, String> filters = new HashMap<>();
            filters.put("country", request.getCountry());
            if (request.getAgreementType() != null) {
                filters.put("agreementType", request.getAgreementType());
            }
            
            queryLoggerService.logSearch(userId, SearchType.AGREEMENTS, filters);
            
            AgreementSearchResultDto response = agreementService.searchAgreements(
                request.getCountry(),
                request.getAgreementType(),
                request.getLimit(),
                request.getOffset()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching agreements", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
