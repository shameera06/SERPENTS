package com.shareptiles.app.service;

import com.shareptiles.app.model.*;
import com.shareptiles.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DashboardService {

    @Autowired
    private ReptileRepository reptileRepository;

    @Autowired
    private HealthAlertRepository healthAlertRepository;

    @Autowired
    private FeedingScheduleRepository feedingScheduleRepository;

    @Autowired
    private FeedingRecordRepository feedingRecordRepository;

    @Autowired
    private DiseasePredictionRepository diseasePredictionRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalReptiles = reptileRepository.countByIsArchivedFalse();
        long activeReptiles = reptileRepository.countByStatusAndIsArchivedFalse("active");
        long inactiveReptiles = totalReptiles - activeReptiles;

        long totalAlerts = healthAlertRepository.count();
        long activeAlerts = healthAlertRepository.countByStatus("active");
        long criticalAlerts = healthAlertRepository.countBySeverityAndStatus("critical", "active");

        long activeSchedules = feedingScheduleRepository.countByStatus("active");
        List<FeedingSchedule> dueSchedules = feedingScheduleRepository.findByStatusAndNextDueAtLessThanEqualOrderByNextDueAtAsc(
                "active", LocalDateTime.now().plusDays(1));

        long totalPredictions = diseasePredictionRepository.count();
        long pendingReviews = diseasePredictionRepository.countByStatus("pending_review");

        stats.put("totalReptiles", totalReptiles);
        stats.put("activeReptiles", activeReptiles);
        stats.put("inactiveReptiles", inactiveReptiles);
        stats.put("totalAlerts", totalAlerts);
        stats.put("activeAlerts", activeAlerts);
        stats.put("criticalAlerts", criticalAlerts);
        stats.put("activeSchedules", activeSchedules);
        stats.put("dueSchedulesCount", dueSchedules.size());
        stats.put("dueSchedules", dueSchedules);
        stats.put("totalPredictions", totalPredictions);
        stats.put("pendingReviews", pendingReviews);

        return stats;
    }
}
