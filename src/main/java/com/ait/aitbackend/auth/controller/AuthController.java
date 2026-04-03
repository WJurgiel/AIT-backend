package com.ait.aitbackend.auth.controller;


import com.ait.aitbackend.auth.dto.login.LoginRequest;
import com.ait.aitbackend.auth.dto.login.LoginResponse;
import com.ait.aitbackend.auth.dto.registration.RegistrationRequest;
import com.ait.aitbackend.auth.dto.registration.RegistrationResponse;
import com.ait.aitbackend.auth.service.AuthService;
import com.ait.aitbackend.user.entity.UserProfile;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService)
    {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login (@Valid @RequestBody LoginRequest request)
    {
        String token = authService.loginUser(request.username(), request.password());

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register (@Valid @RequestBody RegistrationRequest request)
    {
        UserProfile createdUser = authService.registerUser(
                request.username(),
                request.email(),
                request.password());
        RegistrationResponse response = new RegistrationResponse(createdUser.getUsername());

        return ResponseEntity.ok(response);
    }
}
