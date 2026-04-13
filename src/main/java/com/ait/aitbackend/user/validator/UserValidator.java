package com.ait.aitbackend.user.validator;

import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.exceptions.UserAlreadyExistsException;
import com.ait.aitbackend.user.exceptions.UserDoesNotExistException;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserValidator {

    UserProfileRepository userRepository;
    PasswordEncoder passwordEncoder;

    public void validateUserRegister(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username '" + username + "' already exists!");
        }

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User with email '" + email + "' already exists - please log in to proceed.");
        }
    }

    public void validateUserLogin(String username, String password) {
        UserProfile user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserDoesNotExistException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
    }
}
