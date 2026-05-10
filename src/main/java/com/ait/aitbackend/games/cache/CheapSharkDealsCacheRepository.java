package com.ait.aitbackend.games.cache;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface CheapSharkDealsCacheRepository extends MongoRepository<CheapSharkDealsCacheDocument, String> {
	List<CheapSharkDealsCacheDocument> findAllByCacheKeyAndExpiresAtAfterOrderByResultOrderAsc(String cacheKey, Instant expiresAt);

	Page<CheapSharkDealsCacheDocument> findByCacheKeyAndExpiresAtAfter(String cacheKey, Instant expiresAt, Pageable pageable);

	void deleteAllByCacheKey(String cacheKey);
}

