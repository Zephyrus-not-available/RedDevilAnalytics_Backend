package com.reddevil.reddevilanalytics_backend.provider.footballdata;

import com.reddevil.reddevilanalytics_backend.provider.client.FixtureProviderClient;
import com.reddevil.reddevilanalytics_backend.provider.client.StandingsProviderClient;
import com.reddevil.reddevilanalytics_backend.provider.dto.FixtureDTO;
import com.reddevil.reddevilanalytics_backend.provider.dto.StandingDTO;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Qualifier("footballDataClient")
public class FootballDataClient implements FixtureProviderClient, StandingsProviderClient {

    private final WebClient webClient;
    private final RateLimitManager rateLimitManager;

    public FootballDataClient(
            WebClient.Builder webClientBuilder,
            RateLimitManager rateLimitManager,
            @Value("${providers.football-data.base-url}") String baseUrl,
            @Value("${providers.football-data.api-key}") String apiKey) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("X-Auth-Token", apiKey)
                .build();
        this.rateLimitManager = rateLimitManager;
        log.info("FootballDataClient initialized with base URL: {}", baseUrl);
    }

    @Override
    @CircuitBreaker(name = "footballData", fallbackMethod = "getFixturesFallback")
    public List<FixtureDTO> getFixtures(String competitionId, String seasonId) {
        log.debug("Fetching fixtures for competition: {}, season: {}", competitionId, seasonId);
        
        if (!rateLimitManager.allowRequest(RateLimitManager.Provider.FOOTBALL_DATA)) {
            log.warn("Rate limit exceeded for Football-Data.org");
            return Collections.emptyList();
        }

        try {
            FootballDataModels.FixturesResponse response = webClient.get()
                    .uri("/competitions/{competitionId}/matches", competitionId)
                    .retrieve()
                    .bodyToMono(FootballDataModels.FixturesResponse.class)
                    .block();

            rateLimitManager.recordRequest(RateLimitManager.Provider.FOOTBALL_DATA);

            if (response == null || response.matches() == null) {
                log.warn("No fixtures found for competition: {}", competitionId);
                return Collections.emptyList();
            }

            return response.matches().stream()
                    .map(this::mapToFixtureDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching fixtures from Football-Data.org: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch fixtures", e);
        }
    }

    @Override
    @CircuitBreaker(name = "footballData", fallbackMethod = "getFixtureByIdFallback")
    public FixtureDTO getFixtureById(String fixtureId) {
        log.debug("Fetching fixture by ID: {}", fixtureId);
        
        if (!rateLimitManager.allowRequest(RateLimitManager.Provider.FOOTBALL_DATA)) {
            log.warn("Rate limit exceeded for Football-Data.org");
            return null;
        }

        try {
            FootballDataModels.MatchResponse response = webClient.get()
                    .uri("/matches/{matchId}", fixtureId)
                    .retrieve()
                    .bodyToMono(FootballDataModels.MatchResponse.class)
                    .block();

            rateLimitManager.recordRequest(RateLimitManager.Provider.FOOTBALL_DATA);

            if (response == null) {
                log.warn("No fixture found with ID: {}", fixtureId);
                return null;
            }

            return mapToFixtureDTO(response);
        } catch (Exception e) {
            log.error("Error fetching fixture by ID from Football-Data.org: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch fixture by ID", e);
        }
    }

    @Override
    @CircuitBreaker(name = "footballData", fallbackMethod = "getNextFixtureFallback")
    public FixtureDTO getNextFixture(String teamId, String seasonId) {
        log.debug("Fetching next fixture for team: {}, season: {}", teamId, seasonId);
        
        if (!rateLimitManager.allowRequest(RateLimitManager.Provider.FOOTBALL_DATA)) {
            log.warn("Rate limit exceeded for Football-Data.org");
            return null;
        }

        try {
            FootballDataModels.FixturesResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/teams/{teamId}/matches")
                            .queryParam("status", "SCHEDULED")
                            .build(teamId))
                    .retrieve()
                    .bodyToMono(FootballDataModels.FixturesResponse.class)
                    .block();

            rateLimitManager.recordRequest(RateLimitManager.Provider.FOOTBALL_DATA);

            if (response == null || response.matches() == null || response.matches().isEmpty()) {
                log.warn("No upcoming fixtures found for team: {}", teamId);
                return null;
            }

            return response.matches().stream()
                    .map(this::mapToFixtureDTO)
                    .filter(fixture -> fixture.getMatchDate() != null && fixture.getMatchDate().isAfter(LocalDateTime.now()))
                    .min(Comparator.comparing(FixtureDTO::getMatchDate))
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error fetching next fixture from Football-Data.org: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch next fixture", e);
        }
    }

    @Override
    @CircuitBreaker(name = "footballData", fallbackMethod = "getStandingsFallback")
    public List<StandingDTO> getStandings(String competitionId, String seasonId) {
        log.debug("Fetching standings for competition: {}, season: {}", competitionId, seasonId);
        
        if (!rateLimitManager.allowRequest(RateLimitManager.Provider.FOOTBALL_DATA)) {
            log.warn("Rate limit exceeded for Football-Data.org");
            return Collections.emptyList();
        }

        try {
            FootballDataModels.StandingsResponse response = webClient.get()
                    .uri("/competitions/{competitionId}/standings", competitionId)
                    .retrieve()
                    .bodyToMono(FootballDataModels.StandingsResponse.class)
                    .block();

            rateLimitManager.recordRequest(RateLimitManager.Provider.FOOTBALL_DATA);

            if (response == null || response.standings() == null || response.standings().isEmpty()) {
                log.warn("No standings found for competition: {}", competitionId);
                return Collections.emptyList();
            }

            return response.standings().stream()
                    .filter(standing -> "TOTAL".equals(standing.type()))
                    .flatMap(standing -> standing.table().stream())
                    .map(this::mapToStandingDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching standings from Football-Data.org: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch standings", e);
        }
    }

    private FixtureDTO mapToFixtureDTO(FootballDataModels.MatchResponse match) {
        if (match == null) {
            return null;
        }

        TeamDTO homeTeam = mapToTeamDTO(match.homeTeam());
        TeamDTO awayTeam = mapToTeamDTO(match.awayTeam());

        Integer homeScore = null;
        Integer awayScore = null;
        if (match.score() != null && match.score().fullTime() != null) {
            homeScore = match.score().fullTime().home();
            awayScore = match.score().fullTime().away();
        }

        String competitionName = match.competition() != null ? match.competition().name() : null;
        String seasonId = match.season() != null ? String.valueOf(match.season().id()) : null;

        return FixtureDTO.builder()
                .id(match.id())
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .matchDate(match.utcDate())
                .competition(competitionName)
                .season(seasonId)
                .status(match.status())
                .homeScore(homeScore)
                .awayScore(awayScore)
                .venue(match.venue())
                .referee(null)
                .build();
    }

    private StandingDTO mapToStandingDTO(FootballDataModels.TableEntryResponse entry) {
        if (entry == null) {
            return null;
        }

        TeamDTO team = mapToTeamDTO(entry.team());

        return StandingDTO.builder()
                .position(entry.position() != null ? entry.position().longValue() : null)
                .team(team)
                .playedGames(entry.playedGames())
                .won(entry.won())
                .draw(entry.draw())
                .lost(entry.lost())
                .points(entry.points())
                .goalsFor(entry.goalsFor())
                .goalsAgainst(entry.goalsAgainst())
                .goalDifference(entry.goalDifference())
                .form(entry.form())
                .build();
    }

    private TeamDTO mapToTeamDTO(FootballDataModels.TeamResponse team) {
        if (team == null) {
            return null;
        }

        return TeamDTO.builder()
                .id(team.id())
                .name(team.name())
                .shortName(team.shortName() != null ? team.shortName() : team.tla())
                .logoUrl(team.crest())
                .stadium(null)
                .build();
    }

    private List<FixtureDTO> getFixturesFallback(String competitionId, String seasonId, Exception e) {
        log.error("Circuit breaker fallback triggered for getFixtures: {}", e.getMessage());
        return Collections.emptyList();
    }

    private FixtureDTO getFixtureByIdFallback(String fixtureId, Exception e) {
        log.error("Circuit breaker fallback triggered for getFixtureById: {}", e.getMessage());
        return null;
    }

    private FixtureDTO getNextFixtureFallback(String teamId, String seasonId, Exception e) {
        log.error("Circuit breaker fallback triggered for getNextFixture: {}", e.getMessage());
        return null;
    }

    private List<StandingDTO> getStandingsFallback(String competitionId, String seasonId, Exception e) {
        log.error("Circuit breaker fallback triggered for getStandings: {}", e.getMessage());
        return Collections.emptyList();
    }
}
