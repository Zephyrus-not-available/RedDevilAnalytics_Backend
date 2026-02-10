package com.reddevil.reddevilanalytics_backend.repository;

import com.reddevil.reddevilanalytics_backend.domain.Match;
import com.reddevil.reddevilanalytics_backend.domain.MatchStatus;
import com.reddevil.reddevilanalytics_backend.domain.Competition;
import com.reddevil.reddevilanalytics_backend.domain.Season;
import com.reddevil.reddevilanalytics_backend.domain.Team;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByHomeTeamOrAwayTeamOrderByMatchDateDesc(Team homeTeam, Team awayTeam, Pageable pageable);
    List<Match> findByCompetitionAndSeasonOrderByMatchDateAsc(Competition competition, Season season);
    List<Match> findByStatusOrderByMatchDateAsc(MatchStatus status);
    Optional<Match> findFirstByHomeTeamAndAwayTeamAndMatchDateAfterOrderByMatchDateAsc(Team homeTeam, Team awayTeam, LocalDateTime matchDate);
    List<Match> findByMatchDateBetween(LocalDateTime start, LocalDateTime end);
}
