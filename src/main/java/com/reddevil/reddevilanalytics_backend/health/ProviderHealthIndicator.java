package com.reddevil.reddevilanalytics_backend.health;

import com.reddevil.reddevilanalytics_backend.provider.ratelimit.RateLimitManager;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ProviderHealthIndicator implements HealthIndicator {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RateLimitManager rateLimitManager;
    
    private final boolean apiFootballEnabled;
    private final boolean footballDataEnabled;
    private final boolean theSportsDBEnabled;
    private final int apiFootballDailyQuota;

    public ProviderHealthIndicator(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RateLimitManager rateLimitManager,
            @Value("${providers.api-football.enabled:true}") boolean apiFootballEnabled,
            @Value("${providers.football-data.enabled:true}") boolean footballDataEnabled,
            @Value("${providers.thesportsdb.enabled:true}") boolean theSportsDBEnabled,
            @Value("${providers.api-football.daily-quota:100}") int apiFootballDailyQuota) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.rateLimitManager = rateLimitManager;
        this.apiFootballEnabled = apiFootballEnabled;
        this.footballDataEnabled = footballDataEnabled;
        this.theSportsDBEnabled = theSportsDBEnabled;
        this.apiFootballDailyQuota = apiFootballDailyQuota;
    }

    @Override
    public Health health() {
        try {
            Map<String, Object> details = new HashMap<>();
            boolean allProvidersHealthy = true;
            boolean anyCriticalProviderDown = false;

            // Check API-Football
            Map<String, Object> apiFootballDetails = checkProvider(
                    "apiFootball",
                    apiFootballEnabled,
                    RateLimitManager.Provider.API_FOOTBALL,
                    apiFootballDailyQuota,
                    true
            );
            details.put("apiFootball", apiFootballDetails);
            
            if (!"UP".equals(apiFootballDetails.get("status"))) {
                allProvidersHealthy = false;
                if (apiFootballEnabled) {
                    anyCriticalProviderDown = true;
                }
            }

            // Check Football-Data
            Map<String, Object> footballDataDetails = checkProvider(
                    "footballData",
                    footballDataEnabled,
                    RateLimitManager.Provider.FOOTBALL_DATA,
                    10,
                    true
            );
            details.put("footballData", footballDataDetails);
            
            if (!"UP".equals(footballDataDetails.get("status"))) {
                allProvidersHealthy = false;
                if (footballDataEnabled) {
                    anyCriticalProviderDown = true;
                }
            }

            // Check TheSportsDB
            Map<String, Object> theSportsDBDetails = checkProvider(
                    "theSportsDB",
                    theSportsDBEnabled,
                    RateLimitManager.Provider.THESPORTSDB,
                    10000,
                    false
            );
            details.put("theSportsDB", theSportsDBDetails);
            
            if (!"UP".equals(theSportsDBDetails.get("status")) && theSportsDBEnabled) {
                allProvidersHealthy = false;
            }

            details.put("allProvidersHealthy", allProvidersHealthy);
            details.put("criticalProviderDown", anyCriticalProviderDown);

            if (anyCriticalProviderDown) {
                return Health.down()
                        .withDetail("reason", "One or more critical providers are down")
                        .withDetails(details)
                        .build();
            }

            return Health.up().withDetails(details).build();

        } catch (Exception e) {
            log.error("Error checking provider health", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private Map<String, Object> checkProvider(
            String circuitBreakerName,
            boolean enabled,
            RateLimitManager.Provider provider,
            int maxQuota,
            boolean isCritical) {
        
        Map<String, Object> providerDetails = new HashMap<>();
        providerDetails.put("enabled", enabled);
        providerDetails.put("critical", isCritical);

        if (!enabled) {
            providerDetails.put("status", "DISABLED");
            providerDetails.put("circuitBreakerState", "N/A");
            providerDetails.put("remainingQuota", "N/A");
            return providerDetails;
        }

        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
            CircuitBreaker.State state = circuitBreaker.getState();
            providerDetails.put("circuitBreakerState", state.toString());

            int remainingQuota = rateLimitManager.getRemainingQuota(provider);
            providerDetails.put("remainingQuota", remainingQuota);
            providerDetails.put("maxQuota", maxQuota);
            
            double quotaPercentage = (remainingQuota * 100.0) / maxQuota;
            providerDetails.put("quotaUsagePercent", String.format("%.2f%%", 100 - quotaPercentage));

            if (state == CircuitBreaker.State.OPEN || state == CircuitBreaker.State.FORCED_OPEN) {
                providerDetails.put("status", "DOWN");
                providerDetails.put("reason", "Circuit breaker is " + state);
            } else if (remainingQuota <= 0) {
                providerDetails.put("status", "DOWN");
                providerDetails.put("reason", "Quota exhausted");
            } else if (remainingQuota < maxQuota * 0.1) {
                providerDetails.put("status", "DEGRADED");
                providerDetails.put("reason", "Quota running low");
            } else if (state == CircuitBreaker.State.HALF_OPEN) {
                providerDetails.put("status", "DEGRADED");
                providerDetails.put("reason", "Circuit breaker recovering");
            } else {
                providerDetails.put("status", "UP");
            }

        } catch (Exception e) {
            log.error("Error checking provider: {}", circuitBreakerName, e);
            providerDetails.put("status", "UNKNOWN");
            providerDetails.put("error", e.getMessage());
        }

        return providerDetails;
    }
}
