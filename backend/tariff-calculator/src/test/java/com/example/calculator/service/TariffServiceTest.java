package com.example.calculator.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.calculator.dto.TariffResponse;
import com.example.calculator.entity.Product;
import com.example.calculator.entity.Tariff;
import com.example.calculator.exception.NotFoundException;
import com.example.calculator.exception.ValidationException;
import com.example.calculator.repository.ProductRepository;
import com.example.calculator.repository.TariffRepository;
import com.example.calculator.service.SessionTariffService;

@ExtendWith(MockitoExtension.class)
public class TariffServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private SessionTariffService sessionTariffService;

    @InjectMocks
    private TariffService tariffService;

    private Product testProduct;
    private Tariff testTariffWithFTA;
    private Tariff testTariffWithoutFTA;

    @BeforeEach
    public void setUp() {
        // Product setup
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setBrand("Test Brand");
        testProduct.setCost(10.0);
        testProduct.setUnit("piece");

        // Tariff with FTA
        testTariffWithFTA = new Tariff();
        testTariffWithFTA.setCountry("China");
        testTariffWithFTA.setPartner("Singapore");
        testTariffWithFTA.setAhsWeighted(2.0);
        testTariffWithFTA.setMfnWeighted(10.0);

        // Tariff without FTA
        testTariffWithoutFTA = new Tariff();
        testTariffWithoutFTA.setCountry("China");
        testTariffWithoutFTA.setPartner("USA");
        testTariffWithoutFTA.setAhsWeighted(5.0);
        testTariffWithoutFTA.setMfnWeighted(15.0);
    }

    // === Core functionality ===
    @Test
    void calculate_Success_WithFTA() {
        when(productRepository.findByNameAndBrand("Test Product", "Test Brand"))
            .thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("China", "Singapore"))
            .thenReturn(Optional.of(testTariffWithFTA));

        TariffResponse response = tariffService.calculate(
            "Test Product", "Test Brand", "Singapore", "China", 3, null);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("Test Product", response.getData().getProduct());
        assertEquals(30.0, response.getData().getProductCost());
        assertEquals(2.0, response.getData().getTariffRate());
        assertEquals(30.6, response.getData().getTotalCost());
    }

    @Test
    void calculate_Success_WithoutFTA() {
        when(productRepository.findByNameAndBrand("Test Product", "Test Brand"))
            .thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("China", "USA"))
            .thenReturn(Optional.of(testTariffWithoutFTA));

        TariffResponse response = tariffService.calculate(
            "Test Product", "Test Brand", "USA", "China", 2, null);

        assertTrue(response.isSuccess());
        assertEquals(20.0, response.getData().getProductCost());
        assertEquals(15.0, response.getData().getTariffRate());
        assertEquals(23.0, response.getData().getTotalCost());
    }

    // === Exception / error handling ===
    @Test
    void calculate_ProductNotFound() {
        when(productRepository.findByNameAndBrand("Non-existent Product", "Test Brand"))
            .thenReturn(List.of());

        NotFoundException thrown = assertThrows(NotFoundException.class, () ->
            tariffService.calculate("Non-existent Product", "Test Brand", "Singapore", "China", 3, null)
        );

        assertTrue(thrown.getMessage().contains("Product not found"));
    }

    @Test
    void calculate_TariffNotFound() {
        when(productRepository.findByNameAndBrand("Test Product", "Test Brand"))
            .thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("Mars", "Earth"))
            .thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(NotFoundException.class, () ->
            tariffService.calculate("Test Product", "Test Brand", "Earth", "Mars", 3, null)
        );

        assertTrue(thrown.getMessage().contains("Tariff data not available"));
    }

    @Test
    void calculate_QuantityZero() {
        when(productRepository.findByNameAndBrand("Test Product", "Test Brand"))
            .thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("China", "Singapore"))
            .thenReturn(Optional.of(testTariffWithFTA));

        ValidationException thrown = assertThrows(ValidationException.class, () ->
            tariffService.calculate("Test Product", "Test Brand", "Singapore", "China", 0, null)
        );

        assertTrue(thrown.getMessage().contains("Quantity must be greater than 0"));
    }

    @Test
    void calculate_NullInputs_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> {
            tariffService.calculate(
                null, "Test Brand", "Singapore", "China", 1, null
            );
        });

        assertThrows(ValidationException.class, () -> {
            tariffService.calculate(
                "Test Product", null, "Singapore", "China", 1, null
            );
        });

        assertThrows(ValidationException.class, () -> {
            tariffService.calculate(
                "Test Product", "Test Brand", null, "China", 1, null
            );
        });
    }

    @Test
    void calculate_QuantityNegative() {
        assertThrows(ValidationException.class, () -> {
            tariffService.calculate(
                "Test Product", "Test Brand", "Singapore", "China", -5, null);
        });
    }

    @Test
    void calculate_CustomCostNonNumeric() {
        when(productRepository.findByNameAndBrand("Test Product", "Test Brand"))
            .thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("China", "Singapore"))
            .thenReturn(Optional.of(testTariffWithFTA));

        assertThrows(ValidationException.class, () -> {
            tariffService.calculate(
                "Test Product", "Test Brand", "Singapore", "China", 2, "abc");
        });
    }

    @Test
    void calculate_VeryLargeQuantity() {
        when(productRepository.findByNameAndBrand("Test Product", "Test Brand"))
            .thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("China", "Singapore"))
            .thenReturn(Optional.of(testTariffWithFTA));

        double largeQuantity = 1_000_000;
        TariffResponse response = tariffService.calculate(
            "Test Product", "Test Brand", "Singapore", "China", largeQuantity, null);

        assertTrue(response.isSuccess());
        assertEquals(10_000_000.0, response.getData().getProductCost());
        assertEquals(10_200_000.0, response.getData().getTotalCost());
    }

    // TODO: Add tests for calculateWithMode method with user-defined tariffs
    // TODO: Add tests for calculateWithMode method with simulator mode
    // TODO: Add tests for hasFTA method
    // TODO: Add tests for findMatchingUserTariff method
}

