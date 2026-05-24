package com.ait.aitbackend.auth.validator;

import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.exceptions.UserAlreadyExistsException;
import com.ait.aitbackend.user.exceptions.UserDoesNotExistException;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Klasa odpowiedzialna za walidację danych użytkownika
 * podczas rejestracji i logowania.
 */
@Component
@AllArgsConstructor
public class UserValidator {

    UserProfileRepository userRepository;
    PasswordEncoder passwordEncoder;

    public void validateUserRegister(String username, String email) {

        // Sprawdzenie czy nazwa użytkownika już istnieje
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException(
                    "Username '" + username + "' already exists!"
            );
        }

        // Sprawdzenie czy email jest już zajęty
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(
                    "User with email '" + email +
                            "' already exists - please log in to proceed."
            );
        }
    }

    public void validateUserLogin(String username, String password) {

        // Pobranie użytkownika z bazy lub wyrzucenie wyjątku
        UserProfile user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserDoesNotExistException("User not found")
                );

        // Sprawdzenie poprawności hasła
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
    }
}