package com.shareptiles.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_alerts")
public class HealthAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reptile_id", nullable = false)
    private Reptile reptile;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "feeding_record_id", unique = true)
    private FeedingRecord feedingRecord;

    @Column(name = "alert_type", nullable = false)
    private String alertType = "weight";

    @Column(nullable = false)
    private String title = "Health Alert: Weight Concern";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String severity = "warning"; // warning | critical

    @Column(nullable = false)
    private String status = "active"; // active | resolved | dismissed

    @Column(nullable = false)
    private String source = "feeding_record"; // feeding_record | manual

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt = LocalDateTime.now();

    @Column(name = "last_email_sent_at")
    private LocalDateTime lastEmailSentAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "evidence_path")
    private String evidencePath;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public HealthAlert() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (triggeredAt == null) triggeredAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Reptile getReptile() { return reptile; }
    public void setReptile(Reptile reptile) { this.reptile = reptile; }
    public FeedingRecord getFeedingRecord() { return feedingRecord; }
    public void setFeedingRecord(FeedingRecord feedingRecord) { this.feedingRecord = feedingRecord; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }
    public LocalDateTime getLastEmailSentAt() { return lastEmailSentAt; }
    public void setLastEmailSentAt(LocalDateTime lastEmailSentAt) { this.lastEmailSentAt = lastEmailSentAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public User getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(User resolvedBy) { this.resolvedBy = resolvedBy; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getEvidencePath() { return evidencePath; }
    public void setEvidencePath(String evidencePath) { this.evidencePath = evidencePath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
