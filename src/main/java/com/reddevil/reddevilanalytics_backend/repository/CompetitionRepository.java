package com.reddevil.reddevilanalytics_backend.repository;

import com.reddevil.reddevilanalytics_backend.domain.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    Optional<Competition> findByName(String name);
    Optional<Competition> findByCode(String code);
}
