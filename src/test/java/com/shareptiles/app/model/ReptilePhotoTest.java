package com.shareptiles.app.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReptilePhotoTest {

    @Test
    void usesCommonSpeciesPhotosForTheSpeciesCards() {
        assertSpeciesPhoto("King Snake", "/assets/img/snakes/king_california_kingsnake.jpg");
        assertSpeciesPhoto("Corn Snake", "/assets/img/snakes/corn_tessera.jpg");
        assertSpeciesPhoto("Python Snake", "/assets/img/snakes/royal.jpg");
    }

    @Test
    void prefersUploadedSpecimenPhotoOverMorphAndSpeciesPhotos() {
        Reptile reptile = reptileOfSpecies("Corn Snake");
        reptile.setPhoto("/uploads/reptiles/custom-photo.jpg");

        assertEquals("/uploads/reptiles/custom-photo.jpg", reptile.getPhoto());
    }

    @Test
    void mapsMorphImagesUsingBothSpeciesAndVariety() {
        assertMorphPhoto("Python Snake", "Spider", "/assets/img/snakes/python_spider.jpg");
        assertMorphPhoto("Python Snake", "Pastel", "/assets/img/snakes/python_pastel.jpg");
        assertMorphPhoto("Python Snake", "Banana Python", "/assets/img/snakes/python_banana.jpg");
        assertMorphPhoto("Ball Python", "Pastel", "/assets/img/snakes/python_pastel.jpg");
        assertMorphPhoto("Python Snake", "Blue-Eyed Leucistic (BEL)", "/assets/img/snakes/python_blue_eyed_leucistic.jpg");
        assertMorphPhoto("Python Snake", "Lucy", "/assets/img/snakes/python_blue_eyed_leucistic.jpg");
        assertMorphPhoto("Python Snake", "Red-Eyed Leucistic (REL)", "/assets/img/snakes/python_red_eyed_leucistic.jpg");
        assertMorphPhoto("Python Snake", "Enchi", "/assets/img/snakes/python_enchi.jpg");
        assertMorphPhoto("Python Snake", "Mojave", "/assets/img/snakes/python_mojave.jpg");
        assertMorphPhoto("Corn Snake", "Spider", "/assets/img/snakes/corn_tessera.jpg");
        assertMorphPhoto("Ball Python", "Albino", "/assets/img/snakes/python_albino.jpg");
        assertMorphPhoto("Ball Python", "Pied", "/assets/img/snakes/python_pied.jpg");
        assertMorphPhoto("Ball Python", "Blue-Eyed Leucistic",
                "/assets/img/snakes/python_blue_eyed_leucistic.jpg");
        assertMorphPhoto("Corn Snake", "Anery", "/assets/img/snakes/corn_anerythristic.jpg");
        assertMorphPhoto("King Snake", "Scarlet Kingsnake", "/assets/img/snakes/king_scarlet_kingsnake.jpg");
    }

    @Test
    void mapsAllTenReplacementKingSnakeVarietiesToTheirOwnPhotos() {
        assertMorphPhoto("King Snake", "California Kingsnake", "/assets/img/snakes/king_california_kingsnake.jpg");
        assertMorphPhoto("King Snake", "Mexican Black Kingsnake", "/assets/img/snakes/king_mexican_black_kingsnake.jpg");
        assertMorphPhoto("King Snake", "Florida Kingsnake", "/assets/img/snakes/king_florida_kingsnake.jpg");
        assertMorphPhoto("King Snake", "Speckled Kingsnake", "/assets/img/snakes/king_speckled_kingsnake.jpg");
        assertMorphPhoto("King Snake", "Desert Kingsnake", "/assets/img/snakes/king_desert_kingsnake.jpg");
        assertMorphPhoto("King Snake", "Eastern Kingsnake", "/assets/img/snakes/king_eastern_kingsnake.jpg");
        assertMorphPhoto("King Snake", "Black Kingsnake", "/assets/img/snakes/king_black_kingsnake.jpg");
        assertMorphPhoto("King Snake", "Gray-banded Kingsnake", "/assets/img/snakes/king_gray_banded_kingsnake.jpg");
        assertMorphPhoto("King Snake", "Prairie Kingsnake", "/assets/img/snakes/king_prairie_kingsnake.jpg");
        assertMorphPhoto("King Snake", "Scarlet Kingsnake", "/assets/img/snakes/king_scarlet_kingsnake.jpg");
    }

    @Test
    void mapsAllTenCornSnakeVarietiesToTheirOwnPhotos() {
        assertMorphPhoto("Corn Snake", "Tessera", "/assets/img/snakes/corn_tessera.jpg");
        assertMorphPhoto("Corn Snake", "Snow", "/assets/img/snakes/corn_snow.jpg");
        assertMorphPhoto("Corn Snake", "Amelanistic (Amel)", "/assets/img/snakes/corn_amelanistic.jpg");
        assertMorphPhoto("Corn Snake", "Anerythristic (Anery)", "/assets/img/snakes/corn_anerythristic.jpg");
        assertMorphPhoto("Corn Snake", "Albino", "/assets/img/snakes/corn_albino.jpg");
        assertMorphPhoto("Corn Snake", "Butter", "/assets/img/snakes/corn_butter.jpg");
        assertMorphPhoto("Corn Snake", "Lavender", "/assets/img/snakes/corn_lavender.jpg");
        assertMorphPhoto("Corn Snake", "Palmetto", "/assets/img/snakes/corn_palmetto.jpg");
        assertMorphPhoto("Corn Snake", "Blood Red", "/assets/img/snakes/corn_blood_red.jpg");
        assertMorphPhoto("Corn Snake", "Motley", "/assets/img/snakes/corn_motley.jpg");
    }

    @Test
    void mapsAllTenPythonVarietiesToTheirOwnPhotos() {
        assertMorphPhoto("Python Snake", "Spider", "/assets/img/snakes/python_spider.jpg");
        assertMorphPhoto("Python Snake", "Enchi", "/assets/img/snakes/python_enchi.jpg");
        assertMorphPhoto("Python Snake", "Blue-Eyed Leucistic (BEL)",
                "/assets/img/snakes/python_blue_eyed_leucistic.jpg");
        assertMorphPhoto("Python Snake", "Red-Eyed Leucistic (REL)",
                "/assets/img/snakes/python_red_eyed_leucistic.jpg");
        assertMorphPhoto("Python Snake", "Banana", "/assets/img/snakes/python_banana.jpg");
        assertMorphPhoto("Python Snake", "Albino", "/assets/img/snakes/python_albino.jpg");
        assertMorphPhoto("Python Snake", "Pied (Piebald)", "/assets/img/snakes/python_pied.jpg");
        assertMorphPhoto("Python Snake", "Pastel", "/assets/img/snakes/python_pastel.jpg");
        assertMorphPhoto("Python Snake", "Clown", "/assets/img/snakes/python_clown.jpg");
        assertMorphPhoto("Python Snake", "Mojave", "/assets/img/snakes/python_mojave.jpg");
    }

    @Test
    void fallsBackToAnUploadedSpeciesPhotoForUnmappedMorphs() {
        SpeciesVariety variety = new SpeciesVariety();
        Species species = new Species("Python Snake", null, null, true);
        species.setPhoto("/uploads/species/python.jpg");
        variety.setSpecies(species);
        variety.setName("Custom Morph");

        assertEquals("/uploads/species/python.jpg", variety.getPhoto());
    }

    @Test
    void doesNotUseAStoredMorphAssetAsASpeciesFallback() {
        Species python = new Species("Python Snake", null, null, true);
        python.setPhoto("/assets/img/snakes/royal.jpg");

        assertEquals("/assets/img/snakes/royal.jpg", python.getPhoto());
    }

    @Test
    void ignoresStaleSpecimenMorphImageAndUsesTheExactVarietyMapping() {
        Reptile reptile = reptileOfSpecies("Python Snake");
        SpeciesVariety variety = new SpeciesVariety(reptile.getSpecies(), "Pastel", true);
        reptile.setSpeciesVariety(variety);
        reptile.setPhoto("/assets/img/snakes/royal.jpg");

        assertEquals("/assets/img/snakes/python_pastel.jpg", reptile.getPhoto());
    }

    @Test
    void preservesUploadedSpeciesPhoto() {
        Species species = new Species("Custom Lizard", null, null, true);
        species.setPhoto("/uploads/species/custom-lizard.jpg");

        assertEquals("/uploads/species/custom-lizard.jpg", species.getPhoto());
    }

    private void assertSpeciesPhoto(String speciesName, String expectedPhoto) {
        assertEquals(expectedPhoto, reptileOfSpecies(speciesName).getPhoto());
    }

    private void assertMorphPhoto(String speciesName, String varietyName, String expectedPhoto) {
        Reptile reptile = reptileOfSpecies(speciesName);
        reptile.setSpeciesVariety(new SpeciesVariety(reptile.getSpecies(), varietyName, true));
        assertEquals(expectedPhoto, reptile.getPhoto());
    }

    private Reptile reptileOfSpecies(String speciesName) {
        Reptile reptile = new Reptile();
        reptile.setSpecies(new Species(speciesName, null, null, true));
        return reptile;
    }
}
