package com.example.tariff.exception;

// used in case of export failures like file writing errors (feature to be implemented later)
public class ExportException extends RuntimeException {
    public ExportException(String message) {
        super(message);
    }
    
    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
