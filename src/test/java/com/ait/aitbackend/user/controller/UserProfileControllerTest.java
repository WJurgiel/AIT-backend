package com.ait.aitbackend.user.controller;

import com.ait.aitbackend.user.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(UserProfileControllerTest.class)
public class UserProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

//    @Autowired
//    private ObjectMapper objectMapper;

    @MockitoBean
    private UserProfileService userService;

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        // GIVEN
        when(userService.getUserByUsername("Unknown")).thenReturn(Optional.empty());

        // WHEN & THEN
        mockMvc.perform(get("/api/users/Unknown"))
                .andExpect(status().isNotFound());
    }
}
