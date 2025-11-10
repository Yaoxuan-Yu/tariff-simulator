package com.example.tariff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NewsApiClient {
    @Value("${news.api.key}")
    private String newsApiKey;
    
    @Value("${guardian.api.key}")
    private String guardianApiKey;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public List<NewsArticle> fetchFromNewsAPI(String query) {
        try {
            String url = "https://newsapi.org/v2/everything?q=" + query 
                + "&sortBy=relevancy&language=en&apiKey=" + newsApiKey;
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            List<NewsArticle> articles = new ArrayList<>();
            
            if (root.has("articles")) {
                root.get("articles").forEach(article -> {
                    NewsArticle na = new NewsArticle();
                    na.setExternalId("newsapi-" + article.get("url").asText());
                    na.setTitle(article.get("title").asText());
                    na.setSummary(article.get("description").asText());
                    na.setContent(article.get("content").asText());
                    na.setSource(article.get("source").get("name").asText());
                    na.setAuthor(article.get("author").asText("Unknown"));
                    na.setArticleUrl(article.get("url").asText());
                    na.setImageUrl(article.get("urlToImage").asText(""));
                    na.setPublishedDate(article.get("publishedAt").asText());
                    articles.add(na);
                });
            }
            
            return articles;
        } catch (Exception e) {
            log.error("Error fetching from NewsAPI", e);
            return new ArrayList<>();
        }
    }
    
    public List<NewsArticle> fetchFromGuardianAPI(String query) {
        try {
            String url = "https://open-platform.theguardian.com/search?q=" + query 
                + "&api-key=" + guardianApiKey;
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            List<NewsArticle> articles = new ArrayList<>();
            
            if (root.has("response") && root.get("response").has("results")) {
                root.get("response").get("results").forEach(article -> {
                    NewsArticle na = new NewsArticle();
                    na.setExternalId("guardian-" + article.get("id").asText());
                    na.setTitle(article.get("webTitle").asText());
                    na.setSummary(article.get("webTitle").asText());
                    na.setSource("The Guardian");
                    na.setArticleUrl(article.get("webUrl").asText());
                    na.setPublishedDate(article.get("webPublicationDate").asText());
                    articles.add(na);
                });
            }
            
            return articles;
        } catch (Exception e) {
            log.error("Error fetching from Guardian API", e);
            return new ArrayList<>();
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
        
        public String getExternalId() { return externalId; }
        public void setExternalId(String externalId) { this.externalId = externalId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public String getArticleUrl() { return articleUrl; }
        public void setArticleUrl(String articleUrl) { this.articleUrl = articleUrl; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getPublishedDate() { return publishedDate; }
        public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }
    }
}
