package com.example.integration.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.example.integration.dto.TariffRateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// service for fetching tariff data from WITS API
@Service
public class WitsApiService {

    private static final String WITS_API_BASE_URL = "https://wits.worldbank.org/API/V1/SDMX/V21/datasource/TRN/reporter/%s/partner/%s/product/%s/year/ALL/datatype/reported?format=JSON";
    private static final String API_DATA_FOUND_LOG = "[API Data Found] Reporter=%s, Partner=%s, HS Code=%s | Latest Year=%d";
    private static final int DEFAULT_YEAR = 0;
    private static final int AHS_RATE_INDEX = 0;
    private static final int MFN_RATE_INDEX = 1;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public WitsApiService(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    // fetch tariff rates from WITS API for given reporter, partner, and HS code
    public List<TariffRateDto> fetchTariffs(String reporterCode, String partnerCode, String hsCode) {
        List<TariffRateDto> result = new ArrayList<>();
        String apiUrl = String.format(WITS_API_BASE_URL, reporterCode, partnerCode, hsCode);

        try {
            
            String apiResponse = restClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(apiResponse);

            JsonNode dataSets = rootNode.path("dataSets");
            if (dataSets.isMissingNode() || !dataSets.isArray() || dataSets.size() == 0) {
                return result;
            }

            JsonNode seriesNode = dataSets.get(0).path("series");
            if (seriesNode.isMissingNode() || seriesNode.isEmpty()) {
                return result;
            }

            JsonNode obsYearsNode = rootNode.path("structure")
                    .path("dimensions")
                    .path("observation")
                    .get(0)
                    .path("values");

            // build index to year mapping from observation dimensions
            Map<Integer, Integer> indexToYearMap = new HashMap<>();
            for (int i = 0; i < obsYearsNode.size(); i++) {
                indexToYearMap.put(i, obsYearsNode.get(i).path("id").asInt());
            }

            // parse tariff data from series observations
            Map<Integer, TariffRateDto> yearToTariffMap = new HashMap<>();
            for (Map.Entry<String, JsonNode> seriesEntry : seriesNode.properties()) {
                JsonNode observations = seriesEntry.getValue().path("observations");
                if (observations.isMissingNode() || observations.isEmpty()) {
                    continue;
                }

                for (Map.Entry<String, JsonNode> obsEntry : observations.properties()) {
                    int obsIndex = Integer.parseInt(obsEntry.getKey());
                    int year = indexToYearMap.getOrDefault(obsIndex, DEFAULT_YEAR);

                    JsonNode obsValues = obsEntry.getValue();
                    double ahsRate = obsValues.get(AHS_RATE_INDEX).asDouble();
                    double mfnRate = obsValues.get(MFN_RATE_INDEX).asDouble();

                    yearToTariffMap.put(year,
                            new TariffRateDto(reporterCode, partnerCode, hsCode, year, ahsRate, mfnRate));
                }
            }

            // return latest year's tariff data
            if (!yearToTariffMap.isEmpty()) {
                int latestYear = Collections.max(yearToTariffMap.keySet());
                result.add(yearToTariffMap.get(latestYear));
                System.out.printf(API_DATA_FOUND_LOG + "%n",
                        reporterCode, partnerCode, hsCode, latestYear);
            }

        } catch (JsonProcessingException | NumberFormatException | RestClientResponseException e) {
            return Collections.emptyList();
        } catch (RestClientException e) {
            return Collections.emptyList();
        }

        return result;
    }

}
