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
import java.util.stream.Collectors;
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

            List<Product> products = productRepository.findByNameAndBrand(productName, brand);
            if (products.isEmpty()) {
                return new TariffResponse(false, "Product not found in database");
            }
            
            Product selectedProduct = products.get(0);

            Tariff tariff = tariffRepository.findByCountryAndPartner(importingTo, exportingFrom)
                    .orElse(null);
            
            if (tariff == null) {
                return new TariffResponse(false, "Tariff data not available for this country pair");
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
        } catch (Exception e) {
            return new TariffResponse(false, "An unexpected error occurred during calculation: " + e.getMessage());
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
        List<Product> products = productRepository.findByName(product);
        return products.stream()
                .map(p -> new BrandInfo(p.getBrand(), p.getCost(), p.getUnit()))
                .collect(Collectors.toList());
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

    public TariffDefinitionsResponse addUserTariffDefinition(TariffDefinitionsResponse.TariffDefinitionDto dto) {
        try {
            // Basic validation
            if (dto.getProduct() == null || dto.getProduct().isEmpty() ||
                dto.getExportingFrom() == null || dto.getExportingFrom().isEmpty() ||
                dto.getImportingTo() == null || dto.getImportingTo().isEmpty() ||
                dto.getType() == null || dto.getType().isEmpty()) {
                return new TariffDefinitionsResponse(false, "Missing required fields for user-defined tariff");
            }
            if (dto.getId() == null || dto.getId().isEmpty()) {
                dto.setId("user-" + System.currentTimeMillis());
            }
            userDefinedTariffs.add(dto);
            return new TariffDefinitionsResponse(true, List.of(dto));
        } catch (Exception e) {
            return new TariffDefinitionsResponse(false, "Failed to add user-defined tariff: " + e.getMessage());
        }
    }
}

