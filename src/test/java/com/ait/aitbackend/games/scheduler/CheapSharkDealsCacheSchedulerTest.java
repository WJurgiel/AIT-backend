package com.ait.aitbackend.games.scheduler;

import com.ait.aitbackend.games.service.CheapSharkService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CheapSharkDealsCacheSchedulerTest {

    @Test
    void shouldRefreshAllConfiguredStores() {
        CheapSharkService cheapSharkService = mock(CheapSharkService.class);
        CheapSharkDealsCacheScheduler scheduler = new CheapSharkDealsCacheScheduler(
                cheapSharkService,
                "1, 7,25"
        );

        scheduler.refreshAllDeals();

        verify(cheapSharkService).refreshDeals(1);
        verify(cheapSharkService).refreshDeals(7);
        verify(cheapSharkService).refreshDeals(25);
    }
}

