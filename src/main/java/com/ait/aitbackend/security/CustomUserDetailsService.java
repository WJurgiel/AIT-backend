package com.ait.aitbackend.security;

import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementacja UserDetailsService wykorzystywana przez Spring Security
 * do pobierania użytkownika z bazy danych podczas logowania.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserProfileRepository userRepository;

    public CustomUserDetailsService(UserProfileRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Ładuje użytkownika na podstawie username i mapuje go na Spring Security UserDetails.
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        UserProfile userProfile = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        return User.builder()
                .username(userProfile.getUsername())
                .password(userProfile.getPassword())
                .roles("USER")
                .build();
    }
}