package com.ait.aitbackend.user.controller;

import com.ait.aitbackend.security.JwtService;
import com.ait.aitbackend.user.dto.UserAboutMeResponse;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.service.UserProfileService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserProfileController {
    private final UserProfileService userService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfile> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username).
                map(ResponseEntity::ok).
                orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserAboutMeResponse> getAboutMe(@CookieValue(name="jwt") String token)
    {
        String currentUsername = jwtService.extractUsername(token);

        UserProfile user = userService.getUserByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new UserAboutMeResponse(user.getUsername(), user.getEmail()));
    }

}
