package com.ait.aitbackend.global.exceptionHandlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Globalny handler wyjątków dla całej aplikacji.
 * Obsługuje błędy walidacji, błędy zewnętrznych API oraz problemy z połączeniem.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Obsługa błędów walidacji (@Valid).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex)
    {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Obsługa błędów zwracanych przez zewnętrzne API (np. CheapShark, RAWG).
     */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<String> handleExternalApiResponse(RestClientResponseException ex) {
        String body = ex.getResponseBodyAsString();
        if (body.isBlank()) {
            body = "{\"error\":\"External API error\",\"message\":\"Upstream returned " + ex.getStatusCode().value() + "\"}";
        }

        return ResponseEntity.status(ex.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    /**
     * Obsługa timeoutów / problemów z połączeniem do API.
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, String>> handleExternalApiConnection() {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Gateway Timeout");
        error.put("message", "External API is unavailable");
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(error);
    }
}