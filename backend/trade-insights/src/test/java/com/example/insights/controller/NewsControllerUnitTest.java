package com.example.insights.controller;

import com.example.insights.dto.NewsSearchRequest;
import com.example.insights.dto.NewsSearchResultDto;
import com.example.insights.service.NewsService;
import com.example.insights.service.QueryLoggerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsControllerUnitTest {

    @Mock
    private NewsService newsService;

    @Mock
    private QueryLoggerService queryLoggerService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NewsController controller;

    private NewsSearchRequest request;
    private NewsSearchResultDto mockResponse;

    @BeforeEach
    void setUp() {
        request = new NewsSearchRequest();
        request.setQuery("trade");
        request.setCountry("Singapore");
        request.setProduct("Electronics");
        request.setLimit(10);
        request.setOffset(0);

        mockResponse = new NewsSearchResultDto("success", Collections.emptyList(), 0, 10, 0);
    }

    @Test
    void testSearchNews_Success() {
        // Arrange
        when(authentication.getName()).thenReturn("test-user");
        when(newsService.searchNews(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(mockResponse);
        doNothing().when(queryLoggerService).logSearch(anyString(), any(), any());

        // Act
        ResponseEntity<NewsSearchResultDto> response = controller.searchNews(request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(newsService).searchNews("trade", "Singapore", "Electronics", 10, 0);
        verify(queryLoggerService).logSearch(anyString(), any(), any());
    }

    @Test
    void testSearchNews_QueryTooShort() {
        // Arrange
        request.setQuery("a");

        // Act
        ResponseEntity<NewsSearchResultDto> response = controller.searchNews(request, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(newsService, never()).searchNews(anyString(), anyString(), anyString(), anyInt(), anyInt());
    }
}

