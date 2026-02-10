package com.reddevil.reddevilanalytics_backend.service;

import com.reddevil.reddevilanalytics_backend.domain.*;
import com.reddevil.reddevilanalytics_backend.provider.client.FixtureProviderClient;
import com.reddevil.reddevilanalytics_backend.provider.client.LiveMatchProviderClient;
import com.reddevil.reddevilanalytics_backend.provider.dto.FixtureDTO;
import com.reddevil.reddevilanalytics_backend.provider.dto.LiveMatchDTO;
import com.reddevil.reddevilanalytics_backend.repository.CompetitionRepository;
import com.reddevil.reddevilanalytics_backend.repository.MatchRepository;
import com.reddevil.reddevilanalytics_backend.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final CompetitionRepository competitionRepository;
    private final SeasonRepository seasonRepository;
    private final ExternalRefService externalRefService;
    private final FixtureProviderClient fixtureProviderClient;
    private final LiveMatchProviderClient liveMatchProviderClient;

    @Cacheable(value = "nextMatch", key = "#teamId + '_' + #seasonId")
    @Transactional(readOnly = true)
    public Optional<Match> getNextMatch(Long teamId, Long seasonId) {
        log.debug("Getting next match for team ID {} and season ID {}", teamId, seasonId);
        
        LocalDateTime now = LocalDateTime.now();
        List<Match> matches = matchRepository.findByMatchDateBetween(now, now.plusMonths(1));
        
        return matches.stream()
                .filter(m -> (m.getHomeTeam().getId().equals(teamId) || m.getAwayTeam().getId().equals(teamId))
                        && (seasonId == null || m.getSeason().getId().equals(seasonId)))
                .filter(m -> m.getMatchDate().isAfter(now))
                .min((m1, m2) -> m1.getMatchDate().compareTo(m2.getMatchDate()));
    }

    @Transactional(readOnly = true)
    public Optional<Match> getMatchById(Long matchId) {
        log.debug("Getting match by ID: {}", matchId);
        return matchRepository.findById(matchId);
    }

    @Transactional
    public void syncFixtures(Long competitionId, Long seasonId) {
        log.info("Syncing fixtures for competition ID {} and season ID {}", competitionId, seasonId);
        
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
            List<FixtureDTO> fixtures = fixtureProviderClient.getFixtures(externalCompId, externalSeasonId);
            log.info("Fetched {} fixtures from provider", fixtures.size());
            
            for (FixtureDTO fixtureDto : fixtures) {
                saveOrUpdateFixture(fixtureDto, competition, season);
            }
            
            log.info("Successfully synced {} fixtures", fixtures.size());
        } catch (Exception e) {
            log.error("Error syncing fixtures: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to sync fixtures", e);
        }
    }

    @Transactional(readOnly = true)
    public List<Match> getLiveMatches(Long competitionId) {
        log.debug("Getting live matches for competition ID: {}", competitionId);
        
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            log.error("Competition not found");
            return new ArrayList<>();
        }
        
        Competition competition = competitionOpt.get();
        Optional<String> externalCompIdOpt = externalRefService.getExternalId(
                EntityType.COMPETITION, competition.getId(), Provider.API_FOOTBALL);
        
        if (externalCompIdOpt.isEmpty()) {
            log.warn("No external competition ID found for competition {}", competition.getName());
            return new ArrayList<>();
        }
        
        try {
            List<LiveMatchDTO> liveMatches = liveMatchProviderClient.getLiveMatches(externalCompIdOpt.get());
            log.info("Fetched {} live matches from provider", liveMatches.size());
            
            return liveMatches.stream()
                    .map(this::mergeLiveDataWithFixture)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } catch (Exception e) {
            log.error("Error fetching live matches: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private void saveOrUpdateFixture(FixtureDTO fixtureDto, Competition competition, Season season) {
        Team homeTeam = externalRefService.findOrCreateTeam(
                Provider.FOOTBALL_DATA, 
                fixtureDto.getHomeTeam().getId().toString(), 
                fixtureDto.getHomeTeam());
        
        Team awayTeam = externalRefService.findOrCreateTeam(
                Provider.FOOTBALL_DATA, 
                fixtureDto.getAwayTeam().getId().toString(), 
                fixtureDto.getAwayTeam());
        
        Optional<Match> existingMatch = matchRepository.findFirstByHomeTeamAndAwayTeamAndMatchDateAfterOrderByMatchDateAsc(
                homeTeam, awayTeam, fixtureDto.getMatchDate().minusDays(1));
        
        Match match;
        if (existingMatch.isPresent()) {
            match = existingMatch.get();
            log.debug("Updating existing match: {} vs {}", homeTeam.getName(), awayTeam.getName());
        } else {
            match = Match.builder()
                    .homeTeam(homeTeam)
                    .awayTeam(awayTeam)
                    .competition(competition)
                    .season(season)
                    .build();
            log.debug("Creating new match: {} vs {}", homeTeam.getName(), awayTeam.getName());
        }
        
        match.setMatchDate(fixtureDto.getMatchDate());
        match.setStatus(mapStatus(fixtureDto.getStatus()));
        match.setHomeScore(fixtureDto.getHomeScore());
        match.setAwayScore(fixtureDto.getAwayScore());
        match.setVenue(fixtureDto.getVenue());
        match.setReferee(fixtureDto.getReferee());
        
        matchRepository.save(match);
    }

    private Optional<Match> mergeLiveDataWithFixture(LiveMatchDTO liveDto) {
        Team homeTeam = externalRefService.findOrCreateTeam(
                Provider.API_FOOTBALL, 
                liveDto.getHomeTeam().getId().toString(), 
                liveDto.getHomeTeam());
        
        Team awayTeam = externalRefService.findOrCreateTeam(
                Provider.API_FOOTBALL, 
                liveDto.getAwayTeam().getId().toString(), 
                liveDto.getAwayTeam());
        
        LocalDateTime now = LocalDateTime.now();
        Optional<Match> matchOpt = matchRepository.findFirstByHomeTeamAndAwayTeamAndMatchDateAfterOrderByMatchDateAsc(
                homeTeam, awayTeam, now.minusHours(3));
        
        if (matchOpt.isEmpty()) {
            log.warn("No stored match found for live match: {} vs {}", homeTeam.getName(), awayTeam.getName());
            return Optional.empty();
        }
        
        Match match = matchOpt.get();
        match.setHomeScore(liveDto.getHomeScore());
        match.setAwayScore(liveDto.getAwayScore());
        match.setStatus(mapStatus(liveDto.getStatus()));
        
        return Optional.of(match);
    }

    private MatchStatus mapStatus(String status) {
        if (status == null) {
            return MatchStatus.SCHEDULED;
        }
        return switch (status.toUpperCase()) {
            case "SCHEDULED", "TIMED", "NS" -> MatchStatus.SCHEDULED;
            case "IN_PLAY", "LIVE", "1H", "2H", "HT", "ET", "P" -> MatchStatus.LIVE;
            case "FINISHED", "FT", "AET", "PEN" -> MatchStatus.FINISHED;
            case "POSTPONED", "PST" -> MatchStatus.POSTPONED;
            case "CANCELLED", "CANC" -> MatchStatus.CANCELLED;
            default -> MatchStatus.SCHEDULED;
        };
    }
}
