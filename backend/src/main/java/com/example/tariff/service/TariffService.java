package com.example.tariff.service;
import com.example.tariff.dto.TariffResponse;
import com.example.tariff.dto.BrandInfo;
import com.example.tariff.dto.TariffDefinitionsResponse;
import com.example.tariff.entity.Product;
import com.example.tariff.entity.Tariff;
import com.example.tariff.repository.ProductRepository;
import com.example.tariff.repository.TariffRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// has main business logic including tariff calculations, data retrieval and fta logic (ahs vs mhn)
@Service
public class TariffService {
    private final TariffRepository tariffRepository;
    private final ProductRepository productRepository;
    private final List<TariffDefinitionsResponse.TariffDefinitionDto> userDefinedTariffs = new ArrayList<>();
    
    public TariffService(TariffRepository tariffRepository, ProductRepository productRepository) {
        this.tariffRepository = tariffRepository;
        this.productRepository = productRepository;
    }


    public TariffResponse calculateWithMode(
        String productName,
        String brand,
        String exportingFrom,
        String importingTo,
        double quantity,
        String customCost,
        String mode,
        String userTariffId
    ) {
        if (mode != null && mode.equalsIgnoreCase("user")) {
            TariffDefinitionsResponse.TariffDefinitionDto selected = findMatchingUserTariff(
                userTariffId, productName, exportingFrom, importingTo
            );
            if (selected == null) {
                return new TariffResponse(false, "Selected user-defined tariff not found or not applicable");
            }

            try {
                List<Product> products = productRepository.findByNameAndBrand(productName, brand);
                if (products.isEmpty()) {
                    return new TariffResponse(false, "Product not found in database");
                }

                Product selectedProduct = products.get(0);
                double unitCost = customCost != null && !customCost.isEmpty() ?
                    Double.parseDouble(customCost) : selectedProduct.getCost();
                double productCost = unitCost * quantity;

                double tariffRate = selected.getRate();
                double tariffAmount = (productCost * tariffRate) / 100;
                double totalCost = productCost + tariffAmount;

                List<TariffResponse.BreakdownItem> breakdown = new ArrayList<>();
                breakdown.add(new TariffResponse.BreakdownItem(
                    "Product Cost", "Base Cost", "100%", productCost));
                breakdown.add(new TariffResponse.BreakdownItem(
                    "Import Tariff (" + selected.getType() + ")",
                    "Tariff",
                    String.format("%.2f%%", tariffRate),
                    tariffAmount));

                TariffResponse.TariffCalculationData data = new TariffResponse.TariffCalculationData(
                    selectedProduct.getName(),
                    selectedProduct.getBrand(),
                    exportingFrom,
                    importingTo,
                    quantity,
                    selectedProduct.getUnit(),
                    productCost,
                    totalCost,
                    tariffRate,
                    selected.getType() + " (user-defined)",
                    breakdown
                );

                return new TariffResponse(true, data);
            } catch (Exception e) {
                return new TariffResponse(false, "An unexpected error occurred during calculation: " + e.getMessage());
            }
        }

        // default: global
        return calculate(productName, brand, exportingFrom, importingTo, quantity, customCost);
    }

    private TariffDefinitionsResponse.TariffDefinitionDto findMatchingUserTariff(
        String userTariffId,
        String product,
        String exportingFrom,
        String importingTo
    ) {
        // If an explicit ID is provided, prefer exact match
        if (userTariffId != null && !userTariffId.isEmpty()) {
            for (TariffDefinitionsResponse.TariffDefinitionDto dto : userDefinedTariffs) {
                if (userTariffId.equals(dto.getId())
                    && product.equals(dto.getProduct())
                    && exportingFrom.equals(dto.getExportingFrom())
                    && importingTo.equals(dto.getImportingTo())) {
                    return dto;
                }
            }
            return null;
        }
        // Otherwise, pick the first applicable user-defined tariff for the route and product
        for (TariffDefinitionsResponse.TariffDefinitionDto dto : userDefinedTariffs) {
            if (product.equals(dto.getProduct())
                && exportingFrom.equals(dto.getExportingFrom())
                && importingTo.equals(dto.getImportingTo())) {
                return dto;
            }
        }
        return null;
    }
    public TariffResponse calculate(String productName, String brand, String exportingFrom, String importingTo, 
                                   double quantity, String customCost) {
        try {
            // Validate inputs
            if (productName == null || productName.trim().isEmpty()) {
                throw new com.example.tariff.exception.ValidationException("Product name is required");
            }
            if (brand == null || brand.trim().isEmpty()) {
                throw new com.example.tariff.exception.ValidationException("Brand is required");
            }
            if (exportingFrom == null || exportingFrom.trim().isEmpty()) {
                throw new com.example.tariff.exception.ValidationException("Exporting country is required");
            }
            if (importingTo == null || importingTo.trim().isEmpty()) {
                throw new com.example.tariff.exception.ValidationException("Importing country is required");
            }
            if (quantity <= 0) {
                throw new com.example.tariff.exception.ValidationException("Quantity must be greater than 0");
            }

            List<Product> products = productRepository.findByNameAndBrand(productName, brand);
            if (products.isEmpty()) {
                throw new com.example.tariff.exception.NotFoundException("Product not found: " + productName + " - " + brand);
            }
            
            Product selectedProduct = products.get(0);

            Tariff tariff = tariffRepository.findByCountryAndPartner(importingTo, exportingFrom)
                    .orElse(null);
            
            if (tariff == null) {
                throw new com.example.tariff.exception.NotFoundException("Tariff data not available for " + exportingFrom + " to " + importingTo);
            }

            double unitCost = customCost != null && !customCost.isEmpty() ? 
                Double.parseDouble(customCost) : selectedProduct.getCost();
            double productCost = unitCost * quantity;
            

            boolean hasFTAStatus = hasFTA(importingTo, exportingFrom);
            double tariffRate = hasFTAStatus ? tariff.getAhsWeighted() : tariff.getMfnWeighted();

            double tariffAmount = (productCost * tariffRate) / 100;
            double totalCost = productCost + tariffAmount;

            List<TariffResponse.BreakdownItem> breakdown = new ArrayList<>();
            breakdown.add(new TariffResponse.BreakdownItem(
                "Product Cost", "Base Cost", "100%", productCost));
            breakdown.add(new TariffResponse.BreakdownItem(
                "Import Tariff (" + (hasFTAStatus ? "AHS" : "MFN") + ")", 
                "Tariff", 
                String.format("%.2f%%", tariffRate), 
                tariffAmount));
            // Create TariffCalculationData
            TariffResponse.TariffCalculationData data = new TariffResponse.TariffCalculationData(
                selectedProduct.getName(),
                selectedProduct.getBrand(),
                exportingFrom,
                importingTo,
                quantity,
                selectedProduct.getUnit(),
                productCost,
                totalCost,
                tariffRate,
                hasFTAStatus ? "AHS (with FTA)" : "MFN (no FTA)",
                breakdown
            );
            
            return new TariffResponse(true, data);
        } catch (com.example.tariff.exception.ValidationException | com.example.tariff.exception.NotFoundException e) {
            throw e; // Re-throw validation and not found exceptions
        } catch (NumberFormatException e) {
            throw new com.example.tariff.exception.ValidationException("Invalid custom cost format: " + customCost);
        } catch (Exception e) {
            throw new com.example.tariff.exception.DataAccessException("Database error during tariff calculation", e);
        }
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
    public List<String> getAllProducts() {
        return productRepository.findDistinctProducts();
    }
    public List<BrandInfo> getBrandsByProduct(String product) {
        if (product == null || product.trim().isEmpty()) {
            throw new com.example.tariff.exception.ValidationException("Product name cannot be null or empty");
        }
        
        try {
            List<Product> products = productRepository.findByName(product);
            if (products.isEmpty()) {
                throw new com.example.tariff.exception.NotFoundException("No products found for: " + product);
            }
            
            return products.stream()
                    .map(p -> new BrandInfo(p.getBrand(), p.getCost(), p.getUnit()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            if (e instanceof com.example.tariff.exception.NotFoundException) {
                throw e;
            }
            throw new com.example.tariff.exception.DataAccessException("Failed to retrieve brands for product: " + product, e);
        }
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
        } catch (com.example.tariff.exception.ValidationException e) {
            throw e; // Re-throw validation exceptions
        } catch (Exception e) {
            throw new com.example.tariff.exception.DataAccessException("Failed to add admin-defined tariff", e);
        }
    }

    public TariffDefinitionsResponse updateAdminTariffDefinition(String id, TariffDefinitionsResponse.TariffDefinitionDto dto) {
        try {
            // Basic validation
            validateTariffDefinition(dto);
            
            // Parse the ID to get country and partner (format: country_partner)
            String[] parts = id.split("_");
            if (parts.length != 2) {
                throw new com.example.tariff.exception.ValidationException("Invalid tariff ID format");
            }
            
            String importingTo = parts[0];
            String exportingFrom = parts[1];
            
            Optional<Tariff> tariffOptional = tariffRepository.findByCountryAndPartner(importingTo, exportingFrom);
            
            if (!tariffOptional.isPresent()) {
                throw new com.example.tariff.exception.NotFoundException("Tariff definition not found for country: " + importingTo + ", partner: " + exportingFrom);
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
        } catch (com.example.tariff.exception.ValidationException | com.example.tariff.exception.NotFoundException e) {
            throw e; // Re-throw validation and not found exceptions
        } catch (Exception e) {
            throw new com.example.tariff.exception.DataAccessException("Failed to update admin-defined tariff", e);
        }
    }

    public void deleteAdminTariffDefinition(String id) {
        try {
            // Parse the ID to get country and partner
            String[] parts = id.split("_");
            if (parts.length != 2) {
                throw new com.example.tariff.exception.ValidationException("Invalid tariff ID format");
            }
            
            String importingTo = parts[0];
            String exportingFrom = parts[1];
            
            Optional<Tariff> tariffOptional = tariffRepository.findByCountryAndPartner(importingTo, exportingFrom);
            
            if (!tariffOptional.isPresent()) {
                throw new com.example.tariff.exception.NotFoundException("Tariff definition not found for country: " + importingTo + ", partner: " + exportingFrom);
            }
            
            // Delete from database
            tariffRepository.delete(tariffOptional.get());
        } catch (com.example.tariff.exception.ValidationException | com.example.tariff.exception.NotFoundException e) {
            throw e; // Re-throw not found exceptions
        } catch (Exception e) {
            throw new com.example.tariff.exception.DataAccessException("Failed to delete admin-defined tariff", e);
        }
    }

    private void validateTariffDefinition(TariffDefinitionsResponse.TariffDefinitionDto dto) {
        if (dto.getImportingTo() == null || dto.getImportingTo().trim().isEmpty()) {
            throw new com.example.tariff.exception.ValidationException("Importing country is required");
        }
        if (dto.getExportingFrom() == null || dto.getExportingFrom().trim().isEmpty()) {
            throw new com.example.tariff.exception.ValidationException("Exporting country is required");
        }
        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            throw new com.example.tariff.exception.ValidationException("Tariff type is required");
        }
        if (!dto.getType().equals("AHS") && !dto.getType().equals("MFN")) {
            throw new com.example.tariff.exception.ValidationException("Tariff type must be either 'AHS' or 'MFN'");
        }
        if (dto.getRate() < 0) {
            throw new com.example.tariff.exception.ValidationException("Tariff rate cannot be negative");
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
}

