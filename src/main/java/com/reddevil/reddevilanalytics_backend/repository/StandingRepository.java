package com.reddevil.reddevilanalytics_backend.repository;

import com.reddevil.reddevilanalytics_backend.domain.Standing;
import com.reddevil.reddevilanalytics_backend.domain.Competition;
import com.reddevil.reddevilanalytics_backend.domain.Season;
import com.reddevil.reddevilanalytics_backend.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StandingRepository extends JpaRepository<Standing, Long> {
    List<Standing> findByCompetitionAndSeasonOrderByPositionAsc(Competition competition, Season season);
    Optional<Standing> findByCompetitionAndSeasonAndTeam(Competition competition, Season season, Team team);
}
