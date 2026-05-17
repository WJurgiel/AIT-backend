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

        String payload = """
                {
                  "count": 1,
                  "results": [
                    {
                      "id": 1,
                      "slug": "the-witcher",
                      "name": "The Witcher"
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo("https://api.rawg.io/api/games?key=test-key&stores=5&search=the-witcher"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        RawgGamesResponseDto result = rawgService.searchGames(5, "the-witcher");

        assertEquals(1, result.getResults().size());
        assertEquals("The Witcher", result.getResults().getFirst().getName());
        mockServer.verify();
    }
}

