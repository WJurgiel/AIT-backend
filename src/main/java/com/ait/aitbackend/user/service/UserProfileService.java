package com.ait.aitbackend.user.service;

import com.ait.aitbackend.user.dto.UpdatePasswordRequest;
import com.ait.aitbackend.user.dto.UpdateProfileRequest;
import com.ait.aitbackend.user.dto.AddWatchedGameRequest;
import com.ait.aitbackend.user.dto.UserPreferencesDto;
import com.ait.aitbackend.games.cache.RawgGameCacheDocument;
import com.ait.aitbackend.games.cache.RawgGamesCacheService;
import com.ait.aitbackend.games.service.RawgGamesMappingService;
import com.ait.aitbackend.user.entity.UserPreferences;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.exceptions.FavoriteGameNotFoundException;
import com.ait.aitbackend.user.exceptions.InvalidPasswordException;
import com.ait.aitbackend.user.exceptions.UserAlreadyExistsException;
import com.ait.aitbackend.user.exceptions.UserDoesNotExistException;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RawgGamesCacheService rawgGamesCacheService;
    private final RawgGamesMappingService rawgGamesMappingService;

    public List<UserProfile> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserProfile> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserProfile getOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserDoesNotExistException("User '" + username + "' not found"));
    }

    public UserProfile updateProfile(String currentUsername, UpdateProfileRequest req) {
        UserProfile user = getOrThrow(currentUsername);

        if (req.username() != null && !req.username().equals(currentUsername)) {
            if (userRepository.existsByUsername(req.username()))
                throw new UserAlreadyExistsException("Username '" + req.username() + "' is already taken");
            user.setUsername(req.username());
        }

        if (req.email() != null && !req.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(req.email()))
                throw new UserAlreadyExistsException("Email '" + req.email() + "' is already in use");
            user.setEmail(req.email());
        }

        return userRepository.save(user);
    }

    public void updatePassword(String username, UpdatePasswordRequest req) {
        UserProfile user = getOrThrow(username);

        if (!passwordEncoder.matches(req.currentPassword(), user.getPassword()))
            throw new InvalidPasswordException("Current password is incorrect");

        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    public UserPreferencesDto getPreferences(String username) {
        UserProfile user = getOrThrow(username);
        UserPreferences p = user.getPreferences();
        return toDto(p);
    }

    public UserPreferencesDto addWatchedGame(String username, AddWatchedGameRequest req) {
        UserProfile user = getOrThrow(username);
        RawgGameCacheDocument rawgGame = rawgGamesCacheService.getFreshGameByRawgId(req.rawgId())
                .orElseThrow(() -> new FavoriteGameNotFoundException("RAWG game with id '" + req.rawgId() + "' was not found in cache"));

        String cheapSharkGameId = rawgGamesMappingService.findCheapSharkGameId(rawgGame.getSlug(), rawgGame.getName())
                .orElseThrow(() -> new FavoriteGameNotFoundException(
                        "Could not map RAWG game '" + rawgGame.getName() + "' to a CheapShark gameId"
                ));

        UserPreferences p = user.getPreferences();
        p.addWatchedGameId(cheapSharkGameId);
        userRepository.save(user);
        return toDto(p);
    }

    public UserPreferencesDto removeWatchedGame(String username, String gameId) {
        UserProfile user = getOrThrow(username);
        UserPreferences p = user.getPreferences();
        p.removeWatchedGameId(gameId);
        userRepository.save(user);
        return toDto(p);
    }

    public UserPreferencesDto updatePreferences(String username, UserPreferencesDto dto) {
        UserProfile user = getOrThrow(username);
        UserPreferences p = user.getPreferences();

        if (dto.platforms() != null) p.setPlatformList(dto.platforms());
        if (dto.genres() != null) p.setGenreList(dto.genres());
        if (dto.watchedGameIds() != null) p.setWatchedGameIdList(dto.watchedGameIds());

        if (dto.notifications() != null) {
            var n = dto.notifications();
            p.setWishlistOnSale(n.wishlistOnSale());
            p.setDailyDigest(n.dailyDigest());
            p.setFlashSales(n.flashSales());
            p.setPriceDropAlerts(n.priceDropAlerts());
        }

        userRepository.save(user);
        return toDto(p);
    }

    private UserPreferencesDto toDto(UserPreferences p) {
        return new UserPreferencesDto(
                p.getPlatformList(),
                p.getGenreList(),
                p.getWatchedGameIdList(),
                new UserPreferencesDto.NotificationsDto(
                        p.isWishlistOnSale(),
                        p.isDailyDigest(),
                        p.isFlashSales(),
                        p.isPriceDropAlerts()
                )
        );
    }
}
