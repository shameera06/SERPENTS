package com.shareptiles.app.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "disease_predictions")
public class DiseasePrediction {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reptile_id")
    private Reptile reptile;

    @Column(name = "species_hint")
    private String speciesHint;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "heatmap_path")
    private String heatmapPath;

    @Column(name = "predicted_disease", nullable = false)
    private String predictedDisease;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal confidence = BigDecimal.ZERO;

    @Column(name = "is_respiratory_infection", nullable = false)
    private boolean isRespiratoryInfection = false;

    @Column(nullable = false)
    private String severity = "Moderate"; // Low | Moderate | High | Critical

    @Column(name = "probabilities_json", columnDefinition = "TEXT")
    private String probabilitiesJson;

    @Column(name = "symptoms_json", columnDefinition = "TEXT")
    private String symptomsJson;

    @Column(name = "treatment_recommendations", columnDefinition = "TEXT")
    private String treatmentRecommendations;

    @Column(name = "user_notes", columnDefinition = "TEXT")
    private String userNotes;

    @Column(nullable = false)
    private String status = "pending_review"; // pending_review | reviewed | converted_to_medical

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public DiseasePrediction() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods to deserialize JSON maps and lists
    public Map<String, Object> getProbabilities() {
        if (probabilitiesJson == null || probabilitiesJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            return MAPPER.readValue(probabilitiesJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public void setProbabilities(Map<String, ?> map) {
        try {
            this.probabilitiesJson = MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            this.probabilitiesJson = "{}";
        }
    }

    public List<String> getSymptomsDetected() {
        if (symptomsJson == null || symptomsJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(symptomsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void setSymptomsDetected(List<String> list) {
        try {
            this.symptomsJson = MAPPER.writeValueAsString(list);
        } catch (Exception e) {
            this.symptomsJson = "[]";
        }
    }

    public String getExactMedicalDiagnosis() {
        if (predictedDisease == null) return "Reptilian Pathology Under Evaluation";
        String p = predictedDisease.toLowerCase();
        if (p.contains("respiratory") || p.contains("ri")) return "Bacterial Bronchopneumonia & Upper/Lower Respiratory Tract Infection (Dyspneic Stage)";
        if (p.contains("incomplete") || p.contains("shed") || p.contains("dysecdysis")) return "Severe Cutaneous Dysecdysis & Pathological Retained Ocular Spectacles";
        if (p.contains("loose") || p.contains("enteritis") || p.contains("motion")) return "Infectious Colitis, Enteritis & Severe Gastrointestinal Dysbiosis";
        if (p.contains("mouth") || p.contains("stomatitis")) return "Infectious Ulcerative Stomatitis & Periodontal Caseous Necrosis (Mouth Rot)";
        if (p.contains("scale") || p.contains("rot") || p.contains("dermatitis")) return "Bacterial Necrotizing Ulcerative Dermatitis & Vesicular Ventral Scale Rot";
        if (p.contains("mite") || p.contains("ophionyssus")) return "Acute Ophionyssus natricis Acariasis & Hematophagous Ectoparasitic Infestation";
        return "Epithelial Intactness & Normal Physiological Equilibrium (Healthy Condition)";
    }

    public String getDetailedPathology() {
        if (predictedDisease == null) return "Comprehensive morphological and neural activation analysis performed.";
        String p = predictedDisease.toLowerCase();
        if (p.contains("respiratory") || p.contains("ri")) {
            return "Gram-negative bacterial colonization (predominantly Pseudomonas fluorescens, Aeromonas hydrophila, or Klebsiella pneumoniae) proliferating within the tracheal mucosa and faveolar pulmonary chambers. Visual features detect glistening oral mucoid secretions, respiratory labial bubbling, and open-mouth postural elevation.";
        }
        if (p.contains("incomplete") || p.contains("shed") || p.contains("dysecdysis")) {
            return "Pathological failure of the stratum corneum to detach cleanly during the ecdysis cycle, caused by chronic ambient humidity deficits (<50%) or systemic dehydration. Keratinized scales remain adhered across the crown and tail tip; retained cloudy spectacles present an immediate risk of permanent corneal ulceration.";
        }
        if (p.contains("loose") || p.contains("enteritis") || p.contains("motion")) {
            return "Acute intestinal mucosal compromise and loss of solid fecal architecture triggered by protozoal flagellates, Cryptosporidium serpentis, or pathogenic bacteria. Visual cues show unformed watery excretion with bilious discolored exudate, indicating acute metabolic dehydration.";
        }
        if (p.contains("mouth") || p.contains("stomatitis")) {
            return "Necrotizing bacterial invasion along the labial margin and gingival sulcus. Photographic features highlight caseous yellow curd-like necrotic plaques, severe gingival hyperemia, mucosal swelling, and inability to fully close oral margins.";
        }
        if (p.contains("scale") || p.contains("rot") || p.contains("dermatitis")) {
            return "Bacterial necrotizing infection of the ventral scutes (vesicular dermatitis) caused by prolonged exposure to damp, contaminated, or unsanitary substrate. The photo shows subscale fluid-filled blister formation, brown necrotic scale discoloration, and ulcerated dermal exposure.";
        }
        if (p.contains("mite") || p.contains("ophionyssus")) {
            return "Hematophagous ectoparasitic infestation by Ophionyssus natricis mites. Visual indicators reveal micro-speckling clustered beneath the chin (gular fold) and ocular spectacles, leading to scale lifting, acute pruritus, and blood-loss anemia.";
        }
        return "Zero clinical pathology detected. Keratinized scale layers display normal lipid sheen, oral mucosa is dry and sealed, eyes and heat pits are clear, and muscle tone is robust.";
    }

    public String getEnvironmentalRx() {
        if (predictedDisease == null) return "Maintain baseline temperature and humidity parameters.";
        String p = predictedDisease.toLowerCase();
        if (p.contains("respiratory") || p.contains("ri")) {
            return "Warm Basking Zone: 88°F - 90°F (31°C - 32°C) | Ambient Warm: 85°F (29°C) | Cool Zone: 78°F (26°C) | Target Humidity: 50% - 55% | Action: Completely eliminate drafts and dusty substrates; line with paper towels.";
        }
        if (p.contains("incomplete") || p.contains("shed") || p.contains("dysecdysis")) {
            return "Ambient Temperature: 84°F - 86°F (29°C - 30°C) | Target Relative Humidity: 70% - 80% | Action: Install a dedicated humidity hide filled with damp Chilean sphagnum moss immediately.";
        }
        if (p.contains("loose") || p.contains("enteritis") || p.contains("motion")) {
            return "Hospital Tub Temp: 84°F - 86°F (29°C - 30°C) | Humidity: 50% | Action: House strictly on sterile white paper towels; sanitize and replace enclosure daily.";
        }
        if (p.contains("mouth") || p.contains("stomatitis")) {
            return "Basking Zone: 88°F - 90°F (31°C - 32°C) | Ambient: 84°F | Target Humidity: 50% - 60% | Action: Remove all rough furnishings; maintain sterile paper towel bedding.";
        }
        if (p.contains("scale") || p.contains("rot") || p.contains("dermatitis")) {
            return "Hospital Enclosure Temp: 84°F - 86°F (29°C - 30°C) | Target Humidity: 40% - 45% (Dry conditions essential) | Action: Strictly remove damp substrate; use dry sterile paper towels.";
        }
        if (p.contains("mite") || p.contains("ophionyssus")) {
            return "Quarantine Room Isolation | Ambient Temp: 80°F - 82°F | Action: Paper towel bedding only; isolate water bowl to prevent drowning spillover.";
        }
        return "Standard Species Husbandry: Kingsnakes 75°-85°F / 40-50% RH | Corn Snakes 75°-85°F / 40-60% RH | Ball Pythons 78°-88°F / 60-70% RH.";
    }

    public List<String> getFirstAidChecklist() {
        List<String> list = new ArrayList<>();
        if (predictedDisease == null) return list;
        String p = predictedDisease.toLowerCase();
        if (p.contains("respiratory") || p.contains("ri")) {
            list.add("1. Immediately isolate specimen in a dedicated, warm hospital vivarium.");
            list.add("2. Elevate ambient enclosure temperature to 85°F-88°F to boost immune metabolism.");
            list.add("3. Remove all loose/damp substrate; line with clean, dry paper towels.");
            list.add("4. Prepare for veterinary antibiotic nebulization (F10SC 1:250 or injectable Ceftazidime).");
        } else if (p.contains("incomplete") || p.contains("shed") || p.contains("dysecdysis")) {
            list.add("1. NEVER peel dry stuck skin or eye caps with force to avoid tissue damage.");
            list.add("2. Provide a 25-minute lukewarm bath (82°F-84°F) with a textured wet washcloth.");
            list.add("3. Prepare a humid hide box with fresh, damp sphagnum moss.");
            list.add("4. Apply sterile ophthalmic lubricant drops if eye cap remains adhered after 2 soaks.");
        } else if (p.contains("loose") || p.contains("enteritis") || p.contains("motion")) {
            list.add("1. Collect fresh fecal sample in sterile container and refrigerate for vet fecal float/smear.");
            list.add("2. Temporarily withhold prey feeding to allow GI tract mucosal resting.");
            list.add("3. Administer lukewarm electrolyte soak (50% unflavored Pedialyte, 50% water).");
            list.add("4. Sterilize enclosure and hides with veterinary F10SC disinfectant.");
        } else if (p.contains("mouth") || p.contains("stomatitis")) {
            list.add("1. Inspect oral cavity gently using a soft silicone probe; do not force teeth.");
            list.add("2. Swab oral mucosal margins with dilute 0.05% Chlorhexidine gluconate solution.");
            list.add("3. Gently debride loose caseous necrotic pus plaques under veterinary guidance.");
            list.add("4. Administer veterinary systemic antibiotics and anti-inflammatories as prescribed.");
        } else if (p.contains("scale") || p.contains("rot") || p.contains("dermatitis")) {
            list.add("1. Relocate to a strictly dry hospital enclosure lined with fresh paper towels.");
            list.add("2. Administer daily 15-minute lukewarm diluted Betadine soak (tea-colored dilution).");
            list.add("3. Pat ventral belly scutes completely dry with sterile surgical gauze.");
            list.add("4. Apply topical Silver Sulfadiazine (Silvadene 1%) cream to ulcerated lesions.");
        } else if (p.contains("mite") || p.contains("ophionyssus")) {
            list.add("1. Place in separate quarantine room far from other collection reptiles.");
            list.add("2. Soak snake in warm water with 1 drop of gentle soap for 30 minutes to drown mites.");
            list.add("3. Strip enclosure and treat with Permethrin premise spray (dry 24h before re-entry).");
            list.add("4. Repeat soak cycles every 7-10 days to disrupt new hatching egg cycles.");
        } else {
            list.add("1. Maintain standard scheduled feeding intervals and clean water bowl.");
            list.add("2. Continue recording routine weight, shed, and defecation entries.");
        }
        return list;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public Reptile getReptile() { return reptile; }
    public void setReptile(Reptile reptile) { this.reptile = reptile; }
    public String getSpeciesHint() { return speciesHint; }
    public void setSpeciesHint(String speciesHint) { this.speciesHint = speciesHint; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getHeatmapPath() { return heatmapPath; }
    public void setHeatmapPath(String heatmapPath) { this.heatmapPath = heatmapPath; }
    public String getPredictedDisease() { return predictedDisease; }
    public void setPredictedDisease(String predictedDisease) { this.predictedDisease = predictedDisease; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public boolean isRespiratoryInfection() { return isRespiratoryInfection; }
    public void setRespiratoryInfection(boolean respiratoryInfection) { isRespiratoryInfection = respiratoryInfection; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getProbabilitiesJson() { return probabilitiesJson; }
    public void setProbabilitiesJson(String probabilitiesJson) { this.probabilitiesJson = probabilitiesJson; }
    public String getSymptomsJson() { return symptomsJson; }
    public void setSymptomsJson(String symptomsJson) { this.symptomsJson = symptomsJson; }
    public String getTreatmentRecommendations() { return treatmentRecommendations; }
    public void setTreatmentRecommendations(String treatmentRecommendations) { this.treatmentRecommendations = treatmentRecommendations; }
    public String getUserNotes() { return userNotes; }
    public void setUserNotes(String userNotes) { this.userNotes = userNotes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public MedicalRecord getMedicalRecord() { return medicalRecord; }
    public void setMedicalRecord(MedicalRecord medicalRecord) { this.medicalRecord = medicalRecord; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
