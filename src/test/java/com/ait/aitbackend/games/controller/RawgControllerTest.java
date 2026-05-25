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

    /**
     * Sprawdza wyszukiwanie tytułów w RAWG – przekazana z parametryzacji nazwa wyszukanej gry musi znaleźć się w zwróconej poprawnej, ustrukturyzowanej liście json.
     * @throws Exception
     */
    @Test
    void shouldReturnRawgPayload() throws Exception {
        RawgGamesResponseDto.RawgGameDto game = new RawgGamesResponseDto.RawgGameDto();
        game.setName("The Witcher");

        RawgGamesResponseDto dto = new RawgGamesResponseDto();
        dto.setResults(List.of(game));

        when(rawgService.searchGames("the-witcher")).thenReturn(dto);

        mockMvc.perform(get("/api/rawg/games")
                        .param("search", "the-witcher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].name").value("The Witcher"));
    }

    /**
     * Upewnia się, że endpoint pozwala na elastyczne wyszukiwanie i nadal działa bez problemów w sytuacji, gdy opcjonalne parametry sklepów są pominięte
     * @throws Exception
     */
    @Test
    void shouldReturnRawgPayloadWithoutStoresFilter() throws Exception {
        RawgGamesResponseDto.RawgGameDto game = new RawgGamesResponseDto.RawgGameDto();
        game.setName("The Witcher");

        RawgGamesResponseDto dto = new RawgGamesResponseDto();
        dto.setResults(List.of(game));

        when(rawgService.searchGames("the-witcher")).thenReturn(dto);

        mockMvc.perform(get("/api/rawg/games")
                        .param("search", "the-witcher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].name").value("The Witcher"));
    }

    /**
     * Weryfikuje działanie odpytywania o specyficzny tytuł – wprowadzony identyfikator gry w adresie endpointu powinien skutkować pobraniem jego konkretnych danych szczegółowych
     * @throws Exception
     */
    @Test
    void shouldReturnSingleGame() throws Exception {
        RawgGamesResponseDto.RawgGameDto game = new RawgGamesResponseDto.RawgGameDto();
        game.setId(123);
        game.setName("The Witcher");

        when(rawgService.getGameById(123)).thenReturn(game);

        mockMvc.perform(get("/api/rawg/games/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.name").value("The Witcher"));
    }
}

