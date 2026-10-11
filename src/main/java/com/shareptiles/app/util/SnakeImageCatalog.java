package com.shareptiles.app.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SnakeImageCatalog {

    public static final String IMAGE_NOT_AVAILABLE = "/assets/img/snakes/image-not-available.svg";

    private static final Set<String> LEGACY_CATALOG_IMAGES = Set.of(
            "/assets/img/snakes/blacky.jpg",
            "/assets/img/snakes/california.jpg",
            "/assets/img/snakes/banded.jpg",
            "/assets/img/snakes/albino_king.jpg",
            "/assets/img/snakes/tessera.jpg",
            "/assets/img/snakes/honey.jpg",
            "/assets/img/snakes/snow.jpg",
            "/assets/img/snakes/ghost_corn.jpg",
            "/assets/img/snakes/spider.jpg",
            "/assets/img/snakes/royal.jpg",
            "/assets/img/snakes/pastel.jpg",
            "/assets/img/snakes/banana_python.jpg"
    );

    private static final Map<Key, String> MORPH_IMAGES = createMorphImages();
    private static final Map<String, String> SPECIES_IMAGES = Map.of(
            normalize("King Snake"), "/assets/img/snakes/king_california_kingsnake.jpg",
            normalize("Corn Snake"), "/assets/img/snakes/corn_tessera.jpg",
            normalize("Python Snake"), "/assets/img/snakes/royal.jpg"
    );

    private SnakeImageCatalog() {}

    public static Optional<String> findMorphImage(String speciesName, String varietyName) {
        if (speciesName == null || varietyName == null) return Optional.empty();
        return Optional.ofNullable(MORPH_IMAGES.get(new Key(normalize(speciesName), normalize(varietyName))));
    }

    public static Optional<String> findSpeciesImage(String speciesName) {
        if (speciesName == null) return Optional.empty();
        return Optional.ofNullable(SPECIES_IMAGES.get(normalize(speciesName)));
    }

    public static boolean isCatalogImage(String photo) {
        return photo != null && (LEGACY_CATALOG_IMAGES.contains(photo)
                || MORPH_IMAGES.containsValue(photo) || SPECIES_IMAGES.containsValue(photo));
    }

    public static boolean isGeneratedMorphImage(String photo) {
        return photo != null && photo.startsWith("/uploads/generated-profiles/morph-");
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase().replaceAll("[^a-z0-9]+", " ").trim();
    }

    private static Map<Key, String> createMorphImages() {
        Map<Key, String> images = new HashMap<>();
        addKingImage(images, "california_kingsnake", "California Kingsnake");
        addKingImage(images, "mexican_black_kingsnake", "Mexican Black Kingsnake");
        addKingImage(images, "florida_kingsnake", "Florida Kingsnake");
        addKingImage(images, "speckled_kingsnake", "Speckled Kingsnake");
        addKingImage(images, "desert_kingsnake", "Desert Kingsnake");
        addKingImage(images, "eastern_kingsnake", "Eastern Kingsnake");
        addKingImage(images, "black_kingsnake", "Black Kingsnake");
        addKingImage(images, "gray_banded_kingsnake", "Gray-banded Kingsnake");
        addKingImage(images, "prairie_kingsnake", "Prairie Kingsnake");
        addKingImage(images, "scarlet_kingsnake", "Scarlet Kingsnake");

        addImage(images, "corn snake", "corn_tessera", "Tessera");
        addImage(images, "corn snake", "corn_snow", "Snow");
        addImage(images, "corn snake", "corn_amelanistic", "Amelanistic", "Amel", "Amelanistic (Amel)");
        addImage(images, "corn snake", "corn_anerythristic",
                "Anerythristic", "Anery", "Anerythristic (Anery)");
        addImage(images, "corn snake", "corn_albino", "Albino");
        addImage(images, "corn snake", "corn_butter", "Butter");
        addImage(images, "corn snake", "corn_lavender", "Lavender");
        addImage(images, "corn snake", "corn_palmetto", "Palmetto");
        addImage(images, "corn snake", "corn_blood_red", "Blood Red");
        addImage(images, "corn snake", "corn_motley", "Motley");

        addPythonImage(images, "spider", "Spider");
        addPythonImage(images, "enchi", "Enchi");
        addPythonImage(images, "blue_eyed_leucistic", "Blue-Eyed Leucistic", "Blue-Eyed Leucistic (BEL)",
                "Blue-Eyed Leucistic/Lucy", "BEL", "Lucy");
        addPythonImage(images, "red_eyed_leucistic", "Red-Eyed Leucistic", "Red-Eyed Leucistic (REL)", "REL");
        addPythonImage(images, "banana", "Banana", "Banana Python", "Banana Ball Python");
        addPythonImage(images, "albino", "Albino");
        addPythonImage(images, "pied", "Pied", "Pied (Piebald)", "Piebald");
        addPythonImage(images, "pastel", "Pastel");
        addPythonImage(images, "clown", "Clown");
        addPythonImage(images, "mojave", "Mojave");
        return Map.copyOf(images);
    }

    private static void addKingImage(Map<Key, String> images, String assetName, String... names) {
        addImage(images, "king snake", "king_" + assetName, names);
        addImage(images, "kingsnake", "king_" + assetName, names);
    }

    private static void addPythonImage(Map<Key, String> images, String assetName, String... names) {
        for (String species : new String[]{"python snake", "ball python", "python"}) {
            addImage(images, species, "python_" + assetName, names);
        }
    }

    private static void addImage(Map<Key, String> images, String species, String assetName, String... names) {
        String path = "/assets/img/snakes/" + assetName + ".jpg";
        for (String name : names) {
            images.put(new Key(species, name), path);
        }
    }

    private record Key(String species, String variety) {
        private Key {
            species = normalize(species);
            variety = normalize(variety);
        }
    }
}
