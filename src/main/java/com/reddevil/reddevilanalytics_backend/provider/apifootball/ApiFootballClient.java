package com.reddevil.reddevilanalytics_backend.provider.apifootball;

import com.reddevil.reddevilanalytics_backend.provider.client.FixtureProviderClient;
import com.reddevil.reddevilanalytics_backend.provider.client.LiveMatchProviderClient;
import com.reddevil.reddevilanalytics_backend.provider.dto.FixtureDTO;
import com.reddevil.reddevilanalytics_backend.provider.dto.LiveMatchDTO;
import com.reddevil.reddevilanalytics_backend.provider.dto.MatchEventDTO;
import com.reddevil.reddevilanalytics_backend.provider.dto.TeamDTO;
import com.reddevil.reddevilanalytics_backend.provider.ratelimit.RateLimitManager;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Qualifier("apiFootballClient")
public class ApiFootballClient implements LiveMatchProviderClient, FixtureProviderClient {

    private final WebClient webClient;
    private final RateLimitManager rateLimitManager;
    private final boolean enabled;
    private final int dailyQuota;

    public ApiFootballClient(
            WebClient.Builder webClientBuilder,
            RateLimitManager rateLimitManager,
            @Value("${providers.api-football.base-url}") String baseUrl,
            @Value("${providers.api-football.api-key}") String apiKey,
            @Value("${providers.api-football.daily-quota:100}") int dailyQuota,
            @Value("${providers.api-football.enabled:true}") boolean enabled) {
        
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("x-rapidapi-key", apiKey)
                .defaultHeader("x-rapidapi-host", "api-football-v1.p.rapidapi.com")
                .build();
        
        this.rateLimitManager = rateLimitManager;
        this.dailyQuota = dailyQuota;
        this.enabled = enabled;
        
        log.info("ApiFootballClient initialized - enabled: {}, dailyQuota: {}", enabled, dailyQuota);
    }

    @Override
    @CircuitBreaker(name = "apiFootball")
    public List<LiveMatchDTO> getLiveMatches(String competitionId) {
        if (!enabled) {
            log.warn("ApiFootball provider is disabled");
            return Collections.emptyList();
        }

        enforceQuota("getLiveMatches");

        try {
            LiveMatchesResponse response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/fixtures")
                            .queryParam("live", "all")
                            .queryParam("league", competitionId)
                            .build())
                    .retrieve()
                    .bodyToMono(LiveMatchesResponse.class)
                    .block();

            logQuotaUsage("getLiveMatches");
            
            if (response == null || response.response() == null) {
                log.warn("No response from API-Football for live matches");
                return Collections.emptyList();
            }

            return response.response().stream()
                    .map(this::mapToLiveMatchDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error fetching live matches from API-Football: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch live matches", e);
        }
    }

    @Override
    @CircuitBreaker(name = "apiFootball")
    public LiveMatchDTO getLiveMatchById(String matchId) {
        if (!enabled) {
            log.warn("ApiFootball provider is disabled");
            return null;
        }

        enforceQuota("getLiveMatchById");

        try {
            LiveMatchesResponse response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/fixtures")
                            .queryParam("id", matchId)
                            .queryParam("live", "all")
                            .build())
                    .retrieve()
                    .bodyToMono(LiveMatchesResponse.class)
                    .block();

            logQuotaUsage("getLiveMatchById");
            
            if (response == null || response.response() == null || response.response().isEmpty()) {
                log.warn("No live match found with ID: {}", matchId);
                return null;
            }

            return mapToLiveMatchDTO(response.response().get(0));
            
        } catch (Exception e) {
            log.error("Error fetching live match by ID from API-Football: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch live match", e);
        }
    }

    @Override
    @CircuitBreaker(name = "apiFootball")
    public List<FixtureDTO> getFixtures(String competitionId, String seasonId) {
        if (!enabled) {
            log.warn("ApiFootball provider is disabled");
            return Collections.emptyList();
        }

        enforceQuota("getFixtures");

        try {
            FixturesResponse response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/fixtures")
                            .queryParam("league", competitionId)
                            .queryParam("season", seasonId)
                            .build())
                    .retrieve()
                    .bodyToMono(FixturesResponse.class)
                    .block();

            logQuotaUsage("getFixtures");
            
            if (response == null || response.response() == null) {
                log.warn("No response from API-Football for fixtures");
                return Collections.emptyList();
            }

            return response.response().stream()
                    .map(this::mapToFixtureDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error fetching fixtures from API-Football: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch fixtures", e);
        }
    }

    @Override
    @CircuitBreaker(name = "apiFootball")
    public FixtureDTO getFixtureById(String fixtureId) {
        if (!enabled) {
            log.warn("ApiFootball provider is disabled");
            return null;
        }

        enforceQuota("getFixtureById");

        try {
            FixturesResponse response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/fixtures")
                            .queryParam("id", fixtureId)
                            .build())
                    .retrieve()
                    .bodyToMono(FixturesResponse.class)
                    .block();

            logQuotaUsage("getFixtureById");
            
            if (response == null || response.response() == null || response.response().isEmpty()) {
                log.warn("No fixture found with ID: {}", fixtureId);
                return null;
            }

            return mapToFixtureDTO(response.response().get(0));
            
        } catch (Exception e) {
            log.error("Error fetching fixture by ID from API-Football: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch fixture", e);
        }
    }

    @Override
    @CircuitBreaker(name = "apiFootball")
    public FixtureDTO getNextFixture(String teamId, String seasonId) {
        if (!enabled) {
            log.warn("ApiFootball provider is disabled");
            return null;
        }

        enforceQuota("getNextFixture");

        try {
            FixturesResponse response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/fixtures")
                            .queryParam("team", teamId)
                            .queryParam("season", seasonId)
                            .queryParam("next", "1")
                            .build())
                    .retrieve()
                    .bodyToMono(FixturesResponse.class)
                    .block();

            logQuotaUsage("getNextFixture");
            
            if (response == null || response.response() == null || response.response().isEmpty()) {
                log.warn("No next fixture found for team ID: {}", teamId);
                return null;
            }

            return mapToFixtureDTO(response.response().get(0));
            
        } catch (Exception e) {
            log.error("Error fetching next fixture from API-Football: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch next fixture", e);
        }
    }

    private void enforceQuota(String operation) {
        int remainingQuota = rateLimitManager.getRemainingQuota(RateLimitManager.Provider.API_FOOTBALL);
        
        log.debug("API-Football quota check - operation: {}, remaining: {}/{}", 
                operation, remainingQuota, dailyQuota);
        
        if (!rateLimitManager.allowRequest(RateLimitManager.Provider.API_FOOTBALL)) {
            log.error("API-Football daily quota exhausted - operation: {}, quota: {}/{}", 
                    operation, remainingQuota, dailyQuota);
            throw new RuntimeException("API-Football daily quota exceeded (" + dailyQuota + " requests/day)");
        }
    }

    private void logQuotaUsage(String operation) {
        int remainingQuota = rateLimitManager.getRemainingQuota(RateLimitManager.Provider.API_FOOTBALL);
        rateLimitManager.recordRequest(RateLimitManager.Provider.API_FOOTBALL);
        
        log.info("API-Football request completed - operation: {}, remaining quota: {}/{}", 
                operation, remainingQuota, dailyQuota);
        
        if (remainingQuota < 10) {
            log.warn("API-Football quota running low - remaining: {}/{}", remainingQuota, dailyQuota);
        }
    }

    private LiveMatchDTO mapToLiveMatchDTO(FixtureItem fixture) {
        List<MatchEventDTO> events = Collections.emptyList();
        if (fixture.events() != null) {
            events = fixture.events().stream()
                    .map(event -> MatchEventDTO.builder()
                            .type(event.type())
                            .minute(event.time() != null ? event.time().elapsed() : null)
                            .player(event.player() != null ? event.player().name() : null)
                            .detail(event.detail())
                            .build())
                    .collect(Collectors.toList());
        }

        return LiveMatchDTO.builder()
                .id(fixture.fixture().id())
                .homeTeam(mapToTeamDTO(fixture.teams().home()))
                .awayTeam(mapToTeamDTO(fixture.teams().away()))
                .homeScore(fixture.goals().home())
                .awayScore(fixture.goals().away())
                .status(fixture.fixture().status().shortStatus())
                .minute(fixture.fixture().status().elapsed())
                .events(events)
                .build();
    }

    private FixtureDTO mapToFixtureDTO(FixtureItem fixture) {
        LocalDateTime matchDate = null;
        if (fixture.fixture().timestamp() != null) {
            matchDate = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochSecond(fixture.fixture().timestamp()),
                    ZoneId.systemDefault()
            );
        } else if (fixture.fixture().date() != null) {
            matchDate = LocalDateTime.parse(
                    fixture.fixture().date(),
                    DateTimeFormatter.ISO_DATE_TIME
            );
        }

        return FixtureDTO.builder()
                .id(fixture.fixture().id())
                .homeTeam(mapToTeamDTO(fixture.teams().home()))
                .awayTeam(mapToTeamDTO(fixture.teams().away()))
                .matchDate(matchDate)
                .status(fixture.fixture().status().shortStatus())
                .homeScore(fixture.goals().home())
                .awayScore(fixture.goals().away())
                .venue(fixture.fixture().venue() != null ? fixture.fixture().venue().name() : null)
                .referee(fixture.fixture().referee())
                .build();
    }

    private TeamDTO mapToTeamDTO(TeamInfo teamInfo) {
        if (teamInfo == null) {
            return null;
        }
        
        return TeamDTO.builder()
                .id(teamInfo.id())
                .name(teamInfo.name())
                .logoUrl(teamInfo.logo())
                .build();
    }
}
