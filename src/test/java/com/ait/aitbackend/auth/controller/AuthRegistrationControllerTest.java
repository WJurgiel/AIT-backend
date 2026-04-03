package com.ait.aitbackend.auth.controller;

import com.ait.aitbackend.auth.dto.registration.RegistrationRequest;
import com.ait.aitbackend.auth.service.AuthService;
import com.ait.aitbackend.security.JwtService;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.exceptions.UserAlreadyExistsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthRegistrationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    private final String username1 = "TestPlayer123";
    private final String username2 = "TestPlayer12345";
    private final String email1 = "test@mail.com";
    private final String email2 = "other@mail.com";
    private final String password1 = "mockpassword1234!";

    @Test
    void shouldReturn200AndValidResponseWhenRegister() throws Exception {
        RegistrationRequest request = new RegistrationRequest(username1, email1, password1);
        UserProfile registeredUser = new UserProfile(username1, email1, password1);
        registeredUser.setId(99L);

        when(authService.registerUser(username1, email1, password1)).thenReturn(registeredUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username1));
    }

    @Test
    void shouldReturnConflictWhenUsernameOrEmailAlreadyExistsWhenRegister() throws Exception {
        RegistrationRequest firstRequest = new RegistrationRequest(username1, email1, password1);
        RegistrationRequest secondRequest = new RegistrationRequest(username2, email1, password1);
        RegistrationRequest thirdRequest = new RegistrationRequest(username1, email2, password1);

        UserProfile createdUser = new UserProfile(username1, email1, password1);
        createdUser.setId(99L);

        when(authService.registerUser(username1, email1, password1)).thenReturn(createdUser);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        when(authService.registerUser(username2, email1, password1)).thenThrow(UserAlreadyExistsException.class);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict());

        when(authService.registerUser(username1, email2, password1)).thenThrow(UserAlreadyExistsException.class);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(thirdRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestWhenInvalidEmailOrUsernameProvided() throws Exception {
        String badUsername = "A";
        String badEmail = "invalid";
        RegistrationRequest firstBadRequest = new RegistrationRequest(badUsername, email1, password1);
        RegistrationRequest secondBadRequest = new RegistrationRequest(username1, badEmail, password1);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstBadRequest)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondBadRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenNoEmailOrUsernameProvided() throws Exception {
        RegistrationRequest firstBadRequest = new RegistrationRequest(null, email1, password1);
        RegistrationRequest secondBadRequest = new RegistrationRequest(username1, null, password1);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstBadRequest)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondBadRequest)))
                .andExpect(status().isBadRequest());
    }
}