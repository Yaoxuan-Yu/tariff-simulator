package com.example.export.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    public void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    public void handleNotFoundException_ReturnsNotFoundStatus() {
        // Arrange
        NotFoundException ex = new NotFoundException("Resource not found");

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleNotFoundException(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource not found", response.getBody().get("error"));
    }

    @Test
    public void handleBadRequestException_ReturnsBadRequestStatus() {
        // Arrange
        BadRequestException ex = new BadRequestException("Invalid input");

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleBadRequestException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid input", response.getBody().get("error"));
    }

    @Test
    public void handleExportException_ReturnsInternalServerError() {
        // Arrange
        ExportException ex = new ExportException("Export failed");

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleExportException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Export failed"));
    }

    @Test
    public void handleDataAccessException_ReturnsInternalServerError() {
        // Arrange
        DataAccessException ex = new DataAccessException("Database error");

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleDataAccessException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Database operation failed"));
    }

    @Test
    public void handleRuntimeException_ReturnsInternalServerError() {
        // Arrange
        RuntimeException ex = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleRuntimeException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unexpected error", response.getBody().get("error"));
    }

    @Test
    public void handleRuntimeException_WithNullMessage_HandlesGracefully() {
        // Arrange
        RuntimeException ex = new RuntimeException();

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleRuntimeException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("error"));
        assertEquals("An unexpected error occurred", response.getBody().get("error"));
    }
}

