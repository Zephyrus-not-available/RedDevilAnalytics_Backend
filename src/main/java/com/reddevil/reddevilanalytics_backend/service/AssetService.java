package com.reddevil.reddevilanalytics_backend.service;

import com.reddevil.reddevilanalytics_backend.domain.*;
import com.reddevil.reddevilanalytics_backend.provider.client.AssetProviderClient;
import com.reddevil.reddevilanalytics_backend.provider.dto.AssetDTO;
import com.reddevil.reddevilanalytics_backend.repository.PlayerAssetRepository;
import com.reddevil.reddevilanalytics_backend.repository.PlayerRepository;
import com.reddevil.reddevilanalytics_backend.repository.TeamAssetRepository;
import com.reddevil.reddevilanalytics_backend.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {

    private final TeamAssetRepository teamAssetRepository;
    private final PlayerAssetRepository playerAssetRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final AssetProviderClient assetProviderClient;

    @Cacheable(value = "teamAssets", key = "#teamId")
    @Transactional(readOnly = true)
    public Optional<TeamAsset> getTeamAssets(Long teamId) {
        log.debug("Getting team assets for team ID: {}", teamId);
        
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            log.error("Team not found with ID: {}", teamId);
            return Optional.empty();
        }
        
        return teamAssetRepository.findByTeam(teamOpt.get());
    }

    @Cacheable(value = "playerAssets", key = "#playerId")
    @Transactional(readOnly = true)
    public Optional<PlayerAsset> getPlayerAssets(Long playerId) {
        log.debug("Getting player assets for player ID: {}", playerId);
        
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            log.error("Player not found with ID: {}", playerId);
            return Optional.empty();
        }
        
        return playerAssetRepository.findByPlayer(playerOpt.get());
    }

    @Transactional
    public void refreshTeamAssets(Long teamId) {
        log.info("Refreshing team assets for team ID: {}", teamId);
        
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            log.error("Team not found with ID: {}", teamId);
            throw new IllegalArgumentException("Team not found");
        }
        
        Team team = teamOpt.get();
        
        try {
            AssetDTO assetDto = assetProviderClient.getTeamAssets(team.getName());
            log.info("Fetched team assets for team: {}", team.getName());
            
            Optional<TeamAsset> existingAsset = teamAssetRepository.findByTeamAndProvider(
                    team, Provider.THESPORTSDB);
            
            TeamAsset teamAsset;
            if (existingAsset.isPresent()) {
                teamAsset = existingAsset.get();
                log.debug("Updating existing team asset for team: {}", team.getName());
            } else {
                teamAsset = TeamAsset.builder()
                        .team(team)
                        .provider(Provider.THESPORTSDB)
                        .build();
                log.debug("Creating new team asset for team: {}", team.getName());
            }
            
            teamAsset.setLogoUrl(assetDto.getLogoUrl());
            teamAsset.setBannerUrl(assetDto.getBannerUrl());
            
            teamAssetRepository.save(teamAsset);
            log.info("Successfully saved team assets for team: {}", team.getName());
        } catch (Exception e) {
            log.error("Error refreshing team assets for team {}: {}", team.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to refresh team assets", e);
        }
    }

    @Transactional
    public void refreshPlayerAssets(Long playerId) {
        log.info("Refreshing player assets for player ID: {}", playerId);
        
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            log.error("Player not found with ID: {}", playerId);
            throw new IllegalArgumentException("Player not found");
        }
        
        Player player = playerOpt.get();
        
        try {
            AssetDTO assetDto = assetProviderClient.getPlayerAssets(player.getName());
            log.info("Fetched player assets for player: {}", player.getName());
            
            Optional<PlayerAsset> existingAsset = playerAssetRepository.findByPlayerAndProvider(
                    player, Provider.THESPORTSDB);
            
            PlayerAsset playerAsset;
            if (existingAsset.isPresent()) {
                playerAsset = existingAsset.get();
                log.debug("Updating existing player asset for player: {}", player.getName());
            } else {
                playerAsset = PlayerAsset.builder()
                        .player(player)
                        .provider(Provider.THESPORTSDB)
                        .build();
                log.debug("Creating new player asset for player: {}", player.getName());
            }
            
            playerAsset.setPhotoUrl(assetDto.getPhotoUrl());
            playerAsset.setCutoutUrl(assetDto.getCutoutUrl());
            
            playerAssetRepository.save(playerAsset);
            log.info("Successfully saved player assets for player: {}", player.getName());
        } catch (Exception e) {
            log.error("Error refreshing player assets for player {}: {}", player.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to refresh player assets", e);
        }
    }
}
