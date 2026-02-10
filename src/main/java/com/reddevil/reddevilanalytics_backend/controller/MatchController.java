package com.reddevil.reddevilanalytics_backend.controller;

import com.reddevil.reddevilanalytics_backend.domain.*;
import com.reddevil.reddevilanalytics_backend.dto.*;
import com.reddevil.reddevilanalytics_backend.repository.MatchPredictionRepository;
import com.reddevil.reddevilanalytics_backend.service.AssetService;
import com.reddevil.reddevilanalytics_backend.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
@Tag(name = "Matches", description = "Match information endpoints")
public class MatchController {

    private final MatchService matchService;
    private final AssetService assetService;
    private final MatchPredictionRepository matchPredictionRepository;

    @GetMapping("/next")
    @Operation(summary = "Get next match for a team", 
               description = "Returns the next scheduled match for the specified team")
    public ResponseEntity<MatchHeroResponse> getNextMatch(
            @Parameter(description = "Team ID") @RequestParam Long teamId,
            @Parameter(description = "Season ID (optional)") @RequestParam(required = false) Long seasonId) {
        
        log.info("Getting next match for team ID {} and season ID {}", teamId, seasonId);
        
        Optional<Match> matchOpt = matchService.getNextMatch(teamId, seasonId);
        
        if (matchOpt.isEmpty()) {
            log.warn("No upcoming match found for team ID {}", teamId);
            return ResponseEntity.notFound().build();
        }
        
        Match match = matchOpt.get();
        MatchHeroResponse response = buildMatchHeroResponse(match);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get match by ID", description = "Returns detailed match information by match ID")
    public ResponseEntity<MatchHeroResponse> getMatchById(
            @Parameter(description = "Match ID") @PathVariable Long id) {
        
        log.info("Getting match by ID: {}", id);
        
        Optional<Match> matchOpt = matchService.getMatchById(id);
        
        if (matchOpt.isEmpty()) {
            log.warn("Match not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        Match match = matchOpt.get();
        MatchHeroResponse response = buildMatchHeroResponse(match);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/live")
    @Operation(summary = "Get live matches", description = "Returns all live matches for a competition")
    public ResponseEntity<LiveScoresResponse> getLiveMatches(
            @Parameter(description = "Competition ID") @RequestParam Long competitionId) {
        
        log.info("Getting live matches for competition ID: {}", competitionId);
        
        List<Match> liveMatches = matchService.getLiveMatches(competitionId);
        
        List<LiveMatchItem> items = liveMatches.stream()
                .map(this::buildLiveMatchItem)
                .collect(Collectors.toList());
        
        LiveScoresResponse response = LiveScoresResponse.builder()
                .matches(items)
                .build();
        
        return ResponseEntity.ok(response);
    }

    private MatchHeroResponse buildMatchHeroResponse(Match match) {
        TeamInfo homeTeam = buildTeamInfo(match.getHomeTeam());
        TeamInfo awayTeam = buildTeamInfo(match.getAwayTeam());
        
        PredictionInfo prediction = null;
        Optional<MatchPrediction> predictionOpt = matchPredictionRepository.findByMatch(match);
        if (predictionOpt.isPresent()) {
            MatchPrediction mp = predictionOpt.get();
            prediction = PredictionInfo.builder()
                    .homeWinProb(mp.getHomeWinProbability())
                    .drawProb(mp.getDrawProbability())
                    .awayWinProb(mp.getAwayWinProbability())
                    .confidence(mp.getConfidenceScore())
                    .build();
        }
        
        Integer currentMinute = null;
        if (match.getStatus() == MatchStatus.LIVE) {
            long minutesElapsed = ChronoUnit.MINUTES.between(match.getMatchDate(), LocalDateTime.now());
            currentMinute = (int) Math.min(minutesElapsed, 90);
        }
        
        return MatchHeroResponse.builder()
                .matchId(match.getId())
                .matchDate(match.getMatchDate())
                .venue(match.getVenue())
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .status(match.getStatus().name())
                .homeScore(match.getHomeScore())
                .awayScore(match.getAwayScore())
                .competition(match.getCompetition() != null ? match.getCompetition().getName() : null)
                .prediction(prediction)
                .currentMinute(currentMinute)
                .build();
    }

    private LiveMatchItem buildLiveMatchItem(Match match) {
        TeamInfo homeTeam = buildTeamInfo(match.getHomeTeam());
        TeamInfo awayTeam = buildTeamInfo(match.getAwayTeam());
        
        Integer minute = null;
        if (match.getStatus() == MatchStatus.LIVE) {
            long minutesElapsed = ChronoUnit.MINUTES.between(match.getMatchDate(), LocalDateTime.now());
            minute = (int) Math.min(minutesElapsed, 90);
        }
        
        return LiveMatchItem.builder()
                .matchId(match.getId())
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .homeScore(match.getHomeScore())
                .awayScore(match.getAwayScore())
                .minute(minute)
                .status(match.getStatus().name())
                .build();
    }

    private TeamInfo buildTeamInfo(Team team) {
        String logoUrl = team.getLogoUrl();
        
        Optional<TeamAsset> assetOpt = assetService.getTeamAssets(team.getId());
        if (assetOpt.isPresent() && assetOpt.get().getLogoUrl() != null) {
            logoUrl = assetOpt.get().getLogoUrl();
        }
        
        return TeamInfo.builder()
                .id(team.getId())
                .name(team.getName())
                .logo(logoUrl)
                .build();
    }
}
