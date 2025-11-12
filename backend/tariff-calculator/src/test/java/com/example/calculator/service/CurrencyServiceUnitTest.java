package com.example.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceUnitTest {

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    void testGetExchangeRate_USD() {
        // Act
        double rate = currencyService.getExchangeRate("USD");

        // Assert
        assertEquals(1.0, rate); // USD to USD should be 1.0
    }

    @Test
    void testGetExchangeRate_OtherCurrency() {
        // Act
        double rate = currencyService.getExchangeRate("EUR");

        // Assert
        assertTrue(rate > 0); // Should return a valid rate
    }

    @Test
    void testConvertFromUSD_USD() {
        // Act
        double result = currencyService.convertFromUSD(100.0, "USD");

        // Assert
        assertEquals(100.0, result);
    }

    @Test
    void testConvertFromUSD_OtherCurrency() {
        // Arrange
        double usdAmount = 100.0;
        double eurRate = currencyService.getExchangeRate("EUR");

        // Act
        double result = currencyService.convertFromUSD(usdAmount, "EUR");

        // Assert
        assertEquals(usdAmount * eurRate, result, 0.01);
    }

    @Test
    void testGetSupportedCurrencies() {
        // Act
        Map<String, Object> result = currencyService.getSupportedCurrencies();

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("currencies"));
        assertTrue(result.containsKey("rates"));
        assertTrue(result.containsKey("lastUpdated"));
    }

    @Test
    void testConvertFromUSD_ZeroAmount() {
        // Act
        double result = currencyService.convertFromUSD(0.0, "EUR");

        // Assert
        assertEquals(0.0, result);
    }

    @Test
    void testConvertFromUSD_InvalidCurrency() {
        // Act - Should default to USD or handle gracefully
        double result = currencyService.convertFromUSD(100.0, "INVALID");

        // Assert - Should not throw, may return USD equivalent or handle error
        assertTrue(result >= 0);
    }
}

