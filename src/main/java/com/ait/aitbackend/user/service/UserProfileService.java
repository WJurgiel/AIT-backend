package com.ait.aitbackend.user.service;

import com.ait.aitbackend.user.dto.UserAboutMeResponse;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserProfileService {
    private final UserProfileRepository userRepository;

    public UserProfileService(UserProfileRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
    }

    public List<UserProfile> getAllUsers()
    {
        return userRepository.findAll();
    }

    public Optional<UserProfile> getUserByUsername(String username)
    {
        return userRepository.findByUsername(username);
    }
}
