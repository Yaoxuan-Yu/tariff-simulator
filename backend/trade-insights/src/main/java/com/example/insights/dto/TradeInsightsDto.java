package com.example.insights.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeInsightsDto {
    private String status;
    private String query;
    private String country;
    private NewsSection newsSection;
    private AgreementsSection agreementsSection;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsSection {
        private String title;
        private List<NewsArticleDto> articles;
        private Integer totalCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgreementsSection {
        private String title;
        private List<AgreementDto> agreements;
        private Integer totalCount;
    }
}

