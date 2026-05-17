package com.ait.aitbackend.games.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for caching RAWG games.
 * Stores minimal game information from RAWG API for efficient searching and filtering.
 * Each game is cached with TTL for auto-expiry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rawg_games_cache")
@CompoundIndexes({
		@CompoundIndex(name = "rawg_id_expires_idx", def = "{'rawgId': 1, 'expiresAt': 1}"),
		@CompoundIndex(name = "slug_expires_idx", def = "{'slug': 1, 'expiresAt': 1}")
})
public class RawgGameCacheDocument {
	@Id
	private String id;

	/**
	 * RAWG game ID (from RAWG API)
	 */
	@Indexed
	private Integer rawgId;

	/**
	 * Game name from RAWG
	 */
	private String name;

	/**
	 * RAWG slug - used for mapping to CheapShark internal name
	 * Needs to be converted: slug (lowercase-with-hyphens) -> internal_name (UPPERCASE_NO_HYPHENS)
	 */
	@Indexed
	private String slug;

	/**
	 * Release date from RAWG (ISO format or null)
	 */
	private String released;

	/**
	 * Background image URL from RAWG
	 */
	private String backgroundImage;

	/**
	 * User rating from RAWG (0-100 scale)
	 */
	private Double rating;

	/**
	 * Metacritic score if available
	 */
	private Integer metacritic;

	/**
	 * Timestamp when this document was cached
	 */
	private Instant cachedAt;

	/**
	 * TTL index - document will be automatically deleted after this time
	 */
	@Indexed(expireAfter = "0s")
	private Instant expiresAt;
}

