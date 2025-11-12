package com.example.insights.service;

import com.example.insights.dto.AgreementSearchResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AgreementServiceUnitTest {

    @InjectMocks
    private AgreementService agreementService;

    @Test
    void testSearchAgreements_Success() {
        // Act
        AgreementSearchResultDto result = agreementService.searchAgreements("Singapore", null, 10, 0);

        // Assert
        assertNotNull(result);
        assertEquals("success", result.getStatus());
        assertNotNull(result.getAgreements());
    }

    @Test
    void testSearchAgreements_WithAgreementType() {
        // Act
        AgreementSearchResultDto result = agreementService.searchAgreements("Singapore", "FTA", 10, 0);

        // Assert
        assertNotNull(result);
        assertEquals("success", result.getStatus());
    }

    @Test
    void testSearchAgreements_WithLimit() {
        // Act
        AgreementSearchResultDto result = agreementService.searchAgreements("Singapore", null, 5, 0);

        // Assert
        assertNotNull(result);
        assertTrue(result.getAgreements().size() <= 5);
    }

    @Test
    void testSearchAgreements_WithOffset() {
        // Act
        AgreementSearchResultDto result1 = agreementService.searchAgreements("Singapore", null, 10, 0);
        AgreementSearchResultDto result2 = agreementService.searchAgreements("Singapore", null, 10, 10);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
    }
}

