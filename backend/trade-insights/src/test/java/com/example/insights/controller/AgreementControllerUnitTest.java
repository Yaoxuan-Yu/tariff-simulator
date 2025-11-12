package com.example.insights.controller;

import com.example.insights.dto.AgreementSearchRequest;
import com.example.insights.dto.AgreementSearchResultDto;
import com.example.insights.service.AgreementService;
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
class AgreementControllerUnitTest {

    @Mock
    private AgreementService agreementService;

    @Mock
    private QueryLoggerService queryLoggerService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AgreementController controller;

    private AgreementSearchRequest request;
    private AgreementSearchResultDto mockResponse;

    @BeforeEach
    void setUp() {
        request = new AgreementSearchRequest();
        request.setCountry("Singapore");
        request.setAgreementType("FTA");
        request.setLimit(10);
        request.setOffset(0);

        mockResponse = new AgreementSearchResultDto("success", Collections.emptyList(), 0, 10, 0);
    }

    @Test
    void testSearchAgreements_Success() {
        // Arrange
        when(authentication.getName()).thenReturn("test-user");
        when(agreementService.searchAgreements(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(mockResponse);
        doNothing().when(queryLoggerService).logSearch(anyString(), any(), any());

        // Act
        ResponseEntity<AgreementSearchResultDto> response = controller.searchAgreements(request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(agreementService).searchAgreements("Singapore", "FTA", 10, 0);
        verify(queryLoggerService).logSearch(anyString(), any(), any());
    }

    @Test
    void testSearchAgreements_MissingCountry() {
        // Arrange
        request.setCountry("");

        // Act
        ResponseEntity<AgreementSearchResultDto> response = controller.searchAgreements(request, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(agreementService, never()).searchAgreements(anyString(), anyString(), anyInt(), anyInt());
    }
}

