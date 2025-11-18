package com.example.insights;

import com.example.calculator.controller.TariffCalculationController;
import com.example.simulator.service.SessionTariffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TariffCalculationController.class)
public class TariffCalculationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionTariffService sessionTariffService;

    @Test
    public void testCalculateTariffEndpoint() throws Exception {
        // Mock the service to return a dummy response
        when(sessionTariffService.getTariffDefinitions()).thenReturn(java.util.List.of());

        // Perform GET request to your endpoint
        mockMvc.perform(get("/api/tariff")
                .param("productId", "test-id")
                .param("country", "Singapore")
                .param("partner", "China"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        // Verify service method was called
        verify(sessionTariffService, times(1)).getTariffDefinitions();
    }
}
