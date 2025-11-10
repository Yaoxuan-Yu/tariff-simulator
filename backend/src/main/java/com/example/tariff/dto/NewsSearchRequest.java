package com.example.tariff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsSearchRequest {
    private String query;
    private String country;
    private String product;
    private Integer limit = 10;
    private Integer offset = 0;
    private String sortBy = "relevance";   
}
