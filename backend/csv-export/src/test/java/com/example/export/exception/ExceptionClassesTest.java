package com.example.export.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExceptionClassesTest {

    @Test
    public void badRequestException_Constructor_SetsMessage() {
        // Arrange & Act
        BadRequestException ex = new BadRequestException("Test message");

        // Assert
        assertEquals("Test message", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    public void notFoundException_Constructor_SetsMessage() {
        // Arrange & Act
        NotFoundException ex = new NotFoundException("Not found message");

        // Assert
        assertEquals("Not found message", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    public void exportException_ConstructorWithMessage_SetsMessage() {
        // Arrange & Act
        ExportException ex = new ExportException("Export error");

        // Assert
        assertEquals("Export error", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    public void exportException_ConstructorWithMessageAndCause_SetsBoth() {
        // Arrange
        Throwable cause = new RuntimeException("Root cause");

        // Act
        ExportException ex = new ExportException("Export error", cause);

        // Assert
        assertEquals("Export error", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    public void dataAccessException_ConstructorWithMessage_SetsMessage() {
        // Arrange & Act
        DataAccessException ex = new DataAccessException("Database error");

        // Assert
        assertEquals("Database error", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    public void dataAccessException_ConstructorWithMessageAndCause_SetsBoth() {
        // Arrange
        Throwable cause = new RuntimeException("Root cause");

        // Act
        DataAccessException ex = new DataAccessException("Database error", cause);

        // Assert
        assertEquals("Database error", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}

