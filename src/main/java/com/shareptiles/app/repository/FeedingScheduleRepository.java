package com.shareptiles.app.repository;

import com.shareptiles.app.model.FeedingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedingScheduleRepository extends JpaRepository<FeedingSchedule, Long> {
    List<FeedingSchedule> findByReptileId(Long reptileId);
    List<FeedingSchedule> findByStatus(String status);
    List<FeedingSchedule> findByStatusAndNextDueAtLessThanEqualOrderByNextDueAtAsc(String status, LocalDateTime due);
    long countByStatus(String status);
}
