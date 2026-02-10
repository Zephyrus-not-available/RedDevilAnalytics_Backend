package com.reddevil.reddevilanalytics_backend.service;

import com.reddevil.reddevilanalytics_backend.domain.*;
import com.reddevil.reddevilanalytics_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SquadService {

    private final SquadMemberRepository squadMemberRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final SeasonRepository seasonRepository;

    @Transactional(readOnly = true)
    public List<SquadMember> getActiveSquad(Long teamId, Long seasonId) {
        log.debug("Getting active squad for team ID {} and season ID {}", teamId, seasonId);
        
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<Season> seasonOpt = seasonRepository.findById(seasonId);
        
        if (teamOpt.isEmpty() || seasonOpt.isEmpty()) {
            log.error("Team or season not found");
            return new ArrayList<>();
        }
        
        return squadMemberRepository.findByTeamAndSeasonAndIsActiveTrue(teamOpt.get(), seasonOpt.get());
    }

    @Transactional
    public SquadMember addSquadMember(Long playerId, Long teamId, Long seasonId, LocalDate startDate) {
        log.info("Adding squad member: player ID {}, team ID {}, season ID {}", playerId, teamId, seasonId);
        
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<Season> seasonOpt = seasonRepository.findById(seasonId);
        
        if (playerOpt.isEmpty() || teamOpt.isEmpty() || seasonOpt.isEmpty()) {
            log.error("Player, team, or season not found");
            throw new IllegalArgumentException("Player, team, or season not found");
        }
        
        Player player = playerOpt.get();
        Team team = teamOpt.get();
        Season season = seasonOpt.get();
        
        SquadMember squadMember = SquadMember.builder()
                .player(player)
                .team(team)
                .season(season)
                .startDate(startDate)
                .isActive(true)
                .shirtNumber(player.getShirtNumber())
                .position(player.getPosition())
                .build();
        
        squadMember = squadMemberRepository.save(squadMember);
        log.info("Successfully added squad member: {} to team {}", player.getName(), team.getName());
        return squadMember;
    }

    @Transactional
    public void removeSquadMember(Long squadMemberId, LocalDate endDate) {
        log.info("Removing squad member ID: {} with end date {}", squadMemberId, endDate);
        
        Optional<SquadMember> squadMemberOpt = squadMemberRepository.findById(squadMemberId);
        if (squadMemberOpt.isEmpty()) {
            log.error("Squad member not found with ID: {}", squadMemberId);
            throw new IllegalArgumentException("Squad member not found");
        }
        
        SquadMember squadMember = squadMemberOpt.get();
        squadMember.setEndDate(endDate);
        squadMember.setIsActive(false);
        
        squadMemberRepository.save(squadMember);
        log.info("Successfully removed squad member: {}", squadMember.getPlayer().getName());
    }
}
