package com.example.insights.service;

import com.example.insights.dto.AgreementSearchResultDto;
import com.example.insights.dto.NewsSearchResultDto;
import com.example.insights.dto.TradeInsightsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeInsightsServiceUnitTest {

    @Mock
    private NewsService newsService;

    @Mock
    private AgreementService agreementService;

    @Mock
    private Executor tradeInsightsExecutor;

    @InjectMocks
    private TradeInsightsService tradeInsightsService;

    @BeforeEach
    void setUp() {
        // Use direct executor for synchronous testing
        when(tradeInsightsExecutor.execute(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        });
    }

    @Test
    void testGetTradeInsights_Success() {
        // Arrange
        NewsSearchResultDto newsResult = new NewsSearchResultDto("success", Collections.emptyList(), 0, 10, 0);
        AgreementSearchResultDto agreementResult = new AgreementSearchResultDto("success", Collections.emptyList(), 0, 10, 0);
        
        when(newsService.searchNews(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(newsResult);
        when(agreementService.searchAgreements(anyString(), any(), anyInt(), anyInt()))
                .thenReturn(agreementResult);

        // Act
        TradeInsightsDto result = tradeInsightsService.getTradeInsights("ASEAN", "Singapore", "Electronics", 10);

        // Assert
        assertNotNull(result);
        assertEquals("success", result.getStatus());
        assertEquals("ASEAN", result.getQuery());
        assertEquals("Singapore", result.getCountry());
        assertNotNull(result.getNewsSection());
        assertNotNull(result.getAgreementsSection());
        verify(newsService).searchNews("ASEAN", "Singapore", "Electronics", 10, 0);
        verify(agreementService).searchAgreements("Singapore", null, 10, 0);
    }

    @Test
    void testGetTradeInsights_NewsServiceFails() {
        // Arrange
        when(newsService.searchNews(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("News service error"));
        AgreementSearchResultDto agreementResult = new AgreementSearchResultDto("success", Collections.emptyList(), 0, 10, 0);
        when(agreementService.searchAgreements(anyString(), any(), anyInt(), anyInt()))
                .thenReturn(agreementResult);

        // Act
        TradeInsightsDto result = tradeInsightsService.getTradeInsights("ASEAN", "Singapore", null, 10);

        // Assert
        assertNotNull(result);
        assertEquals("success", result.getStatus());
        assertNotNull(result.getNewsSection());
        assertEquals(0, result.getNewsSection().getTotalResults());
    }

    @Test
    void testGetTradeInsights_AgreementServiceFails() {
        // Arrange
        NewsSearchResultDto newsResult = new NewsSearchResultDto("success", Collections.emptyList(), 0, 10, 0);
        when(newsService.searchNews(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(newsResult);
        when(agreementService.searchAgreements(anyString(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Agreement service error"));

        // Act
        TradeInsightsDto result = tradeInsightsService.getTradeInsights("ASEAN", "Singapore", null, 10);

        // Assert
        assertNotNull(result);
        assertEquals("success", result.getStatus());
        assertNotNull(result.getAgreementsSection());
    }

    @Test
    void testGetTradeInsights_WithNullLimit() {
        // Arrange
        NewsSearchResultDto newsResult = new NewsSearchResultDto("success", Collections.emptyList(), 0, 10, 0);
        AgreementSearchResultDto agreementResult = new AgreementSearchResultDto("success", Collections.emptyList(), 0, 10, 0);
        when(newsService.searchNews(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(newsResult);
        when(agreementService.searchAgreements(anyString(), any(), anyInt(), anyInt()))
                .thenReturn(agreementResult);

        // Act
        TradeInsightsDto result = tradeInsightsService.getTradeInsights("ASEAN", "Singapore", null, null);

        // Assert
        assertNotNull(result);
        verify(newsService).searchNews(eq("ASEAN"), eq("Singapore"), isNull(), eq(10), eq(0));
    }
}

