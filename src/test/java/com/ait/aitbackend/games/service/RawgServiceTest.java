package com.ait.aitbackend.games.service;

import com.ait.aitbackend.games.dto.rawg.RawgGamesResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RawgServiceTest {

    @Test
    void shouldReturnGamesPayload() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RawgService rawgService = new RawgService(restClientBuilder, "https://api.rawg.io/api", "test-key");

        String payload = "{\"results\":[{\"name\":\"The Witcher\"}]}";

        mockServer.expect(requestTo("https://api.rawg.io/api/games?key=test-key&search=the-witcher"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        RawgGamesResponseDto result = rawgService.searchGames("the-witcher");

        assertEquals(1, result.getResults().size());
        assertEquals("The Witcher", result.getResults().getFirst().getName());
        mockServer.verify();
    }

    @Test
    void shouldReturnSingleGamePayload() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RawgService rawgService = new RawgService(restClientBuilder, "https://api.rawg.io/api", "test-key");

        String payload = "{\"id\":123,\"name\":\"The Witcher\"}";

        mockServer.expect(requestTo("https://api.rawg.io/api/games/123?key=test-key"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        RawgGamesResponseDto.RawgGameDto result = rawgService.getGameById(123);

        assertEquals(123, result.getId());
        assertEquals("The Witcher", result.getName());
        mockServer.verify();
    }

    @Test
    void shouldReturnGamesPayloadWithoutStoresFilter() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RawgService rawgService = new RawgService(restClientBuilder, "https://api.rawg.io/api", "test-key");

        String payload = "{\"results\":[{\"name\":\"The Witcher\"}]}";

        mockServer.expect(requestTo("https://api.rawg.io/api/games?key=test-key&search=the-witcher"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        RawgGamesResponseDto result = rawgService.searchGames("the-witcher");

        assertEquals(1, result.getResults().size());
        assertEquals("The Witcher", result.getResults().getFirst().getName());
        mockServer.verify();
    }
}

