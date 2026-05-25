package com.ait.aitbackend.games.cache;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheapSharkDealsCacheServiceTest {

    /**
     * Upewnia się, że serwis poprawnie pobiera i zwraca z bazy aktualne (niewygasłe) dane ofert zbuforowane pod konkretnym kluczem.
     */
    @Test
    void shouldReturnFreshDealsFromCache() {
        CheapSharkDealsCacheRepository repository = mock(CheapSharkDealsCacheRepository.class);
        CheapSharkDealsCacheService cacheService = new CheapSharkDealsCacheService(repository, 300);

        CheapSharkDealDto deal = new CheapSharkDealDto(
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
        );

        String key = cacheService.buildCacheKey(1);
        CheapSharkDealsCacheDocument document = new CheapSharkDealsCacheDocument(
                key + ":0",
                key,
                0,
                1,
                "100",
                deal,
                Instant.now(),
                Instant.now().plusSeconds(60)
        );

        when(repository.findAllByCacheKeyAndExpiresAtAfterOrderByResultOrderAsc(eq(key), any(Instant.class)))
                .thenReturn(List.of(document));

        Optional<List<CheapSharkDealDto>> result = cacheService.getFreshDeals(1);

        assertTrue(result.isPresent());
        assertEquals("deal-1", result.get().getFirst().dealId());
    }

    /**
     * Sprawdza system unieważniania danych. Jeśli wpisy w cache'u straciły na ważności, serwis powinien je zignorować i zwrócić pusty Optional.
     */
    @Test
    void shouldIgnoreExpiredCacheEntry() {
        CheapSharkDealsCacheRepository repository = mock(CheapSharkDealsCacheRepository.class);
        CheapSharkDealsCacheService cacheService = new CheapSharkDealsCacheService(repository, 300);

        String key = cacheService.buildCacheKey(1);
        when(repository.findAllByCacheKeyAndExpiresAtAfterOrderByResultOrderAsc(eq(key), any(Instant.class)))
                .thenReturn(List.of());

        Optional<List<CheapSharkDealDto>> result = cacheService.getFreshDeals(1);

        assertTrue(result.isEmpty());
    }

    /**
     * Weryfikuje procedurę zapisu. Najpierw usuwane są stare dane dla danego klucza, a nowa lista zapisywana jest do cache'a z zachowaniem właściwej kolejności.
     */
    @Test
    void shouldSaveEachDealAsSeparateDocument() {
        CheapSharkDealsCacheRepository repository = mock(CheapSharkDealsCacheRepository.class);
        CheapSharkDealsCacheService cacheService = new CheapSharkDealsCacheService(repository, 300);

        List<CheapSharkDealDto> deals = List.of(
                new CheapSharkDealDto(
                        "INTERNAL-1",
                        "Game 1",
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
                        "thumb-1"
                ),
                new CheapSharkDealDto(
                        "INTERNAL-2",
                        "Game 2",
                        null,
                        "deal-2",
                        "1",
                        "101",
                        "7.99",
                        "29.99",
                        "1",
                        "73.0",
                        "70",
                        "Mostly Positive",
                        "75",
                        "500",
                        "124",
                        2L,
                        2L,
                        "8.8",
                        "thumb-2"
                )
        );

        cacheService.saveDeals(1, deals);

        verify(repository).deleteAllByCacheKey(cacheService.buildCacheKey(1));

        @SuppressWarnings({"unchecked", "rawtypes"})
        ArgumentCaptor<Iterable<CheapSharkDealsCacheDocument>> documentsCaptor = (ArgumentCaptor) ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(documentsCaptor.capture());

        List<CheapSharkDealsCacheDocument> savedDocuments = StreamSupport.stream(
                documentsCaptor.getValue().spliterator(),
                false
        ).toList();

        assertEquals(2, savedDocuments.size());
        assertEquals(0, savedDocuments.getFirst().getResultOrder());
        assertEquals("deal-1", savedDocuments.getFirst().getDeal().dealId());
        assertEquals("100", savedDocuments.getFirst().getGameId());
        assertEquals(1, savedDocuments.getLast().getResultOrder());
        assertEquals("deal-2", savedDocuments.getLast().getDeal().dealId());
        assertEquals("101", savedDocuments.getLast().getGameId());
    }

    /**
     * Sprawdza, czy przy próbie zapisania zupełnie pustej listy ofert, stary cache zostaje wyczyszczony bez tworzenia żadnych nowych dokumentów.
     */
    @Test
    void shouldRemoveExistingCacheEntriesWhenSavingEmptyList() {
        CheapSharkDealsCacheRepository repository = mock(CheapSharkDealsCacheRepository.class);
        CheapSharkDealsCacheService cacheService = new CheapSharkDealsCacheService(repository, 300);

        cacheService.saveDeals(1, List.of());

        verify(repository).deleteAllByCacheKey(cacheService.buildCacheKey(1));
        verify(repository, org.mockito.Mockito.never()).saveAll(any());
    }
}


