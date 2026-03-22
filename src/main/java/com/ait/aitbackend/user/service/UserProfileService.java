package com.ait.aitbackend.user.service;

import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserProfileService {
    private final UserProfileRepository userRepository;

    public UserProfileService(UserProfileRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    public UserProfile createUser(String username, String email)
    {
        UserProfile newUser = new UserProfile(username, email);
        return userRepository.save(newUser);
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
