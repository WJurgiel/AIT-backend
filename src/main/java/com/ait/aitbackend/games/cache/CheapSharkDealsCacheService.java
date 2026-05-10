package com.ait.aitbackend.games.cache;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            @Value("${cheapshark.cache.deals.ttl-seconds:300}") long ttlSeconds
    ) {
        this.cacheRepository = cacheRepository;
        this.ttlSeconds = ttlSeconds;
    }

    public Optional<List<CheapSharkDealDto>> getFreshDeals(Integer storeId, Integer onSale) {
        String cacheKey = buildCacheKey(storeId, onSale);
        List<CheapSharkDealDto> deals = cacheRepository
                .findAllByCacheKeyAndExpiresAtAfterOrderByResultOrderAsc(cacheKey, Instant.now())
                .stream()
                .map(CheapSharkDealsCacheDocument::getDeal)
                .filter(java.util.Objects::nonNull)
                .toList();

        return deals.isEmpty() ? Optional.empty() : Optional.of(deals);
    }

    public Page<CheapSharkDealDto> getDealsPaged(Integer storeId, Integer onSale, int page, int size) {
        String cacheKey = buildCacheKey(storeId, onSale);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "resultOrder"));
        Page<CheapSharkDealsCacheDocument> pageResult = cacheRepository.findByCacheKeyAndExpiresAtAfter(cacheKey, Instant.now(), pageable);
        return pageResult.map(CheapSharkDealsCacheDocument::getDeal);
    }

    public void saveDeals(Integer storeId, Integer onSale, List<CheapSharkDealDto> deals) {
        String cacheKey = buildCacheKey(storeId, onSale);
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

    public String buildCacheKey(Integer storeId, Integer onSale) {
        return "v1:cheapshark:deals:storeID=" + storeId + ":onSale=" + onSale;
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
