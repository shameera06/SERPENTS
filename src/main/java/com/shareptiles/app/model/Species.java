package com.shareptiles.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shareptiles.app.util.SnakeImageCatalog;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "species")
public class Species {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private SpeciesCategory category;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "scientific_name")
    private String scientificName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean status = true;

    @Column(name = "photo")
    private String photo;

    public String getPhoto() {
        if (hasCustomPhoto()) return photo;
        return SnakeImageCatalog.findSpeciesImage(name).orElse(SnakeImageCatalog.IMAGE_NOT_AVAILABLE);
    }

    public String storedPhoto() { return photo; }

    private boolean hasCustomPhoto() {
        return photo != null && !photo.isBlank() && !SnakeImageCatalog.isCatalogImage(photo);
    }

    public void setPhoto(String photo) { this.photo = photo; }

    @JsonIgnore
    @OneToMany(mappedBy = "species", cascade = CascadeType.ALL)
    private List<SpeciesVariety> varieties = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Species() {}

    public Species(String name, String scientificName, SpeciesCategory category, boolean status) {
        this.name = name;
        this.scientificName = scientificName;
        this.category = category;
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
    public SpeciesCategory getCategory() { return category; }
    public void setCategory(SpeciesCategory category) { this.category = category; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    public List<SpeciesVariety> getVarieties() { return varieties; }
    public void setVarieties(List<SpeciesVariety> varieties) { this.varieties = varieties; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
