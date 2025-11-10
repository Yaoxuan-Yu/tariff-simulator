package com.example.tariff.controller;

import com.example.tariff.dto.NewsSearchRequest;
import com.example.tariff.dto.NewsSearchResultDto;
import com.example.tariff.service.NewsService;
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
@RequestMapping("/api/news")
@Validated
@Tag(name = "Trade News", description = "API endpoints for searching trade-related news")
@CrossOrigin(origins = "*")
public class NewsController {
    @Autowired
    private NewsService newsService;
    
    @Autowired
    private QueryLoggerService queryLoggerService;
    
    @Operation(summary = "Search for trade-related news articles")
    @PostMapping("/search")
    public ResponseEntity<NewsSearchResultDto> searchNews(
            @Valid @RequestBody NewsSearchRequest request,
            Authentication authentication) {
        
        try {
            if (request.getQuery() == null || request.getQuery().trim().isEmpty() 
                    || request.getQuery().length() < 2) {
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
            
            queryLoggerService.logSearch(userId, SearchType.NEWS, filters);
            
            NewsSearchResultDto response = newsService.searchNews(
                request.getQuery(),
                request.getCountry(),
                request.getProduct(),
                request.getLimit(),
                request.getOffset()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching news", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
