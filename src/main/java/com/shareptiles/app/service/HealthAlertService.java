package com.shareptiles.app.service;

import com.shareptiles.app.model.FeedingRecord;
import com.shareptiles.app.model.HealthAlert;
import com.shareptiles.app.model.User;
import com.shareptiles.app.repository.FeedingRecordRepository;
import com.shareptiles.app.repository.HealthAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HealthAlertService {

    @Autowired
    private HealthAlertRepository healthAlertRepository;

    @Autowired
    private FeedingRecordRepository feedingRecordRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Transactional
    public HealthAlert createWeightAlertFromFeedingRecord(FeedingRecord record) {
        if (!record.isWeightAlertTriggered()) {
            return null;
        }

        Optional<HealthAlert> existing = healthAlertRepository.findByFeedingRecordId(record.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        HealthAlert alert = new HealthAlert();
        alert.setReptile(record.getReptile());
        alert.setFeedingRecord(record);
        alert.setAlertType("weight");
        alert.setTitle("Critical Health Alert: Insufficient Weight Gain (< 10g in 3 Feedings)");
        alert.setMessage(record.getWeightAlertMessage() != null ? record.getWeightAlertMessage() : WeightMonitoringService.ALERT_MESSAGE);
        alert.setSeverity("critical");
        alert.setStatus("active");
        alert.setSource("feeding_record");
        alert.setTriggeredAt(record.getFeedingDate() != null ? record.getFeedingDate() : LocalDateTime.now());

        HealthAlert saved = healthAlertRepository.save(alert);
        try {
            emailNotificationService.sendHealthAlertNotification(saved);
        } catch (Exception e) {
            // Logged inside service
        }
        return saved;
    }

    @Transactional
    public void syncWeightAlertsForReptile(Long reptileId) {
        List<FeedingRecord> records = feedingRecordRepository.findByReptileIdOrderByFeedingDateDescIdDesc(reptileId);
        for (FeedingRecord record : records) {
            if (record.isWeightAlertTriggered()) {
                createWeightAlertFromFeedingRecord(record);
            }
        }
    }

    @Transactional
    public HealthAlert resolveAlert(Long alertId, User admin, String notes) {
        HealthAlert alert = healthAlertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Health alert not found: " + alertId));
        alert.setStatus("resolved");
        alert.setResolvedBy(admin);
        alert.setResolvedAt(LocalDateTime.now());
        if (notes != null && !notes.isBlank()) {
            alert.setNotes(notes);
        }
        return healthAlertRepository.save(alert);
    }

    @Transactional
    public HealthAlert dismissAlert(Long alertId, User admin, String notes) {
        HealthAlert alert = healthAlertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Health alert not found: " + alertId));
        alert.setStatus("dismissed");
        alert.setResolvedBy(admin);
        alert.setResolvedAt(LocalDateTime.now());
        if (notes != null && !notes.isBlank()) {
            alert.setNotes(notes);
        }
        return healthAlertRepository.save(alert);
    }

    @Transactional
    public HealthAlert updateEvidence(Long alertId, String evidencePath, String notes) {
        HealthAlert alert = healthAlertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Health alert not found: " + alertId));
        if (evidencePath != null) {
            alert.setEvidencePath(evidencePath);
        }
        if (notes != null) {
            alert.setNotes(notes);
        }
        return healthAlertRepository.save(alert);
    }
}
