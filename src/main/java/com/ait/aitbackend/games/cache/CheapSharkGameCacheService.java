package com.ait.aitbackend.games.cache;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkGameDetailsDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class CheapSharkGameCacheService {
	private final CheapSharkGameCacheRepository cacheRepository;
	private final long ttlSeconds;

	public CheapSharkGameCacheService(
			CheapSharkGameCacheRepository cacheRepository,
			@Value("${cheapshark.cache.games.ttl-seconds:3600}") long ttlSeconds
	) {
		this.cacheRepository = cacheRepository;
		this.ttlSeconds = ttlSeconds;
	}

	public Optional<CheapSharkGameDetailsDto> getFreshGame(String gameId) {
		String normalizedGameId = normalizeGameId(gameId);
		Optional<CheapSharkGameCacheDocument> cached = Optional.ofNullable(
				cacheRepository.findByGameIdAndExpiresAtAfter(normalizedGameId, Instant.now())
		).orElse(Optional.empty());

		return cached.map(CheapSharkGameCacheDocument::getGameDetails);
	}

	public void saveGame(String gameId, CheapSharkGameDetailsDto gameDetails) {
		if (gameDetails == null) {
			return;
		}

		String normalizedGameId = normalizeGameId(gameId);
		Instant now = Instant.now();
		CheapSharkGameCacheDocument document = new CheapSharkGameCacheDocument(
				buildDocumentId(normalizedGameId),
				normalizedGameId,
				gameDetails,
				now,
				now.plusSeconds(ttlSeconds)
		);
		cacheRepository.save(document);
	}

	private String normalizeGameId(String gameId) {
		return gameId == null ? null : gameId.trim();
	}

	private String buildDocumentId(String gameId) {
		return "v1:cheapshark:game:" + gameId;
	}
}


