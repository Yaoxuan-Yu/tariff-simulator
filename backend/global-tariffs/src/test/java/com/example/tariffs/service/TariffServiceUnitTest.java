package com.example.tariffs.service;

import com.example.tariffs.dto.TariffDefinitionsResponse;
import com.example.tariffs.entity.Tariff;
import com.example.tariffs.repository.ProductRepository;
import com.example.tariffs.repository.TariffRepository;
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

    @InjectMocks
    private TariffService tariffService;

    private Tariff testTariff;

    @BeforeEach
    void setUp() {
        testTariff = new Tariff();
        testTariff.setCountry("Singapore");
        testTariff.setPartner("Malaysia");
        testTariff.setAhsWeighted(5.0);
        testTariff.setMfnWeighted(10.0);
    }

    @Test
    void testGetAllCountries() {
        // Arrange
        when(tariffRepository.findDistinctCountries()).thenReturn(List.of("Singapore", "Malaysia"));

        // Act
        List<String> result = tariffService.getAllCountries();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(tariffRepository).findDistinctCountries();
    }

    @Test
    void testGetTariffDefinitions_Success() {
        // Arrange
        when(productRepository.findDistinctProducts()).thenReturn(List.of("Product1", "Product2"));
        when(tariffRepository.findAll()).thenReturn(List.of(testTariff));

        // Act
        TariffDefinitionsResponse response = tariffService.getTariffDefinitions();

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getTariffs());
        assertTrue(response.getTariffs().size() > 0);
        verify(productRepository).findDistinctProducts();
        verify(tariffRepository).findAll();
    }

    @Test
    void testGetTariffDefinitions_ExceptionHandling() {
        // Arrange
        when(productRepository.findDistinctProducts()).thenThrow(new RuntimeException("Database error"));

        // Act
        TariffDefinitionsResponse response = tariffService.getTariffDefinitions();

        // Assert
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
    }

    @Test
    void testGetGlobalTariffDefinitions() {
        // Arrange
        when(productRepository.findDistinctProducts()).thenReturn(List.of("Product1"));
        when(tariffRepository.findAll()).thenReturn(List.of(testTariff));

        // Act
        TariffDefinitionsResponse response = tariffService.getGlobalTariffDefinitions();

        // Assert
        assertTrue(response.isSuccess());
        verify(productRepository).findDistinctProducts();
    }

    @Test
    void testGetUserTariffDefinitions_Empty() {
        // Act
        TariffDefinitionsResponse response = tariffService.getUserTariffDefinitions();

        // Assert
        assertTrue(response.isSuccess());
        assertTrue(response.getTariffs().isEmpty());
    }

    @Test
    void testAddAdminTariffDefinition_Success_AHS() {
        // Arrange
        TariffDefinitionsResponse.TariffDefinitionDto dto = new TariffDefinitionsResponse.TariffDefinitionDto();
        dto.setProduct("TestProduct");
        dto.setExportingFrom("Malaysia");
        dto.setImportingTo("Singapore");
        dto.setType("AHS");
        dto.setRate(5.0);
        dto.setEffectiveDate("2024-01-01");
        dto.setExpirationDate("2024-12-31");

        when(tariffRepository.findByCountryAndPartner("Singapore", "Malaysia"))
                .thenReturn(Optional.empty());
        when(tariffRepository.save(any(Tariff.class))).thenReturn(testTariff);

        // Act
        TariffDefinitionsResponse response = tariffService.addAdminTariffDefinition(dto);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(1, response.getTariffs().size());
        verify(tariffRepository).save(any(Tariff.class));
    }

    @Test
    void testAddAdminTariffDefinition_Success_MFN() {
        // Arrange
        TariffDefinitionsResponse.TariffDefinitionDto dto = new TariffDefinitionsResponse.TariffDefinitionDto();
        dto.setProduct("TestProduct");
        dto.setExportingFrom("Malaysia");
        dto.setImportingTo("Singapore");
        dto.setType("MFN");
        dto.setRate(10.0);

        when(tariffRepository.findByCountryAndPartner("Singapore", "Malaysia"))
                .thenReturn(Optional.empty());
        when(tariffRepository.save(any(Tariff.class))).thenReturn(testTariff);

        // Act
        TariffDefinitionsResponse response = tariffService.addAdminTariffDefinition(dto);

        // Assert
        assertTrue(response.isSuccess());
        verify(tariffRepository).save(any(Tariff.class));
    }

    @Test
    void testAddAdminTariffDefinition_ValidationError_MissingImportingTo() {
        // Arrange
        TariffDefinitionsResponse.TariffDefinitionDto dto = new TariffDefinitionsResponse.TariffDefinitionDto();
        dto.setExportingFrom("Malaysia");
        dto.setType("AHS");
        dto.setRate(5.0);
        // Missing importingTo

        // Act & Assert
        assertThrows(com.example.tariffs.exception.ValidationException.class, () -> {
            tariffService.addAdminTariffDefinition(dto);
        });
    }

    @Test
    void testAddAdminTariffDefinition_ValidationError_InvalidType() {
        // Arrange
        TariffDefinitionsResponse.TariffDefinitionDto dto = new TariffDefinitionsResponse.TariffDefinitionDto();
        dto.setImportingTo("Singapore");
        dto.setExportingFrom("Malaysia");
        dto.setType("INVALID");
        dto.setRate(5.0);

        // Act & Assert
        assertThrows(com.example.tariffs.exception.ValidationException.class, () -> {
            tariffService.addAdminTariffDefinition(dto);
        });
    }

    @Test
    void testAddAdminTariffDefinition_ValidationError_NegativeRate() {
        // Arrange
        TariffDefinitionsResponse.TariffDefinitionDto dto = new TariffDefinitionsResponse.TariffDefinitionDto();
        dto.setImportingTo("Singapore");
        dto.setExportingFrom("Malaysia");
        dto.setType("AHS");
        dto.setRate(-5.0);

        // Act & Assert
        assertThrows(com.example.tariffs.exception.ValidationException.class, () -> {
            tariffService.addAdminTariffDefinition(dto);
        });
    }

    @Test
    void testUpdateAdminTariffDefinition_Success() {
        // Arrange
        String id = "Singapore_Malaysia";
        TariffDefinitionsResponse.TariffDefinitionDto dto = new TariffDefinitionsResponse.TariffDefinitionDto();
        dto.setType("AHS");
        dto.setRate(7.0);

        when(tariffRepository.findByCountryAndPartner("Singapore", "Malaysia"))
                .thenReturn(Optional.of(testTariff));
        when(tariffRepository.save(any(Tariff.class))).thenReturn(testTariff);

        // Act
        TariffDefinitionsResponse response = tariffService.updateAdminTariffDefinition(id, dto);

        // Assert
        assertTrue(response.isSuccess());
        verify(tariffRepository).save(any(Tariff.class));
    }

    @Test
    void testUpdateAdminTariffDefinition_NotFound() {
        // Arrange
        String id = "Singapore_Malaysia";
        TariffDefinitionsResponse.TariffDefinitionDto dto = new TariffDefinitionsResponse.TariffDefinitionDto();
        dto.setType("AHS");
        dto.setRate(7.0);

        when(tariffRepository.findByCountryAndPartner("Singapore", "Malaysia"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(com.example.tariffs.exception.NotFoundException.class, () -> {
            tariffService.updateAdminTariffDefinition(id, dto);
        });
    }

    @Test
    void testUpdateAdminTariffDefinition_InvalidIdFormat() {
        // Arrange
        String id = "InvalidFormat";
        TariffDefinitionsResponse.TariffDefinitionDto dto = new TariffDefinitionsResponse.TariffDefinitionDto();

        // Act & Assert
        assertThrows(com.example.tariffs.exception.ValidationException.class, () -> {
            tariffService.updateAdminTariffDefinition(id, dto);
        });
    }

    @Test
    void testDeleteAdminTariffDefinition_Success() {
        // Arrange
        String id = "Singapore_Malaysia";
        when(tariffRepository.findByCountryAndPartner("Singapore", "Malaysia"))
                .thenReturn(Optional.of(testTariff));
        doNothing().when(tariffRepository).delete(testTariff);

        // Act
        assertDoesNotThrow(() -> {
            tariffService.deleteAdminTariffDefinition(id);
        });

        // Assert
        verify(tariffRepository).delete(testTariff);
    }

    @Test
    void testDeleteAdminTariffDefinition_NotFound() {
        // Arrange
        String id = "Singapore_Malaysia";
        when(tariffRepository.findByCountryAndPartner("Singapore", "Malaysia"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(com.example.tariffs.exception.NotFoundException.class, () -> {
            tariffService.deleteAdminTariffDefinition(id);
        });
    }
}

