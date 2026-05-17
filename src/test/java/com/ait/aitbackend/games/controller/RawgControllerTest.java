package com.ait.aitbackend.games.controller;

import com.ait.aitbackend.games.cache.RawgGamesCacheService;
import com.ait.aitbackend.games.dto.rawg.RawgGameListItemDto;
import com.ait.aitbackend.games.dto.rawg.RawgGamesPageResponse;
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

import static org.mockito.ArgumentMatchers.any;
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
    private RawgGamesCacheService rawgGamesCacheService;

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

    @Test
    void shouldReturnCachedPagedGames() throws Exception {
        RawgGamesPageResponse response = new RawgGamesPageResponse(
                List.of(new RawgGameListItemDto(123, "The Witcher 3", "the-witcher-3", "2015-05-18", "thumb", 4.9, 93, "987")),
                0,
                20,
                1,
                1,
                true
        );

        when(rawgGamesCacheService.getCachedGamesPage(any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/rawg/games/cached")
                        .param("search", "witcher")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("The Witcher 3"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}

