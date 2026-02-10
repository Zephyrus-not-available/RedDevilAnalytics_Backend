package com.reddevil.reddevilanalytics_backend.provider.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitManager {

    public enum Provider {
        API_FOOTBALL,
        FOOTBALL_DATA,
        THESPORTSDB
    }

    private final Map<Provider, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitManager() {
        initializeBuckets();
    }

    private void initializeBuckets() {
        buckets.put(Provider.API_FOOTBALL, createBucket(100, Duration.ofDays(1)));
        buckets.put(Provider.FOOTBALL_DATA, createBucket(10, Duration.ofMinutes(1)));
        buckets.put(Provider.THESPORTSDB, createBucket(10000, Duration.ofHours(1)));
    }

    private Bucket createBucket(long capacity, Duration refillPeriod) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(capacity, refillPeriod));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public boolean allowRequest(Provider provider) {
        Bucket bucket = buckets.get(provider);
        if (bucket == null) {
            log.warn("No rate limit configuration found for provider: {}", provider);
            return true;
        }

        boolean allowed = bucket.tryConsume(1);
        if (!allowed) {
            log.warn("Rate limit exceeded for provider: {}", provider);
        }
        return allowed;
    }

    public void recordRequest(Provider provider) {
        // TODO: Implement request tracking/persistence if needed for analytics
        log.debug("Request recorded for provider: {}", provider);
    }

    public int getRemainingQuota(Provider provider) {
        Bucket bucket = buckets.get(provider);
        if (bucket == null) {
            log.warn("No rate limit configuration found for provider: {}", provider);
            return -1;
        }
        return (int) bucket.getAvailableTokens();
    }
}
