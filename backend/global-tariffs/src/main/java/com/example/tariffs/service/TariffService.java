package com.example.tariffs.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.tariffs.dto.TariffDefinitionsResponse;
import com.example.tariffs.dto.TariffRateDto;
import com.example.tariffs.entity.Product;
import com.example.tariffs.entity.Tariff;
import com.example.tariffs.entity.TariffId;
import com.example.tariffs.repository.ProductRepository;
import com.example.tariffs.repository.TariffRepository;
import com.example.tariffs.service.api.WitsApiService;

// has main business logic for tariff definitions and tariff data management
@Service
public class TariffService {
    private final TariffRepository tariffRepository;
    private final ProductRepository productRepository;
    private final WitsApiService witsApiService;
    private final List<TariffDefinitionsResponse.TariffDefinitionDto> userDefinedTariffs = new ArrayList<>();
    
    public TariffService(TariffRepository tariffRepository, ProductRepository productRepository, 
                        WitsApiService witsApiService) {
        this.tariffRepository = tariffRepository;
        this.productRepository = productRepository;
        this.witsApiService = witsApiService;
    }

    private boolean hasFTA(String importCountry, String exportCountry) {
        List<String> ftaCountries = Arrays.asList(
            "Australia", "China", "Indonesia", "India", "Japan", 
            "Malaysia", "Philippines", "Singapore", "Vietnam"
        );
        
        return ftaCountries.contains(importCountry) && ftaCountries.contains(exportCountry);
    }

    public List<String> getAllCountries() {
        return tariffRepository.findDistinctCountries();
    }

    public List<String> getAllPartners() {
        return tariffRepository.findDistinctPartners();
    }

    public TariffDefinitionsResponse getTariffDefinitions() {
        try {
            // Use distinct product names to avoid duplicating rows per brand in definitions
            List<String> products = productRepository.findDistinctProducts();
            List<Tariff> tariffs = tariffRepository.findAll();
            
            List<TariffDefinitionsResponse.TariffDefinitionDto> definitions = new ArrayList<>();
            int id = 1;
            

            for (String productName : products) {
                for (Tariff tariff : tariffs) {

                    boolean hasFTA = hasFTA(tariff.getCountry(), tariff.getPartner());
                    String type = hasFTA ? "AHS" : "MFN";
                    double rate = hasFTA ? tariff.getAhsWeighted() : tariff.getMfnWeighted();
                    
                    if (hasFTA || tariff.getAhsWeighted().equals(tariff.getMfnWeighted())) {
                        definitions.add(new TariffDefinitionsResponse.TariffDefinitionDto(
                            String.valueOf(id++),
                            productName,
                            tariff.getPartner(),
                            tariff.getCountry(),
                            type,
                            rate,
                            "1/1/2022", // Default effective date
                            "Ongoing"   // Default expiration date
                        ));
                    }
                }
            }
            
            return new TariffDefinitionsResponse(true, definitions);
        } catch (Exception e) {
            return new TariffDefinitionsResponse(false, "Failed to retrieve tariff definitions: " + e.getMessage());
        }
    }

    // Global (database-derived) tariff definitions
    public TariffDefinitionsResponse getGlobalTariffDefinitions() {
        return getTariffDefinitions();
    }

    // User-defined tariff definitions (in-memory)
    public TariffDefinitionsResponse getUserTariffDefinitions() {
        return new TariffDefinitionsResponse(true, new ArrayList<>(userDefinedTariffs));
    }

    // Admin CRUD: Save to database (for admins only)
    public TariffDefinitionsResponse addAdminTariffDefinition(TariffDefinitionsResponse.TariffDefinitionDto dto) {
        try {
            // Basic validation
            validateTariffDefinition(dto);
            
            // Check for existing tariff in database
            String importingTo = dto.getImportingTo();
            String exportingFrom = dto.getExportingFrom();
            
            Optional<Tariff> existingTariff = tariffRepository.findByCountryAndPartner(importingTo, exportingFrom);
            
            Tariff tariff;
            if (existingTariff.isPresent()) {
                // Update existing tariff
                tariff = existingTariff.get();
            } else {
                // Create new tariff
                tariff = new Tariff();
                tariff.setCountry(importingTo);
                tariff.setPartner(exportingFrom);
            }
            
            // Set the rates based on type
            if ("AHS".equals(dto.getType())) {
                tariff.setAhsWeighted(dto.getRate());
                // Keep MFN rate if it exists, otherwise set to AHS rate
                if (tariff.getMfnWeighted() == null) {
                    tariff.setMfnWeighted(dto.getRate());
                }
            } else if ("MFN".equals(dto.getType())) {
                tariff.setMfnWeighted(dto.getRate());
                // Keep AHS rate if it exists, otherwise set to MFN rate
                if (tariff.getAhsWeighted() == null) {
                    tariff.setAhsWeighted(dto.getRate());
                }
            }
            
            // Save to database
            tariffRepository.save(tariff);
            
            // Return the saved tariff as DTO
            TariffDefinitionsResponse.TariffDefinitionDto responseDto = 
                convertToDto(tariff, dto.getProduct(), dto.getEffectiveDate(), dto.getExpirationDate(), dto.getType());
            
            return new TariffDefinitionsResponse(true, List.of(responseDto));
        } catch (com.example.tariffs.exception.ValidationException e) {
            throw e; // Re-throw validation exceptions
        } catch (Exception e) {
            throw new com.example.tariffs.exception.DataAccessException("Failed to add admin-defined tariff", e);
        }
    }

    public TariffDefinitionsResponse updateAdminTariffDefinition(String id, TariffDefinitionsResponse.TariffDefinitionDto dto) {
        try {
            // Basic validation
            validateTariffDefinition(dto);
            
            // Parse the ID to get country and partner (format: country_partner)
            String[] parts = id.split("_");
            if (parts.length != 2) {
                throw new com.example.tariffs.exception.ValidationException("Invalid tariff ID format");
            }
            
            String importingTo = parts[0];
            String exportingFrom = parts[1];
            
            Optional<Tariff> tariffOptional = tariffRepository.findByCountryAndPartner(importingTo, exportingFrom);
            
            if (!tariffOptional.isPresent()) {
                throw new com.example.tariffs.exception.NotFoundException("Tariff definition not found for country: " + importingTo + ", partner: " + exportingFrom);
            }
            
            Tariff tariff = tariffOptional.get();
            
            // Update the rates based on type
            if ("AHS".equals(dto.getType())) {
                tariff.setAhsWeighted(dto.getRate());
            } else if ("MFN".equals(dto.getType())) {
                tariff.setMfnWeighted(dto.getRate());
            }
            
            // Save to database
            tariffRepository.save(tariff);
            
            // Return the updated tariff as DTO
            TariffDefinitionsResponse.TariffDefinitionDto responseDto = 
                convertToDto(tariff, dto.getProduct(), dto.getEffectiveDate(), dto.getExpirationDate(), dto.getType());
            
            return new TariffDefinitionsResponse(true, List.of(responseDto));
        } catch (com.example.tariffs.exception.ValidationException | com.example.tariffs.exception.NotFoundException e) {
            throw e; // Re-throw validation and not found exceptions
        } catch (Exception e) {
            throw new com.example.tariffs.exception.DataAccessException("Failed to update admin-defined tariff", e);
        }
    }

    public void deleteAdminTariffDefinition(String id) {
        try {
            // Parse the ID to get country and partner
            String[] parts = id.split("_");
            if (parts.length != 2) {
                throw new com.example.tariffs.exception.ValidationException("Invalid tariff ID format");
            }
            
            String importingTo = parts[0];
            String exportingFrom = parts[1];
            
            Optional<Tariff> tariffOptional = tariffRepository.findByCountryAndPartner(importingTo, exportingFrom);
            
            if (!tariffOptional.isPresent()) {
                throw new com.example.tariffs.exception.NotFoundException("Tariff definition not found for country: " + importingTo + ", partner: " + exportingFrom);
            }
            
            // Delete from database
            tariffRepository.delete(tariffOptional.get());
        } catch (com.example.tariffs.exception.ValidationException | com.example.tariffs.exception.NotFoundException e) {
            throw e; // Re-throw not found exceptions
        } catch (Exception e) {
            throw new com.example.tariffs.exception.DataAccessException("Failed to delete admin-defined tariff", e);
        }
    }

    private void validateTariffDefinition(TariffDefinitionsResponse.TariffDefinitionDto dto) {
        if (dto.getImportingTo() == null || dto.getImportingTo().trim().isEmpty()) {
            throw new com.example.tariffs.exception.ValidationException("Importing country is required");
        }
        if (dto.getExportingFrom() == null || dto.getExportingFrom().trim().isEmpty()) {
            throw new com.example.tariffs.exception.ValidationException("Exporting country is required");
        }
        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            throw new com.example.tariffs.exception.ValidationException("Tariff type is required");
        }
        if (!dto.getType().equals("AHS") && !dto.getType().equals("MFN")) {
            throw new com.example.tariffs.exception.ValidationException("Tariff type must be either 'AHS' or 'MFN'");
        }
        if (dto.getRate() < 0) {
            throw new com.example.tariffs.exception.ValidationException("Tariff rate cannot be negative");
        }
    }

    private TariffDefinitionsResponse.TariffDefinitionDto convertToDto(
        Tariff tariff, 
        String product, 
        String effectiveDate, 
        String expirationDate,
        String type
    ) {
        String id = tariff.getCountry() + "_" + tariff.getPartner();
        double rate = "AHS".equals(type) ? tariff.getAhsWeighted() : tariff.getMfnWeighted();
        
        return new TariffDefinitionsResponse.TariffDefinitionDto(
            id,
            product,
            tariff.getPartner(),
            tariff.getCountry(),
            type,
            rate,
            effectiveDate != null ? effectiveDate : "N/A",
            expirationDate != null ? expirationDate : "Ongoing"
        );
    }

    // WITS API integration methods
    private static final Map<String, String> COUNTRY_CODE_MAP = Map.of(
            "036", "Australia",
            "156", "China",
            "356", "India",
            "360", "Indonesia",
            "392", "Japan",
            "458", "Philippines",
            "608", "Malaysia",
            "702", "Singapore",
            "704", "Vietnam",
            "840", "United States"
    );

    @Async("tariffApiExecutor")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateTariffsAsync(String reporterCode, String partnerCode, String hsCode) {
        String reporterName = COUNTRY_CODE_MAP.getOrDefault(reporterCode, reporterCode);
        String partnerName = COUNTRY_CODE_MAP.getOrDefault(partnerCode, partnerCode);

        try {
            // Call API to get latest tariff data
            List<TariffRateDto> latestTariffs = witsApiService.fetchTariffs(reporterCode, partnerCode, hsCode);
            if (!latestTariffs.isEmpty()) {
                TariffRateDto latestTariff = latestTariffs.get(0);
                System.out.printf("[Updated] Reporter=%s, Partner=%s, HS Code=%s | AHS=%.2f%% | MFN=%.2f%%%n",
                        reporterName, partnerName, hsCode,
                        latestTariff.getAhsWeighted(),
                        latestTariff.getMfnWeighted());

                // Build Tariff entity
                Tariff tariff = new Tariff();
                tariff.setCountry(reporterName);
                tariff.setPartner(partnerName);
                tariff.setAhsWeighted(latestTariff.getAhsWeighted());
                tariff.setMfnWeighted(latestTariff.getMfnWeighted());

                // Find existing or create new
                TariffId tariffId = new TariffId(reporterName, partnerName);
                tariffRepository.findById(tariffId)
                        .ifPresentOrElse(
                                existingTariff -> {
                                    existingTariff.setAhsWeighted(tariff.getAhsWeighted());
                                    existingTariff.setMfnWeighted(tariff.getMfnWeighted());
                                    tariffRepository.save(existingTariff);
                                },
                                () -> tariffRepository.save(tariff)
                        );
            }
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 80)) : "Unknown error";
            System.err.printf("[Error] Reporter=%s, Partner=%s, HS Code=%s: %s%n",
                    reporterName, partnerName, hsCode, errorMsg);
        }
    }
}

