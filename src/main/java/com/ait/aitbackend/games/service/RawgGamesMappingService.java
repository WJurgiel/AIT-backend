package com.ait.aitbackend.games.service;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkGameSearchDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for mapping RAWG games to CheapShark games.
 * Converts RAWG slug to CheapShark internal name and finds the matching gameId.
 *
 * Conversion rule: RAWG slug (e.g., "the-witcher-enhanced-edition-directors-cut")
 * -> Internal name (e.g., "THEWITCHERENHANCEDEDITIONDIRECTORSCUT")
 * - Remove all hyphens
 * - Convert to uppercase
 */
@Service
public class RawgGamesMappingService {
	private static final Logger log = LoggerFactory.getLogger(RawgGamesMappingService.class);
	private final Map<String, Optional<String>> slugToGameIdCache = new ConcurrentHashMap<>();

	private final CheapSharkService cheapSharkService;

	public RawgGamesMappingService(CheapSharkService cheapSharkService) {
		this.cheapSharkService = cheapSharkService;
	}

	/**
	 * Convert RAWG slug to CheapShark internal name format
	 * Rule: remove hyphens and convert to uppercase
	 *
	 * @param slug RAWG slug (e.g., "the-witcher-enhanced-edition-directors-cut")
	 * @return CheapShark internal name (e.g., "THEWITCHERENHANCEDEDITIONDIRECTORSCUT")
	 */
	public String convertSlugToInternalName(String slug) {
		if (slug == null || slug.isBlank()) {
			return "";
		}
		return slug.toUpperCase().replace("-", "");
	}

	/**
	 * Find CheapShark gameId for a RAWG game by its slug
	 * Strategy:
	 * 1. Convert slug to internal name
	 * 2. Search in CheapShark by the converted name
	 * 3. Find exact or best match
	 *
	 * @param slug RAWG slug
	 * @param gameName RAWG game name (fallback for search)
	 * @return Optional containing CheapShark gameId if found
	 */
	public Optional<String> findCheapSharkGameId(String slug, String gameName) {
		String internalName = convertSlugToInternalName(slug);

		log.debug("Mapping RAWG game - slug: {}, name: {}, internalName: {}", slug, gameName, internalName);

		if (slug != null && !slug.isBlank()) {
			String cacheKey = slug.trim().toLowerCase();
			Optional<String> cached = slugToGameIdCache.get(cacheKey);
			if (cached != null) {
				return cached;
			}

			Optional<String> resolved = resolveCheapSharkGameId(slug, internalName, gameName);
			slugToGameIdCache.put(cacheKey, resolved);
			return resolved;
		}

		return resolveCheapSharkGameId(slug, internalName, gameName);
	}

	private Optional<String> resolveCheapSharkGameId(String slug, String internalName, String gameName) {

		// Try search by converted name first, but only when the slug produced something meaningful
		if (!internalName.isBlank()) {
			Optional<String> gameIdFromInternalName = searchGameByInternalName(internalName);
			if (gameIdFromInternalName.isPresent()) {
				log.debug("Found CheapShark gameId from internal name: {}", gameIdFromInternalName.get());
				return gameIdFromInternalName;
			}
		}

		// Fallback: search by game name
		if (gameName != null && !gameName.isBlank()) {
			Optional<String> gameIdFromName = searchGameByName(gameName);
			if (gameIdFromName.isPresent()) {
				log.debug("Found CheapShark gameId from game name: {}", gameIdFromName.get());
				return gameIdFromName;
			}
		}

		log.debug("Could not find CheapShark gameId for RAWG slug: {}", slug);
		return Optional.empty();
	}

	/**
	 * Search for game in CheapShark by internal name
	 */
	private Optional<String> searchGameByInternalName(String internalName) {
		if (internalName == null || internalName.isBlank()) {
			return Optional.empty();
		}

		try {
			List<CheapSharkGameSearchDto> results = cheapSharkService.searchGamesByTitle(internalName);
			if (results != null && !results.isEmpty()) {
				// Return the first (most relevant) result
				return Optional.of(results.getFirst().gameId());
			}
		} catch (Exception e) {
			log.warn("Error searching CheapShark by internal name: {}", internalName, e);
		}
		return Optional.empty();
	}

	/**
	 * Search for game in CheapShark by game name
	 */
	private Optional<String> searchGameByName(String gameName) {
		try {
			List<CheapSharkGameSearchDto> results = cheapSharkService.searchGamesByTitle(gameName);
			if (results != null && !results.isEmpty()) {
				// Return the first (most relevant) result
				return Optional.of(results.getFirst().gameId());
			}
		} catch (Exception e) {
			log.warn("Error searching CheapShark by game name: {}", gameName, e);
		}
		return Optional.empty();
	}
}


