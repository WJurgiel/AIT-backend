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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userService;
    private final JwtService jwtService;

    @Value("${app.security.jwt.cookie-name:jwt}")
    private String jwtCookieName;

    @Value("${app.security.jwt.cookie-max-age-seconds:86400}")
    private long jwtCookieMaxAgeSeconds;

    @Value("${app.security.jwt.cookie-secure:false}")
    private boolean jwtCookieSecure;

    @Value("${app.security.jwt.cookie-same-site:Lax}")
    private String jwtCookieSameSite;

    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfile> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/users/me */
    @GetMapping("/me")
    public ResponseEntity<UserAboutMeResponse> getMe(@CookieValue(name = "jwt") String token) {
        String username = jwtService.extractUsername(token);
        UserProfile user = userService.getOrThrow(username);
        return ResponseEntity.ok(new UserAboutMeResponse(user.getUsername(), user.getEmail(), user.getCreatedAt()));
    }

    /**
     * PATCH /api/users/me
     * Jeśli username się zmienił — wystawiamy nowy JWT w cookie,
     * żeby sesja od razu odzwierciedlała nowy subject.
     */
    @PatchMapping("/me")
    public ResponseEntity<UserAboutMeResponse> updateMe(
            @CookieValue(name = "jwt") String token,
            @Valid @RequestBody UpdateProfileRequest req) {

        String oldUsername = jwtService.extractUsername(token);
        UserProfile updated = userService.updateProfile(oldUsername, req);

        UserAboutMeResponse body = new UserAboutMeResponse(
                updated.getUsername(), updated.getEmail(), updated.getCreatedAt());

        // brak zmiany username: normalna odpowiedz
        if (updated.getUsername().equals(oldUsername)) {
            return ResponseEntity.ok(body);
        }

        // zmiana username: nowy token
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

    /** PATCH /api/users/me/password */
    @PatchMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            @CookieValue(name = "jwt") String token,
            @Valid @RequestBody UpdatePasswordRequest req) {
        String username = jwtService.extractUsername(token);
        userService.updatePassword(username, req);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/users/me/preferences */
    @GetMapping("/me/preferences")
    public ResponseEntity<UserPreferencesDto> getPreferences(@CookieValue(name = "jwt") String token) {
        String username = jwtService.extractUsername(token);
        return ResponseEntity.ok(userService.getPreferences(username));
    }

    /** GET /api/users/me/preferences/watched-games */
    @GetMapping("/me/preferences/watched-games")
    public ResponseEntity<List<String>> getWatchedGameIds(@CookieValue(name = "jwt") String token) {
        String username = jwtService.extractUsername(token);
        return ResponseEntity.ok(userService.getPreferences(username).watchedGameIds());
    }

    /** PUT /api/users/me/preferences */
    @PutMapping("/me/preferences")
    public ResponseEntity<UserPreferencesDto> updatePreferences(
            @CookieValue(name = "jwt") String token,
            @RequestBody UserPreferencesDto dto) {
        String username = jwtService.extractUsername(token);
        return ResponseEntity.ok(userService.updatePreferences(username, dto));
    }

    /** POST /api/users/me/preferences/watched-games */
    @PostMapping("/me/preferences/watched-games")
    public ResponseEntity<UserPreferencesDto> addWatchedGame(
            @CookieValue(name = "jwt") String token,
            @Valid @RequestBody AddWatchedGameRequest req) {
        String username = jwtService.extractUsername(token);
        return ResponseEntity.ok(userService.addWatchedGame(username, req));
    }

    /** DELETE /api/users/me/preferences/watched-games/{gameId} */
    @DeleteMapping("/me/preferences/watched-games/{gameId}")
    public ResponseEntity<UserPreferencesDto> removeWatchedGame(
            @CookieValue(name = "jwt") String token,
            @PathVariable String gameId) {
        String username = jwtService.extractUsername(token);
        return ResponseEntity.ok(userService.removeWatchedGame(username, gameId));
    }
}
