package com.ait.aitbackend.user.exceptions;

/**
 * Wyjątek wyrzucany gdy użytkownik istnieje już w bazie danych
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
