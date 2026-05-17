package com.ait.aitbackend.games.cache;

import com.ait.aitbackend.games.dto.rawg.RawgGameListItemDto;
import com.ait.aitbackend.games.dto.rawg.RawgGamesPageResponse;
import com.ait.aitbackend.games.dto.rawg.RawgGamesResponseDto;
import com.ait.aitbackend.games.service.RawgGamesMappingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for caching RAWG games in MongoDB.
 * Handles storing and retrieving game data from cache with TTL.
 */
@Service
public class RawgGamesCacheService {
	private final RawgGameCacheRepository cacheRepository;
	private final RawgGamesMappingService mappingService;
	private final long ttlSeconds;

	public RawgGamesCacheService(
			RawgGameCacheRepository cacheRepository,
			RawgGamesMappingService mappingService,
			@Value("${rawg.cache.games.ttl-seconds:86400}") long ttlSeconds
	) {
		this.cacheRepository = cacheRepository;
		this.mappingService = mappingService;
		this.ttlSeconds = ttlSeconds;
	}

	/**
	 * Save all games from RAWG response to cache
	 */
	public void saveGames(RawgGamesResponseDto response) {
		if (response == null || response.getResults() == null) {
			return;
		}

		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(ttlSeconds);
		List<RawgGameCacheDocument> documents = new ArrayList<>();

		response.getResults().forEach(rawgGame -> {
			if (rawgGame == null || rawgGame.getId() == null || rawgGame.getSlug() == null || rawgGame.getSlug().isBlank()) {
				return;
			}

			documents.add(new RawgGameCacheDocument(
					buildDocumentId(rawgGame.getId()),
					rawgGame.getId(),
					rawgGame.getName(),
					rawgGame.getSlug(),
					rawgGame.getReleased(),
					rawgGame.getBackgroundImage(),
					rawgGame.getRating(),
					rawgGame.getMetacritic(),
					now,
					expiresAt
			));
		});

		if (!documents.isEmpty()) {
			cacheRepository.saveAll(documents);
		}
	}

	/**
	 * Get a single game by RAWG ID if cache is fresh
	 */
	public Optional<RawgGameCacheDocument> getFreshGameByRawgId(Integer rawgId) {
		return cacheRepository.findByRawgIdAndExpiresAtAfter(rawgId, Instant.now());
	}

	/**
	 * Get a single game by slug if cache is fresh
	 */
	public Optional<RawgGameCacheDocument> getFreshGameBySlug(String slug) {
		return cacheRepository.findBySlugAndExpiresAtAfter(slug, Instant.now());
	}

	public Page<RawgGameCacheDocument> searchGamesByName(String nameSearch, Pageable pageable) {
		if (nameSearch == null || nameSearch.isBlank()) {
			return getAllCachedGames(pageable);
		}

		return cacheRepository.findByNameContainingIgnoreCaseAndExpiresAtAfter(
				nameSearch.trim(),
				Instant.now(),
				pageable
		);
	}

	public RawgGamesPageResponse getCachedGamesPage(String search, Pageable pageable) {
		Page<RawgGameCacheDocument> page = searchGamesByName(search, pageable);
		return new RawgGamesPageResponse(
				page.map(this::toListItemDto).getContent(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isLast()
		);
	}

	/**
	 * Get all cached games with pagination
	 */
	public Page<RawgGameCacheDocument> getAllCachedGames(Pageable pageable) {
		return cacheRepository.findByExpiresAtAfter(Instant.now(), pageable);
	}

	/**
	 * Count total cached games that haven't expired
	 */
	public Long countCachedGames() {
		return cacheRepository.countByExpiresAtAfter(Instant.now());
	}

	/**
	 * Clear expired cache entries manually (MongoDB TTL index should handle this automatically)
	 */
	public void clearExpiredCache() {
		cacheRepository.deleteAll(
				cacheRepository.findAll().stream()
						.filter(doc -> doc.getExpiresAt().isBefore(Instant.now()))
						.toList()
		);
	}

	private RawgGameListItemDto toListItemDto(RawgGameCacheDocument document) {
		String cheapSharkGameId = mappingService.findCheapSharkGameId(document.getSlug(), document.getName())
				.orElse(null);

		return new RawgGameListItemDto(
				document.getRawgId(),
				document.getName(),
				document.getSlug(),
				document.getReleased(),
				document.getBackgroundImage(),
				document.getRating(),
				document.getMetacritic(),
				cheapSharkGameId
		);
	}

	private String buildDocumentId(Integer rawgId) {
		return "v1:rawg:game:" + rawgId;
	}
}

