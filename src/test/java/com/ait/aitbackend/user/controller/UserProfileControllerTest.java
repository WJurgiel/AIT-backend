package com.ait.aitbackend.user.controller;

import com.ait.aitbackend.security.JwtService;
import com.ait.aitbackend.user.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void shouldAddFavoriteUsingQueryParam() throws Exception {
        when(jwtService.extractUsername("jwt")).thenReturn(username1);

        mockMvc.perform(post("/api/users/me/favorites")
                        .cookie(new jakarta.servlet.http.Cookie("jwt", "jwt"))
                        .param("gameId", "C5QpU2wB0LJGRegXgpnA6MANIOpLL8HuNKpp+Q/UC8s="))
                .andExpect(status().isNoContent());

        verify(userService).addFavoriteGame(username1, "C5QpU2wB0LJGRegXgpnA6MANIOpLL8HuNKpp+Q/UC8s=");
    }

    @Test
    void shouldRemoveFavoriteUsingQueryParam() throws Exception {
        when(jwtService.extractUsername("jwt")).thenReturn(username1);

        mockMvc.perform(delete("/api/users/me/favorites")
                        .cookie(new jakarta.servlet.http.Cookie("jwt", "jwt"))
                        .param("gameId", "C5QpU2wB0LJGRegXgpnA6MANIOpLL8HuNKpp+Q/UC8s="))
                .andExpect(status().isNoContent());

        verify(userService).removeFavoriteGame(username1, "C5QpU2wB0LJGRegXgpnA6MANIOpLL8HuNKpp+Q/UC8s=");
    }
}
