package com.ait.aitbackend.user.controller;

import com.ait.aitbackend.user.dto.UserDto;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.exceptions.UserAlreadyExistsException;
import com.ait.aitbackend.user.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileController.class)
public class UserProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserProfileService userService;

    private final String username1 = "TestPlayer123";
    private final String username2 = "TestPlayer12345";
    private final String email1 = "test@mail.com";
    private final String email2 = "other@mail.com";
    @Test
    void shouldReturn200AndUserWhenCreatingUser() throws Exception
    {
        UserDto request = new UserDto(username1, email1);
        UserProfile createdUser = new UserProfile(username1, email1);
        createdUser.setId(99L);

        when(userService.createUser(username1, email1)).thenReturn(createdUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.username").value(username1))
                .andExpect(jsonPath("$.email").value(email1));
    }

    @Test
    void shouldReturnConflictWhenUsernameOrEmailAlreadyExists() throws Exception
    {
        UserDto firstRequest = new UserDto(username1, email1);
        UserDto secondRequest = new UserDto(username1, email2);
        UserDto thirdRequest = new UserDto(username2, email1);

        UserProfile createdUser = new UserProfile(username1, email1);
        createdUser.setId(99L);

        when(userService.createUser(username1, email1)).thenReturn(createdUser);
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        when(userService.createUser(username1, email2)).thenThrow(UserAlreadyExistsException.class);
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict());

        when(userService.createUser(username2, email1)).thenThrow(UserAlreadyExistsException.class);
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(thirdRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestWhenInvalidEmailOrUsernameProvided() throws Exception
    {
        String badUsername = "A";
        String badEmail = "invalid";
        UserDto firstBadRequest = new UserDto(badUsername, email1);
        UserDto secondBadRequest = new UserDto(username1, badEmail);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstBadRequest)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondBadRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenNoEmailOrUsernameProvided() throws Exception
    {
        UserDto firstBadRequest = new UserDto(null, email1);
        UserDto secondBadRequest = new UserDto(username1, null);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstBadRequest)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondBadRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception
    {
        // GIVEN
        when(userService.getUserByUsername("Unknown")).thenReturn(Optional.empty());

        // WHEN & THEN
        mockMvc.perform(get("/api/users/Unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200AndListOfUsersWhenGettingAllUsers() throws Exception
    {
        List<UserProfile> existingUsers = new LinkedList<UserProfile>();
        existingUsers.add(new UserProfile(username1, email1));
        existingUsers.add(new UserProfile(username2, email2));

        when(userService.getAllUsers()).thenReturn(existingUsers);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(2));
    }
}
