package com.example.insights.service;

import com.example.insights.dto.AgreementDto;
import com.example.insights.dto.AgreementSearchResultDto;
import com.example.insights.entity.Tariff;
import com.example.insights.repository.TariffRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AgreementService {

    private final TariffRepository tariffRepository;

    public AgreementService(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    public AgreementSearchResultDto searchAgreements(
        String country,
        String agreementType,
        Integer limit,
        Integer offset
    ) {
        int resolvedLimit = limit != null && limit > 0 ? limit : 10;
        int resolvedOffset = offset != null && offset >= 0 ? offset : 0;
        String normalizedCountry = country == null ? "" : country.trim();
        String normalizedAgreementType = agreementType == null ? "" : agreementType.trim();

        try {
            List<Tariff> allTariffs = tariffRepository.findAll();

            List<Tariff> filtered = allTariffs.stream()
                .filter(tariff -> matchesCountry(tariff, normalizedCountry))
                .collect(Collectors.toList());

            List<AgreementDto> page = filtered.stream()
                .skip(resolvedOffset)
                .limit(resolvedLimit)
                .map(tariff -> convertToDto(tariff, normalizedAgreementType))
                .collect(Collectors.toList());

            int pageNumber = resolvedLimit > 0 ? resolvedOffset / resolvedLimit : 0;

            return new AgreementSearchResultDto(
                "success",
                page,
                filtered.size(),
                resolvedLimit,
                pageNumber
            );
        } catch (Exception e) {
            log.error("Error searching agreements", e);
            return new AgreementSearchResultDto(
                "error",
                new ArrayList<>(),
                0,
                resolvedLimit,
                0
            );
        }
    }

    private boolean matchesCountry(Tariff tariff, String country) {
        if (country.isEmpty()) {
            return true;
        }
        return countryEquals(tariff.getCountry(), country) || countryEquals(tariff.getPartner(), country);
    }

    private boolean countryEquals(String value, String target) {
        if (value == null) {
            return false;
        }
        return value.trim().equalsIgnoreCase(target);
    }

    private AgreementDto convertToDto(Tariff tariff, String agreementType) {
        AgreementDto dto = new AgreementDto();
        dto.setId(tariff.getCountry() + "-" + tariff.getPartner());
        dto.setTitle(tariff.getCountry() + " - " + tariff.getPartner() + " Tariff Agreement");
        dto.setSummary(String.format(
            Locale.ENGLISH,
            "Tariff rates: AHS %.2f%%, MFN %.2f%%",
            nullSafeRate(tariff.getAhsWeighted()),
            nullSafeRate(tariff.getMfnWeighted())
        ));

        List<String> countries = new ArrayList<>();
        countries.add(tariff.getCountry());
        countries.add(tariff.getPartner());
        dto.setCountries(countries);

        dto.setAgreementType(agreementType.isEmpty() ? "Tariff Rate Agreement" : agreementType);
        dto.setDocumentUrl(null);
        dto.setSource("Tariff Database");
        dto.setPublishedDate(LocalDate.now());
        dto.setEffectiveDate(null);

        return dto;
    }

    private double nullSafeRate(Double value) {
        return value != null ? value : 0.0;
    }
}

