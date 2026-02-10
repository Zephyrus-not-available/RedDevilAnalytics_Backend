package com.reddevil.reddevilanalytics_backend.controller;

import com.reddevil.reddevilanalytics_backend.domain.PlayerAsset;
import com.reddevil.reddevilanalytics_backend.domain.TeamAsset;
import com.reddevil.reddevilanalytics_backend.dto.AssetResponse;
import com.reddevil.reddevilanalytics_backend.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@Tag(name = "Assets", description = "Team and player asset endpoints")
public class AssetController {

    private final AssetService assetService;

    @GetMapping("/teams/{teamId}")
    @Operation(summary = "Get team assets", 
               description = "Returns logo and banner URLs for a team")
    public ResponseEntity<AssetResponse> getTeamAssets(
            @Parameter(description = "Team ID") @PathVariable Long teamId) {
        
        log.info("Getting team assets for team ID: {}", teamId);
        
        Optional<TeamAsset> assetOpt = assetService.getTeamAssets(teamId);
        
        if (assetOpt.isEmpty()) {
            log.warn("No assets found for team ID: {}", teamId);
            return ResponseEntity.notFound().build();
        }
        
        TeamAsset asset = assetOpt.get();
        AssetResponse response = AssetResponse.builder()
                .logoUrl(asset.getLogoUrl())
                .bannerUrl(asset.getBannerUrl())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/players/{playerId}")
    @Operation(summary = "Get player assets", 
               description = "Returns photo and cutout URLs for a player")
    public ResponseEntity<AssetResponse> getPlayerAssets(
            @Parameter(description = "Player ID") @PathVariable Long playerId) {
        
        log.info("Getting player assets for player ID: {}", playerId);
        
        Optional<PlayerAsset> assetOpt = assetService.getPlayerAssets(playerId);
        
        if (assetOpt.isEmpty()) {
            log.warn("No assets found for player ID: {}", playerId);
            return ResponseEntity.notFound().build();
        }
        
        PlayerAsset asset = assetOpt.get();
        AssetResponse response = AssetResponse.builder()
                .photoUrl(asset.getPhotoUrl())
                .cutoutUrl(asset.getCutoutUrl())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
