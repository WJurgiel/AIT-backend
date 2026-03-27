package com.ait.aitbackend.user.exceptionHandlers;

import com.ait.aitbackend.user.exceptions.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class UserExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String,String>> handleUserAlreadyExists(UserAlreadyExistsException ex)
    {
        Map<String,String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Conflict");
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}
