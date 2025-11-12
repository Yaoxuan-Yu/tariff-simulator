package com.example.calculator.service;

import com.example.calculator.dto.TariffComparisonDTO;
import com.example.calculator.dto.TariffHistoryDTO;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TariffComparisonServiceUnitTest {

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private TariffComparisonService comparisonService;

    private Product testProduct;
    private Tariff testTariff1;
    private Tariff testTariff2;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("TestProduct");
        testProduct.setCost(100.0);
        testProduct.setUnit("kg");

        testTariff1 = new Tariff();
        testTariff1.setCountry("Singapore");
        testTariff1.setPartner("Malaysia");
        testTariff1.setAhsWeighted(5.0);
        testTariff1.setMfnWeighted(10.0);

        testTariff2 = new Tariff();
        testTariff2.setCountry("Thailand");
        testTariff2.setPartner("Malaysia");
        testTariff2.setAhsWeighted(8.0);
        testTariff2.setMfnWeighted(12.0);
    }

    @Test
    void testCompareMultipleCountries_Success() {
        // Arrange
        when(productRepository.findByName("TestProduct")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("Singapore", "Malaysia"))
                .thenReturn(Optional.of(testTariff1));
        when(tariffRepository.findByCountryAndPartner("Thailand", "Malaysia"))
                .thenReturn(Optional.of(testTariff2));
        when(currencyService.convertFromUSD(anyDouble(), eq("USD"))).thenAnswer(i -> i.getArgument(0));

        // Act
        TariffComparisonDTO result = comparisonService.compareMultipleCountries(
                "TestProduct", "Malaysia", List.of("Singapore", "Thailand"), 10.0, null, "USD"
        );

        // Assert
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().getComparisons().size());
        assertNotNull(result.getData().getChartData());
        verify(productRepository).findByName("TestProduct");
    }

    @Test
    void testCompareMultipleCountries_ProductNotFound() {
        // Arrange
        when(productRepository.findByName("NonExistentProduct")).thenReturn(List.of());

        // Act
        TariffComparisonDTO result = comparisonService.compareMultipleCountries(
                "NonExistentProduct", "Malaysia", List.of("Singapore"), 10.0, null, "USD"
        );

        // Assert
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    void testCompareMultipleCountries_InvalidInput() {
        // Act
        TariffComparisonDTO result = comparisonService.compareMultipleCountries(
                "", "Malaysia", List.of("Singapore"), 10.0, null, "USD"
        );

        // Assert
        assertFalse(result.isSuccess());
    }

    @Test
    void testCompareMultipleCountries_InvalidQuantity() {
        // Act
        TariffComparisonDTO result = comparisonService.compareMultipleCountries(
                "TestProduct", "Malaysia", List.of("Singapore"), 0.0, null, "USD"
        );

        // Assert
        assertFalse(result.isSuccess());
    }

    @Test
    void testCompareMultipleCountries_NoTariffData() {
        // Arrange
        when(productRepository.findByName("TestProduct")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner(anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Act
        TariffComparisonDTO result = comparisonService.compareMultipleCountries(
                "TestProduct", "Malaysia", List.of("NoDataCountry"), 10.0, null, "USD"
        );

        // Assert
        assertFalse(result.isSuccess());
    }

    @Test
    void testCompareMultipleCountries_SortsByTotalCost() {
        // Arrange
        when(productRepository.findByName("TestProduct")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("Singapore", "Malaysia"))
                .thenReturn(Optional.of(testTariff1)); // Lower rate
        when(tariffRepository.findByCountryAndPartner("Thailand", "Malaysia"))
                .thenReturn(Optional.of(testTariff2)); // Higher rate
        when(currencyService.convertFromUSD(anyDouble(), eq("USD"))).thenAnswer(i -> i.getArgument(0));

        // Act
        TariffComparisonDTO result = comparisonService.compareMultipleCountries(
                "TestProduct", "Malaysia", List.of("Thailand", "Singapore"), 10.0, null, "USD"
        );

        // Assert
        assertTrue(result.isSuccess());
        var comparisons = result.getData().getComparisons();
        assertEquals(2, comparisons.size());
        // Singapore should be ranked first (lower total cost)
        assertEquals(1, comparisons.get(0).getRank());
        assertEquals("Singapore", comparisons.get(0).getCountry());
    }

    @Test
    void testGetTariffHistory_Success() {
        // Arrange
        when(productRepository.findByName("TestProduct")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("Singapore", "Malaysia"))
                .thenReturn(Optional.of(testTariff1));

        // Act
        TariffHistoryDTO result = comparisonService.getTariffHistory(
                "TestProduct", "Malaysia", "Singapore", "2024-01-01", "2024-06-01"
        );

        // Assert
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getTimePoints());
        assertTrue(result.getData().getTimePoints().size() > 0);
    }

    @Test
    void testGetTariffHistory_ProductNotFound() {
        // Arrange
        when(productRepository.findByName("NonExistentProduct")).thenReturn(List.of());

        // Act
        TariffHistoryDTO result = comparisonService.getTariffHistory(
                "NonExistentProduct", "Malaysia", "Singapore", null, null
        );

        // Assert
        assertFalse(result.isSuccess());
    }

    @Test
    void testGetTariffHistory_InvalidInput() {
        // Act
        TariffHistoryDTO result = comparisonService.getTariffHistory(
                "", "Malaysia", "Singapore", null, null
        );

        // Assert
        assertFalse(result.isSuccess());
    }

    @Test
    void testGetTariffTrends_Success() {
        // Arrange
        when(productRepository.findByName("TestProduct")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("Singapore", "Malaysia"))
                .thenReturn(Optional.of(testTariff1));

        // Act
        List<Map<String, Object>> result = comparisonService.getTariffTrends(
                List.of("Singapore"), List.of("Malaysia"), List.of("TestProduct"), "2024-01-01", "2024-06-01"
        );

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    void testGetTariffTrends_InvalidInput() {
        // Act & Assert
        assertThrows(com.example.calculator.exception.ValidationException.class, () -> {
            comparisonService.getTariffTrends(
                    List.of(), List.of("Malaysia"), List.of("TestProduct"), null, null
            );
        });
    }

    @Test
    void testGetTariffTrends_EmptyProducts() {
        // Act & Assert
        assertThrows(com.example.calculator.exception.ValidationException.class, () -> {
            comparisonService.getTariffTrends(
                    List.of("Singapore"), List.of("Malaysia"), List.of(), null, null
            );
        });
    }
}

