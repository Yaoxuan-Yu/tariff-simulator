package com.example.export.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// maps service exceptions to http status codes
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 when entity not found
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage() != null ? ex.getMessage() : "Resource not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 400 for validation issues
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequestException(BadRequestException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage() != null ? ex.getMessage() : "Bad request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 500 when csv export fails
    @ExceptionHandler(ExportException.class)
    public ResponseEntity<Map<String, String>> handleExportException(ExportException ex) {
        Map<String, String> body = new HashMap<>();
        String message = ex.getMessage() != null ? ex.getMessage() : "Unknown error";
        body.put("error", "Export failed: " + message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // 500 for downstream data access failures
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> handleDataAccessException(DataAccessException ex) {
        Map<String, String> body = new HashMap<>();
        String message = ex.getMessage() != null ? ex.getMessage() : "Unknown error";
        body.put("error", "Database operation failed: " + message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // fallback 500
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

