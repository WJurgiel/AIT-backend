package com.ait.aitbackend.user.repository;

import com.ait.aitbackend.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repozytorium odpowiedzialne za operacje na encji UserProfile.
 * Umożliwia komunikację z bazą danych.
 */
@Repository
public interface UserProfileRepository
        extends JpaRepository<UserProfile, Long> {

    // Wyszukiwanie użytkownika po username
    Optional<UserProfile> findByUsername(String username);

    // Sprawdzenie czy username już istnieje
    boolean existsByUsername(String username);

    // Sprawdzenie czy email już istnieje
    boolean existsByEmail(String email);
}