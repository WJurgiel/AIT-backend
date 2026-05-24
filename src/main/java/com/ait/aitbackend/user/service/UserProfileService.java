package com.ait.aitbackend.user.service;

import com.ait.aitbackend.user.dto.UpdatePasswordRequest;
import com.ait.aitbackend.user.dto.UpdateProfileRequest;
import com.ait.aitbackend.user.dto.UserPreferencesDto;
import com.ait.aitbackend.user.entity.UserPreferences;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.exceptions.InvalidPasswordException;
import com.ait.aitbackend.user.exceptions.UserAlreadyExistsException;
import com.ait.aitbackend.user.exceptions.UserDoesNotExistException;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serwis odpowiedzialny za zarządzanie profilem użytkownika,
 * preferencjami oraz listą ulubionych gier.
 */
@Service
@AllArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Pobiera użytkownika lub rzuca wyjątek jeśli nie istnieje.
     */
    public UserProfile getOrThrow(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserDoesNotExistException(
                                "User '" + username + "' not found"
                        )
                );
    }

    /**
     * Aktualizacja danych profilu użytkownika.
     */
    public UserProfile updateProfile(
            String currentUsername,
            UpdateProfileRequest req) {

        UserProfile user = getOrThrow(currentUsername);

        // zmiana username
        if (req.username() != null &&
                !req.username().equals(currentUsername)) {

            if (userRepository.existsByUsername(req.username()))
                throw new UserAlreadyExistsException(
                        "Username '" + req.username() + "' is already taken"
                );

            user.setUsername(req.username());
        }

        // zmiana emaila
        if (req.email() != null &&
                !req.email().equals(user.getEmail())) {

            if (userRepository.existsByEmail(req.email()))
                throw new UserAlreadyExistsException(
                        "Email '" + req.email() + "' is already in use"
                );

            user.setEmail(req.email());
        }

        return userRepository.save(user);
    }

    /**
     * Zmiana hasła użytkownika.
     */
    public void updatePassword(String username, UpdatePasswordRequest req) {
        UserProfile user = getOrThrow(username);

        // walidacja aktualnego hasła
        if (!passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    /**
     * Pobranie preferencji użytkownika.
     */
    public UserPreferencesDto getPreferences(String username) {

        UserProfile user = getOrThrow(username);

        return toDto(user.getPreferences());
    }

    /**
     * Aktualizacja preferencji użytkownika.
     */
    public UserPreferencesDto updatePreferences(
            String username,
            UserPreferencesDto dto) {

        UserProfile user = getOrThrow(username);

        UserPreferences p = user.getPreferences();

        if (dto.platforms() != null)
            p.setPlatformList(dto.platforms());

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

    /**
     * Dodanie gry do ulubionych.
     */
    public void addFavoriteGame(String username, String gameId) {
        UserProfile user = getOrThrow(username);
        UserPreferences p = user.getPreferences();

        List<String> favorites = p.getFavoriteGameIdsList();

        if (!favorites.contains(gameId)) {
            favorites.add(gameId);
            p.setFavoriteGameIdsList(favorites);
            userRepository.save(user);
        }
    }

    /**
     * Usunięcie gry z ulubionych.
     */
    public void removeFavoriteGame(String username, String gameId) {
        UserProfile user = getOrThrow(username);
        UserPreferences p = user.getPreferences();

        List<String> favorites = p.getFavoriteGameIdsList();

        if (favorites.remove(gameId)) {
            p.setFavoriteGameIdsList(favorites);
            userRepository.save(user);
        }
    }

    /**
     * Pobranie listy ulubionych gier.
     */
    public List<String> getFavoriteGames(String username) {
        UserProfile user = getOrThrow(username);
        return user.getPreferences().getFavoriteGameIdsList();
    }

    /**
     * Sprawdzenie czy gra jest ulubiona.
     */
    public boolean isFavorite(String username, String gameId) {
        return getFavoriteGames(username).contains(gameId);
    }

    /**
     * Konwersja encji preferencji do DTO.
     */
    private UserPreferencesDto toDto(UserPreferences p) {
        return new UserPreferencesDto(
                p.getPlatformList(),
                new UserPreferencesDto.NotificationsDto(
                        p.isWishlistOnSale(),
                        p.isDailyDigest(),
                        p.isFlashSales(),
                        p.isPriceDropAlerts()
                ),
                p.getFavoriteGameIdsList()
        );
    }
}