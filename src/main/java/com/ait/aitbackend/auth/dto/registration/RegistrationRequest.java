package com.ait.aitbackend.auth.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO przechowujące dane rejestracji użytkownika.
 * Zawiera nazwę użytkownika, email oraz hasło.
 */
public record RegistrationRequest(

        // Nazwa użytkownika nie może być pusta, powinna mieć od 3 do 50 znaków
        @NotBlank(message = "Username cannot be empty!")
        @Size(min = 3, max = 50, message = "Username should contain 3-50 characters")
        String username,

        // Email nie może być pusty, musi mieć poprawny format
        @NotBlank(message = "Email cannot be empty!")
        @Email(message = "Incorrect email format provided!")
        String email,

        // Hasło nie może być puste, powinno mieć więcej niż 8 znaków
        @NotBlank(message = "Password cannot be empty!")
        @Size(min = 8, message = "Password must have at least 8 characters")
        String password
) {
}
