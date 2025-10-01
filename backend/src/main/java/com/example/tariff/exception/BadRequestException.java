package com.example.tariff.exception;

// allows for input validation for the api endpoints 
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
