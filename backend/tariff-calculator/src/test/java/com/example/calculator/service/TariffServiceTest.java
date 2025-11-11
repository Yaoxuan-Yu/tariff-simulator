package com.example.calculator.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setCost(10.0);
        testProduct.setUnit("piece");

        testTariffWithFTA = new Tariff();
        testTariffWithFTA.setCountry("China");
        testTariffWithFTA.setPartner("Singapore");
        testTariffWithFTA.setAhsWeighted(2.0);
        testTariffWithFTA.setMfnWeighted(10.0);

        testTariffWithoutFTA = new Tariff();
        testTariffWithoutFTA.setCountry("China");
        testTariffWithoutFTA.setPartner("USA");
        testTariffWithoutFTA.setAhsWeighted(5.0);
        testTariffWithoutFTA.setMfnWeighted(15.0);
    }

    // === Core functionality ===

    @Test
    void calculate_Success_WithFTA() {
        when(productRepository.findByName("Test Product")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("China", "Singapore"))
                .thenReturn(Optional.of(testTariffWithFTA));

        TariffResponse response = tariffService.calculate(
                "Test Product", "Singapore", "China", 3, null);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("Test Product", response.getData().getProduct());
        assertEquals(30.0, response.getData().getProductCost());
        assertEquals(2.0, response.getData().getTariffRate());
        assertEquals(30.6, response.getData().getTotalCost());
    }

    @Test
    void calculate_Success_WithoutFTA() {
        when(productRepository.findByName("Test Product")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("China", "USA"))
                .thenReturn(Optional.of(testTariffWithoutFTA));

        TariffResponse response = tariffService.calculate(
                "Test Product", "USA", "China", 2, null);

        assertTrue(response.isSuccess());
        assertEquals(20.0, response.getData().getProductCost());
        assertEquals(15.0, response.getData().getTariffRate());
        assertEquals(23.0, response.getData().getTotalCost());
    }

    // === Error handling ===

    @Test
    void calculate_ProductNotFound_ShouldThrow() {
        when(productRepository.findByName("Non-existent Product")).thenReturn(List.of());

        NotFoundException thrown = assertThrows(NotFoundException.class, () ->
                tariffService.calculate("Non-existent Product", "Singapore", "China", 3, null)
        );
        assertTrue(thrown.getMessage().contains("Product not found"));
    }

    @Test
    void calculate_TariffNotFound_ShouldThrow() {
        when(productRepository.findByName("Test Product")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("Mars", "Earth"))
                .thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(NotFoundException.class, () ->
                tariffService.calculate("Test Product", "Earth", "Mars", 3, null)
        );
        assertTrue(thrown.getMessage().contains("Tariff data not available"));
    }

    @Test
    void calculate_QuantityZero_ShouldThrow() {
        when(productRepository.findByName("Test Product")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("China", "Singapore"))
                .thenReturn(Optional.of(testTariffWithFTA));

        ValidationException thrown = assertThrows(ValidationException.class, () ->
                tariffService.calculate("Test Product", "Singapore", "China", 0, null)
        );
        assertTrue(thrown.getMessage().contains("Quantity must be greater than 0"));
    }

    @Test
    void calculate_NullInputs_ShouldThrowValidationException() {
        ValidationException p1 = assertThrows(ValidationException.class, () ->
                tariffService.calculate(null, "Singapore", "China", 1, null)
        );
        assertTrue(p1.getMessage().contains("Product name is required"));

        ValidationException p2 = assertThrows(ValidationException.class, () ->
                tariffService.calculate("Test Product", null, "China", 1, null)
        );
        assertTrue(p2.getMessage().contains("Exporting country is required"));

        ValidationException p3 = assertThrows(ValidationException.class, () ->
                tariffService.calculate("Test Product", "Singapore", null, 1, null)
        );
        assertTrue(p3.getMessage().contains("Importing country is required"));
    }

    @Test
    void calculate_QuantityNegative_ShouldThrow() {
        ValidationException thrown = assertThrows(ValidationException.class, () ->
                tariffService.calculate("Test Product", "Singapore", "China", -5, null)
        );
        assertTrue(thrown.getMessage().contains("Quantity must be greater than 0"));
    }

    @Test
    void calculate_CustomCostNonNumeric_ShouldThrow() {
        when(productRepository.findByName("Test Product")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("China", "Singapore"))
                .thenReturn(Optional.of(testTariffWithFTA));

        ValidationException thrown = assertThrows(ValidationException.class, () ->
                tariffService.calculate("Test Product", "Singapore", "China", 2, "abc")
        );
        assertTrue(thrown.getMessage().contains("Invalid custom cost format"));
    }

    @Test
    void calculate_VeryLargeQuantity_Success() {
        when(productRepository.findByName("Test Product")).thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("China", "Singapore"))
                .thenReturn(Optional.of(testTariffWithFTA));

        double largeQuantity = 1_000_000;
        TariffResponse response = tariffService.calculate(
                "Test Product", "Singapore", "China", largeQuantity, null);

        assertTrue(response.isSuccess());
        assertEquals(10_000_000.0, response.getData().getProductCost());
        assertEquals(10_200_000.0, response.getData().getTotalCost());
    }

    // TODO: Add future tests for calculateWithMode and hasFTA when extended
}
