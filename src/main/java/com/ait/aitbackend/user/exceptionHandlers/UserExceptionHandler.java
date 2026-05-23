package com.ait.aitbackend.user.exceptionHandlers;

import com.ait.aitbackend.user.exceptions.InvalidPasswordException;
import com.ait.aitbackend.user.exceptions.UserAlreadyExistsException;
import com.ait.aitbackend.user.exceptions.UserDoesNotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Globalny handler wyjątków związanych z użytkownikami.
 * Zamienia wyjątki na odpowiedzi HTTP z komunikatem błędu.
 */
@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String,String>> handleUserAlreadyExists(
            UserAlreadyExistsException ex) {

        return error(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage()
        );
    }

    @ExceptionHandler(UserDoesNotExistException.class)
    public ResponseEntity<Map<String,String>> handleUserDoesNotExists(
            UserDoesNotExistException ex) {

        return error(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String,String>> handleBadCredentials(
            BadCredentialsException ex) {

        return error(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String,String>> handleInvalidPassword(
            InvalidPasswordException ex) {

        return error(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage()
        );
    }

    /**
     * Tworzy ujednoliconą odpowiedź błędu.
     */
    private ResponseEntity<Map<String,String>> error(
            HttpStatus status,
            String error,
            String message) {

        Map<String,String> body = new HashMap<>();

        body.put("error", error);
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}