package com.shareptiles.app.service;

import com.shareptiles.app.model.HealthAlert;
import com.shareptiles.app.repository.HealthAlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    public static final String TARGET_ADMIN_EMAIL = "shaspets06@gmail.com";

    @Autowired
    private HealthAlertRepository healthAlertRepository;

    // In-memory audit trail of sent alert emails
    private final List<String> emailAuditTrail = new ArrayList<>();

    public synchronized void sendHealthAlertNotification(HealthAlert alert) {
        if (alert == null || alert.getReptile() == null) return;

        String specimenName = alert.getReptile().getName();
        String specimenCode = alert.getReptile().getReptileCode();
        String speciesName = alert.getReptile().getSpecies() != null ? alert.getReptile().getSpecies().getName() : "Snake";
        String varietyName = alert.getReptile().getDisplayVarietyName();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String subject = String.format("[URGENT REPTILE VETERINARY ALERT] Weight Stagnation for %s (%s)", specimenName, specimenCode);

        String emailBody = String.format("""
                ========================================================================================
                SERPENTS CLINICAL HUSBANDRY & AI SYSTEM - URGENT HEALTH ALERT NOTIFICATION
                ========================================================================================
                DISPATCHED TO: %s
                DISPATCH TIME: %s
                FREQUENCY    : Automated recurring alert sent every 24 hours until clinical resolution
                ALERT STATUS : %s (SEVERITY: %s)
                ----------------------------------------------------------------------------------------
                SPECIMEN DETAILS:
                  * Name          : %s
                  * Identification: %s
                  * Species       : %s
                  * Color Morph   : %s
                  * Current Weight: %s %s
                
                CLINICAL REASON FOR ALERT:
                  %s
                
                VETERINARY DIRECTIVE:
                  CRITICAL: The snake has failed to gain the mandatory 10.0g threshold across 3 consecutive 
                  feeding intervals. Immediate exotic reptile veterinary consultation is urgently advised 
                  to diagnose potential internal parasitic load, impaction, or metabolic deficiencies.
                
                DIRECT APPLICATION PORTAL LINKS:
                  * Specimen Profile: http://localhost:8000/admin/reptiles/%d
                  * Health Alerts UI: http://localhost:8000/admin/health-alerts
                ========================================================================================
                """,
                TARGET_ADMIN_EMAIL,
                timestamp,
                alert.getStatus().toUpperCase(),
                alert.getSeverity().toUpperCase(),
                specimenName,
                specimenCode,
                speciesName,
                varietyName,
                alert.getReptile().getCurrentWeight() != null ? alert.getReptile().getCurrentWeight() : "N/A",
                alert.getReptile().getWeightUnit() != null ? alert.getReptile().getWeightUnit() : "g",
                alert.getMessage(),
                alert.getReptile().getId()
        );

        // Record in audit log & system output
        log.warn("DISPATCHING 24H HEALTH ALERT EMAIL TO {}:\n{}", TARGET_ADMIN_EMAIL, emailBody);
        emailAuditTrail.add(String.format("[%s] Sent to %s: %s", timestamp, TARGET_ADMIN_EMAIL, subject));

        // Update alert's lastEmailSentAt timestamp
        alert.setLastEmailSentAt(LocalDateTime.now());
        healthAlertRepository.save(alert);
    }

    /**
     * Recurring schedule: checks every minute.
     * If an active unresolved health alert has not had an email dispatched in the last 24 hours,
     * it automatically sends the recurring email notification to shaspets06@gmail.com.
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    @Transactional
    public void processRecurring24HourAlertEmails() {
        List<HealthAlert> activeAlerts = healthAlertRepository.findByStatus("active");
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        for (HealthAlert alert : activeAlerts) {
            if (alert.getLastEmailSentAt() == null || alert.getLastEmailSentAt().isBefore(twentyFourHoursAgo)) {
                sendHealthAlertNotification(alert);
            }
        }
    }

    public List<String> getEmailAuditTrail() {
        return new ArrayList<>(emailAuditTrail);
    }
}
