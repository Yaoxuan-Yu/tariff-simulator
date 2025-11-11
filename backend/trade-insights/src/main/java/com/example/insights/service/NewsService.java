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

@Slf4j
@Service
public class NewsService {

    private final NewsApiClient newsApiClient;

    public NewsService(NewsApiClient newsApiClient) {
        this.newsApiClient = newsApiClient;
    }

    public NewsSearchResultDto searchNews(String query, String country, String product,
                                          Integer limit, Integer offset) {
        String searchString = buildSearchQuery(query, country, product);
        int resolvedLimit = limit != null && limit > 0 ? limit : 10;
        int resolvedOffset = offset != null && offset >= 0 ? offset : 0;

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

    private List<NewsApiClient.NewsArticle> deduplicateArticles(List<NewsApiClient.NewsArticle> articles) {
        Map<String, NewsApiClient.NewsArticle> deduped = new LinkedHashMap<>();
        for (NewsApiClient.NewsArticle article : articles) {
            if (article.getExternalId() != null && !deduped.containsKey(article.getExternalId())) {
                deduped.put(article.getExternalId(), article);
            }
        }
        return new ArrayList<>(deduped.values());
    }

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
        dto.setRelevanceScore(0.95);
        return dto;
    }
}

