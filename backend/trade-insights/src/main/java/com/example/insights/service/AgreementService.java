package com.example.insights.service;

import com.example.insights.dto.AgreementDto;
import com.example.insights.dto.AgreementSearchResultDto;
import com.example.insights.entity.Tariff;
import com.example.insights.repository.TariffRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

// service for searching and converting tariff data into trade agreement representations
@Slf4j
@Service
public class AgreementService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_OFFSET = 0;
    private static final String DEFAULT_AGREEMENT_TYPE = "Tariff Rate Agreement";
    private static final String AGREEMENT_SOURCE = "Tariff Database";
    private static final String AGREEMENT_TITLE_FORMAT = "%s - %s Tariff Agreement";
    private static final String AGREEMENT_SUMMARY_FORMAT = "Tariff rates: AHS %.2f%%, MFN %.2f%%";

    private final TariffRepository tariffRepository;

    public AgreementService(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    // search for trade agreements based on country with pagination (returns empty results if database unavailable)
    public AgreementSearchResultDto searchAgreements(
        String country,
        String agreementType,
        Integer limit,
        Integer offset
    ) {
        int resolvedLimit = limit != null && limit > 0 ? limit : DEFAULT_LIMIT;
        int resolvedOffset = offset != null && offset >= 0 ? offset : DEFAULT_OFFSET;
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
        } catch (InvalidDataAccessResourceUsageException e) {
            // tariff table doesn't exist - return empty results gracefully
            log.debug("Tariff database table not available, returning empty agreement results");
            return new AgreementSearchResultDto(
                "success",
                Collections.emptyList(),
                0,
                resolvedLimit,
                0
            );
        } catch (SQLGrammarException e) {
            // SQL grammar error (table doesn't exist) - return empty results gracefully
            log.debug("Tariff database table not available (SQL grammar error), returning empty agreement results");
            return new AgreementSearchResultDto(
                "success",
                Collections.emptyList(),
                0,
                resolvedLimit,
                0
            );
        } catch (DataAccessException e) {
            // other database errors - log and return empty results
            log.warn("Database error searching agreements: {}", e.getMessage());
            return new AgreementSearchResultDto(
                "success",
                Collections.emptyList(),
                0,
                resolvedLimit,
                0
            );
        } catch (Exception e) {
            // any other errors - log and return empty results
            log.warn("Error searching agreements: {}", e.getMessage());
            return new AgreementSearchResultDto(
                "success",
                Collections.emptyList(),
                0,
                resolvedLimit,
                0
            );
        }
    }

    // check if tariff matches the country filter
    private boolean matchesCountry(Tariff tariff, String country) {
        if (country.isEmpty()) {
            return true;
        }
        return countryEquals(tariff.getCountry(), country) || countryEquals(tariff.getPartner(), country);
    }

    // case-insensitive country name comparison
    private boolean countryEquals(String value, String target) {
        if (value == null) {
            return false;
        }
        return value.trim().equalsIgnoreCase(target);
    }

    // convert tariff entity to agreement DTO
    private AgreementDto convertToDto(Tariff tariff, String agreementType) {
        AgreementDto dto = new AgreementDto();
        dto.setId(tariff.getCountry() + "-" + tariff.getPartner());
        dto.setTitle(String.format(Locale.ENGLISH, AGREEMENT_TITLE_FORMAT, tariff.getCountry(), tariff.getPartner()));
        dto.setSummary(String.format(
            Locale.ENGLISH,
            AGREEMENT_SUMMARY_FORMAT,
            nullSafeRate(tariff.getAhsWeighted()),
            nullSafeRate(tariff.getMfnWeighted())
        ));

        List<String> countries = new ArrayList<>();
        countries.add(tariff.getCountry());
        countries.add(tariff.getPartner());
        dto.setCountries(countries);

        dto.setAgreementType(agreementType.isEmpty() ? DEFAULT_AGREEMENT_TYPE : agreementType);
        dto.setDocumentUrl(null);
        dto.setSource(AGREEMENT_SOURCE);
        dto.setPublishedDate(LocalDate.now());
        dto.setEffectiveDate(null);

        return dto;
    }

    // safely convert nullable rate to double
    private double nullSafeRate(Double value) {
        return value != null ? value : 0.0;
    }
}

