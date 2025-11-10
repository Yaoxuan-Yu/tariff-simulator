package com.example.tariff.service;


import com.example.tariff.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class TradeInsightsService {
    @Autowired
    private NewsService newsService;
    
    @Autowired
    private AgreementService agreementService;
    
    public TradeInsightsDto getTradeInsights(String query, String country, String product, Integer limit) {
        try {
            // Parallel execution for performance
            CompletableFuture<NewsSearchResultDto> newsFuture = CompletableFuture.supplyAsync(() ->
                newsService.searchNews(query, country, product, limit, 0)
            );
            
            CompletableFuture<AgreementSearchResultDto> agreementsFuture = CompletableFuture.supplyAsync(() ->
                agreementService.searchAgreements(country, null, limit, 0)
            );
            
            CompletableFuture.allOf(newsFuture, agreementsFuture).join();
            
            NewsSearchResultDto newsResp = newsFuture.join();
            AgreementSearchResultDto agreementsResp = agreementsFuture.join();
            
            TradeInsightsDto.NewsSection newsSection = new TradeInsightsDto.NewsSection(
                "Related News Articles",
                newsResp.getArticles(),
                newsResp.getTotalResults()
            );
            
            TradeInsightsDto.AgreementsSection agreementsSection = 
                new TradeInsightsDto.AgreementsSection(
                "Related Trade Agreements",
                agreementsResp.getAgreements(),
                agreementsResp.getTotalResults()
            );
            
            TradeInsightsDto insights = new TradeInsightsDto();
            insights.setStatus("success");
            insights.setQuery(query);
            insights.setCountry(country);
            insights.setNewsSection(newsSection);
            insights.setAgreementsSection(agreementsSection);
            
            return insights;
        } catch (Exception e) {
            log.error("Error getting trade insights", e);
            throw new RuntimeException("Failed to fetch trade insights", e);
        }
    }  
}
