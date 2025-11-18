package com.example.integration.service;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.example.integration.dto.TariffRateDto;

@ExtendWith(MockitoExtension.class)
public class WitsApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WitsApiService witsApiService;

    @Test
    void fetchTariffs_ShouldReturnParsedDto() {
        String url = "https://api.wits.worldbank.org/test";
        TariffRateDto[] mockResponse = new TariffRateDto[]{
            new TariffRateDto("SG", "CN", 5.0)
        };

        when(restTemplate.getForObject(url, TariffRateDto[].class)).thenReturn(mockResponse);

        TariffRateDto[] result = witsApiService.fetchTariffs(url);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("SG", result[0].getReporter());
    }
}
