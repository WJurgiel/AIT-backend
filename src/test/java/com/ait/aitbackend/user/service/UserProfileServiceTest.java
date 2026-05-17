package com.ait.aitbackend.user.service;

import com.ait.aitbackend.games.cache.RawgGameCacheDocument;
import com.ait.aitbackend.games.cache.RawgGamesCacheService;
import com.ait.aitbackend.games.service.RawgGamesMappingService;
import com.ait.aitbackend.user.dto.AddWatchedGameRequest;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RawgGamesCacheService rawgGamesCacheService;

    @Mock
    private RawgGamesMappingService rawgGamesMappingService;

    @InjectMocks
    private UserProfileService userService;

    private final String username1 = "TestPlayer123";
    private final String username2 = "TestPlayer12345";
    private final String email1 = "test@mail.com";
    private final String email2 = "other@mail.com";
    private final String password1 = "mockpassword1234!";

    @Test
    void shouldFindUserByUsername()
    {
        UserProfile existingUser = new UserProfile(username1, email1, password1);
        when(userRepository.findByUsername(username1)).thenReturn(Optional.of(existingUser));

        Optional<UserProfile> result = userService.getUserByUsername(username1);

        assertTrue(result.isPresent());
        assertEquals(username1, result.get().getUsername());
        assertEquals(email1, result.get().getEmail());
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
        existingUsers.add(new UserProfile(username1, email1, password1));
        existingUsers.add(new UserProfile(username2, email2, password1));

        when(userRepository.findAll()).thenReturn(existingUsers);

        List<UserProfile> result = userService.getAllUsers();
        assertEquals(2, result.size());
        assertEquals(username1, result.getFirst().getUsername());
        assertEquals(email1, result.getFirst().getEmail());
        assertEquals(username2, result.getLast().getUsername());
        assertEquals(email2, result.getLast().getEmail());
    }

    @Test
    void shouldReturnWatchedGameIdsInPreferences() {
        UserProfile existingUser = new UserProfile(username1, email1, password1);
        existingUser.getPreferences().setWatchedGameIdList(new java.util.ArrayList<>(List.of("CS-1", "CS-2")));
        when(userRepository.findByUsername(username1)).thenReturn(Optional.of(existingUser));

        var result = userService.getPreferences(username1);

        assertEquals(List.of("CS-1", "CS-2"), result.watchedGameIds());
    }

    @Test
    void shouldAddWatchedGameByRawgId() {
        UserProfile user = new UserProfile(username1, email1, password1);
        RawgGameCacheDocument rawgGame = new RawgGameCacheDocument(
                "v1:rawg:game:1",
                1,
                "The Witcher",
                "the-witcher",
                "2007-10-26",
                "thumb",
                4.8,
                92,
                java.time.Instant.now(),
                java.time.Instant.now().plusSeconds(3600)
        );

        when(userRepository.findByUsername(username1)).thenReturn(Optional.of(user));
        when(rawgGamesCacheService.getFreshGameByRawgId(1)).thenReturn(Optional.of(rawgGame));
        when(rawgGamesMappingService.findCheapSharkGameId("the-witcher", "The Witcher")).thenReturn(Optional.of("CS-1"));

        var result = userService.addWatchedGame(username1, new AddWatchedGameRequest(1));

        assertEquals(List.of("CS-1"), result.watchedGameIds());
        verify(userRepository).save(user);
    }

    @Test
    void shouldRemoveWatchedGame() {
        UserProfile user = new UserProfile(username1, email1, password1);
        user.getPreferences().setWatchedGameIdList(new java.util.ArrayList<>(List.of("CS-1", "CS-2")));

        when(userRepository.findByUsername(username1)).thenReturn(Optional.of(user));

        var result = userService.removeWatchedGame(username1, "CS-1");

        assertEquals(List.of("CS-2"), result.watchedGameIds());
        verify(userRepository).save(user);
    }
}
