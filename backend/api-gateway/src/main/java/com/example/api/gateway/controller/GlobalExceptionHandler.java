package com.example.api.gateway.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// centralised exception handling so every response shape is consistent
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 4xx errors from downstream services
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientError(HttpClientErrorException ex) {
        return buildResponseFromDownstream(ex);
    }

    // 5xx errors from downstream services
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpServerError(HttpServerErrorException ex) {
        return buildResponseFromDownstream(ex);
    }

    // network/service availability issues
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientError(RestClientException ex) {
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        ErrorResponse response = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "Upstream service is unavailable: " + ex.getMessage(),
                Instant.now(),
                Collections.emptyMap()
        );
        return ResponseEntity.status(status).body(response);
    }

    // catch-all gateway failure
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse response = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                Optional.ofNullable(ex.getMessage()).orElse("Unexpected error"),
                Instant.now(),
                Collections.emptyMap()
        );
        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<ErrorResponse> buildResponseFromDownstream(HttpStatusCodeException ex) {
        HttpStatusCode statusCode = ex.getStatusCode();
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null) {
            status = HttpStatus.valueOf(statusCode.value());
        }
        String responseBody = ex.getResponseBodyAsString();

        if (responseBody != null && !responseBody.isBlank()) {
            try {
                Map<String, Object> parsedBody = objectMapper.readValue(responseBody, MAP_TYPE);
                String message = Optional.ofNullable(parsedBody.get("message"))
                        .map(Object::toString)
                        .orElse(status.getReasonPhrase());
                ErrorResponse response = new ErrorResponse(
                        statusCode.value(),
                        status.getReasonPhrase(),
                        message,
                        Instant.now(),
                        Collections.unmodifiableMap(parsedBody)
                );
                return ResponseEntity.status(status).body(response);
            } catch (Exception parsingFailure) {
                // Fall through to return the raw body as the message
                ErrorResponse response = new ErrorResponse(
                        statusCode.value(),
                        status.getReasonPhrase(),
                        responseBody,
                        Instant.now(),
                        Collections.emptyMap()
                );
                return ResponseEntity.status(status).body(response);
            }
        }

        ErrorResponse response = new ErrorResponse(
                statusCode.value(),
                status.getReasonPhrase(),
                Optional.ofNullable(ex.getMessage()).orElse(status.getReasonPhrase()),
                Instant.now(),
                Collections.emptyMap()
        );
        return ResponseEntity.status(status).body(response);
    }

    // immutable dto returned to clients
    public record ErrorResponse(
            int status,
            String error,
            String message,
            Instant timestamp,
            Map<String, Object> details
    ) { }
}
