package com.example.insights.service;

import com.example.insights.dto.NewsSearchResultDto;
import com.example.insights.service.NewsApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceUnitTest {

    @Mock
    private NewsApiClient newsApiClient;

    @InjectMocks
    private NewsService newsService;

    @Test
    void testSearchNews_Success() {
        // Arrange
        NewsSearchResultDto mockResult = new NewsSearchResultDto(
                "success", Collections.emptyList(), 0, 10, 0
        );
        when(newsApiClient.searchNews(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(mockResult);

        // Act
        NewsSearchResultDto result = newsService.searchNews("trade", "Singapore", "Electronics", 10, 0);

        // Assert
        assertNotNull(result);
        assertEquals("success", result.getStatus());
        verify(newsApiClient).searchNews("trade", "Singapore", "Electronics", 10, 0);
    }

    @Test
    void testSearchNews_WithNullParameters() {
        // Arrange
        NewsSearchResultDto mockResult = new NewsSearchResultDto(
                "success", Collections.emptyList(), 0, 10, 0
        );
        when(newsApiClient.searchNews(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(mockResult);

        // Act
        NewsSearchResultDto result = newsService.searchNews("trade", null, null, 10, 0);

        // Assert
        assertNotNull(result);
        verify(newsApiClient).searchNews("trade", null, null, 10, 0);
    }

    @Test
    void testSearchNews_ClientThrowsException() {
        // Arrange
        when(newsApiClient.searchNews(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("API error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            newsService.searchNews("trade", "Singapore", null, 10, 0);
        });
    }
}

