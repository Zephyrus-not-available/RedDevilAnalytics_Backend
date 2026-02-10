package com.reddevil.reddevilanalytics_backend.service;

import com.reddevil.reddevilanalytics_backend.domain.*;
import com.reddevil.reddevilanalytics_backend.provider.client.StandingsProviderClient;
import com.reddevil.reddevilanalytics_backend.provider.dto.StandingDTO;
import com.reddevil.reddevilanalytics_backend.repository.CompetitionRepository;
import com.reddevil.reddevilanalytics_backend.repository.SeasonRepository;
import com.reddevil.reddevilanalytics_backend.repository.StandingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandingsService {

    private final StandingRepository standingRepository;
    private final CompetitionRepository competitionRepository;
    private final SeasonRepository seasonRepository;
    private final ExternalRefService externalRefService;
    private final StandingsProviderClient standingsProviderClient;

    @Cacheable(value = "standings", key = "#competitionId + '_' + #seasonId")
    @Transactional(readOnly = true)
    public List<Standing> getStandings(Long competitionId, Long seasonId) {
        log.debug("Getting standings for competition ID {} and season ID {}", competitionId, seasonId);
        
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        Optional<Season> seasonOpt = seasonRepository.findById(seasonId);
        
        if (competitionOpt.isEmpty() || seasonOpt.isEmpty()) {
            log.error("Competition or season not found");
            return new ArrayList<>();
        }
        
        return standingRepository.findByCompetitionAndSeasonOrderByPositionAsc(
                competitionOpt.get(), seasonOpt.get());
    }

    @CacheEvict(value = "standings", key = "#competitionId + '_' + #seasonId")
    @Transactional
    public void syncStandings(Long competitionId, Long seasonId) {
        log.info("Syncing standings for competition ID {} and season ID {}", competitionId, seasonId);
        
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        Optional<Season> seasonOpt = seasonRepository.findById(seasonId);
        
        if (competitionOpt.isEmpty() || seasonOpt.isEmpty()) {
            log.error("Competition or season not found");
            throw new IllegalArgumentException("Competition or season not found");
        }
        
        Competition competition = competitionOpt.get();
        Season season = seasonOpt.get();
        
        Optional<String> externalCompIdOpt = externalRefService.getExternalId(
                EntityType.COMPETITION, competition.getId(), Provider.FOOTBALL_DATA);
        
        if (externalCompIdOpt.isEmpty()) {
            log.warn("No external competition ID found for competition {}", competition.getName());
            return;
        }
        
        String externalCompId = externalCompIdOpt.get();
        String externalSeasonId = season.getName();
        
        try {
            List<StandingDTO> standingsDto = standingsProviderClient.getStandings(externalCompId, externalSeasonId);
            log.info("Fetched {} standings from provider", standingsDto.size());
            
            for (StandingDTO standingDto : standingsDto) {
                saveOrUpdateStanding(standingDto, competition, season);
            }
            
            log.info("Successfully synced {} standings", standingsDto.size());
        } catch (Exception e) {
            log.error("Error syncing standings: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to sync standings", e);
        }
    }

    private void saveOrUpdateStanding(StandingDTO standingDto, Competition competition, Season season) {
        Team team = externalRefService.findOrCreateTeam(
                Provider.FOOTBALL_DATA,
                standingDto.getTeam().getId().toString(),
                standingDto.getTeam());
        
        Optional<Standing> existingStanding = standingRepository.findByCompetitionAndSeasonAndTeam(
                competition, season, team);
        
        Standing standing;
        if (existingStanding.isPresent()) {
            standing = existingStanding.get();
            log.debug("Updating existing standing for team: {}", team.getName());
        } else {
            standing = Standing.builder()
                    .competition(competition)
                    .season(season)
                    .team(team)
                    .build();
            log.debug("Creating new standing for team: {}", team.getName());
        }
        
        standing.setPosition(standingDto.getPosition().intValue());
        standing.setPlayedGames(standingDto.getPlayedGames());
        standing.setWon(standingDto.getWon());
        standing.setDraw(standingDto.getDraw());
        standing.setLost(standingDto.getLost());
        standing.setPoints(standingDto.getPoints());
        standing.setGoalsFor(standingDto.getGoalsFor());
        standing.setGoalsAgainst(standingDto.getGoalsAgainst());
        standing.setGoalDifference(standingDto.getGoalDifference());
        standing.setForm(standingDto.getForm());
        
        standingRepository.save(standing);
    }
}
