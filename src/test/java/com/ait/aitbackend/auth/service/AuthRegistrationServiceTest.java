package com.ait.aitbackend.user.service;

import com.ait.aitbackend.auth.service.AuthService;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.exceptions.UserAlreadyExistsException;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthRegistrationServiceTest {
    @Mock
    private UserProfileRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private final String username1 = "TestPlayer123";
    private final String username2 = "TestPlayer12345";
    private final String email1 = "test@mail.com";
    private final String email2 = "other@mail.com";
    private final String password1 = "mockpassword1234!";

    @Test
    void shouldCreateNewUser()
    {
        UserProfile mockUser = new UserProfile(username1, email1, password1);
        mockUser.setId(1L);

        when(userRepository.save(any(UserProfile.class))).thenReturn(mockUser);

        UserProfile result = authService.registerUser(username1, email1, password1);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(username1, result.getUsername());

        verify(userRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists()
    {
        when(userRepository.existsByUsername(username1)).thenReturn(true);

        UserAlreadyExistsException thrownException = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.registerUser(username1, email1, password1));

        assertTrue(thrownException.getMessage().contains("already exists"));

        verify(userRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists()
    {
        when(userRepository.existsByEmail(email1)).thenReturn(true);

        UserAlreadyExistsException thrownException = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.registerUser(username1, email1, password1));

        assertTrue(thrownException.getMessage().contains("already exists"));

        verify(userRepository, never()).save(any(UserProfile.class));
    }
}
