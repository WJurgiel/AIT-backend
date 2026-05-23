package com.ait.aitbackend.user.dto;

import java.time.LocalDateTime;

/**
 * DTO przechowujące odpowiedź profilu użytkownika.
 * Zawiera nazwę użytkownika, email, datę utworzenia konta
 */
public record UserAboutMeResponse(
        String username,
        String email,
        LocalDateTime createdAt
) {}
