package com.ait.aitbackend.auth.dto.login;

/**
 * DTO przechowujące odpowiedź logowania użytkownika.
 * Zawiera JWT token.
 */
public record LoginResponse(
        // token JTW
        String token
) {}
