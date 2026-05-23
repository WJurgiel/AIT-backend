package com.ait.aitbackend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO przechowujące dane do aktualizacji hasła.
 * Zawiera aktualne hasło oraz nowe hasło.
 */
public record UpdatePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters")
        String newPassword
) {}
