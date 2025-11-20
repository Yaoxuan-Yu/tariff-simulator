package com.example.tariffs.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tariffs.dto.TariffDefinitionsResponse;
import com.example.tariffs.entity.Tariff;
import com.example.tariffs.entity.TariffId;
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
            // Use distinct product names to avoid duplicating rows in definitions
            List<String> products = productRepository.findDistinctProducts();

            // Use a Set to track unique combinations and avoid duplicates
            // Key format: "product_country_partner_type"
            Set<String> seen = new java.util.HashSet<>();
            List<TariffDefinitionsResponse.TariffDefinitionDto> definitions = new ArrayList<>();
            int idCounter = 1;

            // Pre-load all products with their HS codes to avoid repeated queries
            Map<String, String> productHsCodeMap = new HashMap<>();
            for (String productName : products) {
                List<com.example.tariffs.entity.Product> productList = productRepository.findByName(productName);
                if (!productList.isEmpty() && productList.get(0).getHsCode() != null && !productList.get(0).getHsCode().trim().isEmpty()) {
                    productHsCodeMap.put(productName, productList.get(0).getHsCode());
                }
            }

            // For each product, query all its tariffs in one efficient query
            for (String productName : products) {
                String hsCode = productHsCodeMap.get(productName);
                if (hsCode == null || hsCode.trim().isEmpty()) {
                    continue; // Skip products without HS codes
                }

                // Query all distinct country/partner combinations for this HS code in ONE query
                // This is much more efficient than nested loops
                List<Object[]> tariffRows = tariffRepository.findDistinctCountryPartnerByHsCode(hsCode);
                
                for (Object[] row : tariffRows) {
                    // row[0] = country, row[1] = partner, row[2] = ahs_weighted, row[3] = mfn_weighted
                    String country = (String) row[0];
                    String partner = (String) row[1];
                    Double ahsWeighted = row[2] != null ? ((Number) row[2]).doubleValue() : null;
                    Double mfnWeighted = row[3] != null ? ((Number) row[3]).doubleValue() : null;
                    
                    // Skip tariffs with null rates
                    if (ahsWeighted == null && mfnWeighted == null) {
                        continue;
                    }
                    
                    boolean ftaRoute = hasFTA(country, partner);
                    String type = ftaRoute ? "AHS" : "MFN";
                    
                    // Handle null values - use 0.0 as default if rate is null
                    double rate;
                    if (ftaRoute) {
                        rate = ahsWeighted != null ? ahsWeighted : (mfnWeighted != null ? mfnWeighted : 0.0);
                    } else {
                        rate = mfnWeighted != null ? mfnWeighted : (ahsWeighted != null ? ahsWeighted : 0.0);
                    }

                    // Check if FTA route or rates are equal (with null safety)
                    boolean ratesEqual = (ahsWeighted != null && mfnWeighted != null && ahsWeighted.equals(mfnWeighted)) ||
                                       (ahsWeighted == null && mfnWeighted == null);
                    
                    if (ftaRoute || ratesEqual) {
                        // Create unique key to avoid duplicates
                        String uniqueKey = productName + "_" + country + "_" + partner + "_" + type;
                        
                        // Only add if we haven't seen this combination before
                        if (!seen.contains(uniqueKey)) {
                            seen.add(uniqueKey);
                            definitions.add(new TariffDefinitionsResponse.TariffDefinitionDto(
                                    String.valueOf(idCounter++),
                                    productName,
                                    partner,
                                    country,
                                    type,
                                    rate,
                                    "2022-01-01",
                                    "Ongoing"
                            ));
                        }
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

    // add or update tariff override (admin) - creates new entry if it doesn't exist
    @Transactional
    public TariffDefinitionsResponse addAdminTariffDefinition(TariffDefinitionsResponse.TariffDefinitionDto dto) {
        try {
            validateTariffDefinition(dto);

            String importingTo = dto.getImportingTo();
            String exportingFrom = dto.getExportingFrom();

            // Get hs_code from product name
            if (dto.getProduct() == null || dto.getProduct().trim().isEmpty()) {
                throw new com.example.tariffs.exception.ValidationException("Product is required to add tariff");
            }
            
            List<com.example.tariffs.entity.Product> products = productRepository.findByName(dto.getProduct());
            if (products.isEmpty()) {
                throw new com.example.tariffs.exception.NotFoundException(
                    "Product not found: " + dto.getProduct());
            }
            
            // Use the first product's hs_code (products with same name should have same hs_code)
            String hsCode = products.get(0).getHsCode();
            if (hsCode == null || hsCode.trim().isEmpty()) {
                throw new com.example.tariffs.exception.ValidationException(
                    "Product '" + dto.getProduct() + "' does not have an HS code");
            }

            // Determine the rates to set based on tariff type
            Double ahsWeighted;
            Double mfnWeighted;
            
            if ("AHS".equals(dto.getType())) {
                ahsWeighted = dto.getRate();
                mfnWeighted = dto.getRate(); // Set both to the same rate for new entries
            } else if ("MFN".equals(dto.getType())) {
                mfnWeighted = dto.getRate();
                ahsWeighted = dto.getRate(); // Set both to the same rate for new entries
            } else {
                throw new com.example.tariffs.exception.ValidationException("Invalid tariff type: " + dto.getType());
            }

            // Check if tariff exists for this specific product (country/partner/hs_code)
            boolean exists = tariffRepository.existsByCountryPartnerAndHsCode(importingTo, exportingFrom, hsCode);
            
            if (!exists) {
                // Create new tariff entry - use 2022 as default year to match existing data pattern
                int year = 2022;
                
                int insertedRows = tariffRepository.insertTariffRate(importingTo, exportingFrom, hsCode, year, ahsWeighted, mfnWeighted);
                if (insertedRows == 0) {
                    throw new com.example.tariffs.exception.DataAccessException("Failed to create new tariff: no rows were inserted");
                }
                
                log.info("Created new tariff entry for country: {}, partner: {}, product: {} (hs_code: {}, year: {})", 
                        importingTo, exportingFrom, dto.getProduct(), hsCode, year);
            } else {
                // Update existing tariff - preserve the other rate type if it exists
                Optional<Tariff> existingTariffOptional = tariffRepository.findByCountryPartnerAndHsCode(importingTo, exportingFrom, hsCode);
                if (existingTariffOptional.isPresent()) {
                    Tariff existingTariff = existingTariffOptional.get();
                    
                    // Preserve existing rates if they exist and are non-zero
                    if ("AHS".equals(dto.getType())) {
                        // Update AHS, preserve MFN if it exists
                        if (existingTariff.getMfnWeighted() != null && existingTariff.getMfnWeighted() != 0.0) {
                            mfnWeighted = existingTariff.getMfnWeighted();
                        }
                    } else if ("MFN".equals(dto.getType())) {
                        // Update MFN, preserve AHS if it exists
                        if (existingTariff.getAhsWeighted() != null && existingTariff.getAhsWeighted() != 0.0) {
                            ahsWeighted = existingTariff.getAhsWeighted();
                        }
                    }
                }
                
                // Update all rows for this product (country + partner + hs_code, all years)
                int updatedRows = tariffRepository.updateTariffRatesByProduct(importingTo, exportingFrom, hsCode, ahsWeighted, mfnWeighted);
                if (updatedRows == 0) {
                    throw new com.example.tariffs.exception.DataAccessException("Failed to update tariff: no rows were updated");
                }
            }

            // Fetch the tariff to return
            Optional<Tariff> savedTariffOptional = tariffRepository.findByCountryPartnerAndHsCode(importingTo, exportingFrom, hsCode);
            if (!savedTariffOptional.isPresent()) {
                throw new com.example.tariffs.exception.DataAccessException("Failed to retrieve tariff after save");
            }
            
            Tariff savedTariff = savedTariffOptional.get();

            TariffDefinitionsResponse.TariffDefinitionDto responseDto =
                    convertToDto(savedTariff, dto.getProduct(), dto.getEffectiveDate(), dto.getExpirationDate(), dto.getType());
            
            // Preserve the original ID from the DTO if it exists (for user-defined tariffs)
            if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
                responseDto.setId(dto.getId());
            }

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

    // update existing tariff override (admin) - creates if doesn't exist
    @Transactional
    public TariffDefinitionsResponse updateAdminTariffDefinition(String id, TariffDefinitionsResponse.TariffDefinitionDto dto) {
        try {
            validateTariffDefinition(dto);

            String[] parts = id.split("_");
            if (parts.length != 2) {
                throw new com.example.tariffs.exception.ValidationException("Invalid tariff ID format");
            }

            String importingTo = parts[0];
            String exportingFrom = parts[1];

            // Get hs_code from product name to update only the specific product's tariff
            if (dto.getProduct() == null || dto.getProduct().trim().isEmpty()) {
                throw new com.example.tariffs.exception.ValidationException("Product is required to update tariff");
            }
            
            List<com.example.tariffs.entity.Product> products = productRepository.findByName(dto.getProduct());
            if (products.isEmpty()) {
                throw new com.example.tariffs.exception.NotFoundException(
                    "Product not found: " + dto.getProduct());
            }
            
            // Use the first product's hs_code (products with same name should have same hs_code)
            String hsCode = products.get(0).getHsCode();
            if (hsCode == null || hsCode.trim().isEmpty()) {
                throw new com.example.tariffs.exception.ValidationException(
                    "Product '" + dto.getProduct() + "' does not have an HS code");
            }

            // Check if tariff exists for this specific product (country/partner/hs_code)
            boolean exists = tariffRepository.existsByCountryPartnerAndHsCode(importingTo, exportingFrom, hsCode);
            
            if (!exists) {
                throw new com.example.tariffs.exception.NotFoundException(
                    "Cannot update tariff: no tariff entries found for country '" + importingTo + 
                    "', partner '" + exportingFrom + "', and product '" + dto.getProduct() + "' (HS code: " + hsCode + "). " +
                    "Please ensure tariff data exists in the database for this specific product.");
            }

            // Fetch an existing tariff for this product to get current rates
            Optional<Tariff> tariffOptional = tariffRepository.findByCountryPartnerAndHsCode(importingTo, exportingFrom, hsCode);
            if (!tariffOptional.isPresent()) {
                throw new com.example.tariffs.exception.NotFoundException(
                    "Tariff not found for country: " + importingTo + ", partner: " + exportingFrom + ", product: " + dto.getProduct());
            }
            
            Tariff existingTariff = tariffOptional.get();

            // Determine the rates to set - preserve the other rate type if it exists and is non-zero
            Double ahsWeighted = existingTariff.getAhsWeighted();
            Double mfnWeighted = existingTariff.getMfnWeighted();
            
            if ("AHS".equals(dto.getType())) {
                ahsWeighted = dto.getRate();
                // Preserve MFN if it exists and is non-zero, otherwise set to same as AHS
                if (mfnWeighted == null || mfnWeighted == 0.0) {
                    mfnWeighted = dto.getRate();
                }
            } else if ("MFN".equals(dto.getType())) {
                mfnWeighted = dto.getRate();
                // Preserve AHS if it exists and is non-zero, otherwise set to same as MFN
                if (ahsWeighted == null || ahsWeighted == 0.0) {
                    ahsWeighted = dto.getRate();
                }
            } else {
                throw new com.example.tariffs.exception.ValidationException("Invalid tariff type: " + dto.getType());
            }

            // Update only rows for this specific product (country + partner + hs_code)
            // This updates all years for this product, but only this product
            int updatedRows = tariffRepository.updateTariffRatesByProduct(importingTo, exportingFrom, hsCode, ahsWeighted, mfnWeighted);
            if (updatedRows == 0) {
                throw new com.example.tariffs.exception.DataAccessException("Failed to update tariff: no rows were updated");
            }
            
            // Fetch one of the updated tariffs to return
            Optional<Tariff> savedTariffOptional = tariffRepository.findByCountryPartnerAndHsCode(importingTo, exportingFrom, hsCode);
            if (!savedTariffOptional.isPresent()) {
                throw new com.example.tariffs.exception.DataAccessException("Failed to retrieve tariff after update");
            }
            
            Tariff savedTariff = savedTariffOptional.get();

            TariffDefinitionsResponse.TariffDefinitionDto responseDto =
                    convertToDto(savedTariff, dto.getProduct(), dto.getEffectiveDate(), dto.getExpirationDate(), dto.getType());
            
            // Preserve the original ID from the DTO if it exists (for user-defined tariffs)
            if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
                responseDto.setId(dto.getId());
            }

            upsertUserTariff(responseDto);

            return new TariffDefinitionsResponse(true, List.of(responseDto));
        } catch (com.example.tariffs.exception.ValidationException e) {
            throw e;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Handle duplicate key errors - this should not happen if logic is correct, but handle gracefully
            log.error("Data integrity violation when updating tariff {}: {}", id, e.getMessage());
            throw new com.example.tariffs.exception.DataAccessException("Failed to update tariff: tariff may already exist with different values. Please refresh and try again.", e);
        } catch (Exception e) {
            log.error("Failed to update admin-defined tariff {}", id, e);
            throw new com.example.tariffs.exception.DataAccessException("Failed to update admin-defined tariff: " + e.getMessage(), e);
        }
    }

    // delete override (admin) - for modified global tariffs (in-memory)
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
    
    // delete global tariff from database (admin only) - deletes by product, country, and partner
    @Transactional
    public void deleteGlobalTariffDefinition(TariffDefinitionsResponse.TariffDefinitionDto dto) {
        try {
            if (dto.getProduct() == null || dto.getProduct().trim().isEmpty()) {
                throw new com.example.tariffs.exception.ValidationException("Product is required to delete tariff");
            }
            if (dto.getImportingTo() == null || dto.getImportingTo().trim().isEmpty()) {
                throw new com.example.tariffs.exception.ValidationException("Importing country is required");
            }
            if (dto.getExportingFrom() == null || dto.getExportingFrom().trim().isEmpty()) {
                throw new com.example.tariffs.exception.ValidationException("Exporting country is required");
            }
            
            String importingTo = dto.getImportingTo();
            String exportingFrom = dto.getExportingFrom();
            
            // Get hs_code from product name
            List<com.example.tariffs.entity.Product> products = productRepository.findByName(dto.getProduct());
            if (products.isEmpty()) {
                throw new com.example.tariffs.exception.NotFoundException(
                    "Product not found: " + dto.getProduct());
            }
            
            String hsCode = products.get(0).getHsCode();
            if (hsCode == null || hsCode.trim().isEmpty()) {
                throw new com.example.tariffs.exception.ValidationException(
                    "Product '" + dto.getProduct() + "' does not have an HS code");
            }
            
            // Check if tariff exists
            boolean exists = tariffRepository.existsByCountryPartnerAndHsCode(importingTo, exportingFrom, hsCode);
            if (!exists) {
                throw new com.example.tariffs.exception.NotFoundException(
                    "Tariff not found for country: " + importingTo + ", partner: " + exportingFrom + ", product: " + dto.getProduct());
            }
            
            // Delete all rows for this product (country + partner + hs_code, all years)
            int deletedRows = tariffRepository.deleteTariffRatesByProduct(importingTo, exportingFrom, hsCode);
            if (deletedRows == 0) {
                throw new com.example.tariffs.exception.DataAccessException("Failed to delete tariff: no rows were deleted");
            }
            
            log.info("Deleted {} tariff row(s) for country: {}, partner: {}, product: {} (hs_code: {})", 
                    deletedRows, importingTo, exportingFrom, dto.getProduct(), hsCode);
        } catch (com.example.tariffs.exception.ValidationException | com.example.tariffs.exception.NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete global tariff", e);
            throw new com.example.tariffs.exception.DataAccessException("Failed to delete global tariff: " + e.getMessage(), e);
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
        Double ahsWeighted = tariff.getAhsWeighted();
        Double mfnWeighted = tariff.getMfnWeighted();
        
        // Handle null values - use 0.0 as default if rate is null
        double rate;
        if ("AHS".equals(type)) {
            rate = ahsWeighted != null ? ahsWeighted : (mfnWeighted != null ? mfnWeighted : 0.0);
        } else {
            rate = mfnWeighted != null ? mfnWeighted : (ahsWeighted != null ? ahsWeighted : 0.0);
        }

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
    
    // add user-defined tariff (stores in-memory only, no database operations)
    public void addUserTariffDefinition(TariffDefinitionsResponse.TariffDefinitionDto dto) {
        if (dto == null) {
            throw new com.example.tariffs.exception.ValidationException("Tariff definition is required");
        }
        if (dto.getId() == null || dto.getId().trim().isEmpty()) {
            throw new com.example.tariffs.exception.ValidationException("Tariff ID is required for user-defined tariffs");
        }
        
        // Validate basic fields
        if (dto.getProduct() == null || dto.getProduct().trim().isEmpty()) {
            throw new com.example.tariffs.exception.ValidationException("Product is required");
        }
        if (dto.getImportingTo() == null || dto.getImportingTo().trim().isEmpty()) {
            throw new com.example.tariffs.exception.ValidationException("Importing country is required");
        }
        if (dto.getExportingFrom() == null || dto.getExportingFrom().trim().isEmpty()) {
            throw new com.example.tariffs.exception.ValidationException("Exporting country is required");
        }
        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            throw new com.example.tariffs.exception.ValidationException("Tariff type is required");
        }
        if (dto.getRate() < 0) {
            throw new com.example.tariffs.exception.ValidationException("Tariff rate cannot be negative");
        }
        
        // Add to in-memory list (upsert behavior - replace if exists)
        upsertUserTariff(dto);
    }
    
    // delete user-defined tariff by ID (public method for controller)
    public void deleteUserTariffDefinition(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new com.example.tariffs.exception.ValidationException("Tariff ID is required");
        }
        
        // URL decode the ID in case it was encoded
        String decodedId = id;
        try {
            decodedId = java.net.URLDecoder.decode(id, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            // If decoding fails, use original ID (already set above)
            log.warn("Failed to decode tariff ID: {}", id);
        }
        
        final String finalDecodedId = decodedId;
        boolean removed = userDefinedTariffs.removeIf(existing -> existing.getId().equals(finalDecodedId));
        if (!removed) {
            throw new com.example.tariffs.exception.NotFoundException(
                "User tariff definition not found with ID: " + finalDecodedId);
        }
    }
}


