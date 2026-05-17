package com.ait.aitbackend.games.service;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkGameSearchDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RawgGamesMappingServiceTest {

    @Test
    void shouldConvertRawgSlugToCheapSharkInternalName() {
        RawgGamesMappingService service = new RawgGamesMappingService(mock(CheapSharkService.class));

        assertEquals("THEWITCHERENHANCEDEDITIONDIRECTORSCUT",
                service.convertSlugToInternalName("the-witcher-enhanced-edition-directors-cut"));
    }

    @Test
    void shouldNotQueryCheapSharkWhenSlugAndNameAreBlank() {
        CheapSharkService cheapSharkService = mock(CheapSharkService.class);
        RawgGamesMappingService service = new RawgGamesMappingService(cheapSharkService);

        assertTrue(service.findCheapSharkGameId(" ", "").isEmpty());
        verify(cheapSharkService, never()).searchGamesByTitle(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldFallbackToGameNameWhenInternalNameSearchFails() {
        CheapSharkService cheapSharkService = mock(CheapSharkService.class);
        RawgGamesMappingService service = new RawgGamesMappingService(cheapSharkService);

        when(cheapSharkService.searchGamesByTitle("THEWITCHER")).thenReturn(List.of());
        when(cheapSharkService.searchGamesByTitle("Wiedźmin")).thenReturn(List.of(
                new CheapSharkGameSearchDto("123", null, null, null, "WIEDŹMIN", "WIEDŹMIN", null)
        ));

        assertEquals("123", service.findCheapSharkGameId("the-witcher", "Wiedźmin").orElseThrow());
        verify(cheapSharkService).searchGamesByTitle("THEWITCHER");
        verify(cheapSharkService).searchGamesByTitle("Wiedźmin");
    }

    @Test
    void shouldCacheMappingForRepeatedSlugLookups() {
        CheapSharkService cheapSharkService = mock(CheapSharkService.class);
        RawgGamesMappingService service = new RawgGamesMappingService(cheapSharkService);

        when(cheapSharkService.searchGamesByTitle("THEWITCHER")).thenReturn(List.of(
                new CheapSharkGameSearchDto("123", null, null, null, "THEWITCHER", "THEWITCHER", null)
        ));

        assertEquals("123", service.findCheapSharkGameId("the-witcher", "Wiedźmin").orElseThrow());
        assertEquals("123", service.findCheapSharkGameId("the-witcher", "Wiedźmin 2").orElseThrow());

        verify(cheapSharkService).searchGamesByTitle("THEWITCHER");
        verify(cheapSharkService, never()).searchGamesByTitle("Wiedźmin 2");
    }
}

