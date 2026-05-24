package com.ait.aitbackend.user.exceptions;

/**
 * Wyjątek wyrzucany gdy użytkownik nie istnieje w bazie danych
 */
public class UserDoesNotExistException extends RuntimeException {
    public UserDoesNotExistException(String message) {
        super(message);
    }
}
