package com.reddevil.reddevilanalytics_backend.repository;

import com.reddevil.reddevilanalytics_backend.domain.TeamAsset;
import com.reddevil.reddevilanalytics_backend.domain.Team;
import com.reddevil.reddevilanalytics_backend.domain.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamAssetRepository extends JpaRepository<TeamAsset, Long> {
    Optional<TeamAsset> findByTeamAndProvider(Team team, Provider provider);
    Optional<TeamAsset> findByTeam(Team team);
}
