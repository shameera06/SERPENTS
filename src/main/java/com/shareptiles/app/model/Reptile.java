package com.shareptiles.app.model;

import com.shareptiles.app.util.SnakeImageCatalog;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reptiles")
public class Reptile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "species_id", nullable = false)
    private Species species;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "species_variety_id")
    private SpeciesVariety speciesVariety;

    @Column(nullable = false)
    private String name;

    @Column(name = "reptile_code", unique = true)
    private String reptileCode;

    @Column(nullable = false)
    private String sex = "unknown"; // male | female | unknown

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    @Column(name = "acquisition_source")
    private String acquisitionSource;

    @Column(name = "current_weight", precision = 8, scale = 2)
    private BigDecimal currentWeight;

    @Column(name = "weight_unit")
    private String weightUnit = "g";

    @Column(name = "length", precision = 8, scale = 2)
    private BigDecimal length;

    @Column(name = "length_unit")
    private String lengthUnit = "cm";

    @Column(nullable = false)
    private String status = "active"; // active | inactive | transferred | sold | deceased

    private String photo;

    @Column(name = "parivesh_certificate_path")
    private String pariveshCertificatePath;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Reptile() {}

    public Reptile(String name, Species species, SpeciesVariety variety, BigDecimal currentWeight, String sex) {
        this.name = name;
        this.species = species;
        this.speciesVariety = variety;
        this.currentWeight = currentWeight;
        this.sex = sex;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getDisplayVarietyName() {
        if (speciesVariety != null) {
            return speciesVariety.getName();
        }
        return "Standard";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Species getSpecies() { return species; }
    public void setSpecies(Species species) { this.species = species; }
    public SpeciesVariety getSpeciesVariety() { return speciesVariety; }
    public void setSpeciesVariety(SpeciesVariety speciesVariety) { this.speciesVariety = speciesVariety; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getReptileCode() { return reptileCode; }
    public void setReptileCode(String reptileCode) { this.reptileCode = reptileCode; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public LocalDate getAcquisitionDate() { return acquisitionDate; }
    public void setAcquisitionDate(LocalDate acquisitionDate) { this.acquisitionDate = acquisitionDate; }
    public String getAcquisitionSource() { return acquisitionSource; }
    public void setAcquisitionSource(String acquisitionSource) { this.acquisitionSource = acquisitionSource; }
    public BigDecimal getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(BigDecimal currentWeight) { this.currentWeight = currentWeight; }
    public String getWeightUnit() { return weightUnit; }
    public void setWeightUnit(String weightUnit) { this.weightUnit = weightUnit; }
    public BigDecimal getLength() { return length; }
    public void setLength(BigDecimal length) { this.length = length; }
    public String getLengthUnit() { return lengthUnit; }
    public void setLengthUnit(String lengthUnit) { this.lengthUnit = lengthUnit; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPhoto() {
        if (photo != null && !photo.isBlank()
                && !SnakeImageCatalog.isCatalogImage(photo)
                && !SnakeImageCatalog.isGeneratedMorphImage(photo)) return photo;
        if (speciesVariety != null && speciesVariety.getPhoto() != null) return speciesVariety.getPhoto();
        if (species != null) return species.getPhoto();
        return SnakeImageCatalog.IMAGE_NOT_AVAILABLE;
    }
    public String storedPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public String getPariveshCertificatePath() { return pariveshCertificatePath; }
    public void setPariveshCertificatePath(String pariveshCertificatePath) { this.pariveshCertificatePath = pariveshCertificatePath; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
