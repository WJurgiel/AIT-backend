package com.ait.aitbackend.auth.controller;

import com.ait.aitbackend.auth.dto.login.LoginRequest;
import com.ait.aitbackend.auth.dto.login.LoginResponse;
import com.ait.aitbackend.auth.dto.registration.RegistrationRequest;
import com.ait.aitbackend.auth.dto.registration.RegistrationResponse;
import com.ait.aitbackend.auth.service.AuthService;
import com.ait.aitbackend.user.entity.UserProfile;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kontroler odpowiedzialny za logowanie i rejestrację użytkowników.
 * Obsługuje endpointy związane z autoryzacją oraz JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // Konfiguracja ciasteczka JWT pobierana z application.properties
    @Value("${app.security.jwt.cookie-name:jwt}")
    private String jwtCookieName;

    @Value("${app.security.jwt.cookie-max-age-seconds:86400}")
    private long jwtCookieMaxAgeSeconds;

    @Value("${app.security.jwt.cookie-secure:false}")
    private boolean jwtCookieSecure;

    @Value("${app.security.jwt.cookie-same-site:Lax}")
    private String jwtCookieSameSite;

    public AuthController(AuthService authService)
    {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request)
    {
        // Logowanie użytkownika i wygenerowanie JWT
        String token = authService.loginUser(
                request.username(),
                request.password()
        );

        // Utworzenie ciasteczka HTTP-only z tokenem
        ResponseCookie jwtCookie = ResponseCookie.from(jwtCookieName, token)
                .httpOnly(true)
                .secure(jwtCookieSecure)
                .path("/")
                .maxAge(jwtCookieMaxAgeSeconds)
                .sameSite(jwtCookieSameSite)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new LoginResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegistrationRequest request)
    {
        // Rejestracja nowego użytkownika
        UserProfile createdUser = authService.registerUser(
                request.username(),
                request.email(),
                request.password()
        );

        RegistrationResponse response =
                new RegistrationResponse(createdUser.getUsername());

        // Automatyczne logowanie po rejestracji
        String token = authService.loginUser(
                request.username(),
                request.password()
        );

        // Utworzenie ciasteczka JWT
        ResponseCookie jwtCookie = ResponseCookie.from(jwtCookieName, token)
                .httpOnly(true)
                .secure(jwtCookieSecure)
                .path("/")
                .maxAge(jwtCookieMaxAgeSeconds)
                .sameSite(jwtCookieSameSite)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }
}