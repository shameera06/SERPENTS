package com.shareptiles.app.repository;

import com.shareptiles.app.model.HealthAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthAlertRepository extends JpaRepository<HealthAlert, Long> {
    List<HealthAlert> findAllByOrderByTriggeredAtDesc();
    List<HealthAlert> findByStatusOrderByTriggeredAtDesc(String status);
    List<HealthAlert> findByStatus(String status);
    List<HealthAlert> findByReptileIdOrderByTriggeredAtDesc(Long reptileId);
    List<HealthAlert> findByReptileIdAndStatus(Long reptileId, String status);
    Optional<HealthAlert> findByFeedingRecordId(Long feedingRecordId);

    long countByStatus(String status);
    long countBySeverityAndStatus(String severity, String status);
    long countByReptileId(Long reptileId);
    long countByReptileIdAndStatus(Long reptileId, String status);
}
