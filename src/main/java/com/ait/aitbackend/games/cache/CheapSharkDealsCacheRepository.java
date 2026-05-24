package com.ait.aitbackend.games.cache;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

/**
 * Repozytorium MongoDB dla cache ofert CheapShark.
 * Umożliwia pobieranie zapisanych wyników oraz ich czyszczenie.
 */
public interface CheapSharkDealsCacheRepository
		extends MongoRepository<CheapSharkDealsCacheDocument, String> {

	/**
	 * Pobiera cache dla konkretnego klucza,
	 * tylko rekordy które jeszcze nie wygasły.
	 */
	List<CheapSharkDealsCacheDocument>
	findAllByCacheKeyAndExpiresAtAfterOrderByResultOrderAsc(
			String cacheKey,
			Instant expiresAt
	);

	/**
	 * Usuwa wszystkie wpisy cache dla danego klucza.
	 */
	void deleteAllByCacheKey(String cacheKey);
}