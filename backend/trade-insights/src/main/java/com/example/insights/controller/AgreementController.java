package com.example.insights.controller;

import com.example.insights.dto.AgreementSearchRequest;
import com.example.insights.dto.AgreementSearchResultDto;
import com.example.insights.entity.SearchType;
import com.example.insights.service.AgreementService;
import com.example.insights.service.QueryLoggerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@Validated
@Tag(name = "Trade Agreements", description = "Endpoints for searching trade agreements by country")
@CrossOrigin(origins = "*")
@RequestMapping("/api/agreements")
public class AgreementController {

    private final AgreementService agreementService;
    private final QueryLoggerService queryLoggerService;

    public AgreementController(AgreementService agreementService, QueryLoggerService queryLoggerService) {
        this.agreementService = agreementService;
        this.queryLoggerService = queryLoggerService;
    }

    @Operation(summary = "Search for trade agreements by country")
    @PostMapping("/search")
    public ResponseEntity<AgreementSearchResultDto> searchAgreements(
        @Valid @RequestBody AgreementSearchRequest request,
        Authentication authentication
    ) {
        if (request.getCountry() == null || request.getCountry().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
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
            log.error("Failed to search agreements", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
