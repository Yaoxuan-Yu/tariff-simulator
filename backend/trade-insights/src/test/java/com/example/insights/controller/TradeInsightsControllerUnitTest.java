package com.example.insights.controller;

import com.example.insights.dto.TradeInsightsDto;
import com.example.insights.dto.TradeInsightsRequest;
import com.example.insights.service.QueryLoggerService;
import com.example.insights.service.TradeInsightsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeInsightsControllerUnitTest {

    @Mock
    private TradeInsightsService tradeInsightsService;

    @Mock
    private QueryLoggerService queryLoggerService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TradeInsightsController controller;

    private TradeInsightsRequest request;
    private TradeInsightsDto mockResponse;

    @BeforeEach
    void setUp() {
        request = new TradeInsightsRequest();
        request.setQuery("ASEAN trade");
        request.setCountry("Singapore");
        request.setProduct("Electronics");
        request.setLimit(10);

        mockResponse = new TradeInsightsDto();
        mockResponse.setStatus("success");
        mockResponse.setQuery("ASEAN trade");
    }

    @Test
    void testGetTradeInsights_Success() {
        // Arrange
        when(authentication.getName()).thenReturn("test-user");
        when(tradeInsightsService.getTradeInsights(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(mockResponse);
        doNothing().when(queryLoggerService).logSearch(anyString(), any(), any());

        // Act
        ResponseEntity<TradeInsightsDto> response = controller.getTradeInsights(request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tradeInsightsService).getTradeInsights("ASEAN trade", "Singapore", "Electronics", 10);
        verify(queryLoggerService).logSearch(anyString(), any(), any());
    }

    @Test
    void testGetTradeInsights_InvalidQuery() {
        // Arrange
        request.setQuery("");

        // Act
        ResponseEntity<TradeInsightsDto> response = controller.getTradeInsights(request, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(tradeInsightsService, never()).getTradeInsights(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void testGetTradeInsights_QueryTooShort() {
        // Arrange
        request.setQuery("a");

        // Act
        ResponseEntity<TradeInsightsDto> response = controller.getTradeInsights(request, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetTradeInsights_AnonymousUser() {
        // Arrange
        when(tradeInsightsService.getTradeInsights(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(mockResponse);
        doNothing().when(queryLoggerService).logSearch(anyString(), any(), any());

        // Act
        ResponseEntity<TradeInsightsDto> response = controller.getTradeInsights(request, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(queryLoggerService).logSearch(eq("anonymous"), any(), any());
    }
}

