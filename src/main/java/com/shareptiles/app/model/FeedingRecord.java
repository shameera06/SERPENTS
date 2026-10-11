package com.shareptiles.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "feeding_records")
public class FeedingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reptile_id", nullable = false)
    private Reptile reptile;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "feeding_schedule_id")
    private FeedingSchedule feedingSchedule;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "food_type_id", nullable = false)
    private FoodType foodType;

    @Column(name = "feeding_date", nullable = false)
    private LocalDateTime feedingDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "quantity_unit", nullable = false)
    private String quantityUnit = "piece";

    @Column(name = "weight_before", precision = 10, scale = 2)
    private BigDecimal weightBefore;

    @Column(name = "weight_after", precision = 10, scale = 2)
    private BigDecimal weightAfter;

    @Column(name = "weight_after_defecation", precision = 10, scale = 2)
    private BigDecimal weightAfterDefecation;

    @Column(name = "weight_change", precision = 10, scale = 2)
    private BigDecimal weightChange;

    @Column(name = "weight_trend")
    private String weightTrend; // gain | no_gain | loss

    @Column(name = "weight_alert_triggered", nullable = false)
    private boolean weightAlertTriggered = false;

    @Column(name = "weight_alert_message", columnDefinition = "TEXT")
    private String weightAlertMessage;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public FeedingRecord() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public BigDecimal getEffectiveWeight() {
        if (weightAfterDefecation != null) {
            return weightAfterDefecation;
        }
        if (weightAfter != null) {
            return weightAfter;
        }
        return weightBefore;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Reptile getReptile() { return reptile; }
    public void setReptile(Reptile reptile) { this.reptile = reptile; }
    public FeedingSchedule getFeedingSchedule() { return feedingSchedule; }
    public void setFeedingSchedule(FeedingSchedule feedingSchedule) { this.feedingSchedule = feedingSchedule; }
    public FoodType getFoodType() { return foodType; }
    public void setFoodType(FoodType foodType) { this.foodType = foodType; }
    public LocalDateTime getFeedingDate() { return feedingDate; }
    public void setFeedingDate(LocalDateTime feedingDate) { this.feedingDate = feedingDate; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getQuantityUnit() { return quantityUnit; }
    public void setQuantityUnit(String quantityUnit) { this.quantityUnit = quantityUnit; }
    public BigDecimal getWeightBefore() { return weightBefore; }
    public void setWeightBefore(BigDecimal weightBefore) { this.weightBefore = weightBefore; }
    public BigDecimal getWeightAfter() { return weightAfter; }
    public void setWeightAfter(BigDecimal weightAfter) { this.weightAfter = weightAfter; }
    public BigDecimal getWeightAfterDefecation() { return weightAfterDefecation; }
    public void setWeightAfterDefecation(BigDecimal weightAfterDefecation) { this.weightAfterDefecation = weightAfterDefecation; }
    public BigDecimal getWeightChange() { return weightChange; }
    public void setWeightChange(BigDecimal weightChange) { this.weightChange = weightChange; }
    public String getWeightTrend() { return weightTrend; }
    public void setWeightTrend(String weightTrend) { this.weightTrend = weightTrend; }
    public boolean isWeightAlertTriggered() { return weightAlertTriggered; }
    public void setWeightAlertTriggered(boolean weightAlertTriggered) { this.weightAlertTriggered = weightAlertTriggered; }
    public String getWeightAlertMessage() { return weightAlertMessage; }
    public void setWeightAlertMessage(String weightAlertMessage) { this.weightAlertMessage = weightAlertMessage; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
