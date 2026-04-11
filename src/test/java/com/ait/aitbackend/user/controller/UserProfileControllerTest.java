package com.ait.aitbackend.user.controller;

import com.ait.aitbackend.security.JwtService;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileController.class)
public class UserProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProfileService userService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtService jwtService;

    private final String username1 = "TestPlayer123";
    private final String username2 = "TestPlayer12345";
    private final String email1 = "test@mail.com";
    private final String email2 = "other@mail.com";
    private final String password1 = "mockpassword1234!";


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
        existingUsers.add(new UserProfile(username1, email1, password1));
        existingUsers.add(new UserProfile(username2, email2, password1));

        when(userService.getAllUsers()).thenReturn(existingUsers);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(2));
    }
}
