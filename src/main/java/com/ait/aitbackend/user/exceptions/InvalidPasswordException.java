package com.ait.aitbackend.user.exceptions;

/**
 * Wyjątek wyrzucany w przypadku nieprawidłowego hasła
 */
public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
