package com.ait.aitbackend.games.cache;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkGameDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cheapshark_games_cache")
@CompoundIndexes({
		@CompoundIndex(name = "gameid_expires_idx", def = "{'gameId': 1, 'expiresAt': 1}")
})
public class CheapSharkGameCacheDocument {
	@Id
	private String id;

	@Indexed
	private String gameId;

	private CheapSharkGameDetailsDto gameDetails;
	private Instant cachedAt;

	@Indexed(expireAfter = "0s")
	private Instant expiresAt;
}

