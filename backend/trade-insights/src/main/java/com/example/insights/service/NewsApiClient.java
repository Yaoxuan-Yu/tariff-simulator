package com.example.insights.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NewsApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${news.api.key:}")
    private String newsApiKey;

    @Value("${guardian.api.key:}")
    private String guardianApiKey;

    public NewsApiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<NewsArticle> fetchFromNewsAPI(String query) {
        if (newsApiKey == null || newsApiKey.isBlank()) {
            log.warn("Skipping NewsAPI fetch because news.api.key is not configured");
            return List.of();
        }

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://newsapi.org/v2/everything?q=" + encodedQuery
                + "&sortBy=relevancy&language=en&apiKey=" + newsApiKey;

            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                return List.of();
            }

            JsonNode root = objectMapper.readTree(response);
            List<NewsArticle> articles = new ArrayList<>();

            if (root.has("articles")) {
                root.get("articles").forEach(article -> {
                    NewsArticle na = new NewsArticle();
                    na.setExternalId("newsapi-" + article.path("url").asText());
                    na.setTitle(article.path("title").asText(null));
                    na.setSummary(article.path("description").asText(null));
                    na.setContent(article.path("content").asText(null));
                    na.setSource(article.path("source").path("name").asText(null));
                    na.setAuthor(article.path("author").asText(null));
                    na.setArticleUrl(article.path("url").asText(null));
                    na.setImageUrl(article.path("urlToImage").asText(null));
                    na.setPublishedDate(article.path("publishedAt").asText(null));
                    articles.add(na);
                });
            }

            return articles;
        } catch (RestClientException ex) {
            log.error("HTTP error fetching from NewsAPI: {}", ex.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("Error parsing NewsAPI response", e);
            return List.of();
        }
    }

    public List<NewsArticle> fetchFromGuardianAPI(String query) {
        if (guardianApiKey == null || guardianApiKey.isBlank()) {
            log.warn("Skipping Guardian API fetch because guardian.api.key is not configured");
            return List.of();
        }

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://content.guardianapis.com/search?q=" + encodedQuery
                + "&show-fields=trailText,body&api-key=" + guardianApiKey;

            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                return List.of();
            }

            JsonNode root = objectMapper.readTree(response);
            List<NewsArticle> articles = new ArrayList<>();

            if (root.has("response") && root.get("response").has("results")) {
                root.get("response").get("results").forEach(article -> {
                    NewsArticle na = new NewsArticle();
                    na.setExternalId("guardian-" + article.path("id").asText());
                    na.setTitle(article.path("webTitle").asText(null));
                    na.setSummary(article.path("fields").path("trailText").asText(null));
                    na.setContent(article.path("fields").path("body").asText(null));
                    na.setSource("The Guardian");
                    na.setArticleUrl(article.path("webUrl").asText(null));
                    na.setPublishedDate(article.path("webPublicationDate").asText(null));
                    articles.add(na);
                });
            }

            return articles;
        } catch (RestClientException ex) {
            log.error("HTTP error fetching from Guardian API: {}", ex.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("Error parsing Guardian API response", e);
            return List.of();
        }
    }

    public static class NewsArticle {
        private String externalId;
        private String title;
        private String summary;
        private String content;
        private String source;
        private String author;
        private String articleUrl;
        private String imageUrl;
        private String publishedDate;

        public String getExternalId() {
            return externalId;
        }

        public void setExternalId(String externalId) {
            this.externalId = externalId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getArticleUrl() {
            return articleUrl;
        }

        public void setArticleUrl(String articleUrl) {
            this.articleUrl = articleUrl;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getPublishedDate() {
            return publishedDate;
        }

        public void setPublishedDate(String publishedDate) {
            this.publishedDate = publishedDate;
        }
    }
}

