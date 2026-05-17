package com.ait.aitbackend.games.cache;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RawgGameCacheRepository extends MongoRepository<RawgGameCacheDocument, String> {

	/**
	 * Find a game by its RAWG ID if the cache entry hasn't expired
	 */
	Optional<RawgGameCacheDocument> findByRawgIdAndExpiresAtAfter(Integer rawgId, Instant now);

	/**
	 * Find a game by its slug if the cache entry hasn't expired
	 */
	Optional<RawgGameCacheDocument> findBySlugAndExpiresAtAfter(String slug, Instant now);

	/**
	 * Search games by name (case-insensitive) with pagination and valid cache
	 */
	Page<RawgGameCacheDocument> findByNameContainingIgnoreCaseAndExpiresAtAfter(
			String nameSearch,
			Instant now,
			Pageable pageable
	);

	/**
	 * Find all games with valid cache (not expired) with pagination
	 */
	Page<RawgGameCacheDocument> findByExpiresAtAfter(Instant now, Pageable pageable);

	/**
	 * Count total cached games that haven't expired
	 */
	Long countByExpiresAtAfter(Instant now);
}


