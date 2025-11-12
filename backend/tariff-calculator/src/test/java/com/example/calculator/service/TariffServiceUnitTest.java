package com.example.calculator.service;

import com.example.calculator.dto.TariffResponse;
import com.example.calculator.entity.Product;
import com.example.calculator.entity.Tariff;
import com.example.calculator.repository.ProductRepository;
import com.example.calculator.repository.TariffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TariffServiceUnitTest {

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SessionTariffService sessionTariffService;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private TariffService tariffService;

    private Product testProduct;
    private Tariff testTariff;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("TestProduct");
        testProduct.setCost(100.0);
        testProduct.setUnit("kg");

        testTariff = new Tariff();
        testTariff.setCountry("Country2");
        testTariff.setPartner("Country1");
        testTariff.setAhsWeighted(5.0);
        testTariff.setMfnWeighted(10.0);
    }

    @Test
    void testCalculate_Success_WithFTA() {
        // Arrange
        when(productRepository.findByName("TestProduct")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("Country2", "Country1"))
                .thenReturn(Optional.of(testTariff));
        when(currencyService.convertFromUSD(anyDouble(), eq("USD"))).thenAnswer(i -> i.getArgument(0));

        // Act
        TariffResponse response = tariffService.calculate(
                "TestProduct", "Country1", "Country2", 10.0, null, "USD"
        );

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("TestProduct", response.getData().getProduct());
        assertEquals(5.0, response.getData().getTariffRate()); // AHS rate for FTA
        assertEquals("AHS (with FTA)", response.getData().getTariffType());
        verify(productRepository).findByName("TestProduct");
        verify(tariffRepository).findByCountryAndPartner("Country2", "Country1");
    }

    @Test
    void testCalculate_Success_WithMFN() {
        // Arrange
        testTariff.setCountry("NonFTACountry");
        testTariff.setPartner("Country1");
        when(productRepository.findByName("TestProduct")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("NonFTACountry", "Country1"))
                .thenReturn(Optional.of(testTariff));
        when(currencyService.convertFromUSD(anyDouble(), eq("USD"))).thenAnswer(i -> i.getArgument(0));

        // Act
        TariffResponse response = tariffService.calculate(
                "TestProduct", "Country1", "NonFTACountry", 10.0, null, "USD"
        );

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(10.0, response.getData().getTariffRate()); // MFN rate
        assertEquals("MFN (no FTA)", response.getData().getTariffType());
    }

    @Test
    void testCalculate_WithCustomCost() {
        // Arrange
        when(productRepository.findByName("TestProduct")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("Country2", "Country1"))
                .thenReturn(Optional.of(testTariff));
        when(currencyService.convertFromUSD(anyDouble(), eq("USD"))).thenAnswer(i -> i.getArgument(0));

        // Act
        TariffResponse response = tariffService.calculate(
                "TestProduct", "Country1", "Country2", 10.0, "150.0", "USD"
        );

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(1500.0, response.getData().getProductCost()); // 150 * 10
    }

    @Test
    void testCalculate_ProductNotFound() {
        // Arrange
        when(productRepository.findByName("NonExistentProduct")).thenReturn(List.of());

        // Act & Assert
        assertThrows(com.example.calculator.exception.NotFoundException.class, () -> {
            tariffService.calculate("NonExistentProduct", "Country1", "Country2", 10.0, null, "USD");
        });
    }

    @Test
    void testCalculate_TariffNotFound() {
        // Arrange
        when(productRepository.findByName("TestProduct")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("Country2", "Country1"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(com.example.calculator.exception.NotFoundException.class, () -> {
            tariffService.calculate("TestProduct", "Country1", "Country2", 10.0, null, "USD");
        });
    }

    @Test
    void testCalculate_InvalidQuantity() {
        // Act & Assert
        assertThrows(com.example.calculator.exception.ValidationException.class, () -> {
            tariffService.calculate("TestProduct", "Country1", "Country2", 0.0, null, "USD");
        });
    }

    @Test
    void testCalculate_InvalidCustomCost() {
        // Arrange
        when(productRepository.findByName("TestProduct")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("Country2", "Country1"))
                .thenReturn(Optional.of(testTariff));

        // Act & Assert
        assertThrows(com.example.calculator.exception.ValidationException.class, () -> {
            tariffService.calculate("TestProduct", "Country1", "Country2", 10.0, "invalid", "USD");
        });
    }

    @Test
    void testCalculate_WithCurrencyConversion() {
        // Arrange
        when(productRepository.findByName("TestProduct")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("Country2", "Country1"))
                .thenReturn(Optional.of(testTariff));
        when(currencyService.convertFromUSD(1000.0, "EUR")).thenReturn(850.0);
        when(currencyService.convertFromUSD(50.0, "EUR")).thenReturn(42.5);
        when(currencyService.convertFromUSD(1050.0, "EUR")).thenReturn(892.5);

        // Act
        TariffResponse response = tariffService.calculate(
                "TestProduct", "Country1", "Country2", 10.0, null, "EUR"
        );

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(850.0, response.getData().getProductCost());
        assertEquals("EUR", response.getData().getCurrency());
        verify(currencyService, times(3)).convertFromUSD(anyDouble(), eq("EUR"));
    }

    @Test
    void testGetAllTariffs() {
        // Arrange
        when(tariffRepository.findAll()).thenReturn(List.of(testTariff));
        when(currencyService.convertFromUSD(anyDouble(), eq("USD"))).thenAnswer(i -> i.getArgument(0));

        // Act
        var tariffs = tariffService.getAllTariffs(1000.0, "USD");

        // Assert
        assertNotNull(tariffs);
        assertEquals(1, tariffs.size());
        verify(tariffRepository).findAll();
    }

    @Test
    void testGetTariffsByCountry() {
        // Arrange
        when(tariffRepository.findByCountry("Country2")).thenReturn(List.of(testTariff));
        when(currencyService.convertFromUSD(anyDouble(), eq("USD"))).thenAnswer(i -> i.getArgument(0));

        // Act
        var tariffs = tariffService.getTariffsByCountry("Country2", 1000.0, "USD");

        // Assert
        assertNotNull(tariffs);
        assertEquals(1, tariffs.size());
        verify(tariffRepository).findByCountry("Country2");
    }

    @Test
    void testGetTariffByCountryAndPartner_NotFound() {
        // Arrange
        when(tariffRepository.findByCountryAndPartner("Country2", "Country1"))
                .thenReturn(Optional.empty());

        // Act
        var tariff = tariffService.getTariffByCountryAndPartner("Country2", "Country1", 1000.0, "USD");

        // Assert
        assertNull(tariff);
    }
}

