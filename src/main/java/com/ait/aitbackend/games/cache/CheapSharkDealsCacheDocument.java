package com.ait.aitbackend.games.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cheapshark_deals_cache")
public class CheapSharkDealsCacheDocument {
    @Id
    private String cacheKey;
    private Integer storeId;
    private Integer onSale;
    private String responsePayload;
    private Integer itemsCount;
    private Instant cachedAt;

    @Indexed(expireAfter = "0s")
    private Instant expiresAt;
}



