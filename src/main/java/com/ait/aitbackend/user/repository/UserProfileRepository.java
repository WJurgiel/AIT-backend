package com.ait.aitbackend.user.repository;

import com.ait.aitbackend.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
