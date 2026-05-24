package com.ait.aitbackend.games.cache;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
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
 * Dokument cache'ujący oferty CheapShark w MongoDB.
 * Umożliwia szybkie odczyty bez ponownego odpytywania API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cheapshark_deals_cache")
@CompoundIndexes({
        @CompoundIndex(
                name = "cachekey_expires_order_idx",
                def = "{'cacheKey': 1, 'expiresAt': 1, 'resultOrder': 1}"
        )
})
public class CheapSharkDealsCacheDocument {

    @Id
    private String id;

    @Indexed
    private String cacheKey;

    // kolejność w wynikach (dla paginacji / sortowania)
    private Integer resultOrder;

    private Integer storeId;

    @Indexed
    private String gameId;

    // zapisany deal
    private CheapSharkDealDto deal;

    private Instant cachedAt;

    // TTL index – Mongo automatycznie usuwa wygasłe rekordy
    @Indexed(expireAfter = "0s")
    private Instant expiresAt;
}