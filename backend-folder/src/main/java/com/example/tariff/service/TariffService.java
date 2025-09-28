package com.example.tariff.service;

import com.example.tariff.dto.TariffResponse;
import com.example.tariff.entity.Product;
import com.example.tariff.entity.Tariff;
import com.example.tariff.exception.NotFoundException;
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
        
        Tariff tariff = tariffRepository.findByCountryAndPartner(importCountry, exportCountry)
                .orElseThrow(() -> new NotFoundException("Tariff not found for countries: " + importCountry + " -> " + exportCountry));

        // Find product by HS code and brand (for validation)
        List<Product> products = productRepository.findByHsCodeAndBrand(hsCode, brand);
        if (products.isEmpty()) {
            throw new NotFoundException("Product not found for HS Code: " + hsCode + " and Brand: " + brand);
        }

        // Return only tariff rates without calculations

        return new TariffResponse(
                importCountry,
                exportCountry,
                hsCode,
                brand,
                tariff.getAhsWeighted(),
                tariff.getMfnWeighted()
        );
    }

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
