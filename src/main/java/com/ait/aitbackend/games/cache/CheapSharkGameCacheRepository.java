package com.ait.aitbackend.games.cache;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.Optional;

/**
 * Repozytorium cache dla szczegółów gier CheapShark.
 * Umożliwia szybkie pobieranie danych jeśli nie wygasły (TTL).
 */
public interface CheapSharkGameCacheRepository
		extends MongoRepository<CheapSharkGameCacheDocument, String> {

	/**
	 * Pobiera cache gry jeśli nie wygasł.
	 */
	Optional<CheapSharkGameCacheDocument>
	findByGameIdAndExpiresAtAfter(String gameId, Instant expiresAt);
}