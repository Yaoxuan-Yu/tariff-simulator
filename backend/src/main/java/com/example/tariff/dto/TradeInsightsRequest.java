package com.example.tariff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeInsightsRequest {
    private String query;
    private String country;
    private String product;
    private Integer limit = 10;
}
