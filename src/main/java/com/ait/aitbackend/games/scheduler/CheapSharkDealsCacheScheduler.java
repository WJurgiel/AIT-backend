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

@Component
public class CheapSharkDealsCacheScheduler {
    private static final Logger log = LoggerFactory.getLogger(CheapSharkDealsCacheScheduler.class);

    private final CheapSharkService cheapSharkService;
    private final List<Integer> storeIds;
    private final Integer onSale;

    public CheapSharkDealsCacheScheduler(
            CheapSharkService cheapSharkService,
            @Value("${cheapshark.cache.deals.scheduler.store-ids:1,7,25}") String storeIdsCsv,
            @Value("${cheapshark.cache.deals.scheduler.on-sale:1}") Integer onSale
    ) {
        this.cheapSharkService = cheapSharkService;
        this.storeIds = Arrays.stream(storeIdsCsv.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(Integer::valueOf)
                .toList();
        this.onSale = onSale;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCacheOnStartup() {
        log.info("Warming up CheapShark deals cache on application startup for stores {} and onSale={}", storeIds, onSale);
        refreshAllDeals();
    }

    @Scheduled(
            fixedDelayString = "${cheapshark.cache.deals.ttl-seconds:3600}000",
            initialDelayString = "${cheapshark.cache.deals.ttl-seconds:3600}000"
    )
    public void refreshAllDealsScheduled() {
        refreshAllDeals();
    }

    public void refreshAllDeals() {
        for (Integer storeId : storeIds) {
            try {
                log.info("Refreshing CheapShark deals cache for storeId={} onSale={}", storeId, onSale);
                cheapSharkService.refreshDeals(storeId, onSale);
            } catch (Exception ex) {
                log.error("Failed to refresh CheapShark deals cache for storeId={} onSale={}", storeId, onSale, ex);
            }
        }
    }
}


