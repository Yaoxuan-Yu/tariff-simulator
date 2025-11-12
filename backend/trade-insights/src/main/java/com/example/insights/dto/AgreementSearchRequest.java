package com.example.insights.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgreementSearchRequest {
    @NotBlank
    private String country;
    private String agreementType;
    @Min(1)
    private Integer limit = 10;
    @Min(0)
    private Integer offset = 0;
}

