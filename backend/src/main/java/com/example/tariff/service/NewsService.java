package com.example.tariff.service;

import com.example.tariff.dto.NewsArticleDto;
import com.example.tariff.dto.NewsSearchResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NewsService {
    @Autowired
    private NewsApiClient newsApiClient;
    
    public NewsSearchResultDto searchNews(String query, String country, String product, 
                                        Integer limit, Integer offset) {
        String searchString = buildSearchQuery(query, country, product);
        
        // Fetch from multiple sources
        List<NewsApiClient.NewsArticle> articles = new ArrayList<>();
        articles.addAll(newsApiClient.fetchFromNewsAPI(searchString));
        articles.addAll(newsApiClient.fetchFromGuardianAPI(searchString));
        
        // Deduplicate
        List<NewsApiClient.NewsArticle> deduped = deduplicateArticles(articles);
        
        // Convert to DTO
        List<NewsArticleDto> dtoArticles = deduped.stream()
            .skip(offset)
            .limit(limit)
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return new NewsSearchResultDto(
            "success",
            dtoArticles,
            deduped.size(),
            limit,
            offset / limit
        );
    }
    
    private String buildSearchQuery(String query, String country, String product) {
        StringBuilder sb = new StringBuilder(query);
        if (country != null && !country.isEmpty()) {
            sb.append(" ").append(country);
        }
        if (product != null && !product.isEmpty()) {
            sb.append(" ").append(product);
        }
        return sb.toString();
    }
    
    private List<NewsApiClient.NewsArticle> deduplicateArticles(List<NewsApiClient.NewsArticle> articles) {
        return articles.stream()
            .collect(Collectors.toMap(
                NewsApiClient.NewsArticle::getExternalId,
                a -> a,
                (existing, replacement) -> existing
            ))
            .values()
            .stream()
            .collect(Collectors.toList());
    }
    
    private NewsArticleDto convertToDTO(NewsApiClient.NewsArticle article) {
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
