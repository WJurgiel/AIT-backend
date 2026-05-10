package com.ait.aitbackend.games.cache;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.Optional;

public interface CheapSharkGameCacheRepository extends MongoRepository<CheapSharkGameCacheDocument, String> {
	Optional<CheapSharkGameCacheDocument> findByGameIdAndExpiresAtAfter(String gameId, Instant expiresAt);
}

