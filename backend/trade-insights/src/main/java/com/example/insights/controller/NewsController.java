package com.example.insights.controller;

import com.example.insights.dto.NewsSearchRequest;
import com.example.insights.dto.NewsSearchResultDto;
import com.example.insights.entity.SearchType;
import com.example.insights.service.NewsService;
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

// routes news search requests to news service
@Slf4j
@RestController
@Validated
@Tag(name = "Trade News", description = "Endpoints for discovering news related to trade activity")
@CrossOrigin(origins = "*")
@RequestMapping("/api/news")
public class NewsController {

    private static final String ANONYMOUS_USER = "anonymous";
    private static final int MIN_QUERY_LENGTH = 2;

    private final NewsService newsService;
    private final QueryLoggerService queryLoggerService;

    public NewsController(NewsService newsService, QueryLoggerService queryLoggerService) {
        this.newsService = newsService;
        this.queryLoggerService = queryLoggerService;
    }

    // POST /api/news/search -> search for trade-related news articles
    @Operation(summary = "Search for trade-related news articles")
    @PostMapping("/search")
    public ResponseEntity<NewsSearchResultDto> searchNews(
        @Valid @RequestBody NewsSearchRequest request,
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

        queryLoggerService.logSearch(userId, SearchType.NEWS, filters);

        NewsSearchResultDto response = newsService.searchNews(
            request.getQuery(),
            request.getCountry(),
            request.getProduct(),
            request.getLimit(),
            request.getOffset()
        );

        return ResponseEntity.ok(response);
    }
}

