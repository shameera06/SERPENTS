package com.shareptiles.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "poop_records")
public class PoopRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reptile_id", nullable = false)
    private Reptile reptile;

    @Column(name = "poop_date", nullable = false)
    private LocalDateTime poopDate;

    @Column(length = 30, nullable = false)
    private String consistency = "normal"; // normal, watery, hard, mucus

    @Column(name = "urates_status", length = 30, nullable = false)
    private String uratesStatus = "normal_white"; // normal_white, chalky, yellowish, absent

    @Column(name = "weight_after_poop", precision = 8, scale = 2)
    private BigDecimal weightAfterPoop;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PoopRecord() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
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
    public LocalDateTime getPoopDate() { return poopDate; }
    public void setPoopDate(LocalDateTime poopDate) { this.poopDate = poopDate; }
    public String getConsistency() { return consistency; }
    public void setConsistency(String consistency) { this.consistency = consistency; }
    public String getUratesStatus() { return uratesStatus; }
    public void setUratesStatus(String uratesStatus) { this.uratesStatus = uratesStatus; }
    public BigDecimal getWeightAfterPoop() { return weightAfterPoop; }
    public void setWeightAfterPoop(BigDecimal weightAfterPoop) { this.weightAfterPoop = weightAfterPoop; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
