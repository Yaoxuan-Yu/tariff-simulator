package com.example.tariff.service;

import com.example.tariff.dto.AgreementDto;
import com.example.tariff.dto.AgreementSearchResultDto;
import com.example.tariff.entity.Tariff;
import com.example.tariff.repository.TariffRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AgreementService {
    
    @Autowired
    private TariffRepository tariffRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public AgreementSearchResultDto searchAgreements(String country, String agreementType, 
                                                   Integer limit, Integer offset) {
        try {
            // Get all tariffs from database
            List<Tariff> allTariffs = tariffRepository.findAll();
            
            // Filter by country (searching in both country and partner fields)
            List<Tariff> filtered = allTariffs.stream()
                .filter(tariff -> tariff.getCountry().equalsIgnoreCase(country) || 
                                 tariff.getPartner().equalsIgnoreCase(country))
                .collect(Collectors.toList());
            
            // Convert to DTOs
            List<AgreementDto> agreements = filtered.stream()
                .skip(offset)
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return new AgreementSearchResultDto(
                "success",
                agreements,
                filtered.size(),
                limit,
                offset / limit
            );
        } catch (Exception e) {
            log.error("Error searching agreements", e);
            return new AgreementSearchResultDto(
                "error",
                new java.util.ArrayList<>(), 0, limit, 0
            );
        }
    }
    
    private AgreementDto convertToDTO(Tariff entity) {
        AgreementDto dto = new AgreementDto();
        dto.setId(entity.getCountry() + "-" + entity.getPartner());
        dto.setTitle(entity.getCountry() + " - " + entity.getPartner() + " Tariff Agreement");
        dto.setSummary("Tariff rates: AHS " + entity.getAhsWeighted() + "%, MFN " + entity.getMfnWeighted() + "%");
        
        // Set countries as a list
        List<String> countries = new ArrayList<>();
        countries.add(entity.getCountry());
        countries.add(entity.getPartner());
        dto.setCountries(countries);
        
        dto.setAgreementType("Tariff Rate Agreement");
        dto.setDocumentUrl(""); // Your tariff table doesn't have this
        dto.setSource("Tariff Database");
        dto.setPublishedDate(null); // Your tariff table doesn't have dates
        dto.setEffectiveDate(null);
        
        return dto;
    }
}