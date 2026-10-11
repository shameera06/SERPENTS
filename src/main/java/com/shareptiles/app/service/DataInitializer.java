package com.shareptiles.app.service;

import com.shareptiles.app.model.*;
import com.shareptiles.app.repository.*;
import com.shareptiles.app.util.SnakeImageCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpeciesCategoryRepository categoryRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired
    private SpeciesVarietyRepository varietyRepository;

    @Autowired
    private FoodTypeRepository foodTypeRepository;

    @Autowired
    private ReptileRepository reptileRepository;

    @Autowired
    private FeedingScheduleRepository feedingScheduleRepository;

    @Autowired
    private FeedingRecordRepository feedingRecordRepository;

    @Autowired
    private SheddingRecordRepository sheddingRecordRepository;

    @Autowired
    private PoopRecordRepository poopRecordRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private HealthAlertRepository healthAlertRepository;

    @Autowired
    private WeightMonitoringService weightMonitoringService;

    @Autowired
    private HealthAlertService healthAlertService;

    @Override
    @Transactional
    public void run(String... args) {
        initUsers();
        initSpeciesHierarchy();
        initFoodTypes();
        initReptilesAndRecords();
        ensureMinimumReptilesPerSpecies();
        syncSnakePhotosAndHistory();
    }

    private void initUsers() {
        User admin = userRepository.findByEmail("admin@example.com").orElse(null);
        if (admin == null) {
            userRepository.save(new User("System Admin", "admin@example.com", "ChangeMe123!", "admin"));
        } else {
            admin.setRole("admin");
            admin.setPassword("ChangeMe123!");
            userRepository.save(admin);
        }

        if (!userRepository.existsByEmail("shaspets06@gmail.com")) {
            userRepository.save(new User("Sha Pets Admin", "shaspets06@gmail.com", "ChangeMe123!", "admin"));
        }

        User staff = userRepository.findByEmail("staff@example.com").orElse(null);
        if (staff == null) {
            userRepository.save(new User("Exotic Care Staff", "staff@example.com", "ChangeMe123!", "staff"));
        } else {
            staff.setRole("staff");
            staff.setPassword("ChangeMe123!");
            userRepository.save(staff);
        }
    }

    private void initSpeciesHierarchy() {
        SpeciesCategory snakeCategory = categoryRepository.findByName("Snake")
                .orElseGet(() -> categoryRepository.save(new SpeciesCategory("Snake", true)));

        // 3 Species
        Species kingSnake = speciesRepository.findByName("King Snake")
                .orElseGet(() -> speciesRepository.save(
                        new Species("King Snake", "Lampropeltis getula", snakeCategory, true)));
        migrateVarieties(kingSnake, Map.ofEntries(
                Map.entry("Blacky", "California Kingsnake"),
                Map.entry("California", "Mexican Black Kingsnake"),
                Map.entry("Banded", "Florida Kingsnake"),
                Map.entry("Albino King", "Speckled Kingsnake"),
                Map.entry("High White", "Desert Kingsnake"),
                Map.entry("Desert Banded", "Eastern Kingsnake"),
                Map.entry("Lavender", "Black Kingsnake"),
                Map.entry("Aberrant", "Gray-banded Kingsnake"),
                Map.entry("Reverse Stripe", "Prairie Kingsnake"),
                Map.entry("Axanthic", "Scarlet Kingsnake")
        ));
        createSpeciesWithVarieties(snakeCategory, "King Snake", "Lampropeltis getula",
                List.of("California Kingsnake", "Mexican Black Kingsnake", "Florida Kingsnake",
                        "Speckled Kingsnake", "Desert Kingsnake", "Eastern Kingsnake", "Black Kingsnake",
                        "Gray-banded Kingsnake", "Prairie Kingsnake", "Scarlet Kingsnake"));

        Species cornSnake = speciesRepository.findByName("Corn Snake")
                .orElseGet(() -> speciesRepository.save(
                        new Species("Corn Snake", "Pantherophis guttatus", snakeCategory, true)));
        migrateVarieties(cornSnake, Map.ofEntries(
                Map.entry("Honey", "Amelanistic (Amel)"),
                Map.entry("Ghost Corn", "Albino"),
                Map.entry("Okeetee", "Butter"),
                Map.entry("Miami Phase", "Blood Red"),
                Map.entry("Anerythristic", "Anerythristic (Anery)")
        ));
        createSpeciesWithVarieties(snakeCategory, "Corn Snake", "Pantherophis guttatus",
                List.of("Tessera", "Snow", "Amelanistic (Amel)", "Anerythristic (Anery)", "Albino",
                        "Butter", "Lavender", "Palmetto", "Blood Red", "Motley"));

        Species pythonSnake = speciesRepository.findByName("Python Snake")
                .orElseGet(() -> speciesRepository.save(
                        new Species("Python Snake", "Python regius", snakeCategory, true)));
        migrateVarieties(pythonSnake, Map.ofEntries(
                Map.entry("Royal", "Blue-Eyed Leucistic (BEL)"),
                Map.entry("Pinstripe", "Red-Eyed Leucistic (REL)"),
                Map.entry("Banana Python", "Banana"),
                Map.entry("Pied", "Pied (Piebald)")
        ));
        createSpeciesWithVarieties(snakeCategory, "Python Snake", "Python regius",
                List.of("Spider", "Enchi", "Blue-Eyed Leucistic (BEL)", "Red-Eyed Leucistic (REL)",
                        "Banana", "Albino", "Pied (Piebald)", "Pastel", "Clown", "Mojave"));
    }

    private void createSpeciesWithVarieties(SpeciesCategory cat, String name, String scientific, List<String> varieties) {
        Species species = speciesRepository.findByName(name)
                .orElseGet(() -> speciesRepository.save(new Species(name, scientific, cat, true)));

        for (String vName : varieties) {
            varietyRepository.findBySpeciesIdAndName(species.getId(), vName)
                    .orElseGet(() -> varietyRepository.save(new SpeciesVariety(species, vName, true)));
        }
    }

    private void migrateVarieties(Species species, Map<String, String> renamedVarieties) {
        Map<String, SpeciesVariety> varietiesToRename = new LinkedHashMap<>();
        for (String oldName : renamedVarieties.keySet()) {
            varietyRepository.findBySpeciesIdAndName(species.getId(), oldName)
                    .ifPresent(variety -> varietiesToRename.put(oldName, variety));
        }

        for (SpeciesVariety variety : varietiesToRename.values()) {
            variety.setName("__variety_migration_" + variety.getId());
            varietyRepository.save(variety);
        }

        for (Map.Entry<String, String> rename : renamedVarieties.entrySet()) {
            SpeciesVariety variety = varietiesToRename.get(rename.getKey());
            if (variety == null) continue;

            String imagePath = SnakeImageCatalog.findMorphImage(species.getName(), rename.getValue())
                    .orElseThrow(() -> new IllegalStateException("No verified image mapping for "
                            + species.getName() + " variety " + rename.getValue()));
            SpeciesVariety existingTarget = varietyRepository
                    .findBySpeciesIdAndName(species.getId(), rename.getValue()).orElse(null);
            if (existingTarget == null) {
                variety.setName(rename.getValue());
                variety.setPhoto(imagePath);
                varietyRepository.save(variety);
                continue;
            }

            List<Reptile> reptilesToMove = reptileRepository.findBySpeciesVarietyId(variety.getId());
            for (Reptile reptile : reptilesToMove) {
                reptile.setSpeciesVariety(existingTarget);
            }
            reptileRepository.saveAll(reptilesToMove);
            existingTarget.setPhoto(imagePath);
            varietyRepository.save(existingTarget);
            varietyRepository.delete(variety);
        }
    }

    private void initFoodTypes() {
        createFoodCategoryWithChildren("Mouse", List.of("Pinky", "Fuzzy", "Adult"), "piece");
        createFoodCategoryWithChildren("Rat", List.of("Pup", "Small", "Medium", "Large"), "piece");
        createFoodCategoryWithChildren("Cricket", List.of(), "piece");
        createFoodCategoryWithChildren("Mealworm", List.of(), "gram");
        createFoodCategoryWithChildren("Dubia Roach", List.of(), "piece");
        createFoodCategoryWithChildren("Leafy Greens", List.of(), "gram");
        createFoodCategoryWithChildren("Other", List.of(), "piece");
    }

    private void createFoodCategoryWithChildren(String catName, List<String> children, String defaultUnit) {
        FoodType parent = foodTypeRepository.findByNameAndParentIsNull(catName)
                .orElseGet(() -> foodTypeRepository.save(new FoodType(catName, null, children.isEmpty() ? defaultUnit : null, true)));

        for (String childName : children) {
            foodTypeRepository.findByNameAndParentId(childName, parent.getId())
                    .orElseGet(() -> foodTypeRepository.save(new FoodType(childName, parent, defaultUnit, true)));
        }
    }

    private void initReptilesAndRecords() {
        if (reptileRepository.count() > 0) {
            return;
        }

        User admin = userRepository.findByEmail("admin@example.com").orElse(null);

        // Predefined 12 snakes matching project spec
        List<Object[]> snakeSpecs = List.of(
                new Object[]{"California Kingsnake", "King Snake", "California Kingsnake", 220.0, "male"},
                new Object[]{"Mexican Black Kingsnake", "King Snake", "Mexican Black Kingsnake", 280.0, "female"},
                new Object[]{"Florida Kingsnake", "King Snake", "Florida Kingsnake", 360.0, "male"},
                new Object[]{"Speckled Kingsnake", "King Snake", "Speckled Kingsnake", 300.0, "female"},

                new Object[]{"Tessera", "Corn Snake", "Tessera", 400.0, "female"},
                new Object[]{"Amelanistic", "Corn Snake", "Amelanistic (Amel)", 350.0, "male"},
                new Object[]{"Snow", "Corn Snake", "Snow", 500.0, "female"},
                new Object[]{"Anerythristic", "Corn Snake", "Anerythristic (Anery)", 320.0, "unknown"},

                new Object[]{"Spider", "Python Snake", "Spider", 420.0, "male"},
                new Object[]{"Blue-Eyed Leucistic", "Python Snake", "Blue-Eyed Leucistic (BEL)", 550.0, "female"},
                new Object[]{"Pastel", "Python Snake", "Pastel", 600.0, "female"},
                new Object[]{"Banana", "Python Snake", "Banana", 480.0, "male"}
        );

        FoodType fuzzyMouse = foodTypeRepository.findByNameAndParentIsNull("Mouse")
                .flatMap(p -> foodTypeRepository.findByNameAndParentId("Fuzzy", p.getId()))
                .orElseGet(() -> foodTypeRepository.findAll().get(0));

        int index = 1;
        for (Object[] spec : snakeSpecs) {
            String name = (String) spec[0];
            String speciesName = (String) spec[1];
            String varietyName = (String) spec[2];
            Double weight = (Double) spec[3];
            String sex = (String) spec[4];

            Species species = speciesRepository.findByName(speciesName).orElse(null);
            if (species == null) continue;

            SpeciesVariety variety = varietyRepository.findBySpeciesIdAndName(species.getId(), varietyName).orElse(null);

            Reptile reptile = new Reptile();
            reptile.setName(name);
            reptile.setSpecies(species);
            reptile.setSpeciesVariety(variety);
            reptile.setSex(sex);
            reptile.setCurrentWeight(BigDecimal.valueOf(weight));
            reptile.setWeightUnit("g");
            reptile.setLength(BigDecimal.valueOf(weight * 0.15 + 30));
            reptile.setLengthUnit("cm");
            reptile.setDateOfBirth(LocalDate.now().minusMonths(12 + index));
            reptile.setAcquisitionDate(LocalDate.now().minusMonths(6));
            reptile.setAcquisitionSource("Sha Captive Breeding");
            reptile.setStatus("active");
            reptile.setReptileCode(String.format("REP-%06d", index));
            reptile = reptileRepository.save(reptile);

            // Create feeding schedule
            FeedingSchedule schedule = new FeedingSchedule();
            schedule.setReptile(reptile);
            schedule.setFoodType(fuzzyMouse);
            schedule.setQuantity(BigDecimal.valueOf(1.00));
            schedule.setQuantityUnit("piece");
            schedule.setIntervalDays(7);
            schedule.setStartDate(LocalDate.now().minusMonths(3));
            schedule.setFirstDueAt(LocalDateTime.now().minusDays(14));
            schedule.setNextDueAt(LocalDateTime.now().plusDays((index % 5) + 1));
            schedule.setStatus("active");
            schedule = feedingScheduleRepository.save(schedule);

            // Create feeding records (historical)
            for (int r = 3; r >= 1; r--) {
                FeedingRecord record = new FeedingRecord();
                record.setReptile(reptile);
                record.setFeedingSchedule(schedule);
                record.setFoodType(fuzzyMouse);
                record.setFeedingDate(LocalDateTime.now().minusDays(r * 7));
                record.setQuantity(BigDecimal.valueOf(1.00));
                record.setQuantityUnit("piece");

                double baseW = weight - (r * 8.0);
                record.setWeightBefore(BigDecimal.valueOf(baseW));
                record.setWeightAfter(BigDecimal.valueOf(baseW + 12.0));
                record.setWeightAfterDefecation(BigDecimal.valueOf(baseW + 10.0));
                record.setCreatedBy(admin);
                record = feedingRecordRepository.save(record);
            }

            weightMonitoringService.recalculateReptileWeightHistory(reptile.getId());
            healthAlertService.syncWeightAlertsForReptile(reptile.getId());

            // Add shedding record
            SheddingRecord shedding = new SheddingRecord();
            shedding.setReptile(reptile);
            shedding.setSheddingDate(LocalDate.now().minusDays(15));
            shedding.setStatus("complete");
            shedding.setObservations("Full, clean, one-piece shed including eyecaps and tail tip.");
            shedding.setCreatedBy(admin);
            sheddingRecordRepository.save(shedding);

            // Add poop record
            PoopRecord poop = new PoopRecord();
            poop.setReptile(reptile);
            poop.setPoopDate(LocalDateTime.now().minusDays(4));
            poop.setConsistency("normal");
            poop.setUratesStatus("normal_white");
            poop.setWeightAfterPoop(BigDecimal.valueOf(weight));
            poop.setObservations("Well-formed dark fecal pellet with firm white urates.");
            poop.setCreatedBy(admin);
            poopRecordRepository.save(poop);

            // For the first snake, add a medical record and sample health alert
            if (index == 1) {
                MedicalRecord medical = new MedicalRecord();
                medical.setReptile(reptile);
                medical.setTreatmentDate(LocalDate.now().minusMonths(1));
                medical.setSymptomsOrCondition("Routine Annual Exotic Wellness Exam");
                medical.setMedicineName("Reptile Electrolytes & Calcium Powder");
                medical.setDosage("0.2g dusting per meal");
                medical.setTreatmentDetails("Full clinical physical evaluation. Clean cloaca, clear eyes, good muscle tone.");
                medical.setVeterinarianName("Dr. Sarah Jenkins, DVM");
                medical.setVeterinarianContact("+1 555-0199");
                medical.setCreatedBy(admin);
                medicalRecordRepository.save(medical);
            }

            index++;
        }
    }

    private void ensureMinimumReptilesPerSpecies() {
        Map<String, double[][]> sizeProfiles = Map.of(
                "King Snake", new double[][]{
                        {200, 240, 280, 320, 360, 400, 440, 480, 520, 560},
                        {35, 45, 55, 70, 80, 90, 100, 110, 120, 130}
                },
                "Corn Snake", new double[][]{
                        {200, 240, 280, 320, 360, 400, 440, 480, 520, 560},
                        {25, 35, 45, 60, 75, 90, 105, 120, 130, 140}
                },
                "Python Snake", new double[][]{
                        {200, 240, 280, 320, 360, 400, 440, 480, 520, 560},
                        {35, 45, 60, 80, 100, 120, 140, 160, 180, 200}
                }
        );
        for (Map.Entry<String, double[][]> entry : sizeProfiles.entrySet()) {
            String speciesName = entry.getKey();
            Species species = speciesRepository.findByName(speciesName)
                    .orElseThrow(() -> new IllegalStateException("Missing species for inventory seeding: " + speciesName));
            List<SpeciesVariety> varieties = varietyRepository.findBySpeciesId(species.getId());
            List<Reptile> existing = reptileRepository.findBySpeciesIdAndIsArchivedFalse(species.getId());
            double[][] profile = entry.getValue();

            for (int index = existing.size(); index < 10; index++) {
                SpeciesVariety variety = varieties.get(index % varieties.size());
                Reptile reptile = new Reptile();
                reptile.setName(speciesName + " Sample " + String.format("%02d", index + 1));
                reptile.setSpecies(species);
                reptile.setSpeciesVariety(variety);
                reptile.setSex(index % 2 == 0 ? "female" : "male");
                reptile.setCurrentWeight(BigDecimal.valueOf(profile[0][index]));
                reptile.setWeightUnit("g");
                reptile.setLength(BigDecimal.valueOf(profile[1][index]));
                reptile.setLengthUnit("cm");
                reptile.setDateOfBirth(LocalDate.now().minusMonths(6 + index * 4L));
                reptile.setAcquisitionDate(LocalDate.now().minusMonths(2));
                reptile.setAcquisitionSource("Sanctuary sample inventory");
                reptile.setStatus("active");
                reptile.setNotes("Sample inventory record. Replace with the specimen's verified details.");
                reptile = reptileRepository.save(reptile);
                reptile.setReptileCode(String.format("REP-%06d", reptile.getId()));
                reptileRepository.save(reptile);
            }
        }
    }

    private void syncSnakePhotosAndHistory() {
        User admin = userRepository.findByEmail("admin@example.com").orElse(null);

        for (Species species : speciesRepository.findAll()) {
            if (SnakeImageCatalog.isCatalogImage(species.storedPhoto())) {
                species.setPhoto(null);
                speciesRepository.save(species);
            }
        }
        for (SpeciesVariety variety : varietyRepository.findAll()) {
            if (SnakeImageCatalog.isCatalogImage(variety.storedPhoto())
                    || SnakeImageCatalog.isGeneratedMorphImage(variety.storedPhoto())) {
                String exactMorphImage = variety.getSpecies() == null
                        ? null
                        : SnakeImageCatalog.findMorphImage(
                                variety.getSpecies().getName(),
                                variety.getName()).orElse(null);
                if (!variety.storedPhoto().equals(exactMorphImage)) {
                    variety.setPhoto(exactMorphImage);
                    varietyRepository.save(variety);
                }
            }
        }

        FoodType fuzzyMouse = foodTypeRepository.findByNameAndParentIsNull("Mouse")
                .flatMap(p -> foodTypeRepository.findByNameAndParentId("Fuzzy", p.getId()))
                .orElseGet(() -> foodTypeRepository.findAll().stream().findFirst().orElse(null));

        List<Reptile> reptiles = reptileRepository.findAll();
        reptiles.sort(Comparator.comparing(Reptile::getId));
        Map<String, Integer> speciesRanks = new HashMap<>();
        for (Reptile reptile : reptiles) {
            if (SnakeImageCatalog.isCatalogImage(reptile.storedPhoto())
                    || SnakeImageCatalog.isGeneratedMorphImage(reptile.storedPhoto())) {
                String exactMorphImage = reptile.getSpeciesVariety() == null || reptile.getSpecies() == null
                        ? null
                        : SnakeImageCatalog.findMorphImage(
                                reptile.getSpecies().getName(),
                                reptile.getSpeciesVariety().getName()).orElse(null);
                if (!reptile.storedPhoto().equals(exactMorphImage)) {
                    reptile.setPhoto(exactMorphImage);
                    reptileRepository.save(reptile);
                }
            }

            boolean seededSnake = isSeededSnake(reptile);
            BigDecimal currentW = reptile.getCurrentWeight() != null
                    ? reptile.getCurrentWeight() : BigDecimal.valueOf(200.0);
            BigDecimal startingW = currentW.max(BigDecimal.valueOf(200))
                    .min(BigDecimal.valueOf(600));
            BigDecimal targetW = currentW;
            if (seededSnake) {
                String speciesName = reptile.getSpecies().getName();
                int rank = Math.min(speciesRanks.merge(speciesName, 1, Integer::sum) - 1, 9);
                int startingGrams = 200 + rank * 40;
                double speciesCeiling = switch (speciesName) {
                    case "King Snake" -> 1200.0;
                    case "Corn Snake" -> 900.0;
                    case "Python Snake" -> 1500.0;
                    default -> 1500.0;
                };
                double finalGrams = 300.0 + rank * (speciesCeiling - 300.0) / 9.0;
                startingW = BigDecimal.valueOf(startingGrams);
                targetW = BigDecimal.valueOf(finalGrams).setScale(2, java.math.RoundingMode.HALF_UP);
                reptile.setCurrentWeight(targetW);
                reptile.setWeightUnit("g");
                reptileRepository.save(reptile);
            }

            // Keep two sample shed observations for every snake.
            List<SheddingRecord> existingSheds = sheddingRecordRepository.findByReptileIdOrderBySheddingDateDescIdDesc(reptile.getId());
            if (existingSheds.size() < 2) {
                int needed = 2 - existingSheds.size();
                for (int i = 0; i < needed; i++) {
                    int slot = existingSheds.size() + i;

                    SheddingRecord s = new SheddingRecord();
                    s.setReptile(reptile);
                    s.setSheddingDate(LocalDate.now().minusDays(90L * (slot + 1)));
                    s.setStatus("complete");
                    s.setObservations("Synthetic demo history; replace with the specimen's verified shed observations.");
                    s.setCreatedBy(admin);
                    sheddingRecordRepository.save(s);
                }
            }

            // Retain existing records and fill the sample history to ten observations.
            List<PoopRecord> existingPoops = poopRecordRepository.findByReptileIdOrderByPoopDateDescIdDesc(reptile.getId());
            if (existingPoops.size() < 10) {
                int needed = 10 - existingPoops.size();
                for (int i = 0; i < needed; i++) {
                    int slot = existingPoops.size() + i;

                    PoopRecord p = new PoopRecord();
                    p.setReptile(reptile);
                    p.setPoopDate(LocalDateTime.now().minusDays(14L * (slot + 1) + 7));
                    p.setConsistency("normal");
                    p.setUratesStatus("normal_white");
                    p.setWeightAfterPoop(targetW.subtract(BigDecimal.valueOf(2.0 + slot % 3)));
                    p.setNotes("Synthetic demo history; replace with the specimen's measured record.");
                    p.setObservations("Sample normal stool and urates observation; verify against the individual snake.");
                    p.setCreatedBy(admin);
                    poopRecordRepository.save(p);
                }
            }

            // Keep the historical weight trend rising from a plausible juvenile baseline.
            List<FeedingRecord> existingFeedings = feedingRecordRepository.findByReptileIdOrderByFeedingDateDescIdDesc(reptile.getId());
            if (existingFeedings.size() < 10) {
                int needed = 10 - existingFeedings.size();
                for (int i = 0; i < needed; i++) {
                    int slot = existingFeedings.size() + i;

                    FeedingRecord f = new FeedingRecord();
                    f.setReptile(reptile);
                    f.setFoodType(fuzzyMouse);
                    f.setFeedingDate(LocalDateTime.now().minusDays(14L * (10 - slot) + 7));
                    f.setQuantity(BigDecimal.valueOf(1.00));
                    f.setQuantityUnit("piece");

                    BigDecimal growthPerFeeding = targetW.subtract(startingW)
                            .divide(BigDecimal.TEN, 2, java.math.RoundingMode.HALF_UP);
                    BigDecimal wBefore = startingW.add(growthPerFeeding.multiply(BigDecimal.valueOf(slot)));
                    BigDecimal wAfter = startingW.add(growthPerFeeding.multiply(BigDecimal.valueOf(slot + 1)));
                    BigDecimal wAfterDefecation = wAfter.subtract(BigDecimal.valueOf(2.0));

                    f.setWeightBefore(wBefore);
                    f.setWeightAfter(wAfter);
                    f.setWeightAfterDefecation(wAfterDefecation);
                    f.setNotes("Synthetic demo history; replace with verified weights and a veterinarian-approved feeding plan.");
                    f.setCreatedBy(admin);
                    feedingRecordRepository.save(f);
                }
            }

            if (seededSnake) {
                List<FeedingRecord> completeHistory =
                        feedingRecordRepository.findByReptileIdOrderByFeedingDateAscIdAsc(reptile.getId());
                boolean sampleHistory = completeHistory.stream().allMatch(record ->
                        record.getFeedingSchedule() != null
                                || (record.getNotes() != null && record.getNotes().startsWith("Synthetic demo")));
                if (sampleHistory && completeHistory.size() >= 10) {
                    BigDecimal growthPerFeeding = targetW.subtract(startingW)
                            .divide(BigDecimal.valueOf(completeHistory.size()), 2, java.math.RoundingMode.HALF_UP);
                    for (int i = 0; i < completeHistory.size(); i++) {
                        FeedingRecord record = completeHistory.get(i);
                        BigDecimal wBefore = startingW.add(growthPerFeeding.multiply(BigDecimal.valueOf(i)));
                        BigDecimal wAfter = startingW.add(growthPerFeeding.multiply(BigDecimal.valueOf(i + 1)));
                        record.setWeightBefore(wBefore);
                        record.setWeightAfter(wAfter);
                        record.setWeightAfterDefecation(wAfter.subtract(BigDecimal.valueOf(2.0)));
                        feedingRecordRepository.save(record);
                    }
                }
            }

            weightMonitoringService.recalculateReptileWeightHistory(reptile.getId());
            if (seededSnake) {
                reptile.setCurrentWeight(targetW);
                reptile.setWeightUnit("g");
                reptileRepository.save(reptile);
            }
            healthAlertService.syncWeightAlertsForReptile(reptile.getId());
        }

        // Specifically configure Python snake 'Spider' to trigger the Critical Weight Stagnation Alert (<10g across 3 feedings)
        Optional<Reptile> spiderOpt = reptileRepository.findAll().stream()
                .filter(r -> "Spider".equalsIgnoreCase(r.getName()))
                .findFirst();

        if (spiderOpt.isPresent()) {
            Reptile spider = spiderOpt.get();

            // Clear any old alerts and feeding records for Spider
            List<HealthAlert> existingAlerts = healthAlertRepository.findByReptileIdOrderByTriggeredAtDesc(spider.getId());
            healthAlertRepository.deleteAll(existingAlerts);

            List<FeedingRecord> existingFeedings = feedingRecordRepository.findByReptileIdOrderByFeedingDateAscIdAsc(spider.getId());
            feedingRecordRepository.deleteAll(existingFeedings);

            for (int i = 0; i < 7; i++) {
                FeedingRecord history = new FeedingRecord();
                history.setReptile(spider);
                history.setFoodType(fuzzyMouse);
                history.setFeedingDate(LocalDateTime.now().minusDays(140 - i * 14L));
                history.setQuantity(BigDecimal.valueOf(1.00));
                history.setQuantityUnit("piece");
                history.setWeightBefore(BigDecimal.valueOf(200.0 + i * 20.0));
                history.setWeightAfter(BigDecimal.valueOf(220.0 + i * 20.0));
                history.setWeightAfterDefecation(BigDecimal.valueOf(218.0 + i * 20.0));
                history.setNotes("Synthetic demo history; replace with verified specimen measurements.");
                history.setCreatedBy(admin);
                feedingRecordRepository.save(history);
            }

            // Keep three recent low-gain feedings to exercise the sample weight alert.
            double[] weightsBefore = {350.0, 351.0, 352.0};
            double[] weightsAfterDef = {351.0, 352.0, 353.0};
            int[] daysAgo = {21, 14, 7};

            for (int i = 0; i < 3; i++) {
                FeedingRecord f = new FeedingRecord();
                f.setReptile(spider);
                f.setFoodType(fuzzyMouse);
                f.setFeedingDate(LocalDateTime.now().minusDays(daysAgo[i]));
                f.setQuantity(BigDecimal.valueOf(1.00));
                f.setQuantityUnit("piece");
                f.setWeightBefore(BigDecimal.valueOf(weightsBefore[i]));
                f.setWeightAfter(BigDecimal.valueOf(weightsBefore[i] + 4.0));
                f.setWeightAfterDefecation(BigDecimal.valueOf(weightsAfterDef[i]));
                f.setNotes("Synthetic demo alert history: net gain +1.0g. Replace with verified specimen measurements.");
                f.setCreatedBy(admin);
                feedingRecordRepository.save(f);
            }

            spider.setCurrentWeight(BigDecimal.valueOf(353.0));
            reptileRepository.save(spider);

            weightMonitoringService.recalculateReptileWeightHistory(spider.getId());
            healthAlertService.syncWeightAlertsForReptile(spider.getId());
        }
    }

    private boolean isSeededSnake(Reptile reptile) {
        return "Sha Captive Breeding".equals(reptile.getAcquisitionSource())
                || (reptile.getNotes() != null
                && reptile.getNotes().startsWith("Sample inventory record."));
    }
}
