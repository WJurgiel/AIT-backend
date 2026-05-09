package com.ait.aitbackend.games.cache;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class CheapSharkDealsCacheService {
    private final CheapSharkDealsCacheRepository cacheRepository;
    private final ObjectMapper objectMapper;
    private final long ttlSeconds;

    public CheapSharkDealsCacheService(
            CheapSharkDealsCacheRepository cacheRepository,
            ObjectMapper objectMapper,
            @Value("${cheapshark.cache.deals.ttl-seconds:300}") long ttlSeconds
    ) {
        this.cacheRepository = cacheRepository;
        this.objectMapper = objectMapper;
        this.ttlSeconds = ttlSeconds;
    }

    public Optional<List<CheapSharkDealDto>> getFreshDeals(Integer storeId, Integer onSale) {
        String cacheKey = buildCacheKey(storeId, onSale);
        return cacheRepository.findById(cacheKey)
                .filter(entry -> entry.getExpiresAt() != null && entry.getExpiresAt().isAfter(Instant.now()))
                .flatMap(entry -> deserializeDeals(entry.getResponsePayload()));
    }

    public void saveDeals(Integer storeId, Integer onSale, List<CheapSharkDealDto> deals) {
        Instant now = Instant.now();
        CheapSharkDealsCacheDocument document = new CheapSharkDealsCacheDocument(
                buildCacheKey(storeId, onSale),
                storeId,
                onSale,
                serializeDeals(deals),
                deals.size(),
                now,
                now.plusSeconds(ttlSeconds)
        );

        cacheRepository.save(document);
    }

    public String buildCacheKey(Integer storeId, Integer onSale) {
        return "v1:cheapshark:deals:storeID=" + storeId + ":onSale=" + onSale;
    }

    private String serializeDeals(List<CheapSharkDealDto> deals) {
        try {
            return objectMapper.writeValueAsString(deals);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize deals response for cache", ex);
        }
    }

    private Optional<List<CheapSharkDealDto>> deserializeDeals(String responsePayload) {
        if (responsePayload == null || responsePayload.isBlank()) {
            return Optional.empty();
        }

        try {
            List<CheapSharkDealDto> deals = objectMapper.readValue(responsePayload, new TypeReference<>() {
            });
            return Optional.of(deals);
        } catch (JsonProcessingException ex) {
            return Optional.empty();
        }
    }
}


