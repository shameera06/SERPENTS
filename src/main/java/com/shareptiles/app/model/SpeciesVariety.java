package com.shareptiles.app.model;

import com.shareptiles.app.util.SnakeImageCatalog;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "species_varieties", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"species_id", "name"})
})
public class SpeciesVariety {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "species_id", nullable = false)
    private Species species;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean status = true;

    @Column(name = "photo")
    private String photo;

    public String getPhoto() {
        if (hasCustomPhoto()) return photo;
        if (species != null) {
            String mappedPhoto = SnakeImageCatalog.findMorphImage(species.getName(), name).orElse(null);
            if (mappedPhoto != null) return mappedPhoto;
        }
        if (species != null) return species.getPhoto();
        return SnakeImageCatalog.IMAGE_NOT_AVAILABLE;
    }

    public String storedPhoto() { return photo; }

    private boolean hasCustomPhoto() {
        return photo != null && !photo.isBlank()
                && !SnakeImageCatalog.isCatalogImage(photo)
                && !SnakeImageCatalog.isGeneratedMorphImage(photo);
    }

    public void setPhoto(String photo) { this.photo = photo; }

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public SpeciesVariety() {}

    public SpeciesVariety(Species species, String name, boolean status) {
        this.species = species;
        this.name = name;
        this.status = status;
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

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Species getSpecies() { return species; }
    public void setSpecies(Species species) { this.species = species; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
