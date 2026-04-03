package com.ait.aitbackend.user.controller;

import com.ait.aitbackend.user.dto.UserCreateDto;
import com.ait.aitbackend.user.dto.UserResponseDTO;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {
    private final UserProfileService userService;

    public UserProfileController(UserProfileService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDto request)
    {
        UserProfile createdUser = userService.createUser(request.username(), request.email(), request.password());
        UserResponseDTO response = new UserResponseDTO(createdUser.getId(), createdUser.getUsername(), createdUser.getEmail());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfile> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username).
                map(user -> ResponseEntity.ok(user)).
                orElse(ResponseEntity.notFound().build());
    }
}
