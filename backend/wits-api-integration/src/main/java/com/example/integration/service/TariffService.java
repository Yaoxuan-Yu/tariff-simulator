package com.example.integration.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.integration.dto.TariffRateDto;
import com.example.integration.entity.Tariff;
import com.example.integration.repository.TariffRepository;

@Service
public class TariffService {

    private final TariffRepository tariffRepository;
    private final WitsApiService witsApiService;

    private static final Map<String, String> COUNTRY_CODE_MAP = Map.of(
            "036", "Australia", "156", "China", "356", "India", "360", "Indonesia",
            "392", "Japan", "458", "Philippines", "608", "Malaysia", "702", "Singapore",
            "704", "Vietnam", "840", "United States"
    );

    public TariffService(TariffRepository tariffRepository, WitsApiService witsApiService) {
        this.tariffRepository = tariffRepository;
        this.witsApiService = witsApiService;
    }

    /**
     * Update existing DB row with latest tariff from WITS API.
     * Does NOT insert new rows.
     */
    @Async("tariffApiExecutor")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void updateTariffsAsync(String reporterCode, String partnerCode, String hsCode) {
    String reporterName = COUNTRY_CODE_MAP.getOrDefault(reporterCode, reporterCode);
    String partnerName = COUNTRY_CODE_MAP.getOrDefault(partnerCode, partnerCode);

    try {
        // Fetch latest tariff(s) from WITS API
        List<TariffRateDto> latestTariffs = witsApiService.fetchTariffs(reporterCode, partnerCode, hsCode);

        if (!latestTariffs.isEmpty()) {
            TariffRateDto latestTariff = latestTariffs.get(0);

            // Fetch existing DB row by reporter + partner + HS code (ignore year)
            Optional<Tariff> existingTariffOpt = tariffRepository.findByCountryAndPartnerAndHsCode(
                    reporterName, partnerName, hsCode);

            if (existingTariffOpt.isPresent()) {
                Tariff existingTariff = existingTariffOpt.get();

                // Update year + rates
                existingTariff.setYear(latestTariff.getYear());
                existingTariff.setAhsWeighted(latestTariff.getAhsWeighted());
                existingTariff.setMfnWeighted(latestTariff.getMfnWeighted());

                tariffRepository.save(existingTariff);

                System.out.printf("[Updated] Reporter=%s, Partner=%s, HS=%s, Year=%d | AHS=%.2f%% | MFN=%.2f%%%n",
                        reporterName, partnerName, hsCode, latestTariff.getYear(),
                        latestTariff.getAhsWeighted(),
                        latestTariff.getMfnWeighted());
            } else {
                // Row missing in DB, do NOT insert
                System.err.printf("[Error] No existing DB record for Reporter=%s, Partner=%s, HS=%s%n",
                        reporterName, partnerName, hsCode);
            }
        }
    } catch (Exception e) {
        String msg = e.getMessage() != null
                ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 80))
                : "Unknown error";
        System.err.printf("[Error] Reporter=%s, Partner=%s, HS=%s: %s%n",
                reporterName, partnerName, hsCode, msg);
    }
}

}
