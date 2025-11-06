package com.example.product.exception;

// used in case of business logic validation errors (e.g. quantity must be greater than 0, tariff rate cannot be negative etc)
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

