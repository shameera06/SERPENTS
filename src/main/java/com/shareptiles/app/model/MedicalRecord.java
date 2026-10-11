package com.shareptiles.app.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_records")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reptile_id", nullable = false)
    private Reptile reptile;

    @Column(name = "treatment_date", nullable = false)
    private LocalDate treatmentDate;

    @Column(name = "symptoms_or_condition", nullable = false, columnDefinition = "TEXT")
    private String symptomsOrCondition;

    @Column(name = "medicine_name")
    private String medicineName;

    private String dosage;

    @Column(name = "treatment_details", columnDefinition = "TEXT")
    private String treatmentDetails;

    @Column(name = "veterinarian_name")
    private String veterinarianName;

    @Column(name = "veterinarian_contact")
    private String veterinarianContact;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "prescription_path")
    private String prescriptionPath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public MedicalRecord() {}

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
    public LocalDate getTreatmentDate() { return treatmentDate; }
    public void setTreatmentDate(LocalDate treatmentDate) { this.treatmentDate = treatmentDate; }
    public String getSymptomsOrCondition() { return symptomsOrCondition; }
    public void setSymptomsOrCondition(String symptomsOrCondition) { this.symptomsOrCondition = symptomsOrCondition; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getTreatmentDetails() { return treatmentDetails; }
    public void setTreatmentDetails(String treatmentDetails) { this.treatmentDetails = treatmentDetails; }
    public String getVeterinarianName() { return veterinarianName; }
    public void setVeterinarianName(String veterinarianName) { this.veterinarianName = veterinarianName; }
    public String getVeterinarianContact() { return veterinarianContact; }
    public void setVeterinarianContact(String veterinarianContact) { this.veterinarianContact = veterinarianContact; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getPrescriptionPath() { return prescriptionPath; }
    public void setPrescriptionPath(String prescriptionPath) { this.prescriptionPath = prescriptionPath; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
