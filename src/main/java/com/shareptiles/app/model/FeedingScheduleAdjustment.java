package com.shareptiles.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feeding_schedule_adjustments")
public class FeedingScheduleAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "feeding_schedule_id", nullable = false)
    private FeedingSchedule feedingSchedule;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reptile_id", nullable = false)
    private Reptile reptile;

    @Column(name = "original_next_due_at", nullable = false)
    private LocalDateTime originalNextDueAt;

    @Column(name = "adjusted_next_due_at", nullable = false)
    private LocalDateTime adjustedNextDueAt;

    @Column(nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "adjusted_by")
    private User adjustedBy;

    @Column(name = "adjusted_at", nullable = false)
    private LocalDateTime adjustedAt = LocalDateTime.now();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public FeedingScheduleAdjustment() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (adjustedAt == null) adjustedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public FeedingSchedule getFeedingSchedule() { return feedingSchedule; }
    public void setFeedingSchedule(FeedingSchedule feedingSchedule) { this.feedingSchedule = feedingSchedule; }
    public Reptile getReptile() { return reptile; }
    public void setReptile(Reptile reptile) { this.reptile = reptile; }
    public LocalDateTime getOriginalNextDueAt() { return originalNextDueAt; }
    public void setOriginalNextDueAt(LocalDateTime originalNextDueAt) { this.originalNextDueAt = originalNextDueAt; }
    public LocalDateTime getAdjustedNextDueAt() { return adjustedNextDueAt; }
    public void setAdjustedNextDueAt(LocalDateTime adjustedNextDueAt) { this.adjustedNextDueAt = adjustedNextDueAt; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public User getAdjustedBy() { return adjustedBy; }
    public void setAdjustedBy(User adjustedBy) { this.adjustedBy = adjustedBy; }
    public LocalDateTime getAdjustedAt() { return adjustedAt; }
    public void setAdjustedAt(LocalDateTime adjustedAt) { this.adjustedAt = adjustedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
