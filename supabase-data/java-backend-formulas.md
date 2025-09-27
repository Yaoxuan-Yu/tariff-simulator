# Calculation Formulas for Java Backend Implementation

## Overview
This document outlines the exact calculation formulas used in the frontend implementation that should be replicated in your Java backend.

## Core Calculation Logic

### 1. FTA (Free Trade Agreement) Detection
```java
public boolean hasFTA(String importingCountry, String exportingCountry) {
    TariffRate tariff = tariffRateRepository.findByCountryAndPartner(importingCountry, exportingCountry);
    if (tariff == null) {
        return false;
    }
    return tariff.getAhsWeighted() < tariff.getMfnWeighted();
}
```

### 2. Effective Tariff Rate Selection
```java
public BigDecimal getEffectiveTariffRate(String importingCountry, String exportingCountry) {
    TariffRate tariff = tariffRateRepository.findByCountryAndPartner(importingCountry, exportingCountry);
    if (tariff == null) {
        throw new TariffDataNotFoundException("Tariff data not available for this country pair");
    }
    
    boolean hasFTA = tariff.getAhsWeighted().compareTo(tariff.getMfnWeighted()) < 0;
    return hasFTA ? tariff.getAhsWeighted() : tariff.getMfnWeighted();
}
```

### 3. Tariff Calculation Formula
```java
public TariffCalculationResult calculateTariff(TariffCalculationRequest request) {
    // Step 1: Get product information
    Product product = productRepository.findByNameAndBrand(request.getProductName(), request.getBrand());
    if (product == null) {
        throw new ProductNotFoundException("Product not found in database");
    }
    
    // Step 2: Get tariff data
    TariffRate tariffData = tariffRateRepository.findByCountryAndPartner(
        request.getImportingTo(), 
        request.getExportingFrom()
    );
    if (tariffData == null) {
        throw new TariffDataNotFoundException("Tariff data not available for this country pair");
    }
    
    // Step 3: Calculate costs
    BigDecimal quantity = request.getQuantity();
    BigDecimal unitCost = request.getCustomCost() != null ? 
        request.getCustomCost() : product.getCost();
    BigDecimal productCost = unitCost.multiply(quantity);
    
    // Step 4: Determine tariff rate and type
    boolean hasFTAStatus = tariffData.getAhsWeighted().compareTo(tariffData.getMfnWeighted()) < 0;
    BigDecimal tariffRate = hasFTAStatus ? 
        tariffData.getAhsWeighted() : 
        tariffData.getMfnWeighted();
    
    // Step 5: Calculate tariff amount using the exact formula from frontend
    // Formula: tariffAmount = (productCost * tariffRate) / 100
    BigDecimal tariffAmount = productCost.multiply(tariffRate).divide(new BigDecimal("100"));
    
    // Step 6: Calculate total cost
    BigDecimal totalCost = productCost.add(tariffAmount);
    
    // Step 7: Build response
    return TariffCalculationResult.builder()
        .product(product.getName())
        .brand(product.getBrand())
        .exportingFrom(request.getExportingFrom())
        .importingTo(request.getImportingTo())
        .quantity(quantity)
        .unit(product.getUnit())
        .productCost(productCost)
        .totalCost(totalCost)
        .tariffRate(tariffRate)
        .tariffType(hasFTAStatus ? "AHS (with FTA)" : "MFN (no FTA)")
        .breakdown(buildBreakdown(productCost, tariffAmount, tariffRate, hasFTAStatus))
        .build();
}
```

### 4. Breakdown Calculation
```java
private List<BreakdownItem> buildBreakdown(BigDecimal productCost, BigDecimal tariffAmount, 
                                         BigDecimal tariffRate, boolean hasFTAStatus) {
    List<BreakdownItem> breakdown = new ArrayList<>();
    
    // Product cost breakdown
    breakdown.add(BreakdownItem.builder()
        .description("Product Cost")
        .type("Base Cost")
        .rate("100%")
        .amount(productCost)
        .build());
    
    // Tariff breakdown
    breakdown.add(BreakdownItem.builder()
        .description("Import Tariff (" + (hasFTAStatus ? "AHS" : "MFN") + ")")
        .type("Tariff")
        .rate(tariffRate.setScale(2, RoundingMode.HALF_UP) + "%")
        .amount(tariffAmount)
        .build());
    
    return breakdown;
}
```

## Key Points for Java Implementation

### 1. Data Types
- Use `BigDecimal` for all monetary calculations to avoid floating-point precision issues
- Use `BigDecimal` for tariff rates and percentages
- Use `String` for country names, product names, and brands

### 2. Validation Rules
- Validate that product exists in database
- Validate that tariff data exists for the country pair
- Validate that quantity is positive
- Validate that custom cost is positive (if provided)

### 3. Error Handling
- Throw specific exceptions for missing data
- Return appropriate HTTP status codes (404 for not found, 400 for validation errors)
- Include descriptive error messages

### 4. Database Queries
```java
// Repository methods you'll need
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByNameAndBrand(String name, String brand);
    List<Product> findByName(String name);
}

public interface TariffRateRepository extends JpaRepository<TariffRate, Long> {
    Optional<TariffRate> findByCountryAndPartner(String country, String partner);
    List<String> findDistinctCountries();
    List<String> findDistinctPartners();
}
```

### 5. Service Layer Structure
```java
@Service
public class TariffCalculationService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private TariffRateRepository tariffRateRepository;
    
    public TariffCalculationResult calculateTariff(TariffCalculationRequest request) {
        // Implementation as shown above
    }
    
    public List<String> getAvailableCountries() {
        // Return unique countries from tariff_rates table
    }
    
    public List<String> getProductNames() {
        // Return unique product names from products table
    }
    
    public List<Product> getBrandsForProduct(String productName) {
        // Return all brands for a specific product
    }
}
```

### 6. Controller Layer
```java
@RestController
@RequestMapping("/api/tariff")
public class TariffController {
    
    @Autowired
    private TariffCalculationService tariffCalculationService;
    
    @PostMapping("/calculate")
    public ResponseEntity<TariffCalculationResponse> calculateTariff(
            @RequestBody @Valid TariffCalculationRequest request) {
        try {
            TariffCalculationResult result = tariffCalculationService.calculateTariff(request);
            return ResponseEntity.ok(TariffCalculationResponse.success(result));
        } catch (ProductNotFoundException | TariffDataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(TariffCalculationResponse.error(e.getMessage()));
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(TariffCalculationResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/countries")
    public ResponseEntity<List<String>> getCountries() {
        return ResponseEntity.ok(tariffCalculationService.getAvailableCountries());
    }
    
    @GetMapping("/products")
    public ResponseEntity<List<String>> getProductNames() {
        return ResponseEntity.ok(tariffCalculationService.getProductNames());
    }
    
    @GetMapping("/products/{productName}/brands")
    public ResponseEntity<List<Product>> getBrandsForProduct(@PathVariable String productName) {
        return ResponseEntity.ok(tariffCalculationService.getBrandsForProduct(productName));
    }
}
```

## Testing Considerations

### 1. Unit Tests
- Test FTA detection logic with various country pairs
- Test tariff calculation with different scenarios (with/without FTA)
- Test edge cases (zero quantities, missing data)

### 2. Integration Tests
- Test database queries
- Test complete calculation flow
- Test error handling scenarios

### 3. Sample Test Cases
```java
@Test
public void testCalculateTariffWithFTA() {
    // Test case: Australia importing from China (has FTA)
    // Expected: Use AHS rate (0.25%) instead of MFN rate (3.33%)
}

@Test
public void testCalculateTariffWithoutFTA() {
    // Test case: Australia importing from India (no FTA)
    // Expected: Use MFN rate (4.01%) since AHS = MFN
}
```

## Notes
- The frontend implementation uses the exact same logic as documented above
- All calculations should match the frontend results exactly
- Consider implementing caching for frequently accessed tariff data
- Use proper logging for debugging calculation issues
