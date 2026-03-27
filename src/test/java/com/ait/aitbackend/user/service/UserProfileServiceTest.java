package com.ait.aitbackend.user.service;

import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.exceptions.UserAlreadyExistsException;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userRepository;

    @InjectMocks
    private UserProfileService userService;

    private final String username1 = "TestPlayer123";
    private final String username2 = "TestPlayer12345";
    private final String email1 = "test@mail.com";
    private final String email2 = "other@mail.com";

    @Test
    void shouldCreateNewUser()
    {
        UserProfile mockUser = new UserProfile(username1, email1);
        mockUser.setId(1L);

        when(userRepository.save(any(UserProfile.class))).thenReturn(mockUser);

        UserProfile result = userService.createUser(username1, email1);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(username1, result.getUsername());

        verify(userRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void shouldFindUserByUsername()
    {
        UserProfile existingUser = new UserProfile(username1, email1);
        when(userRepository.findByUsername(username1)).thenReturn(Optional.of(existingUser));

        Optional<UserProfile> result = userService.getUserByUsername(username1);

        assertTrue(result.isPresent());
        assertEquals(username1, result.get().getUsername());
        assertEquals(email1, result.get().getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists()
    {
        when(userRepository.existsByUsername(username1)).thenReturn(true);

        UserAlreadyExistsException thrownException = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(username1, email1));

        assertTrue(thrownException.getMessage().contains("already exists"));

        verify(userRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists()
    {
        when(userRepository.existsByEmail(email1)).thenReturn(true);

        UserAlreadyExistsException thrownException = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(username1, email1));

        assertTrue(thrownException.getMessage().contains("already exists"));

        verify(userRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void shouldReturnEmptyOptionalWhenUserNotFound()
    {
        when(userRepository.findByUsername(username1)).thenReturn(Optional.empty());

        Optional<UserProfile> result = userService.getUserByUsername(username1);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnAllUsers()
    {
        List<UserProfile> existingUsers = new LinkedList<UserProfile>();
        existingUsers.add(new UserProfile(username1, email1));
        existingUsers.add(new UserProfile(username2, email2));

        when(userRepository.findAll()).thenReturn(existingUsers);

        List<UserProfile> result = userService.getAllUsers();
        assertEquals(2, result.size());
        assertEquals(username1, result.getFirst().getUsername());
        assertEquals(email1, result.getFirst().getEmail());
        assertEquals(username2, result.getLast().getUsername());
        assertEquals(email2, result.getLast().getEmail());
    }
}
