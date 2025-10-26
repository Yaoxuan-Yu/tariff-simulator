package com.example.tariff.service;

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

import com.example.tariff.dto.TariffDefinitionsResponse;
import com.example.tariff.dto.TariffRateDto;
import com.example.tariff.dto.TariffResponse;
import com.example.tariff.entity.Product;
import com.example.tariff.entity.Tariff;
import com.example.tariff.exception.NotFoundException;
import com.example.tariff.exception.ValidationException;
import com.example.tariff.repository.ProductRepository;
import com.example.tariff.repository.TariffRepository;
import com.example.tariff.service.api.WitsApiService;

@ExtendWith(MockitoExtension.class)
public class TariffServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private WitsApiService witsApiService;

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
    void calculate_CustomCostNonNumeric() {
        when(productRepository.findByNameAndBrand("Test Product", "Test Brand"))
            .thenReturn(List.of(testProduct));
        when(tariffRepository.findByCountryAndPartner("China", "Singapore"))
            .thenReturn(Optional.of(testTariffWithFTA));

        ValidationException thrown = assertThrows(ValidationException.class, () ->
            tariffService.calculate("Test Product", "Test Brand", "Singapore", "China", 2, "abc")
        );

        assertTrue(thrown.getMessage().contains("Invalid custom cost format"));
    }

    // === User-defined tariff tests ===
    @Test
    void calculateWithMode_UserDefinedTariff_Success() {
        String userTariffId = "user-12345";
        String productName = "Test Product";
        String brand = "Test Brand";
        String exportingFrom = "Singapore";
        String importingTo = "China";
        double quantity = 2;
        String customCost = "15";
        String mode = "user";

        when(productRepository.findByNameAndBrand(productName, brand))
            .thenReturn(List.of(testProduct));

        TariffDefinitionsResponse.TariffDefinitionDto userTariff = new TariffDefinitionsResponse.TariffDefinitionDto();
        userTariff.setId(userTariffId);
        userTariff.setProduct(productName);
        userTariff.setExportingFrom(exportingFrom);
        userTariff.setImportingTo(importingTo);
        userTariff.setType("Custom Duty");
        userTariff.setRate(5.0);

        tariffService.addUserTariffDefinition(userTariff);

        TariffResponse response = tariffService.calculateWithMode(
            productName, brand, exportingFrom, importingTo, quantity, customCost, mode, userTariffId);

        assertTrue(response.isSuccess());
        assertEquals(30.0, response.getData().getProductCost());
        assertEquals(5.0, response.getData().getTariffRate());
        assertEquals(31.5, response.getData().getTotalCost());
        assertTrue(response.getData().getTariffType().contains("user-defined"));
    }

    @Test
    void calculateWithMode_UserDefinedTariff_NotFound() {
        String productName = "Test Product";
        String brand = "Test Brand";
        String exportingFrom = "Singapore";
        String importingTo = "China";
        double quantity = 2;
        String mode = "user";
        String nonExistentTariffId = "user-99999";

        TariffResponse response = tariffService.calculateWithMode(
            productName, brand, exportingFrom, importingTo, quantity, null, mode, nonExistentTariffId);

        assertFalse(response.isSuccess());
        assertTrue(response.getError().contains("Selected user-defined tariff not found"));
    }

    // === WitsApiService mock test ===
    @Test
    void testFetchWitsTariffs() {
        when(witsApiService.fetchTariffs("360", "840", "151110"))
            .thenReturn(List.of(
                new TariffRateDto("China", "USA", 5.0, 15.0),
                new TariffRateDto("China", "Singapore", 2.0, 10.0)
            ));

        List<TariffRateDto> tariffs = witsApiService.fetchTariffs("360", "840", "151110");
        assertNotNull(tariffs);
        assertEquals(2, tariffs.size());

        tariffs.forEach(t -> 
            System.out.println(t.getCountry() + " -> " + t.getPartner() +
                               " | AHS: " + t.getAhsWeighted() + ", MFN: " + t.getMfnWeighted())
        );
    }
}
