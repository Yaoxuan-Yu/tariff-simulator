package com.example.integration.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.integration.dto.TariffRateDto;
import com.example.integration.entity.Product;
import com.example.integration.entity.Tariff;
import com.example.integration.entity.TariffId;
import com.example.integration.repository.ProductRepository;
import com.example.integration.repository.TariffRepository;
import com.example.integration.service.WitsApiService;

import java.util.List;
import java.util.Map;

@Service
public class TariffService {
    private final TariffRepository tariffRepository;
    private final WitsApiService witsApiService;

    private static final Map<String, String> COUNTRY_CODE_MAP = Map.of(
            "036", "Australia",
            "156", "China",
            "356", "India",
            "360", "Indonesia",
            "392", "Japan",
            "458", "Philippines",
            "608", "Malaysia",
            "702", "Singapore",
            "704", "Vietnam",
            "840", "United States"
    );

    public TariffService(TariffRepository tariffRepository, WitsApiService witsApiService) {
        this.tariffRepository = tariffRepository;
        this.witsApiService = witsApiService;
    }

    @Async("tariffApiExecutor")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateTariffsAsync(String reporterCode, String partnerCode, String hsCode) {
        String reporterName = COUNTRY_CODE_MAP.getOrDefault(reporterCode, reporterCode);
        String partnerName = COUNTRY_CODE_MAP.getOrDefault(partnerCode, partnerCode);

        try {
            // Call API to get latest tariff data
            List<TariffRateDto> latestTariffs = witsApiService.fetchTariffs(reporterCode, partnerCode, hsCode);
            if (!latestTariffs.isEmpty()) {
                TariffRateDto latestTariff = latestTariffs.get(0);
                System.out.printf("[Updated] Reporter=%s, Partner=%s, HS Code=%s | AHS=%.2f%% | MFN=%.2f%%%n",
                        reporterName, partnerName, hsCode,
                        latestTariff.getAhsWeighted(),
                        latestTariff.getMfnWeighted());

                // Build Tariff entity
                Tariff tariff = new Tariff();
                tariff.setCountry(reporterName);
                tariff.setPartner(partnerName);
                tariff.setAhsWeighted(latestTariff.getAhsWeighted());
                tariff.setMfnWeighted(latestTariff.getMfnWeighted());

                // Find existing or create new
                TariffId tariffId = new TariffId(reporterName, partnerName);
                tariffRepository.findById(tariffId)
                        .ifPresentOrElse(
                                existingTariff -> {
                                    existingTariff.setAhsWeighted(tariff.getAhsWeighted());
                                    existingTariff.setMfnWeighted(tariff.getMfnWeighted());
                                    tariffRepository.save(existingTariff);
                                },
                                () -> tariffRepository.save(tariff)
                        );
            }
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 80)) : "Unknown error";
            System.err.printf("[Error] Reporter=%s, Partner=%s, HS Code=%s: %s%n",
                    reporterName, partnerName, hsCode, errorMsg);
        }
    }
}

