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
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    public UserPreferencesDto updatePreferences(String username, UserPreferencesDto dto) {
        UserProfile user = getOrThrow(username);
        UserPreferences p = user.getPreferences();

        if (dto.platforms() != null) p.setPlatformList(dto.platforms());

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
                new UserPreferencesDto.NotificationsDto(
                        p.isWishlistOnSale(),
                        p.isDailyDigest(),
                        p.isFlashSales(),
                        p.isPriceDropAlerts()
                )
        );
    }
}
