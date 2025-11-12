package com.example.insights.service;

import com.example.insights.dto.NewsArticleDto;
import com.example.insights.dto.NewsSearchResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// service for aggregating and processing news articles from multiple sources
@Slf4j
@Service
public class NewsService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_OFFSET = 0;
    private static final double DEFAULT_RELEVANCE_SCORE = 0.95;

    private final NewsApiClient newsApiClient;

    public NewsService(NewsApiClient newsApiClient) {
        this.newsApiClient = newsApiClient;
    }

    // search for news articles across multiple sources with pagination
    public NewsSearchResultDto searchNews(String query, String country, String product,
                                          Integer limit, Integer offset) {
        String searchString = buildSearchQuery(query, country, product);
        int resolvedLimit = limit != null && limit > 0 ? limit : DEFAULT_LIMIT;
        int resolvedOffset = offset != null && offset >= 0 ? offset : DEFAULT_OFFSET;

        List<NewsApiClient.NewsArticle> articles = new ArrayList<>();
        articles.addAll(newsApiClient.fetchFromNewsAPI(searchString));
        articles.addAll(newsApiClient.fetchFromGuardianAPI(searchString));

        List<NewsApiClient.NewsArticle> deduped = deduplicateArticles(articles);

        List<NewsArticleDto> page = deduped.stream()
            .skip(resolvedOffset)
            .limit(resolvedLimit)
            .map(this::convertToDto)
            .collect(Collectors.toList());

        int pageNumber = resolvedLimit > 0 ? resolvedOffset / resolvedLimit : 0;

        return new NewsSearchResultDto(
            "success",
            page,
            deduped.size(),
            resolvedLimit,
            pageNumber
        );
    }

    // build combined search query from query, country, and product
    private String buildSearchQuery(String query, String country, String product) {
        StringBuilder sb = new StringBuilder(query == null ? "" : query.trim());
        if (country != null && !country.isBlank()) {
            sb.append(" ").append(country.trim());
        }
        if (product != null && !product.isBlank()) {
            sb.append(" ").append(product.trim());
        }
        return sb.toString().trim();
    }

    // remove duplicate articles based on external ID
    private List<NewsApiClient.NewsArticle> deduplicateArticles(List<NewsApiClient.NewsArticle> articles) {
        Map<String, NewsApiClient.NewsArticle> deduped = new LinkedHashMap<>();
        for (NewsApiClient.NewsArticle article : articles) {
            if (article.getExternalId() != null && !deduped.containsKey(article.getExternalId())) {
                deduped.put(article.getExternalId(), article);
            }
        }
        return new ArrayList<>(deduped.values());
    }

    // convert internal news article to DTO for API response
    private NewsArticleDto convertToDto(NewsApiClient.NewsArticle article) {
        NewsArticleDto dto = new NewsArticleDto();
        dto.setId(article.getExternalId());
        dto.setTitle(article.getTitle());
        dto.setSummary(article.getSummary());
        dto.setContent(article.getContent());
        dto.setSource(article.getSource());
        dto.setAuthor(article.getAuthor());
        dto.setArticleUrl(article.getArticleUrl());
        dto.setImageUrl(article.getImageUrl());
        dto.setPublishedDate(article.getPublishedDate());
        dto.setRelevanceScore(DEFAULT_RELEVANCE_SCORE);
        return dto;
    }
}

