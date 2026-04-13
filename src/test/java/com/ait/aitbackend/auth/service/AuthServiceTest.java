package com.ait.aitbackend.auth.service;

import com.ait.aitbackend.security.JwtService;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import com.ait.aitbackend.user.validator.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserProfileRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private AuthService authService;

    private final String username = "testUser";
    private final String email = "test@mail.com";
    private final String password = "password123";

    // ===== REGISTER =====

    @Test
    void shouldRegisterUserSuccessfully() {
        String encodedPassword = "encodedPassword";
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        UserProfile savedUser = new UserProfile(username, email, encodedPassword);
        savedUser.setId(1L);

        when(userRepository.save(any(UserProfile.class))).thenReturn(savedUser);

        UserProfile result = authService.registerUser(username, email, password);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(encodedPassword, result.getPassword());

        verify(userValidator).validateUserRegister(username, email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(UserProfile.class));
    }

    @Test
    void shouldThrowExceptionWhenValidationFailsDuringRegister() {
        doThrow(new RuntimeException("Validation failed"))
                .when(userValidator).validateUserRegister(username, email);

        assertThrows(RuntimeException.class, () ->
                authService.registerUser(username, email, password)
        );

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    // ===== LOGIN =====

    @Test
    void shouldLoginUserSuccessfully() {
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getName()).thenReturn(username);
        String token = "jwt-token";
        when(jwtService.generateToken(username)).thenReturn(token);

        String result = authService.loginUser(username, password);

        assertEquals(token, result);

        verify(userValidator).validateUserLogin(username, password);
        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateToken(username);
    }

    @Test
    void shouldThrowExceptionWhenValidationFailsDuringLogin() {
        doThrow(new RuntimeException("Invalid credentials"))
                .when(userValidator).validateUserLogin(username, password);

        assertThrows(RuntimeException.class, () ->
                authService.loginUser(username, password)
        );

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtService, never()).generateToken(any());
    }
}