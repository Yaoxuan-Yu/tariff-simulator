package com.example.integration.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.integration.entity.Product;
import com.example.integration.repository.ProductRepository;
import com.example.integration.repository.TariffRepository;

@Component
public class TariffScheduler {

    private final TariffService tariffService;
    private final TariffRepository tariffRepository;
    private final ProductRepository productRepository;

    private static final Map<String, String> COUNTRY_NAME_TO_CODE_MAP = Map.of(
            "Australia", "036", "China", "156", "India", "356",
            "Indonesia", "360", "Japan", "392", "Philippines", "458",
            "Malaysia", "608", "Singapore", "702", "Vietnam", "704", "United States", "840"
    );

    public TariffScheduler(TariffService tariffService, TariffRepository tariffRepository, ProductRepository productRepository) {
        this.tariffService = tariffService;
        this.tariffRepository = tariffRepository;
        this.productRepository = productRepository;
    }

    @Async
    public void runUpdate() {
        System.out.println("===== Tariff Data Update Task Started =====");

        List<RequestCombination> requestCombinations = buildRequestCombinations();

        for (RequestCombination combo : requestCombinations) {
            try {
                tariffService.updateTariffsAsync(combo.reporterCode, combo.partnerCode, combo.hsCode);
            } catch (Exception e) {
                System.err.printf("[Error] %s-%s-%s: %s%n", combo.reporterCode, combo.partnerCode, combo.hsCode, e.getMessage());
            }
        }

        System.out.println("===== Tariff Data Update Task Triggered =====");
    }

    private List<RequestCombination> buildRequestCombinations() {
        List<String> reporters = fetchReporters();
        List<String> partners = fetchPartners();
        List<String> hsCodes = fetchHsCodes();

        List<RequestCombination> combos = new ArrayList<>();
        for (String r : reporters) {
            for (String p : partners) {
                if (r.equals(p)) continue;
                for (String h : hsCodes) {
                    combos.add(new RequestCombination(COUNTRY_NAME_TO_CODE_MAP.get(r),
                            COUNTRY_NAME_TO_CODE_MAP.get(p), h));
                }
            }
        }
        return combos;
    }

    // Synchronous DB queries (separate transaction)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private List<String> fetchReporters() {
        return tariffRepository.findAllDistinctCountries().stream().distinct().toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private List<String> fetchPartners() {
        return tariffRepository.findAllDistinctPartners().stream().distinct().toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private List<String> fetchHsCodes() {
        return productRepository.findAll().stream()
                .filter(p -> p.getHsCode() != null && !p.getHsCode().isBlank())
                .map(Product::getHsCode)
                .distinct()
                .toList();
    }

    private static class RequestCombination {
        String reporterCode;
        String partnerCode;
        String hsCode;

        RequestCombination(String reporterCode, String partnerCode, String hsCode) {
            this.reporterCode = reporterCode;
            this.partnerCode = partnerCode;
            this.hsCode = hsCode;
        }
    }
}
