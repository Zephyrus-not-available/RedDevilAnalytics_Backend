package com.reddevil.reddevilanalytics_backend.repository;

import com.reddevil.reddevilanalytics_backend.domain.MatchPrediction;
import com.reddevil.reddevilanalytics_backend.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchPredictionRepository extends JpaRepository<MatchPrediction, Long> {
    Optional<MatchPrediction> findByMatch(Match match);
}
