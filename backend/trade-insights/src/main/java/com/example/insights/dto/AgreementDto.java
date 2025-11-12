package com.example.insights.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgreementDto {
    private String id;
    private String title;
    private String summary;
    private List<String> countries;
    private String agreementType;
    private String documentUrl;
    private String source;
    private LocalDate publishedDate;
    private LocalDate effectiveDate;
}

