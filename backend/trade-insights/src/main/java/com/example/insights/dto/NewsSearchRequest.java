package com.example.insights.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsSearchRequest {
    @NotBlank
    private String query;
    private String country;
    private String product;
    @Min(1)
    private Integer limit = 10;
    @Min(0)
    private Integer offset = 0;
    private String sortBy = "relevance";
}

