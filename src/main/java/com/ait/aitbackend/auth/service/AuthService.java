package com.ait.aitbackend.auth.service;

import com.ait.aitbackend.security.JwtService;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import com.ait.aitbackend.auth.validator.UserValidator;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Serwis odpowiedzialny za logowanie i rejestrację użytkowników.
 * Obsługuje walidację danych, uwierzytelnianie oraz generowanie JWT.
 */
@Service
@AllArgsConstructor
public class AuthService {

    private final UserProfileRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    public String loginUser(String username, String password)
    {
        // Walidacja danych logowania
        userValidator.validateUserLogin(username, password);

        // Uwierzytelnienie użytkownika przez Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        // Wygenerowanie tokena JWT dla zalogowanego użytkownika
        return jwtService.generateToken(authentication.getName());
    }

    public UserProfile registerUser(String username, String email, String password)
    {
        // Walidacja danych rejestracji
        userValidator.validateUserRegister(username, email);

        // Haszowanie hasła przed zapisaniem do bazy
        String hashedPassword = passwordEncoder.encode(password);

        // Tworzenie i zapis nowego użytkownika
        UserProfile newUser = new UserProfile(username, email, hashedPassword);

        return userRepository.save(newUser);
    }
}