package com.shareptiles.app.repository;

import com.shareptiles.app.model.FeedingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedingRecordRepository extends JpaRepository<FeedingRecord, Long> {
    List<FeedingRecord> findByReptileIdOrderByFeedingDateDescIdDesc(Long reptileId);
    List<FeedingRecord> findByReptileIdOrderByFeedingDateAscIdAsc(Long reptileId);
    Optional<FeedingRecord> findTopByReptileIdOrderByFeedingDateDescIdDesc(Long reptileId);
    List<FeedingRecord> findByWeightAlertTriggeredTrue();
    long countByReptileId(Long reptileId);
    long countByWeightAlertTriggeredTrue();
}
