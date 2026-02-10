package com.reddevil.reddevilanalytics_backend.service;

import com.reddevil.reddevilanalytics_backend.domain.Competition;
import com.reddevil.reddevilanalytics_backend.domain.Season;
import com.reddevil.reddevilanalytics_backend.repository.CompetitionRepository;
import com.reddevil.reddevilanalytics_backend.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final MatchService matchService;
    private final StandingsService standingsService;
    private final CompetitionRepository competitionRepository;
    private final SeasonRepository seasonRepository;

    @Scheduled(cron = "0 0 2 * * *")
    public void syncAll() {
        log.info("Starting scheduled sync of all data");
        
        try {
            Optional<Season> currentSeason = seasonRepository.findByIsCurrentTrue();
            if (currentSeason.isEmpty()) {
                log.warn("No current season found, skipping sync");
                return;
            }
            
            List<Competition> competitions = competitionRepository.findAll();
            for (Competition competition : competitions) {
                try {
                    syncCompetitionFixtures(competition.getId());
                    syncCompetitionStandings(competition.getId());
                } catch (Exception e) {
                    log.error("Error syncing competition {}: {}", competition.getName(), e.getMessage(), e);
                }
            }
            
            log.info("Completed scheduled sync of all data");
        } catch (Exception e) {
            log.error("Error during scheduled sync: {}", e.getMessage(), e);
        }
    }

    public void syncCompetitionFixtures(Long competitionId) {
        log.info("Syncing fixtures for competition ID: {}", competitionId);
        
        try {
            Optional<Season> currentSeason = seasonRepository.findByIsCurrentTrue();
            if (currentSeason.isEmpty()) {
                log.warn("No current season found");
                throw new IllegalStateException("No current season found");
            }
            
            matchService.syncFixtures(competitionId, currentSeason.get().getId());
            log.info("Successfully synced fixtures for competition ID: {}", competitionId);
        } catch (Exception e) {
            log.error("Error syncing fixtures for competition {}: {}", competitionId, e.getMessage(), e);
            throw new RuntimeException("Failed to sync fixtures for competition " + competitionId, e);
        }
    }

    public void syncCompetitionStandings(Long competitionId) {
        log.info("Syncing standings for competition ID: {}", competitionId);
        
        try {
            Optional<Season> currentSeason = seasonRepository.findByIsCurrentTrue();
            if (currentSeason.isEmpty()) {
                log.warn("No current season found");
                throw new IllegalStateException("No current season found");
            }
            
            standingsService.syncStandings(competitionId, currentSeason.get().getId());
            log.info("Successfully synced standings for competition ID: {}", competitionId);
        } catch (Exception e) {
            log.error("Error syncing standings for competition {}: {}", competitionId, e.getMessage(), e);
            throw new RuntimeException("Failed to sync standings for competition " + competitionId, e);
        }
    }

    @Scheduled(fixedDelay = 300000)
    public void syncLiveMatches() {
        log.debug("Syncing live matches");
        
        try {
            List<Competition> competitions = competitionRepository.findAll();
            for (Competition competition : competitions) {
                try {
                    matchService.getLiveMatches(competition.getId());
                } catch (Exception e) {
                    log.error("Error syncing live matches for competition {}: {}", 
                            competition.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error during live match sync: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 3 * * SUN")
    public void syncAssets() {
        log.info("Starting scheduled sync of assets");
        
        try {
            log.info("Asset sync would be triggered here for teams and players");
            log.info("Completed scheduled sync of assets");
        } catch (Exception e) {
            log.error("Error during asset sync: {}", e.getMessage(), e);
        }
    }
}
