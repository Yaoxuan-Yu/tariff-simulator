package com.example.insights.controller;

import com.example.insights.dto.TradeInsightsDto;
import com.example.insights.dto.TradeInsightsRequest;
import com.example.insights.entity.SearchType;
import com.example.insights.service.QueryLoggerService;
import com.example.insights.service.TradeInsightsService;
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

// routes trade insights aggregation requests (news + agreements)
@Slf4j
@RestController
@Validated
@Tag(name = "Trade Insights", description = "Aggregate news and agreements into a single view")
@CrossOrigin(origins = "*")
@RequestMapping("/api/trade-insights")
public class TradeInsightsController {

    private static final String ANONYMOUS_USER = "anonymous";
    private static final int MIN_QUERY_LENGTH = 1;

    private final TradeInsightsService tradeInsightsService;
    private final QueryLoggerService queryLoggerService;

    public TradeInsightsController(
        TradeInsightsService tradeInsightsService,
        QueryLoggerService queryLoggerService
    ) {
        this.tradeInsightsService = tradeInsightsService;
        this.queryLoggerService = queryLoggerService;
    }

    // POST /api/trade-insights/search -> get combined news and agreements
    @Operation(summary = "Get combined trade insights (news + agreements)")
    @PostMapping("/search")
    public ResponseEntity<TradeInsightsDto> getTradeInsights(
        @Valid @RequestBody TradeInsightsRequest request,
        Authentication authentication
    ) {
        if (request.getQuery() == null || request.getQuery().trim().length() < MIN_QUERY_LENGTH) {
            return ResponseEntity.badRequest().build();
        }

        String userId = authentication != null ? authentication.getName() : ANONYMOUS_USER;
        Map<String, String> filters = new HashMap<>();
        filters.put("query", request.getQuery());
        if (request.getCountry() != null) {
            filters.put("country", request.getCountry());
        }
        if (request.getProduct() != null) {
            filters.put("product", request.getProduct());
        }

        queryLoggerService.logSearch(userId, SearchType.COMBINED, filters);

        TradeInsightsDto response = tradeInsightsService.getTradeInsights(
            request.getQuery(),
            request.getCountry(),
            request.getProduct(),
            request.getLimit()
        );

        return ResponseEntity.ok(response);
    }
}

