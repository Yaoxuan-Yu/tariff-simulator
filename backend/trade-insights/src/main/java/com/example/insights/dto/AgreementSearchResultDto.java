package com.example.insights.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgreementSearchResultDto {
    private String status;
    private List<AgreementDto> agreements;
    private Integer totalResults;
    private Integer pageSize;
    private Integer page;
}

