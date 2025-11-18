package com.example.calculator.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.calculator.service.TariffCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
public class TariffCalculationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TariffCalculationService tariffCalculationService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private TariffCalculationController controller;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testCalculateTariff_globalMode_success() throws Exception {
        // Mock service response
        when(tariffCalculationService.calculateTariff(any(), eq("global"))).thenReturn(123.45);

        mockMvc.perform(get("/api/tariff")
                        .param("productId", "1")
                        .param("mode", "global")
                        .sessionAttr("tariffHistory", null))
                .andExpect(status().isOk())
                .andExpect(content().string("123.45"));

        verify(tariffCalculationService).calculateTariff(any(), eq("global"));
    }

    @Test
    public void testCalculateTariff_simulatorMode_success() throws Exception {
        when(tariffCalculationService.calculateTariff(any(), eq("simulator"))).thenReturn(67.89);

        mockMvc.perform(get("/api/tariff")
                        .param("productId", "2")
                        .param("mode", "simulator")
                        .sessionAttr("tariffHistory", null))
                .andExpect(status().isOk())
                .andExpect(content().string("67.89"));

        verify(tariffCalculationService).calculateTariff(any(), eq("simulator"));
    }

    @Test
    public void testCalculateTariff_missingProductId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/tariff")
                        .param("mode", "global"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCalculateTariff_invalidMode_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/tariff")
                        .param("productId", "1")
                        .param("mode", "invalidMode"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSessionHistorySaving() throws Exception {
        when(tariffCalculationService.calculateTariff(any(), eq("global"))).thenReturn(50.0);

        mockMvc.perform(get("/api/tariff")
                        .param("productId", "1")
                        .param("mode", "global")
                        .sessionAttr("tariffHistory", null))
                .andExpect(status().isOk())
                .andExpect(content().string("50.0"));

        // Can add verify for session.setAttribute if your controller saves history
        verify(session, atLeastOnce()).setAttribute(eq("tariffHistory"), any());
    }
}
