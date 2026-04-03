package com.ait.aitbackend.auth.service;

import com.ait.aitbackend.security.JwtService;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.exceptions.UserAlreadyExistsException;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserProfileRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    public AuthService(
            UserProfileRepository userRepository,
            AuthenticationManager authenticationManger,
            JwtService jwtService,
            PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManger;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public String loginUser(String username, String password)
    {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        return jwtService.generateToken(authentication.getName());
    }

    public UserProfile registerUser(String username, String email, String password)
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
}
