package com.ait.aitbackend.user.service;

import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    void shouldCreateNewUser()
    {
        // GIVEN
        UserProfile mockUser = new UserProfile("TestUser", "test@test.com");
        mockUser.setId(1L);

        when(userRepository.save(any(UserProfile.class))).thenReturn(mockUser);

        // WHEN
        UserProfile result = userService.createUser("TestUser", "test@test.com");

        // THEN
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TestUser", result.getUsername());

        verify(userRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void shouldFindUserByUsername()
    {
        // GIVEN
        UserProfile existingUser = new UserProfile("TestUser", "test@test.com");
        when(userRepository.findByUsername("TestUser")).thenReturn(Optional.of(existingUser));

        // WHEN
        Optional<UserProfile> result = userService.getUserByUsername("TestUser");

        // THEN
        assertTrue(result.isPresent());
        assertEquals("test@test.com", result.get().getEmail());
    }
}
