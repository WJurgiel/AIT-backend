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

import java.util.List;

/**
 * Kontroler odpowiedzialny za operacje na aktualnie zalogowanym użytkowniku
 * (profil, preferencje, ulubione gry).
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
     * Pobranie danych aktualnie zalogowanego użytkownika.
     */
    @GetMapping("/me")
    public ResponseEntity<UserAboutMeResponse> getMe(
            @CookieValue(name = "jwt") String token) {

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
     * Aktualizacja danych profilu użytkownika.
     * Jeśli zmienia się username → generowany jest nowy JWT.
     */
    @PatchMapping("/me")
    public ResponseEntity<UserAboutMeResponse> updateMe(
            @CookieValue(name = "jwt") String token,
            @Valid @RequestBody UpdateProfileRequest req) {

        String oldUsername = jwtService.extractUsername(token);

        UserProfile updated =
                userService.updateProfile(oldUsername, req);

        UserAboutMeResponse body = new UserAboutMeResponse(
                updated.getUsername(),
                updated.getEmail(),
                updated.getCreatedAt()
        );

        // Jeśli username nie zmienił się → bez zmian w tokenie
        if (updated.getUsername().equals(oldUsername)) {
            return ResponseEntity.ok(body);
        }

        // Jeśli username zmieniony → nowy JWT
        String newToken =
                jwtService.generateToken(updated.getUsername());

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
     * Zmiana hasła użytkownika.
     */
    @PatchMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            @CookieValue(name = "jwt") String token,
            @Valid @RequestBody UpdatePasswordRequest req) {

        String username = jwtService.extractUsername(token);
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
        return ResponseEntity.ok(userService.getPreferences(username));
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

    /**
     * Dodanie gry do ulubionych.
     */
    @PostMapping("/me/favorites")
    public ResponseEntity<Void> addFavorite(
            @CookieValue(name = "jwt") String token,
            @RequestParam String gameId) {

        String username = jwtService.extractUsername(token);
        userService.addFavoriteGame(username, gameId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Usunięcie gry z ulubionych.
     */
    @DeleteMapping("/me/favorites")
    public ResponseEntity<Void> removeFavorite(
            @CookieValue(name = "jwt") String token,
            @RequestParam String gameId) {

        String username = jwtService.extractUsername(token);
        userService.removeFavoriteGame(username, gameId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Pobranie listy ulubionych gier.
     */
    @GetMapping("/me/favorites")
    public ResponseEntity<List<String>> getFavorites(
            @CookieValue(name = "jwt") String token) {

        String username = jwtService.extractUsername(token);
        return ResponseEntity.ok(
                userService.getFavoriteGames(username)
        );
    }

    /**
     * Sprawdzenie czy gra jest w ulubionych.
     */
    @GetMapping("/me/favorites/check")
    public ResponseEntity<Boolean> isFavorite(
            @CookieValue(name = "jwt") String token,
            @RequestParam String gameId) {

        String username = jwtService.extractUsername(token);
        return ResponseEntity.ok(
                userService.isFavorite(username, gameId)
        );
    }
}