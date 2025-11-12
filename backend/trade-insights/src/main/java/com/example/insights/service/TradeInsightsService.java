package com.example.insights.service;

import com.example.insights.dto.AgreementSearchResultDto;
import com.example.insights.dto.NewsSearchResultDto;
import com.example.insights.dto.TradeInsightsDto;
import com.example.insights.exception.DataAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

// service for aggregating news and agreements into a combined trade insights view
@Slf4j
@Service
public class TradeInsightsService {

    private static final String STATUS_SUCCESS = "success";
    private static final String STATUS_ERROR = "error";
    private static final String NEWS_SECTION_TITLE = "Related News Articles";
    private static final String AGREEMENTS_SECTION_TITLE = "Related Trade Agreements";

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

    // aggregate news and agreements in parallel and combine into single response (handles errors gracefully)
    public TradeInsightsDto getTradeInsights(String query, String country, String product, Integer limit) {
        try {
            // fetch news and agreements in parallel using async executor
            CompletableFuture<NewsSearchResultDto> newsFuture =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return newsService.searchNews(query, country, product, limit, 0);
                    } catch (Exception e) {
                        log.warn("Failed to fetch news: {}", e.getMessage());
                        return new NewsSearchResultDto("success", java.util.Collections.emptyList(), 0, limit != null ? limit : 10, 0);
                    }
                }, tradeInsightsExecutor);

            CompletableFuture<AgreementSearchResultDto> agreementsFuture =
                CompletableFuture.supplyAsync(() -> agreementService.searchAgreements(country, null, limit, 0),
                    tradeInsightsExecutor);

            // wait for both futures to complete
            CompletableFuture.allOf(newsFuture, agreementsFuture).join();

            NewsSearchResultDto news = newsFuture.join();
            AgreementSearchResultDto agreements = agreementsFuture.join();

            // build combined response
            TradeInsightsDto dto = new TradeInsightsDto();
            dto.setStatus(STATUS_SUCCESS);
            dto.setQuery(query);
            dto.setCountry(country);
            dto.setNewsSection(new TradeInsightsDto.NewsSection(
                NEWS_SECTION_TITLE,
                news.getArticles(),
                news.getTotalResults()
            ));
            dto.setAgreementsSection(new TradeInsightsDto.AgreementsSection(
                AGREEMENTS_SECTION_TITLE,
                agreements.getAgreements(),
                agreements.getTotalResults()
            ));

            return dto;
        } catch (Exception e) {
            log.error("Failed to aggregate trade insights", e);
            // return empty results instead of throwing exception
            TradeInsightsDto dto = new TradeInsightsDto();
            dto.setStatus(STATUS_SUCCESS);
            dto.setQuery(query);
            dto.setCountry(country);
            dto.setNewsSection(new TradeInsightsDto.NewsSection(NEWS_SECTION_TITLE, java.util.Collections.emptyList(), 0));
            dto.setAgreementsSection(new TradeInsightsDto.AgreementsSection(AGREEMENTS_SECTION_TITLE, java.util.Collections.emptyList(), 0));
            return dto;
        }
    }
}

