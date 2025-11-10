package com.example.tariff.controller;
import com.example.tariff.dto.TradeInsightsRequest;
import com.example.tariff.dto.TradeInsightsDto;
import com.example.tariff.service.TradeInsightsService;
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
@RequestMapping("/api/trade-insights")
@Validated
@Tag(name = "Trade Insights", description = "API endpoints for comprehensive trade insights")
@CrossOrigin(origins = "*")
public class TradeInsightsController {
   @Autowired
    private TradeInsightsService tradeInsightsService;
    
    @Autowired
    private QueryLoggerService queryLoggerService;
    
    @Operation(summary = "Get combined trade insights (news + agreements)")
    @PostMapping("/search")
    public ResponseEntity<TradeInsightsDto> getTradeInsights(
            @Valid @RequestBody TradeInsightsRequest request,
            Authentication authentication) {
        
        try {
            if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            String userId = authentication != null ? authentication.getName() : "anonymous";
            
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
        } catch (Exception e) {
            log.error("Error getting trade insights", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
