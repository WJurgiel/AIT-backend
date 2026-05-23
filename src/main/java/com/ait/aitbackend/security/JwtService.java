package com.ait.aitbackend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Serwis odpowiedzialny za generowanie, parsowanie i walidację JWT.
 * Wykorzystuje bibliotekę JJWT.
 */
@Service
public class JwtService {

    // Klucz sekretu pobierany z zmiennej środowiskowej
    private static final String SECRET_KEY = System.getenv("JWT_TOKEN");

    // Czas życia tokena: 24h
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    /**
     * Generuje JWT dla użytkownika.
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Tworzy klucz podpisu HMAC z SECRET_KEY.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Wyciąga username (subject) z tokena JWT.
     */
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Sprawdza poprawność i ważność tokena JWT.
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}