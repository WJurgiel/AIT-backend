package com.ait.aitbackend.user.controller;

import com.ait.aitbackend.security.JwtService;
import com.ait.aitbackend.user.dto.*;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler odpowiedzialny za zarządzanie użytkownikami,
 * profilem oraz preferencjami użytkownika.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userService;
    private final JwtService jwtService;

    // Konfiguracja ciasteczka JWT
    @Value("${app.security.jwt.cookie-name:jwt}")
    private String jwtCookieName;

    @Value("${app.security.jwt.cookie-max-age-seconds:86400}")
    private long jwtCookieMaxAgeSeconds;

    @Value("${app.security.jwt.cookie-secure:false}")
    private boolean jwtCookieSecure;

    @Value("${app.security.jwt.cookie-same-site:Lax}")
    private String jwtCookieSameSite;

    /**
     * Endpoint zwracający dane aktualnie zalogowanego użytkownika.
     */
    @GetMapping("/me")
    public ResponseEntity<UserAboutMeResponse> getMe(
            @CookieValue(name = "jwt") String token) {

        // Odczyt username z tokena JWT
        String username = jwtService.extractUsername(token);

        UserProfile user = userService.getOrThrow(username);

        return ResponseEntity.ok(
                new UserAboutMeResponse(
                        user.getUsername(),
                        user.getEmail(),
                        user.getCreatedAt()
                )
        );
    }

    /**
     * Aktualizacja danych użytkownika.
     * Jeśli username został zmieniony,
     * generowany jest nowy token JWT.
     */
    @PatchMapping("/me")
    public ResponseEntity<UserAboutMeResponse> updateMe(
            @CookieValue(name = "jwt") String token,
            @Valid @RequestBody UpdateProfileRequest req) {

        String oldUsername = jwtService.extractUsername(token);

        // Aktualizacja profilu użytkownika
        UserProfile updated = userService.updateProfile(oldUsername, req);

        UserAboutMeResponse body = new UserAboutMeResponse(
                updated.getUsername(),
                updated.getEmail(),
                updated.getCreatedAt()
        );

        // Jeśli username się nie zmienił
        if (updated.getUsername().equals(oldUsername)) {
            return ResponseEntity.ok(body);
        }

        // Wygenerowanie nowego JWT po zmianie username
        String newToken = jwtService.generateToken(updated.getUsername());

        ResponseCookie cookie = ResponseCookie.from(jwtCookieName, newToken)
                .httpOnly(true)
                .secure(jwtCookieSecure)
                .path("/")
                .maxAge(jwtCookieMaxAgeSeconds)
                .sameSite(jwtCookieSameSite)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }

    /**
     * Aktualizacja hasła użytkownika.
     */
    @PatchMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            @CookieValue(name = "jwt") String token,
            @Valid @RequestBody UpdatePasswordRequest req) {

        String username = jwtService.extractUsername(token);

        // Zmiana hasła użytkownika
        userService.updatePassword(username, req);

        return ResponseEntity.noContent().build();
    }

    /**
     * Pobranie preferencji użytkownika.
     */
    @GetMapping("/me/preferences")
    public ResponseEntity<UserPreferencesDto> getPreferences(
            @CookieValue(name = "jwt") String token) {

        String username = jwtService.extractUsername(token);

        return ResponseEntity.ok(
                userService.getPreferences(username)
        );
    }

    /**
     * Aktualizacja preferencji użytkownika.
     */
    @PutMapping("/me/preferences")
    public ResponseEntity<UserPreferencesDto> updatePreferences(
            @CookieValue(name = "jwt") String token,
            @RequestBody UserPreferencesDto dto) {

        String username = jwtService.extractUsername(token);

        return ResponseEntity.ok(
                userService.updatePreferences(username, dto)
        );
    }
}