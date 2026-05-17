package com.ait.aitbackend.games.scheduler;

import com.ait.aitbackend.games.cache.RawgGamesCacheService;
import com.ait.aitbackend.games.dto.rawg.RawgGamesResponseDto;
import com.ait.aitbackend.games.service.RawgService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RawgGamesCacheSchedulerTest {

    @Test
    void shouldStopWhenRawgResponseHasNoNextPage() {
        RawgService rawgService = mock(RawgService.class);
        RawgGamesCacheService cacheService = mock(RawgGamesCacheService.class);
        RawgGamesCacheScheduler scheduler = new RawgGamesCacheScheduler(rawgService, cacheService, "1,5,11", 40);

        RawgGamesResponseDto.RawgGameDto game = new RawgGamesResponseDto.RawgGameDto();
        game.setId(1);
        game.setSlug("the-witcher");
        game.setName("The Witcher");

        RawgGamesResponseDto response = new RawgGamesResponseDto();
        response.setResults(List.of(game));
        response.setNext(null);

        when(rawgService.getAllGames(anyString(), anyInt(), anyInt())).thenReturn(response);

        scheduler.refreshAllGames();

        verify(rawgService).getAllGames("1,5,11", 40, 1);
        verify(rawgService, never()).getAllGames("1,5,11", 40, 2);
        verify(cacheService).saveGames(response);
    }

    @Test
    void shouldStopGracefullyOnInvalidPage404() {
        RawgService rawgService = mock(RawgService.class);
        RawgGamesCacheService cacheService = mock(RawgGamesCacheService.class);
        RawgGamesCacheScheduler scheduler = new RawgGamesCacheScheduler(rawgService, cacheService, "1,5,11", 40);

        RawgGamesResponseDto.RawgGameDto game = new RawgGamesResponseDto.RawgGameDto();
        game.setId(1);
        game.setSlug("the-witcher");
        game.setName("The Witcher");

        RawgGamesResponseDto response = new RawgGamesResponseDto();
        response.setResults(List.of(game));
        response.setNext("https://api.rawg.io/api/games?page=2");

        HttpClientErrorException notFound = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                "{\"detail\":\"Invalid page.\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(rawgService.getAllGames(anyString(), anyInt(), anyInt()))
                .thenReturn(response)
                .thenThrow(notFound);

        assertDoesNotThrow(scheduler::refreshAllGames);

        verify(rawgService).getAllGames("1,5,11", 40, 1);
        verify(rawgService).getAllGames("1,5,11", 40, 2);
        // Ensure cache was written once and scheduler stopped gracefully after 404.
        verify(cacheService).saveGames(response);
    }
}



