package com.ait.aitbackend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Tabela przechowująca dane użytkownika.
 * Zawiera id, nazwę użytkownika, email, hasło, datę utworzenia oraz preferencje.
 */
@Setter
@Getter
@Entity
@Table(name = "users")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Embedded
    private UserPreferences preferences = new UserPreferences();

    public UserProfile() {}

    public UserProfile(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
        this.preferences = new UserPreferences();
    }
}
