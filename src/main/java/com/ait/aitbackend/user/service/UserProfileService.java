package com.ait.aitbackend.user.service;

import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.exceptions.UserAlreadyExistsException;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserProfileService {
    private final UserProfileRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileService(UserProfileRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfile createUser(String username, String email, String password)
    {
        if (userRepository.existsByUsername(username))
        {
            throw new UserAlreadyExistsException("Username '" + username + "' already exists!");
        }
        if (userRepository.existsByEmail(email))
        {
            throw new UserAlreadyExistsException("User with email '" + email + "' already exists - please log in to proceed.");
        }
        String hashedPassword = passwordEncoder.encode(password);

        UserProfile newUser = new UserProfile(username, email, hashedPassword);
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
