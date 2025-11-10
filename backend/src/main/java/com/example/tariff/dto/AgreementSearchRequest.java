package com.example.tariff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgreementSearchRequest {
    private String country;
    private String agreementType;
    private Integer limit = 10;
    private Integer offset = 0;
}
