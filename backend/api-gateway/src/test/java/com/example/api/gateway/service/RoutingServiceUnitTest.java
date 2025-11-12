package com.example.api.gateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

import java.util.Enumeration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutingServiceUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private RoutingService routingService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(routingService, "productServiceUrl", "http://product-service:8084");
        ReflectionTestUtils.setField(routingService, "globalTariffsUrl", "http://global-tariffs:8083");
        ReflectionTestUtils.setField(routingService, "simulatorTariffsUrl", "http://simulator-tariffs:8086");
        ReflectionTestUtils.setField(routingService, "tariffCalculatorUrl", "http://tariff-calculator:8081");
        ReflectionTestUtils.setField(routingService, "sessionManagementUrl", "http://session-management:8082");
        ReflectionTestUtils.setField(routingService, "csvExportUrl", "http://csv-export:8085");
        ReflectionTestUtils.setField(routingService, "tradeInsightsUrl", "http://trade-insights:8088");
    }

    @Test
    void testBuildTargetUrl_WithQueryString() {
        // Act
        String result = routingService.buildTargetUrl(
                "http://service:8080", "/api/endpoint", "param1=value1&param2=value2"
        );

        // Assert
        assertEquals("http://service:8080/api/endpoint?param1=value1&param2=value2", result);
    }

    @Test
    void testBuildTargetUrl_WithoutQueryString() {
        // Act
        String result = routingService.buildTargetUrl(
                "http://service:8080", "/api/endpoint", null
        );

        // Assert
        assertEquals("http://service:8080/api/endpoint", result);
    }

    @Test
    void testBuildTargetUrl_EmptyQueryString() {
        // Act
        String result = routingService.buildTargetUrl(
                "http://service:8080", "/api/endpoint", ""
        );

        // Assert
        assertEquals("http://service:8080/api/endpoint", result);
    }

    @Test
    void testCreateHttpEntity_WithBody() {
        // Arrange
        Object body = new Object();
        Enumeration<String> headerNames = Collections.enumeration(Collections.singletonList("Content-Type"));
        when(httpServletRequest.getHeaderNames()).thenReturn(headerNames);
        when(httpServletRequest.getHeaders("Content-Type"))
                .thenReturn(Collections.enumeration(Collections.singletonList("application/json")));
        when(httpServletRequest.getCookies()).thenReturn(null);

        // Act
        HttpEntity<?> result = routingService.createHttpEntity(httpServletRequest, body);

        // Assert
        assertNotNull(result);
        assertEquals(body, result.getBody());
        assertNotNull(result.getHeaders());
    }

    @Test
    void testCreateHttpEntity_WithCookies() {
        // Arrange
        Cookie cookie = new Cookie("SESSION_ID", "test-session-123");
        Enumeration<String> headerNames = Collections.emptyEnumeration();
        when(httpServletRequest.getHeaderNames()).thenReturn(headerNames);
        when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{cookie});

        // Act
        HttpEntity<?> result = routingService.createHttpEntity(httpServletRequest, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.getHeaders().containsKey("Cookie"));
        assertTrue(result.getHeaders().getFirst("Cookie").contains("SESSION_ID=test-session-123"));
    }

    @Test
    void testCreateHttpEntity_WithoutBody() {
        // Arrange
        Enumeration<String> headerNames = Collections.emptyEnumeration();
        when(httpServletRequest.getHeaderNames()).thenReturn(headerNames);
        when(httpServletRequest.getCookies()).thenReturn(null);

        // Act
        HttpEntity<?> result = routingService.createHttpEntity(httpServletRequest, null);

        // Assert
        assertNotNull(result);
        assertNull(result.getBody());
    }

    @Test
    void testForwardRequest() {
        // Arrange
        String url = "http://service:8080/api/endpoint";
        HttpEntity<?> entity = new HttpEntity<>(null);
        ResponseEntity<String> mockResponse = ResponseEntity.ok("Success");
        
        when(restTemplate.exchange(any(java.net.URI.class), eq(HttpMethod.GET), eq(entity), eq(String.class)))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<String> result = routingService.forwardRequest(url, HttpMethod.GET, entity, String.class);

        // Assert
        assertNotNull(result);
        assertEquals("Success", result.getBody());
        verify(restTemplate).exchange(any(java.net.URI.class), eq(HttpMethod.GET), eq(entity), eq(String.class));
    }

    @Test
    void testGetProductServiceUrl() {
        // Act
        String result = routingService.getProductServiceUrl();

        // Assert
        assertEquals("http://product-service:8084", result);
    }

    @Test
    void testGetTariffCalculatorUrl() {
        // Act
        String result = routingService.getTariffCalculatorUrl();

        // Assert
        assertEquals("http://tariff-calculator:8081", result);
    }
}

