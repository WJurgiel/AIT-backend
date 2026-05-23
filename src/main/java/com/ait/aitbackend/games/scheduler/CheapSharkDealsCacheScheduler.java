package com.ait.aitbackend.games.scheduler;

import com.ait.aitbackend.games.service.CheapSharkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Scheduler odpowiedzialny za odświeżanie cache ofert z CheapShark.
 * Uruchamia się przy starcie aplikacji oraz cyklicznie co określony czas.
 */
@Component
public class CheapSharkDealsCacheScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(CheapSharkDealsCacheScheduler.class);

    private final CheapSharkService cheapSharkService;

    // Lista ID sklepów, dla których pobierane są oferty
    private final List<Integer> storeIds;

    public CheapSharkDealsCacheScheduler(
            CheapSharkService cheapSharkService,

            // Domyślnie: Steam, Epic, itd.
            @Value("${cheapshark.cache.deals.scheduler.store-ids:1,7,25}")
            String storeIdsCsv
    ) {
        this.cheapSharkService = cheapSharkService;

        // Konwersja CSV -> lista Integer
        this.storeIds = Arrays.stream(storeIdsCsv.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(Integer::valueOf)
                .toList();
    }

    /**
     * Warm-up cache przy starcie aplikacji
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCacheOnStartup() {

        log.info(
                "Warming up CheapShark deals cache for stores {}",
                storeIds
        );

        refreshAllDeals();
    }

    /**
     * Cykliczne odświeżanie cache co określony czas
     */
    @Scheduled(
            fixedDelayString =
                    "${cheapshark.cache.deals.ttl-seconds:3600}000",
            initialDelayString =
                    "${cheapshark.cache.deals.ttl-seconds:3600}000"
    )
    public void refreshAllDealsScheduled() {

        refreshAllDeals();
    }

    /**
     * Odświeżenie ofert dla wszystkich skonfigurowanych sklepów
     */
    public void refreshAllDeals() {

        for (Integer storeId : storeIds) {

            try {
                log.info(
                        "Refreshing CheapShark cache for storeId={}",
                        storeId
                );

                cheapSharkService.refreshDeals(storeId);

            } catch (Exception ex) {

                log.error(
                        "Failed to refresh cache for storeId={}",
                        storeId,
                        ex
                );
            }
        }
    }
}