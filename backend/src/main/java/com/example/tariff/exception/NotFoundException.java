package com.example.tariff.exception;

// used in case of data not being found in the database 
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
