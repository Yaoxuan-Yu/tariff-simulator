package com.example.insights.exception;

// used in case of database operation failures like connection issues etc
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message) {
        super(message);
    }
    
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

