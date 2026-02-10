package com.reddevil.reddevilanalytics_backend.provider.thesportsdb;

import com.reddevil.reddevilanalytics_backend.provider.client.AssetProviderClient;
import com.reddevil.reddevilanalytics_backend.provider.dto.AssetDTO;
import com.reddevil.reddevilanalytics_backend.provider.ratelimit.RateLimitManager;
import com.reddevil.reddevilanalytics_backend.provider.thesportsdb.TheSportsDBModels.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
@Qualifier("theSportsDBClient")
public class TheSportsDBClient implements AssetProviderClient {

    private final WebClient webClient;
    private final RateLimitManager rateLimitManager;
    private final boolean enabled;
    private final String apiKey;

    public TheSportsDBClient(
            WebClient.Builder webClientBuilder,
            RateLimitManager rateLimitManager,
            @Value("${providers.thesportsdb.base-url}") String baseUrl,
            @Value("${providers.thesportsdb.api-key}") String apiKey,
            @Value("${providers.thesportsdb.enabled:true}") boolean enabled) {
        
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
        
        this.rateLimitManager = rateLimitManager;
        this.apiKey = apiKey;
        this.enabled = enabled;
        
        log.info("TheSportsDBClient initialized - enabled: {}, baseUrl: {}", enabled, baseUrl);
    }

    @Override
    @CircuitBreaker(name = "theSportsDB")
    @Cacheable(value = "teamAssets", key = "#teamName")
    public AssetDTO getTeamAssets(String teamName) {
        if (!enabled) {
            log.warn("TheSportsDB provider is disabled");
            return AssetDTO.builder().build();
        }

        if (!rateLimitManager.allowRequest(RateLimitManager.Provider.THESPORTSDB)) {
            log.warn("Rate limit exceeded for TheSportsDB");
            return AssetDTO.builder().build();
        }

        try {
            log.debug("Fetching team assets for: {}", teamName);
            
            TeamsResponse response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{apiKey}/searchteams.php")
                            .queryParam("t", teamName)
                            .build(apiKey))
                    .retrieve()
                    .bodyToMono(TeamsResponse.class)
                    .block();

            rateLimitManager.recordRequest(RateLimitManager.Provider.THESPORTSDB);
            
            if (response == null || response.teams() == null || response.teams().isEmpty()) {
                log.warn("No team assets found for: {}", teamName);
                return AssetDTO.builder().build();
            }

            TeamAssetData teamData = response.teams().get(0);
            log.debug("Successfully fetched team assets for: {}", teamName);
            
            return AssetDTO.builder()
                    .logoUrl(teamData.strTeamBadge())
                    .bannerUrl(teamData.strTeamBanner())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error fetching team assets from TheSportsDB for team {}: {}", teamName, e.getMessage(), e);
            return AssetDTO.builder().build();
        }
    }

    @Override
    @CircuitBreaker(name = "theSportsDB")
    @Cacheable(value = "playerAssets", key = "#playerName")
    public AssetDTO getPlayerAssets(String playerName) {
        if (!enabled) {
            log.warn("TheSportsDB provider is disabled");
            return AssetDTO.builder().build();
        }

        if (!rateLimitManager.allowRequest(RateLimitManager.Provider.THESPORTSDB)) {
            log.warn("Rate limit exceeded for TheSportsDB");
            return AssetDTO.builder().build();
        }

        try {
            log.debug("Fetching player assets for: {}", playerName);
            
            PlayersResponse response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{apiKey}/searchplayers.php")
                            .queryParam("p", playerName)
                            .build(apiKey))
                    .retrieve()
                    .bodyToMono(PlayersResponse.class)
                    .block();

            rateLimitManager.recordRequest(RateLimitManager.Provider.THESPORTSDB);
            
            if (response == null || response.player() == null || response.player().isEmpty()) {
                log.warn("No player assets found for: {}", playerName);
                return AssetDTO.builder().build();
            }

            PlayerAssetData playerData = response.player().get(0);
            log.debug("Successfully fetched player assets for: {}", playerName);
            
            return AssetDTO.builder()
                    .cutoutUrl(playerData.strCutout())
                    .photoUrl(playerData.strThumb())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error fetching player assets from TheSportsDB for player {}: {}", playerName, e.getMessage(), e);
            return AssetDTO.builder().build();
        }
    }
}
