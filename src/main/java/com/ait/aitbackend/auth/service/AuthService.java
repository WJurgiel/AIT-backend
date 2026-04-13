package com.ait.aitbackend.auth.service;

import com.ait.aitbackend.security.JwtService;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import com.ait.aitbackend.user.validator.UserValidator;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserProfileRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    public String loginUser(String username, String password)
    {
        userValidator.validateUserLogin(username, password);

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
        userValidator.validateUserRegister(username, email);

        String hashedPassword = passwordEncoder.encode(password);

        UserProfile newUser = new UserProfile(username, email, hashedPassword);
        return userRepository.save(newUser);
    }
}
