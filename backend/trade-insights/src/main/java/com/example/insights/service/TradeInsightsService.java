package com.example.insights.service;

import com.example.insights.dto.AgreementSearchResultDto;
import com.example.insights.dto.NewsSearchResultDto;
import com.example.insights.dto.TradeInsightsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class TradeInsightsService {

    private final NewsService newsService;
    private final AgreementService agreementService;
    private final Executor tradeInsightsExecutor;

    public TradeInsightsService(
        NewsService newsService,
        AgreementService agreementService,
        @Qualifier("tradeInsightsExecutor") Executor tradeInsightsExecutor
    ) {
        this.newsService = newsService;
        this.agreementService = agreementService;
        this.tradeInsightsExecutor = tradeInsightsExecutor;
    }

    public TradeInsightsDto getTradeInsights(String query, String country, String product, Integer limit) {
        try {
            CompletableFuture<NewsSearchResultDto> newsFuture =
                CompletableFuture.supplyAsync(() -> newsService.searchNews(query, country, product, limit, 0),
                    tradeInsightsExecutor);

            CompletableFuture<AgreementSearchResultDto> agreementsFuture =
                CompletableFuture.supplyAsync(() -> agreementService.searchAgreements(country, null, limit, 0),
                    tradeInsightsExecutor);

            CompletableFuture.allOf(newsFuture, agreementsFuture).join();

            NewsSearchResultDto news = newsFuture.join();
            AgreementSearchResultDto agreements = agreementsFuture.join();

            TradeInsightsDto dto = new TradeInsightsDto();
            dto.setStatus("success");
            dto.setQuery(query);
            dto.setCountry(country);
            dto.setNewsSection(new TradeInsightsDto.NewsSection(
                "Related News Articles",
                news.getArticles(),
                news.getTotalResults()
            ));
            dto.setAgreementsSection(new TradeInsightsDto.AgreementsSection(
                "Related Trade Agreements",
                agreements.getAgreements(),
                agreements.getTotalResults()
            ));

            return dto;
        } catch (Exception e) {
            log.error("Failed to aggregate trade insights", e);
            TradeInsightsDto dto = new TradeInsightsDto();
            dto.setStatus("error");
            dto.setQuery(query);
            dto.setCountry(country);
            dto.setNewsSection(new TradeInsightsDto.NewsSection("Related News Articles", java.util.List.of(), 0));
            dto.setAgreementsSection(new TradeInsightsDto.AgreementsSection("Related Trade Agreements", java.util.List.of(), 0));
            return dto;
        }
    }
}

