package com.example.tariffs.controller;

import com.example.tariffs.dto.TariffDefinitionsResponse;
import com.example.tariffs.dto.TariffDefinitionsResponse.TariffDefinitionDto;
import com.example.tariffs.exception.BadRequestException;
import com.example.tariffs.exception.NotFoundException;
import com.example.tariffs.exception.ValidationException;
import com.example.tariffs.service.TariffService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Use @WebMvcTest to load only web layer (no JPA context)
@WebMvcTest(GlobalTariffController.class)
public class GlobalTariffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean // Mock the service dependency
    private TariffService tariffService;

    private TariffDefinitionsResponse successResponse;
    private TariffDefinitionDto testDto;

    @BeforeEach
    void setUp() {
        // Test DTO and success response
        testDto = new TariffDefinitionDto(
                "China_Singapore", "Electronics", "Singapore", "China",
                "AHS", 5.0, "2022-01-01", "Ongoing"
        );
        successResponse = new TariffDefinitionsResponse(true, List.of(testDto));
    }

    @Test
    void getTariffDefinitions_ShouldReturnSuccessResponse() throws Exception {
        when(tariffService.getTariffDefinitions()).thenReturn(successResponse);

        mockMvc.perform(get("/api/tariff-definitions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].product").value("Electronics"))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(tariffService, times(1)).getTariffDefinitions();
    }

    @Test
    void getGlobalTariffDefinitions_ShouldReturnSuccessResponse() throws Exception {
        when(tariffService.getGlobalTariffDefinitions()).thenReturn(successResponse);

        mockMvc.perform(get("/api/tariff-definitions/global")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].type").value("AHS"));

        verify(tariffService, times(1)).getGlobalTariffDefinitions();
    }

    @Test
    void getModifiedTariffDefinitions_ShouldReturnSuccessResponse() throws Exception {
        when(tariffService.getUserTariffDefinitions()).thenReturn(successResponse);

        mockMvc.perform(get("/api/tariff-definitions/modified")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("China_Singapore"));

        verify(tariffService, times(1)).getUserTariffDefinitions();
    }

    @Test
    void addModifiedTariffDefinition_ValidRequest_ShouldReturnSuccess() throws Exception {
        Map<String, Object> validRequest = Map.of(
                "product", "Electronics",
                "exportingFrom", "Singapore",
                "importingTo", "China",
                "type", "AHS",
                "rate", 5.0,
                "effectiveDate", "2023-01-01"
        );
        when(tariffService.addAdminTariffDefinition(any(TariffDefinitionDto.class))).thenReturn(successResponse);

        mockMvc.perform(post("/api/tariff-definitions/modified")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(tariffService, times(1)).addAdminTariffDefinition(any(TariffDefinitionDto.class));
    }

    @Test
    void addModifiedTariffDefinition_NullBody_ShouldThrowBadRequest() throws Exception {
        mockMvc.perform(post("/api/tariff-definitions/modified")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(null)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> result.getResolvedException() instanceof BadRequestException)
                .andExpect(result -> result.getResolvedException().getMessage().contains("Tariff definition data is required"));

        verify(tariffService, never()).addAdminTariffDefinition(any());
    }

    @Test
    void updateModifiedTariffDefinition_ValidRequest_ShouldReturnSuccess() throws Exception {
        String tariffId = "China_Singapore";
        Map<String, Object> updateRequest = Map.of(
                "product", "Electronics",
                "exportingFrom", "Singapore",
                "importingTo", "China",
                "type", "AHS",
                "rate", 7.5
        );
        when(tariffService.updateAdminTariffDefinition(anyString(), any(TariffDefinitionDto.class))).thenReturn(successResponse);

        mockMvc.perform(put("/api/tariff-definitions/modified/{id}", tariffId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(tariffService, times(1)).updateAdminTariffDefinition(eq(tariffId), any(TariffDefinitionDto.class));
    }

    @Test
    void updateModifiedTariffDefinition_EmptyId_ShouldThrowBadRequest() throws Exception {
        Map<String, Object> updateRequest = Map.of("product", "Electronics");

        mockMvc.perform(put("/api/tariff-definitions/modified/") // Empty ID
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> result.getResolvedException() instanceof BadRequestException);

        verify(tariffService, never()).updateAdminTariffDefinition(any(), any());
    }

    @Test
    void deleteModifiedTariffDefinition_ValidId_ShouldReturnOk() throws Exception {
        String tariffId = "China_Singapore";
        doNothing().when(tariffService).deleteAdminTariffDefinition(tariffId);

        mockMvc.perform(delete("/api/tariff-definitions/modified/{id}", tariffId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(tariffService, times(1)).deleteAdminTariffDefinition(tariffId);
    }

    @Test
    void exportTariffDefinitions_ShouldReturnNotImplemented() throws Exception {
        mockMvc.perform(get("/api/tariff-definitions/export")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError()) // 501 Not Implemented
                .andExpect(jsonPath("$.error").value("Export functionality not yet implemented"));
    }
}