package com.reddevil.reddevilanalytics_backend.controller;

import com.reddevil.reddevilanalytics_backend.domain.Standing;
import com.reddevil.reddevilanalytics_backend.domain.TeamAsset;
import com.reddevil.reddevilanalytics_backend.dto.StandingItem;
import com.reddevil.reddevilanalytics_backend.dto.StandingsResponse;
import com.reddevil.reddevilanalytics_backend.service.AssetService;
import com.reddevil.reddevilanalytics_backend.service.StandingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/standings")
@RequiredArgsConstructor
@Tag(name = "Standings", description = "League standings endpoints")
public class StandingsController {

    private final StandingsService standingsService;
    private final AssetService assetService;

    @GetMapping
    @Operation(summary = "Get standings", 
               description = "Returns league standings for a competition and season")
    public ResponseEntity<StandingsResponse> getStandings(
            @Parameter(description = "Competition ID", required = true) @RequestParam Long competitionId,
            @Parameter(description = "Season ID", required = true) @RequestParam Long seasonId) {
        
        log.info("Getting standings for competition ID {} and season ID {}", competitionId, seasonId);
        
        List<Standing> standings = standingsService.getStandings(competitionId, seasonId);
        
        if (standings.isEmpty()) {
            log.warn("No standings found for competition ID {} and season ID {}", competitionId, seasonId);
            return ResponseEntity.notFound().build();
        }
        
        List<StandingItem> items = standings.stream()
                .map(this::buildStandingItem)
                .collect(Collectors.toList());
        
        StandingsResponse response = StandingsResponse.builder()
                .standings(items)
                .build();
        
        return ResponseEntity.ok(response);
    }

    private StandingItem buildStandingItem(Standing standing) {
        String logoUrl = standing.getTeam().getLogoUrl();
        
        Optional<TeamAsset> assetOpt = assetService.getTeamAssets(standing.getTeam().getId());
        if (assetOpt.isPresent() && assetOpt.get().getLogoUrl() != null) {
            logoUrl = assetOpt.get().getLogoUrl();
        }
        
        return StandingItem.builder()
                .position(standing.getPosition())
                .teamName(standing.getTeam().getName())
                .logo(logoUrl)
                .played(standing.getPlayedGames())
                .won(standing.getWon())
                .draw(standing.getDraw())
                .lost(standing.getLost())
                .points(standing.getPoints())
                .goalsFor(standing.getGoalsFor())
                .goalsAgainst(standing.getGoalsAgainst())
                .goalDifference(standing.getGoalDifference())
                .form(standing.getForm())
                .build();
    }
}
