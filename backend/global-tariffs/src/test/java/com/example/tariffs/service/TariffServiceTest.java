package com.example.tariffs.service;

import com.example.tariffs.dto.TariffDefinitionsResponse;
import com.example.tariffs.dto.TariffDefinitionsResponse.TariffDefinitionDto;
import com.example.tariffs.entity.Tariff;
import com.example.tariffs.exception.DataAccessException;
import com.example.tariffs.exception.NotFoundException;
import com.example.tariffs.exception.ValidationException;
import com.example.tariffs.repository.ProductRepository;
import com.example.tariffs.repository.TariffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Allow lenient stubbing (avoids unnecessary stub errors)
public class TariffServiceTest {

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private TariffService tariffService;

    private Tariff testTariff;
    private TariffDefinitionDto validDto;
    private List<String> testProducts;

    @BeforeEach
    void setUp() {
        // Test Tariff entity (importingTo = China, exportingFrom = Singapore)
        testTariff = new Tariff();
        testTariff.setCountry("China");
        testTariff.setPartner("Singapore");
        testTariff.setAhsWeighted(2.0);
        testTariff.setMfnWeighted(10.0);

        // Valid DTO (matches testTariff's country/partner)
        validDto = new TariffDefinitionDto(
                "China_Singapore", "Electronics", "Singapore", "China",
                "AHS", 5.0, "2022-01-01", "Ongoing"
        );

        // Test products (2 products = 2 DTOs in getTariffDefinitions)
        testProducts = List.of("Electronics", "Machinery");
    }

    // ------------------------------
    // Utility Method Tests
    // ------------------------------
    @Test
    void hasFTA_FtaCountries_ReturnsTrue() {
        assertTrue(tariffService.hasFTA("China", "Singapore")); // Both in FTA_COUNTRIES
    }

    @Test
    void hasFTA_NonFtaCountries_ReturnsFalse() {
        assertFalse(tariffService.hasFTA("China", "USA")); // USA not in FTA_COUNTRIES
    }

    @Test
    void getAllCountries_ReturnsDistinctCountriesFromRepo() {
        List<String> expectedCountries = List.of("China", "Singapore", "Malaysia");
        when(tariffRepository.findDistinctCountries()).thenReturn(expectedCountries);

        List<String> actualCountries = tariffService.getAllCountries();

        assertEquals(expectedCountries, actualCountries);
        verify(tariffRepository, times(1)).findDistinctCountries();
    }

    // ------------------------------
    // Tariff Retrieval Tests
    // ------------------------------
    @Test
    void getTariffDefinitions_Success_CombinesProductsAndTariffs() {
        // Mock repo responses
        when(productRepository.findDistinctProducts()).thenReturn(testProducts);
        when(tariffRepository.findAll()).thenReturn(List.of(testTariff));

        TariffDefinitionsResponse response = tariffService.getTariffDefinitions();

        // Verify success and data structure
        assertTrue(response.isSuccess());
        assertEquals(2, response.getData().size()); // 2 products × 1 tariff
        TariffDefinitionDto firstDto = response.getData().get(0);
        
        // Service generates sequential IDs (1, 2, ...) for combined tariffs
        assertEquals("1", firstDto.getId()); 
        assertEquals("Electronics", firstDto.getProduct());
        assertEquals("AHS", firstDto.getType()); // FTA route
        assertEquals(2.0, firstDto.getRate()); // AHS rate from testTariff
        assertEquals("2022-01-01", firstDto.getEffectiveDate());
        assertEquals("Ongoing", firstDto.getExpirationDate());
    }

    @Test
    void getTariffDefinitions_ExceptionThrown_ReturnsErrorResponse() {
        when(productRepository.findDistinctProducts()).thenThrow(new RuntimeException("DB Connection Failed"));

        TariffDefinitionsResponse response = tariffService.getTariffDefinitions();

        assertFalse(response.isSuccess());
        assertTrue(response.getError().contains("Failed to retrieve tariff definitions"));
    }

    @Test
    void getGlobalTariffDefinitions_DelegatesToGetTariffDefinitions() {
        when(productRepository.findDistinctProducts()).thenReturn(testProducts);
        when(tariffRepository.findAll()).thenReturn(List.of(testTariff));

        TariffDefinitionsResponse globalResponse = tariffService.getGlobalTariffDefinitions();
        TariffDefinitionsResponse combinedResponse = tariffService.getTariffDefinitions();

        assertEquals(combinedResponse.isSuccess(), globalResponse.isSuccess());
        assertEquals(combinedResponse.getData().size(), globalResponse.getData().size());
    }

    @Test
    void getUserTariffDefinitions_ReturnsInMemoryTariffs() {
        // Add a tariff to in-memory list
        when(tariffRepository.findByCountryAndPartner("China", "Singapore")).thenReturn(Optional.of(testTariff));
        when(tariffRepository.save(any(Tariff.class))).thenReturn(testTariff);
        tariffService.addAdminTariffDefinition(validDto);

        TariffDefinitionsResponse response = tariffService.getUserTariffDefinitions();

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals("China_Singapore", response.getData().get(0).getId());
    }

    // ------------------------------
    // Admin Operation Tests
    // ------------------------------
    @Test
    void addAdminTariffDefinition_ValidDto_NewTariff_CreatesAndReturnsDto() {
        // Stub with ACTUAL values the service uses (importingTo = China, exportingFrom = Singapore)
        when(tariffRepository.findByCountryAndPartner("China", "Singapore")).thenReturn(Optional.empty());
        when(tariffRepository.save(any(Tariff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TariffDefinitionsResponse response = tariffService.addAdminTariffDefinition(validDto);

        assertTrue(response.isSuccess());
        TariffDefinitionDto responseDto = response.getData().get(0);
        assertEquals("China_Singapore", responseDto.getId());
        assertEquals(5.0, responseDto.getRate()); // AHS rate from DTO
        verify(tariffRepository, times(1)).save(any(Tariff.class));
    }

    @Test
    void addAdminTariffDefinition_ValidDto_ExistingTariff_UpdatesAndReturnsDto() {
        when(tariffRepository.findByCountryAndPartner("China", "Singapore")).thenReturn(Optional.of(testTariff));
        when(tariffRepository.save(any(Tariff.class))).thenReturn(testTariff);

        TariffDefinitionsResponse response = tariffService.addAdminTariffDefinition(validDto);

        assertEquals(5.0, response.getData().get(0).getRate());
        verify(tariffRepository, times(1)).save(testTariff);
    }

    @Test
    void addAdminTariffDefinition_InvalidDto_NullImportingTo_ThrowsValidationException() {
        validDto.setImportingTo(null);
        assertThrows(ValidationException.class, () -> tariffService.addAdminTariffDefinition(validDto));
        verify(tariffRepository, never()).save(any());
    }

    @Test
    void updateAdminTariffDefinition_ValidId_ExistingTariff_UpdatesSuccessfully() {
        String validId = "China_Singapore"; // Correct ID format: "importingTo_exportingFrom"
        when(tariffRepository.findByCountryAndPartner("China", "Singapore")).thenReturn(Optional.of(testTariff));
        when(tariffRepository.save(any(Tariff.class))).thenReturn(testTariff);

        TariffDefinitionsResponse response = tariffService.updateAdminTariffDefinition(validId, validDto);

        assertTrue(response.isSuccess());
        assertEquals(5.0, response.getData().get(0).getRate());
        verify(tariffRepository, times(1)).save(testTariff);
    }

    @Test
    void deleteAdminTariffDefinition_ValidId_ExistingTariff_DeletesSuccessfully() {
        String validId = "China_Singapore";
        when(tariffRepository.findByCountryAndPartner("China", "Singapore")).thenReturn(Optional.of(testTariff));
        doNothing().when(tariffRepository).delete(any(Tariff.class));

        assertDoesNotThrow(() -> tariffService.deleteAdminTariffDefinition(validId));
        verify(tariffRepository, times(1)).delete(testTariff);
    }

    // ------------------------------
    // Helper Method Tests
    // ------------------------------
    @Test
    void convertToDto_AhsType_ReturnsCorrectDto() {
        TariffDefinitionDto dto = tariffService.convertToDto(
                testTariff, "Electronics", "2023-01-01", "2024-01-01", "AHS"
        );

        assertEquals("China_Singapore", dto.getId());
        assertEquals("Electronics", dto.getProduct());
        assertEquals("Singapore", dto.getExportingFrom());
        assertEquals("China", dto.getImportingTo());
        assertEquals("AHS", dto.getType());
        assertEquals(2.0, dto.getRate());
    }

    @Test
    void validateTariffDefinition_NegativeRate_ThrowsValidationException() {
        validDto.setRate(-3.0);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            tariffService.validateTariffDefinition(validDto);
        });
        assertEquals("Tariff rate cannot be negative", exception.getMessage());
    }
}