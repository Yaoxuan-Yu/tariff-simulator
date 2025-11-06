package com.example.tariffs.service.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.tariffs.dto.TariffRateDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WitsApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WitsApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<TariffRateDto> fetchTariffs(String reporterCode, String partnerCode, String hsCode) {
        List<TariffRateDto> result = new ArrayList<>();
        String apiUrl = String.format(
                "https://wits.worldbank.org/API/V1/SDMX/V21/datasource/TRN/reporter/%s/partner/%s/product/%s/year/ALL/datatype/reported?format=JSON",
                reporterCode, partnerCode, hsCode
        );

        try {
            String apiResponse = restTemplate.getForObject(apiUrl, String.class);
            JsonNode rootNode = objectMapper.readTree(apiResponse);

            JsonNode dataSets = rootNode.path("dataSets");
            if (dataSets.isMissingNode() && !dataSets.isArray() && dataSets.size() == 0) {
                return result; // No data (not error)
            }

            JsonNode seriesNode = dataSets.get(0).path("series");
            if (seriesNode.isMissingNode() || seriesNode.isEmpty()) {
                return result; // No data (not error)
            }

            Map<Integer, TariffRateDto> yearToTariffMap = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> seriesIterator = seriesNode.fields();

            while (seriesIterator.hasNext()) {
                Map.Entry<String, JsonNode> seriesEntry = seriesIterator.next();
                JsonNode observations = seriesEntry.getValue().path("observations");
                if (observations.isMissingNode() || observations.isEmpty()) {
                    continue;
                }

                Iterator<Map.Entry<String, JsonNode>> obsIterator = observations.fields();
                while (obsIterator.hasNext()) {
                    Map.Entry<String, JsonNode> obsEntry = obsIterator.next();
                    String yearStr = obsEntry.getKey().split(":")[0];
                    int year = Integer.parseInt(yearStr);

                    JsonNode obsValues = obsEntry.getValue();
                    Double ahsRate = obsValues.get(0).asDouble();
                    Double mfnRate = obsValues.get(1).asDouble();

                    yearToTariffMap.put(year, new TariffRateDto(reporterCode, partnerCode, ahsRate, mfnRate));
                }
            }

            // Only add latest year data if exists
            if (!yearToTariffMap.isEmpty()) {
                int latestYear = Collections.max(yearToTariffMap.keySet());
                result.add(yearToTariffMap.get(latestYear));
                // Only log when data is found (avoid noise)
                System.out.printf("[API Data Found] Reporter=%s, Partner=%s, HS Code=%s | Latest Year=%d%n",
                        reporterCode, partnerCode, hsCode, latestYear);
            }

        } catch (Exception e) {
            // Only log REAL errors (e.g., JSON parse error, connection timeout)
            // String errorMsg = e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 80)) : "Unknown error";
            // System.err.printf("[API Error] Reporter=%s, Partner=%s, HS Code=%s: %s%n",
            //         reporterCode, partnerCode, hsCode, errorMsg);
            return Collections.emptyList();
        }

        return result;
    }
}

