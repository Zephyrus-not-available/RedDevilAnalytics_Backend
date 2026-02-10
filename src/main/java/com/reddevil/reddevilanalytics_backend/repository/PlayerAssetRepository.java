package com.reddevil.reddevilanalytics_backend.repository;

import com.reddevil.reddevilanalytics_backend.domain.PlayerAsset;
import com.reddevil.reddevilanalytics_backend.domain.Player;
import com.reddevil.reddevilanalytics_backend.domain.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerAssetRepository extends JpaRepository<PlayerAsset, Long> {
    Optional<PlayerAsset> findByPlayerAndProvider(Player player, Provider provider);
    Optional<PlayerAsset> findByPlayer(Player player);
}
