package com.example.api.gateway.controller;

import com.example.api.gateway.service.RoutingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class InsightsRoutingController {

    private final RoutingService routingService;

    public InsightsRoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @PostMapping("/news/search")
    public ResponseEntity<?> searchNews(
        @RequestBody Map<String, Object> body,
        HttpServletRequest request
    ) {
        HttpEntity<?> entity = routingService.createHttpEntity(request, body);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getTradeInsightsUrl(),
            "/api/news/search",
            request.getQueryString()
        );
        return routingService.forwardRequest(targetUrl, HttpMethod.POST, entity, Object.class);
    }

    @PostMapping("/agreements/search")
    public ResponseEntity<?> searchAgreements(
        @RequestBody Map<String, Object> body,
        HttpServletRequest request
    ) {
        HttpEntity<?> entity = routingService.createHttpEntity(request, body);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getTradeInsightsUrl(),
            "/api/agreements/search",
            request.getQueryString()
        );
        return routingService.forwardRequest(targetUrl, HttpMethod.POST, entity, Object.class);
    }

    @PostMapping("/trade-insights/search")
    public ResponseEntity<?> searchTradeInsights(
        @RequestBody Map<String, Object> body,
        HttpServletRequest request
    ) {
        HttpEntity<?> entity = routingService.createHttpEntity(request, body);
        String targetUrl = routingService.buildTargetUrl(
            routingService.getTradeInsightsUrl(),
            "/api/trade-insights/search",
            request.getQueryString()
        );
        return routingService.forwardRequest(targetUrl, HttpMethod.POST, entity, Object.class);
    }
}

