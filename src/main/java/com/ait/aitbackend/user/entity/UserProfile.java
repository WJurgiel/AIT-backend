package com.ait.aitbackend.user.entity;

import jakarta.persistence.*;

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

    public UserProfile() {}

    public UserProfile(String username, String email)
    {
        this.username = username;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id;}
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
