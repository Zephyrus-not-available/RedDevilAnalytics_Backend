package com.reddevil.reddevilanalytics_backend.repository;

import com.reddevil.reddevilanalytics_backend.domain.SquadMember;
import com.reddevil.reddevilanalytics_backend.domain.Team;
import com.reddevil.reddevilanalytics_backend.domain.Season;
import com.reddevil.reddevilanalytics_backend.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SquadMemberRepository extends JpaRepository<SquadMember, Long> {
    List<SquadMember> findByTeamAndSeasonAndIsActiveTrue(Team team, Season season);
    List<SquadMember> findByPlayerAndIsActiveTrue(Player player);
}
