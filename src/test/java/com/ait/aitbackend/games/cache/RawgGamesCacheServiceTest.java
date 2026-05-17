package com.ait.aitbackend.games.cache;

import com.ait.aitbackend.games.dto.rawg.RawgGamesPageResponse;
import com.ait.aitbackend.games.dto.rawg.RawgGamesResponseDto;
import com.ait.aitbackend.games.service.RawgGamesMappingService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RawgGamesCacheServiceTest {

    @Test
    void shouldSaveOnlyValidGamesUsingBatchSaveAll() {
        RawgGameCacheRepository repository = mock(RawgGameCacheRepository.class);
        RawgGamesMappingService mappingService = mock(RawgGamesMappingService.class);
        RawgGamesCacheService service = new RawgGamesCacheService(repository, mappingService, 86400);

        RawgGamesResponseDto.RawgGameDto valid = new RawgGamesResponseDto.RawgGameDto();
        valid.setId(1);
        valid.setName("The Witcher");
        valid.setSlug("the-witcher");

        RawgGamesResponseDto.RawgGameDto invalid = new RawgGamesResponseDto.RawgGameDto();
        invalid.setName("Broken");

        RawgGamesResponseDto response = new RawgGamesResponseDto();
        response.setResults(List.of(valid, invalid));

        service.saveGames(response);

        verify(repository).saveAll(anyList());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnCachedGamesPageMappedForFrontend() {
        RawgGameCacheRepository repository = mock(RawgGameCacheRepository.class);
        RawgGamesMappingService mappingService = mock(RawgGamesMappingService.class);
        RawgGamesCacheService service = new RawgGamesCacheService(repository, mappingService, 86400);
        Pageable pageable = PageRequest.of(0, 10);

        RawgGameCacheDocument document = new RawgGameCacheDocument(
                "v1:rawg:game:1",
                1,
                "The Witcher",
                "the-witcher",
                "2007-10-26",
                "thumb",
                4.8,
                92,
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );
        Page<RawgGameCacheDocument> page = new PageImpl<>(List.of(document), pageable, 1);

        when(repository.findByNameContainingIgnoreCaseAndExpiresAtAfter(eq("witcher"), any(), eq(pageable)))
                .thenReturn(page);
        when(mappingService.findCheapSharkGameId("the-witcher", "The Witcher")).thenReturn(java.util.Optional.of("123"));

        RawgGamesPageResponse response = service.getCachedGamesPage("witcher", pageable);

        assertEquals(1, response.totalElements());
        assertEquals("The Witcher", response.content().getFirst().name());
        assertEquals("123", response.content().getFirst().cheapsharkGameId());
        assertEquals(1, response.totalPages());
        assertTrue(response.last());
    }

    @Test
    void shouldUseAllCachedGamesWhenSearchIsBlank() {
        RawgGameCacheRepository repository = mock(RawgGameCacheRepository.class);
        RawgGamesMappingService mappingService = mock(RawgGamesMappingService.class);
        RawgGamesCacheService service = new RawgGamesCacheService(repository, mappingService, 86400);
        Pageable pageable = PageRequest.of(0, 10);

        Page<RawgGameCacheDocument> page = new PageImpl<>(List.of(), pageable, 0);
        when(repository.findByExpiresAtAfter(any(), any())).thenReturn(page);

        Page<RawgGameCacheDocument> result = service.searchGamesByName("   ", pageable);

        assertEquals(0, result.getTotalElements());
        verify(repository).findByExpiresAtAfter(any(), any());
    }
}



