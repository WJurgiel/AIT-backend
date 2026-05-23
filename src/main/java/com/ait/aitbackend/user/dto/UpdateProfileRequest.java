package com.ait.aitbackend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO przechowujące dane do aktualizacji profilu użytkownika.
 * Zawiera nazwę użytkownika oraz email.
 */
public record UpdateProfileRequest(
        @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
        String username,

        @Email(message = "Invalid email format")
        String email
) {}
