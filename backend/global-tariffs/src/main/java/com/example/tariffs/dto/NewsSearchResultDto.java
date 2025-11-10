package com.example.tariff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import com.example.tariff.entity.NewsArticleDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsSearchResultDto {
    private String status;
    private List<NewsArticleDto> articles;
    private Integer totalResults;
    private Integer pageSize;
    private Integer currentPage;
}
