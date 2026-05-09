package com.ait.aitbackend.games.controller;

import com.ait.aitbackend.games.dto.rawg.RawgGamesResponseDto;
import com.ait.aitbackend.games.service.RawgService;
import com.ait.aitbackend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RawgController.class)
class RawgControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RawgService rawgService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldReturnRawgPayload() throws Exception {
        RawgGamesResponseDto.RawgGameDto game = new RawgGamesResponseDto.RawgGameDto();
        game.setName("The Witcher");

        RawgGamesResponseDto dto = new RawgGamesResponseDto();
        dto.setResults(List.of(game));

        when(rawgService.searchGames(5, "the-witcher")).thenReturn(dto);

        mockMvc.perform(get("/api/rawg/games")
                        .param("stores", "5")
                        .param("search", "the-witcher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].name").value("The Witcher"));
    }
}

