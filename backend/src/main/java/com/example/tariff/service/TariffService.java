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

    public TariffResponse calculate(String importCountry, String exportCountry, String productName) {
        Tariff tariff = tariffRepository.findByCountryAndPartner(importCountry, exportCountry)
                .orElseThrow(() -> new RuntimeException("Tariff not found"));

        List<Product> products = productRepository.findByProduct(productName);
        double avgCost = products.stream().mapToDouble(Product::getProductCost).average().orElse(0);

        double ahsTariffAmount = avgCost * (tariff.getAhsWeighted() / 100);
        double mfnTariffAmount = avgCost * (tariff.getMfnWeighted() / 100);

        return new TariffResponse(
                importCountry,
                exportCountry,
                productName,
                avgCost,
                tariff.getAhsWeighted(),
                ahsTariffAmount,
                tariff.getMfnWeighted(),
                mfnTariffAmount
        );
    }
}
