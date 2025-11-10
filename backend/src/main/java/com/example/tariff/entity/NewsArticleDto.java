package com.example.tariff.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleDto {
    private String id;
    private String title;
    private String summary;
    private String content;
    private String source;
    private String author;
    private String articleUrl;
    private String imageUrl;
    private String publishedDate;
    private Double relevanceScore;    
}
