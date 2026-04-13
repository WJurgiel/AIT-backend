package com.ait.aitbackend.user.validator;

import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.exceptions.UserAlreadyExistsException;
import com.ait.aitbackend.user.exceptions.UserDoesNotExistException;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserProfileRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserValidator userValidator;

    private final String username = "testUser";
    private final String email = "test@mail.com";
    private final String password = "password123";
    private final String encodedPassword = "encodedPassword";

    // ===== REGISTER =====

    @Test
    void shouldPassValidationWhenUserDoesNotExist() {
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);

        assertDoesNotThrow(() ->
                userValidator.validateUserRegister(username, email)
        );
    }

    @Test
    void shouldThrowExceptionWhenUsernameExists() {
        when(userRepository.existsByUsername(username)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () ->
                userValidator.validateUserRegister(username, email)
        );
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () ->
                userValidator.validateUserRegister(username, email)
        );
    }

    // ===== LOGIN =====

    @Test
    void shouldPassLoginWhenCredentialsAreCorrect() {
        UserProfile user = new UserProfile(username, email, encodedPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        assertDoesNotThrow(() ->
                userValidator.validateUserLogin(username, password)
        );
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistException.class, () ->
                userValidator.validateUserLogin(username, password)
        );
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsInvalid() {
        UserProfile user = new UserProfile(username, email, encodedPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () ->
                userValidator.validateUserLogin(username, password)
        );
    }
}