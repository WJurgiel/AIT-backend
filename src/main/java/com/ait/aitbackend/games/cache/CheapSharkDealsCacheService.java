package com.ait.aitbackend.games.cache;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
public class CheapSharkDealsCacheService {
    private final CheapSharkDealsCacheRepository cacheRepository;
    private final long ttlSeconds;

    public CheapSharkDealsCacheService(
            CheapSharkDealsCacheRepository cacheRepository,
            @Value("${cheapshark.cache.deals.ttl-seconds:3600}") long ttlSeconds
    ) {
        this.cacheRepository = cacheRepository;
        this.ttlSeconds = ttlSeconds;
    }

    public Optional<List<CheapSharkDealDto>> getFreshDeals(Integer storeId) {
        String cacheKey = buildCacheKey(storeId);
        List<CheapSharkDealDto> deals = cacheRepository
                .findAllByCacheKeyAndExpiresAtAfterOrderByResultOrderAsc(cacheKey, Instant.now())
                .stream()
                .map(CheapSharkDealsCacheDocument::getDeal)
                .filter(java.util.Objects::nonNull)
                .toList();

        return deals.isEmpty() ? Optional.empty() : Optional.of(deals);
    }


    public void saveDeals(Integer storeId, List<CheapSharkDealDto> deals) {
        String cacheKey = buildCacheKey(storeId);
        cacheRepository.deleteAllByCacheKey(cacheKey);

        if (deals == null || deals.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        List<CheapSharkDealsCacheDocument> documents = IntStream.range(0, deals.size())
                .mapToObj(resultOrder -> buildCacheDocument(cacheKey, storeId, resultOrder, deals.get(resultOrder), now))
                .toList();

        cacheRepository.saveAll(documents);
    }

    public String buildCacheKey(Integer storeId) {
        String normalizedStoreId = storeId == null ? "all" : storeId.toString();
        return "v1:cheapshark:deals:storeID=" + normalizedStoreId;
    }

    private CheapSharkDealsCacheDocument buildCacheDocument(
            String cacheKey,
            Integer storeId,
            int resultOrder,
            CheapSharkDealDto deal,
            Instant cachedAt
    ) {
        return new CheapSharkDealsCacheDocument(
                buildDocumentId(cacheKey, resultOrder),
                cacheKey,
                resultOrder,
                storeId,
                deal.gameId(),
                deal,
                cachedAt,
                cachedAt.plusSeconds(ttlSeconds)
        );
    }

    private String buildDocumentId(String cacheKey, int resultOrder) {
        return cacheKey + ":" + resultOrder;
    }
}
