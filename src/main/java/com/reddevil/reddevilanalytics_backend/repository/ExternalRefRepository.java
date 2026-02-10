package com.reddevil.reddevilanalytics_backend.repository;

import com.reddevil.reddevilanalytics_backend.domain.ExternalRef;
import com.reddevil.reddevilanalytics_backend.domain.EntityType;
import com.reddevil.reddevilanalytics_backend.domain.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalRefRepository extends JpaRepository<ExternalRef, Long> {
    Optional<ExternalRef> findByEntityTypeAndProviderAndExternalId(EntityType entityType, Provider provider, String externalId);
    List<ExternalRef> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);
    Optional<ExternalRef> findByEntityTypeAndEntityIdAndProvider(EntityType entityType, Long entityId, Provider provider);
}
