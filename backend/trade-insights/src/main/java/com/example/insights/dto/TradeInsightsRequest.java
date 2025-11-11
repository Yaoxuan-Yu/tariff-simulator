package com.example.insights.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeInsightsRequest {
    @NotBlank
    private String query;
    private String country;
    private String product;
    @Min(1)
    private Integer limit = 10;
}

