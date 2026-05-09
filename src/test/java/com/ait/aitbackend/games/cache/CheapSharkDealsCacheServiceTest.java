package com.ait.aitbackend.games.cache;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheapSharkDealsCacheServiceTest {

    @Test
    void shouldReturnFreshDealsFromCache() {
        CheapSharkDealsCacheRepository repository = mock(CheapSharkDealsCacheRepository.class);
        CheapSharkDealsCacheService cacheService = new CheapSharkDealsCacheService(repository, new ObjectMapper(), 300);

        List<CheapSharkDealDto> deals = List.of(new CheapSharkDealDto(
                "INTERNAL",
                "Game",
                null,
                "deal-1",
                "1",
                "100",
                "4.99",
                "19.99",
                "1",
                "75.0",
                "80",
                "Positive",
                "80",
                "1000",
                "123",
                1L,
                1L,
                "9.0",
                "thumb"
        ));

        String key = cacheService.buildCacheKey(1, 1);
        CheapSharkDealsCacheDocument document = new CheapSharkDealsCacheDocument(
                key,
                1,
                1,
                "[{\"dealID\":\"deal-1\"}]",
                1,
                Instant.now(),
                Instant.now().plusSeconds(60)
        );

        when(repository.findById(key)).thenReturn(Optional.of(document));

        Optional<List<CheapSharkDealDto>> result = cacheService.getFreshDeals(1, 1);

        assertTrue(result.isPresent());
        assertEquals("deal-1", result.get().getFirst().dealId());
    }

    @Test
    void shouldIgnoreExpiredCacheEntry() {
        CheapSharkDealsCacheRepository repository = mock(CheapSharkDealsCacheRepository.class);
        CheapSharkDealsCacheService cacheService = new CheapSharkDealsCacheService(repository, new ObjectMapper(), 300);

        String key = cacheService.buildCacheKey(1, 1);
        CheapSharkDealsCacheDocument document = new CheapSharkDealsCacheDocument(
                key,
                1,
                1,
                "[]",
                0,
                Instant.now().minusSeconds(600),
                Instant.now().minusSeconds(1)
        );

        when(repository.findById(key)).thenReturn(Optional.of(document));

        Optional<List<CheapSharkDealDto>> result = cacheService.getFreshDeals(1, 1);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveDealsWithExpiration() {
        CheapSharkDealsCacheRepository repository = mock(CheapSharkDealsCacheRepository.class);
        CheapSharkDealsCacheService cacheService = new CheapSharkDealsCacheService(repository, new ObjectMapper(), 300);

        cacheService.saveDeals(1, 1, List.of());

        verify(repository).save(any(CheapSharkDealsCacheDocument.class));
    }
}


