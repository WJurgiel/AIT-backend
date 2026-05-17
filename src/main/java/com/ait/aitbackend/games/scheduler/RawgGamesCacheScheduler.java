package com.ait.aitbackend.games.scheduler;

import com.ait.aitbackend.games.cache.RawgGamesCacheService;
import com.ait.aitbackend.games.dto.rawg.RawgGamesResponseDto;
import com.ait.aitbackend.games.service.RawgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for caching all RAWG games.
 * Periodically fetches games from RAWG API and stores them in MongoDB.
 * Runs on application startup and then every 24 hours.
 */
@Component
public class RawgGamesCacheScheduler {
	private static final Logger log = LoggerFactory.getLogger(RawgGamesCacheScheduler.class);

	private final RawgService rawgService;
	private final RawgGamesCacheService cacheService;
	private final String storeIds;
	private final int pageSize;

	public RawgGamesCacheScheduler(
			RawgService rawgService,
			RawgGamesCacheService cacheService,
			@Value("${rawg.cache.scheduler.store-ids:1,5,11}") String storeIds,
			@Value("${rawg.cache.scheduler.page-size:40}") int pageSize
	) {
		this.rawgService = rawgService;
		this.cacheService = cacheService;
		this.storeIds = storeIds;
		this.pageSize = pageSize;
	}

	/**
	 * Warm up cache on application startup
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void warmUpCacheOnStartup() {
		log.info("Warming up RAWG games cache on application startup for stores: {} with page size: {}",
				storeIds, pageSize);
		refreshAllGames();
	}

	/**
	 * Refresh all games cache every 24 hours
	 * Fixed delay is 24 hours (86400 seconds), with initial delay of 24 hours
	 */
	@Scheduled(
			fixedDelayString = "${rawg.cache.scheduler.ttl-seconds:86400}000",
			initialDelayString = "${rawg.cache.scheduler.ttl-seconds:86400}000"
	)
	public void refreshAllGamesScheduled() {
		refreshAllGames();
	}

	/**
	 * Refresh all games from RAWG API
	 * Fetches games paginated, starting from page 1
	 */
	public void refreshAllGames() {
		try {
			log.info("Starting RAWG games cache refresh for stores: {}", storeIds);
			int page = 1;
			int totalFetched = 0;

			while (true) {
				try {
					log.debug("Fetching RAWG games - page: {}", page);

					RawgGamesResponseDto response = rawgService.getAllGames(storeIds, pageSize, page);

					if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
						log.info("No more games to fetch from RAWG. Total games fetched and cached: {}", totalFetched);
						break;
					}

					// Save games to cache
					cacheService.saveGames(response);
					totalFetched += response.getResults().size();
					log.debug("Cached {} games from page {}", response.getResults().size(), page);

					if (response.getNext() == null || response.getNext().isBlank()) {
						log.info("Reached last RAWG page {}. Total games fetched and cached: {}", page, totalFetched);
						break;
					}

					page++;
				} catch (HttpClientErrorException.NotFound notFound) {
					log.info("RAWG reported no more pages at page {}. Stopping cache refresh gracefully.", page);
					break;
				} catch (Exception e) {
					log.error("Error fetching RAWG games for page: {}", page, e);
					// Continue with next page instead of failing completely for transient issues
					page++;
				}
			}

			log.info("Successfully completed RAWG games cache refresh. Total games cached: {}", totalFetched);
		} catch (Exception e) {
			log.error("Critical error during RAWG games cache refresh", e);
		}
	}
}

