package com.reddevil.reddevilanalytics_backend.service;

import com.reddevil.reddevilanalytics_backend.domain.*;
import com.reddevil.reddevilanalytics_backend.provider.dto.PlayerDTO;
import com.reddevil.reddevilanalytics_backend.provider.dto.TeamDTO;
import com.reddevil.reddevilanalytics_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalRefService {

    private final ExternalRefRepository externalRefRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final CompetitionRepository competitionRepository;
    private final SeasonRepository seasonRepository;

    @Transactional
    public Team findOrCreateTeam(Provider provider, String externalId, TeamDTO teamDto) {
        log.debug("Finding or creating team with external ID {} from provider {}", externalId, provider);
        
        Optional<ExternalRef> externalRef = externalRefRepository
                .findByEntityTypeAndProviderAndExternalId(EntityType.TEAM, provider, externalId);
        
        if (externalRef.isPresent()) {
            Long teamId = externalRef.get().getEntityId();
            Optional<Team> team = teamRepository.findById(teamId);
            if (team.isPresent()) {
                log.debug("Found existing team: {}", team.get().getName());
                return team.get();
            }
        }
        
        Optional<Team> existingTeam = teamRepository.findByName(teamDto.getName());
        if (existingTeam.isPresent()) {
            saveExternalRef(EntityType.TEAM, existingTeam.get().getId(), provider, externalId);
            log.debug("Linked existing team {} to external ID {}", existingTeam.get().getName(), externalId);
            return existingTeam.get();
        }
        
        Team newTeam = Team.builder()
                .name(teamDto.getName())
                .shortName(teamDto.getShortName())
                .logoUrl(teamDto.getLogoUrl())
                .stadium(teamDto.getStadium())
                .build();
        newTeam = teamRepository.save(newTeam);
        saveExternalRef(EntityType.TEAM, newTeam.getId(), provider, externalId);
        log.info("Created new team: {}", newTeam.getName());
        return newTeam;
    }

    @Transactional
    public Player findOrCreatePlayer(Provider provider, String externalId, PlayerDTO playerDto) {
        log.debug("Finding or creating player with external ID {} from provider {}", externalId, provider);
        
        Optional<ExternalRef> externalRef = externalRefRepository
                .findByEntityTypeAndProviderAndExternalId(EntityType.PLAYER, provider, externalId);
        
        if (externalRef.isPresent()) {
            Long playerId = externalRef.get().getEntityId();
            Optional<Player> player = playerRepository.findById(playerId);
            if (player.isPresent()) {
                log.debug("Found existing player: {}", player.get().getName());
                return player.get();
            }
        }
        
        Optional<Player> existingPlayer = playerRepository.findByName(playerDto.getName());
        if (existingPlayer.isPresent()) {
            saveExternalRef(EntityType.PLAYER, existingPlayer.get().getId(), provider, externalId);
            log.debug("Linked existing player {} to external ID {}", existingPlayer.get().getName(), externalId);
            return existingPlayer.get();
        }
        
        Player newPlayer = Player.builder()
                .name(playerDto.getName())
                .position(playerDto.getPosition())
                .shirtNumber(playerDto.getShirtNumber())
                .nationality(playerDto.getNationality())
                .dateOfBirth(playerDto.getDateOfBirth())
                .build();
        newPlayer = playerRepository.save(newPlayer);
        saveExternalRef(EntityType.PLAYER, newPlayer.getId(), provider, externalId);
        log.info("Created new player: {}", newPlayer.getName());
        return newPlayer;
    }

    @Transactional
    public Competition findOrCreateCompetition(Provider provider, String externalId, String name) {
        log.debug("Finding or creating competition with external ID {} from provider {}", externalId, provider);
        
        Optional<ExternalRef> externalRef = externalRefRepository
                .findByEntityTypeAndProviderAndExternalId(EntityType.COMPETITION, provider, externalId);
        
        if (externalRef.isPresent()) {
            Long competitionId = externalRef.get().getEntityId();
            Optional<Competition> competition = competitionRepository.findById(competitionId);
            if (competition.isPresent()) {
                log.debug("Found existing competition: {}", competition.get().getName());
                return competition.get();
            }
        }
        
        Optional<Competition> existingCompetition = competitionRepository.findByName(name);
        if (existingCompetition.isPresent()) {
            saveExternalRef(EntityType.COMPETITION, existingCompetition.get().getId(), provider, externalId);
            log.debug("Linked existing competition {} to external ID {}", existingCompetition.get().getName(), externalId);
            return existingCompetition.get();
        }
        
        Competition newCompetition = Competition.builder()
                .name(name)
                .build();
        newCompetition = competitionRepository.save(newCompetition);
        saveExternalRef(EntityType.COMPETITION, newCompetition.getId(), provider, externalId);
        log.info("Created new competition: {}", newCompetition.getName());
        return newCompetition;
    }

    @Transactional
    public Season findOrCreateSeason(String name, LocalDate startDate, LocalDate endDate) {
        log.debug("Finding or creating season: {}", name);
        
        Optional<Season> existingSeason = seasonRepository.findByName(name);
        if (existingSeason.isPresent()) {
            log.debug("Found existing season: {}", existingSeason.get().getName());
            return existingSeason.get();
        }
        
        Season newSeason = Season.builder()
                .name(name)
                .startDate(startDate)
                .endDate(endDate)
                .isCurrent(false)
                .build();
        newSeason = seasonRepository.save(newSeason);
        log.info("Created new season: {}", newSeason.getName());
        return newSeason;
    }

    @Transactional(readOnly = true)
    public Optional<Long> getInternalId(EntityType type, Provider provider, String externalId) {
        log.debug("Getting internal ID for {} with external ID {} from provider {}", type, externalId, provider);
        
        return externalRefRepository
                .findByEntityTypeAndProviderAndExternalId(type, provider, externalId)
                .map(ExternalRef::getEntityId);
    }

    @Transactional(readOnly = true)
    public Optional<String> getExternalId(EntityType type, Long entityId, Provider provider) {
        log.debug("Getting external ID for {} entity ID {} from provider {}", type, entityId, provider);
        
        return externalRefRepository
                .findByEntityTypeAndEntityIdAndProvider(type, entityId, provider)
                .map(ExternalRef::getExternalId);
    }

    @Transactional
    public void saveExternalRef(EntityType type, Long entityId, Provider provider, String externalId) {
        log.debug("Saving external reference for {} ID {} with external ID {} from provider {}", 
                type, entityId, externalId, provider);
        
        Optional<ExternalRef> existing = externalRefRepository
                .findByEntityTypeAndEntityIdAndProvider(type, entityId, provider);
        
        if (existing.isEmpty()) {
            ExternalRef externalRef = ExternalRef.builder()
                    .entityType(type)
                    .entityId(entityId)
                    .provider(provider)
                    .externalId(externalId)
                    .build();
            externalRefRepository.save(externalRef);
            log.debug("Saved external reference for {} ID {}", type, entityId);
        } else {
            log.debug("External reference already exists for {} ID {}", type, entityId);
        }
    }
}
