package com.example.integration.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.integration.entity.Product;
import com.example.integration.repository.ProductRepository;
import com.example.integration.repository.TariffRepository;

@ExtendWith(MockitoExtension.class)
public class TariffSchedulerTest {

    @Mock
    private TariffService tariffService;

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private TariffScheduler tariffScheduler;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setName("Test Product");
    }

    @Test
    void runUpdate_ShouldCallTariffServiceOnce() {
        when(productRepository.findAll()).thenReturn(List.of(testProduct));

        tariffScheduler.runUpdate();

        // Verifies the service method was called at least once
        verify(tariffService, times(1)).updateTariffs();
    }

}
