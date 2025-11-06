package com.example.integration.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.integration.entity.Product;
import com.example.integration.repository.ProductRepository;
import com.example.integration.repository.TariffRepository;
import com.example.integration.service.TariffService;

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

    public void runUpdate() {
        System.out.println("\n===== Tariff Data Update Task Started =====");
        long startTime = System.currentTimeMillis();

        // 同步查 DB（单独事务，查完释放连接）
        List<String> uniqueReporters = fetchReporters();
        List<String> uniquePartners = fetchPartners();
        List<String> uniqueHsCodes = fetchHsCodes();

        // 生成请求组合
        List<RequestCombination> requestCombinations = new ArrayList<>();
        List<String> processedKeys = new ArrayList<>();
        for (String reporterName : uniqueReporters) {
            String reporterCode = COUNTRY_NAME_TO_CODE_MAP.get(reporterName);
            if (reporterCode == null) continue;
            for (String partnerName : uniquePartners) {
                String partnerCode = COUNTRY_NAME_TO_CODE_MAP.get(partnerName);
                if (partnerCode == null || reporterCode.equals(partnerCode)) continue;
                for (String hsCode : uniqueHsCodes) {
                    String key = reporterCode + "-" + partnerCode + "-" + hsCode;
                    if (!processedKeys.contains(key)) {
                        processedKeys.add(key);
                        requestCombinations.add(new RequestCombination(reporterCode, partnerCode, hsCode));
                    }
                }
            }
        }

        // 异步发 API（2个并发）
        CountDownLatch latch = new CountDownLatch(requestCombinations.size());
        for (RequestCombination combo : requestCombinations) {
            tariffService.updateTariffsAsync(combo.reporterCode, combo.partnerCode, combo.hsCode);
            latch.countDown(); // 无需传参，直接计数
        }

        // 等待完成
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[Interrupted] Task stopped: %s%n", e.getMessage());
        }

        // 总结
        long totalTime = (System.currentTimeMillis() - startTime) / 1000;
        System.out.printf("===== Tariff Data Update Task Completed =====");
        System.out.printf("Total Requests: %d | Total Time: %ds%n", requestCombinations.size(), totalTime);
    }

    // 同步 DB 查询（单独事务，释放连接）
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

