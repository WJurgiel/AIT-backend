package com.ait.aitbackend.auth.dto.login;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO przechowujące dane logowania użytkownika.
 * Zawiera nazwę użytkownika oraz hasło.
 */
public record LoginRequest(

        // Nazwa użytkownika nie może być pusta
        @NotBlank(message = "Username cannot be empty!")
        String username,

        // Hasło nie może być puste
        @NotBlank(message = "Password cannot be empty!")
        String password

) {}