package com.example.tariffs.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.tariffs.dto.TariffDefinitionsResponse;
import com.example.tariffs.entity.Tariff;
import com.example.tariffs.repository.ProductRepository;
import com.example.tariffs.repository.TariffRepository;

// business logic for tariff definitions and persistence
@Service
public class TariffService {
    private static final Logger log = LoggerFactory.getLogger(TariffService.class);
    private static final Set<String> FTA_COUNTRIES = Set.of(
            "Australia", "China", "Indonesia", "India", "Japan",
            "Malaysia", "Philippines", "Singapore", "Vietnam"
    );

    private final TariffRepository tariffRepository;
    private final ProductRepository productRepository;
    private final List<TariffDefinitionsResponse.TariffDefinitionDto> userDefinedTariffs =
            new CopyOnWriteArrayList<>();

    public TariffService(TariffRepository tariffRepository, ProductRepository productRepository) {
        this.tariffRepository = tariffRepository;
        this.productRepository = productRepository;
    }

    // determine if route is covered by FTA list
    private boolean hasFTA(String importCountry, String exportCountry) {
        return FTA_COUNTRIES.contains(importCountry) && FTA_COUNTRIES.contains(exportCountry);
    }

    // list distinct importing countries
    public List<String> getAllCountries() {
        return tariffRepository.findDistinctCountries();
    }

    // build combined tariff definitions from database records and known products
    public TariffDefinitionsResponse getTariffDefinitions() {
        try {
            List<String> products = productRepository.findDistinctProducts();
            List<Tariff> tariffs = tariffRepository.findAll();

            List<TariffDefinitionsResponse.TariffDefinitionDto> definitions = new ArrayList<>();
            int idCounter = 1;

            for (String productName : products) {
                for (Tariff tariff : tariffs) {
                    boolean ftaRoute = hasFTA(tariff.getCountry(), tariff.getPartner());
                    String type = ftaRoute ? "AHS" : "MFN";
                    double rate = ftaRoute ? tariff.getAhsWeighted() : tariff.getMfnWeighted();

                    if (ftaRoute || tariff.getAhsWeighted().equals(tariff.getMfnWeighted())) {
                        definitions.add(new TariffDefinitionsResponse.TariffDefinitionDto(
                                String.valueOf(idCounter++),
                                productName,
                                tariff.getPartner(),
                                tariff.getCountry(),
                                type,
                                rate,
                                "2022-01-01",
                                "Ongoing"
                        ));
                    }
                }
            }

            return new TariffDefinitionsResponse(true, definitions);
        } catch (Exception e) {
            log.error("Failed to build tariff definitions", e);
            return new TariffDefinitionsResponse(false, "Failed to retrieve tariff definitions: " + e.getMessage());
        }
    }

    // global definitions mirror main dataset
    public TariffDefinitionsResponse getGlobalTariffDefinitions() {
        return getTariffDefinitions();
    }

    // admin managed overrides stored in-memory for quick reference
    public TariffDefinitionsResponse getUserTariffDefinitions() {
        return new TariffDefinitionsResponse(true, new ArrayList<>(userDefinedTariffs));
    }

    // add or update tariff override (admin)
    public TariffDefinitionsResponse addAdminTariffDefinition(TariffDefinitionsResponse.TariffDefinitionDto dto) {
        try {
            validateTariffDefinition(dto);

            String importingTo = dto.getImportingTo();
            String exportingFrom = dto.getExportingFrom();

            Optional<Tariff> existingTariff = tariffRepository.findByCountryAndPartner(importingTo, exportingFrom);

            Tariff tariff = existingTariff.orElseGet(() -> {
                Tariff newTariff = new Tariff();
                newTariff.setCountry(importingTo);
                newTariff.setPartner(exportingFrom);
                newTariff.setAhsWeighted(0.0);
                newTariff.setMfnWeighted(0.0);
                return newTariff;
            });

            if ("AHS".equals(dto.getType())) {
                tariff.setAhsWeighted(dto.getRate());
                if (!existingTariff.isPresent() || tariff.getMfnWeighted() == null || tariff.getMfnWeighted() == 0.0) {
                    tariff.setMfnWeighted(dto.getRate());
                }
            } else if ("MFN".equals(dto.getType())) {
                tariff.setMfnWeighted(dto.getRate());
                if (!existingTariff.isPresent() || tariff.getAhsWeighted() == null || tariff.getAhsWeighted() == 0.0) {
                    tariff.setAhsWeighted(dto.getRate());
                }
            }

            Tariff savedTariff = tariffRepository.save(tariff);

            TariffDefinitionsResponse.TariffDefinitionDto responseDto =
                    convertToDto(savedTariff, dto.getProduct(), dto.getEffectiveDate(), dto.getExpirationDate(), dto.getType());

            upsertUserTariff(responseDto);

            return new TariffDefinitionsResponse(true, List.of(responseDto));
        } catch (com.example.tariffs.exception.ValidationException e) {
            log.warn("Validation error while adding tariff: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to add admin-defined tariff", e);
            throw new com.example.tariffs.exception.DataAccessException("Failed to add admin-defined tariff: " + e.getMessage(), e);
        }
    }

    // update existing tariff override (admin)
    public TariffDefinitionsResponse updateAdminTariffDefinition(String id, TariffDefinitionsResponse.TariffDefinitionDto dto) {
        try {
            validateTariffDefinition(dto);

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

            if ("AHS".equals(dto.getType())) {
                tariff.setAhsWeighted(dto.getRate());
            } else if ("MFN".equals(dto.getType())) {
                tariff.setMfnWeighted(dto.getRate());
            }

            tariffRepository.save(tariff);

            TariffDefinitionsResponse.TariffDefinitionDto responseDto =
                    convertToDto(tariff, dto.getProduct(), dto.getEffectiveDate(), dto.getExpirationDate(), dto.getType());

            upsertUserTariff(responseDto);

            return new TariffDefinitionsResponse(true, List.of(responseDto));
        } catch (com.example.tariffs.exception.ValidationException | com.example.tariffs.exception.NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update admin-defined tariff {}", id, e);
            throw new com.example.tariffs.exception.DataAccessException("Failed to update admin-defined tariff", e);
        }
    }

    // delete override (admin)
    public void deleteAdminTariffDefinition(String id) {
        try {
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

            tariffRepository.delete(tariffOptional.get());
            removeUserTariff(id);
        } catch (com.example.tariffs.exception.ValidationException | com.example.tariffs.exception.NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete admin-defined tariff {}", id, e);
            throw new com.example.tariffs.exception.DataAccessException("Failed to delete admin-defined tariff", e);
        }
    }

    // sanity-check input dto
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

    // convert entity + context into dto
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

    // maintain in-memory list of overrides
    private void upsertUserTariff(TariffDefinitionsResponse.TariffDefinitionDto dto) {
        userDefinedTariffs.removeIf(existing -> existing.getId().equals(dto.getId()));
        userDefinedTariffs.add(dto);
    }

    // remove override from in-memory list
    private void removeUserTariff(String id) {
        userDefinedTariffs.removeIf(existing -> existing.getId().equals(id));
    }
}
