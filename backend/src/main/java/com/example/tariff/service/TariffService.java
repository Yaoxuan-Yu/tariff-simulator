package com.example.tariff.service;

import com.example.tariff.dto.TariffResponse;
import com.example.tariff.entity.Product;
import com.example.tariff.entity.Tariff;
import com.example.tariff.repository.ProductRepository;
import com.example.tariff.repository.TariffRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TariffService {

    private final TariffRepository tariffRepository;
    private final ProductRepository productRepository;

    public TariffService(TariffRepository tariffRepository, ProductRepository productRepository) {
        this.tariffRepository = tariffRepository;
        this.productRepository = productRepository;
    }

    public TariffResponse calculate(String importCountry, String exportCountry, String hsCode, String brand) {
        // Find tariff information
        Tariff tariff = tariffRepository.findByCountryAndPartner(importCountry, exportCountry)
                .orElseThrow(() -> new RuntimeException("Tariff not found for countries: " + importCountry + " -> " + exportCountry));

        // Find product by HS code and brand
        List<Product> products = productRepository.findByHsCodeAndBrand(hsCode, brand);
        if (products.isEmpty()) {
            throw new RuntimeException("Product not found for HS Code: " + hsCode + " and Brand: " + brand);
        }

        // Use numeric product cost directly
        double productCost = products.get(0).getProductCost() == null ? 0.0 : products.get(0).getProductCost();
        
        // Calculate tariff amounts
        double ahsTariffAmount = productCost * (tariff.getAhsWeighted() / 100);
        double mfnTariffAmount = productCost * (tariff.getMfnWeighted() / 100);

        return new TariffResponse(
                importCountry,
                exportCountry,
                hsCode,
                brand,
                productCost,
                tariff.getAhsWeighted(),
                ahsTariffAmount,
                tariff.getMfnWeighted(),
                mfnTariffAmount
        );
    }

    // Removed parsing method since we now use numeric column

    // Additional methods for frontend dropdown population
    public List<String> getAllCountries() {
        return tariffRepository.findDistinctCountries();
    }

    public List<String> getAllPartners() {
        return tariffRepository.findDistinctPartners();
    }

    public List<String> getAllHsCodes() {
        return productRepository.findDistinctHsCodes();
    }

    public List<String> getBrandsByHsCode(String hsCode) {
        return productRepository.findDistinctBrandsByHsCode(hsCode);
    }
}
