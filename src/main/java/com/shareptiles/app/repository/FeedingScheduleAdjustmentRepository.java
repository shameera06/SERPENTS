package com.shareptiles.app.repository;

import com.shareptiles.app.model.FeedingScheduleAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedingScheduleAdjustmentRepository extends JpaRepository<FeedingScheduleAdjustment, Long> {
    List<FeedingScheduleAdjustment> findByFeedingScheduleIdOrderByAdjustedAtDesc(Long scheduleId);
    List<FeedingScheduleAdjustment> findByReptileIdOrderByAdjustedAtDesc(Long reptileId);
}
