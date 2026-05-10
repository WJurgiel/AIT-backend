package com.ait.aitbackend.games.cache;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface CheapSharkDealsCacheRepository extends MongoRepository<CheapSharkDealsCacheDocument, String> {

	List<CheapSharkDealsCacheDocument> findAllByCacheKeyAndExpiresAtAfterOrderByResultOrderAsc(String cacheKey, Instant expiresAt);

	void deleteAllByCacheKey(String cacheKey);
}

