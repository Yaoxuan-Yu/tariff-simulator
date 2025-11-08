package com.example.integration.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.integration.dto.TariffRateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
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
            if (dataSets.isMissingNode() || !dataSets.isArray() || dataSets.size() == 0) {
                return result; // No data
            }

            JsonNode seriesNode = dataSets.get(0).path("series");
            if (seriesNode.isMissingNode() || seriesNode.isEmpty()) {
                return result; // No data
            }

            // Fetch the observation year mapping from the structure
            JsonNode obsYearsNode = rootNode.path("structure")
                    .path("dimensions")
                    .path("observation")
                    .get(0)
                    .path("values");
            Map<Integer, Integer> indexToYearMap = new HashMap<>();
            for (int i = 0; i < obsYearsNode.size(); i++) {
                indexToYearMap.put(i, obsYearsNode.get(i).path("id").asInt());
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
                    int obsIndex = Integer.parseInt(obsEntry.getKey());
                    int year = indexToYearMap.getOrDefault(obsIndex, 0); // fallback if mapping missing

                    JsonNode obsValues = obsEntry.getValue();
                    double ahsRate = obsValues.get(0).asDouble();
                    double mfnRate = obsValues.get(1).asDouble();

                    yearToTariffMap.put(year, new TariffRateDto(reporterCode, partnerCode, hsCode, year, ahsRate, mfnRate));
                }
            }

            // Add only the latest year
            if (!yearToTariffMap.isEmpty()) {
                int latestYear = Collections.max(yearToTariffMap.keySet());
                result.add(yearToTariffMap.get(latestYear));
                System.out.printf("[API Data Found] Reporter=%s, Partner=%s, HS Code=%s | Latest Year=%d%n",
                        reporterCode, partnerCode, hsCode, latestYear);
            }

        } catch (JsonProcessingException | NumberFormatException | RestClientException e) {
            // System.err.printf("[API Error] Reporter=%s, Partner=%s, HS Code=%s | %s%n",
            //     reporterCode, partnerCode, hsCode, e.getMessage());
            return Collections.emptyList();
        }

        return result;
    }
    }
