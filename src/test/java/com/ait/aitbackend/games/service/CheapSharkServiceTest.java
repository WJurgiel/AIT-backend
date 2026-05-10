package com.ait.aitbackend.games.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CheapSharkServiceTest {

    private static final String API_BASE_URL = "https://www.cheapshark.com/api/1.0";
    private static final String REDIRECT_BASE_URL = "https://www.cheapshark.com";

    @Test
    void shouldReturnDealsPayload() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        com.ait.aitbackend.games.cache.CheapSharkDealsCacheService cacheService = mock(com.ait.aitbackend.games.cache.CheapSharkDealsCacheService.class);
        when(cacheService.getFreshDeals(1, 1)).thenReturn(Optional.empty());
        CheapSharkService cheapSharkService = new CheapSharkService(restClientBuilder, API_BASE_URL, REDIRECT_BASE_URL, cacheService);

        String payload = "[{\"dealID\":\"abc123\"}]";

        mockServer.expect(requestTo("https://www.cheapshark.com/api/1.0/deals?storeID=1&onSale=1"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        List<com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto> result = cheapSharkService.getDeals(1, 1);

        assertEquals(1, result.size());
        assertEquals("abc123", result.getFirst().dealId());
        verify(cacheService).saveDeals(eq(1), eq(1), any());
        mockServer.verify();
    }

    @Test
    void shouldReturnCachedDealsWithoutCallingExternalApi() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        com.ait.aitbackend.games.cache.CheapSharkDealsCacheService cacheService = mock(com.ait.aitbackend.games.cache.CheapSharkDealsCacheService.class);

        List<com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto> cachedDeals = List.of(
                new com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto(
                        "INTERNAL",
                        "Cached Game",
                        null,
                        "cached-1",
                        "1",
                        "10",
                        "1.99",
                        "9.99",
                        "1",
                        "80.0",
                        "0",
                        "N/A",
                        "0",
                        "0",
                        null,
                        0L,
                        0L,
                        "0.0",
                        "thumb"
                )
        );

        when(cacheService.getFreshDeals(1, 1)).thenReturn(Optional.of(cachedDeals));
        CheapSharkService cheapSharkService = new CheapSharkService(restClientBuilder, API_BASE_URL, REDIRECT_BASE_URL, cacheService);

        List<com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto> result = cheapSharkService.getDeals(1, 1);

        assertEquals(1, result.size());
        assertEquals("cached-1", result.getFirst().dealId());
        verify(cacheService, never()).saveDeals(eq(1), eq(1), any());
        mockServer.verify();
    }

    @Test
    void shouldForceRefreshDealsBypassingCache() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        com.ait.aitbackend.games.cache.CheapSharkDealsCacheService cacheService = mock(com.ait.aitbackend.games.cache.CheapSharkDealsCacheService.class);
        CheapSharkService cheapSharkService = new CheapSharkService(restClientBuilder, API_BASE_URL, REDIRECT_BASE_URL, cacheService);

        String payload = "[{\"dealID\":\"fresh-1\"}]";

        mockServer.expect(requestTo("https://www.cheapshark.com/api/1.0/deals?storeID=1&onSale=1"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        List<com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto> result = cheapSharkService.refreshDeals(1, 1);

        assertEquals(1, result.size());
        assertEquals("fresh-1", result.getFirst().dealId());
        verify(cacheService).saveDeals(eq(1), eq(1), any());
        mockServer.verify();
    }

    @Test
    void shouldEncodeDealIdWithSpecialCharacters() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        com.ait.aitbackend.games.cache.CheapSharkDealsCacheService cacheService = mock(com.ait.aitbackend.games.cache.CheapSharkDealsCacheService.class);
        CheapSharkService cheapSharkService = new CheapSharkService(restClientBuilder, API_BASE_URL, REDIRECT_BASE_URL, cacheService);

        String dealId = "x77a6faCQSCDjyCF/e6U0ed+202eYPAdrpMjRjoJvYc=";
        String payload = "{\"gameInfo\":{\"gameID\":\"112330\"},\"cheaperStores\":[],\"cheapestPrice\":{\"price\":\"3.99\",\"date\":1766082419}}";

        mockServer.expect(requestTo("https://www.cheapshark.com/api/1.0/deals?id=x77a6faCQSCDjyCF%2Fe6U0ed%2B202eYPAdrpMjRjoJvYc%3D"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDetailsDto result = cheapSharkService.getDealById(dealId);

        assertEquals("112330", result.gameInfo().gameId());
        mockServer.verify();
    }

    @Test
    void shouldNotDoubleEncodeAlreadyEncodedDealId() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        com.ait.aitbackend.games.cache.CheapSharkDealsCacheService cacheService = mock(com.ait.aitbackend.games.cache.CheapSharkDealsCacheService.class);
        CheapSharkService cheapSharkService = new CheapSharkService(restClientBuilder, API_BASE_URL, REDIRECT_BASE_URL, cacheService);

        String encodedDealId = "x77a6faCQSCDjyCF%2Fe6U0ed%2B202eYPAdrpMjRjoJvYc%3D";
        String payload = "{\"gameInfo\":{\"gameID\":\"112330\"},\"cheaperStores\":[],\"cheapestPrice\":{\"price\":\"3.99\",\"date\":1766082419}}";

        mockServer.expect(requestTo("https://www.cheapshark.com/api/1.0/deals?id=x77a6faCQSCDjyCF%2Fe6U0ed%2B202eYPAdrpMjRjoJvYc%3D"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDetailsDto result = cheapSharkService.getDealById(encodedDealId);

        assertEquals("112330", result.gameInfo().gameId());
        mockServer.verify();
    }

    @Test
    void shouldBuildRedirectUrlFromRawDealId() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        com.ait.aitbackend.games.cache.CheapSharkDealsCacheService cacheService = mock(com.ait.aitbackend.games.cache.CheapSharkDealsCacheService.class);
        CheapSharkService cheapSharkService = new CheapSharkService(restClientBuilder, API_BASE_URL, REDIRECT_BASE_URL, cacheService);

        String dealId = "x77a6faCQSCDjyCF/e6U0ed+202eYPAdrpMjRjoJvYc=";

        String result = cheapSharkService.buildRedirectUrl(dealId);

        assertEquals(
                "https://www.cheapshark.com/redirect?dealID=x77a6faCQSCDjyCF%2Fe6U0ed%2B202eYPAdrpMjRjoJvYc%3D",
                result
        );
    }

    @Test
    void shouldBuildRedirectUrlWithoutDoubleEncoding() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        com.ait.aitbackend.games.cache.CheapSharkDealsCacheService cacheService = mock(com.ait.aitbackend.games.cache.CheapSharkDealsCacheService.class);
        CheapSharkService cheapSharkService = new CheapSharkService(restClientBuilder, API_BASE_URL, REDIRECT_BASE_URL, cacheService);

        String dealId = "x77a6faCQSCDjyCF%2Fe6U0ed%2B202eYPAdrpMjRjoJvYc%3D";

        String result = cheapSharkService.buildRedirectUrl(dealId);

        assertEquals(
                "https://www.cheapshark.com/redirect?dealID=x77a6faCQSCDjyCF%2Fe6U0ed%2B202eYPAdrpMjRjoJvYc%3D",
                result
        );
    }
}


