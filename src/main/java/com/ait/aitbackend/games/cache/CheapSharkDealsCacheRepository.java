package com.ait.aitbackend.games.cache;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CheapSharkDealsCacheRepository extends MongoRepository<CheapSharkDealsCacheDocument, String> {
}

