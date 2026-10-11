package com.shareptiles.app.service;

import com.shareptiles.app.model.Species;
import com.shareptiles.app.model.SpeciesVariety;
import com.shareptiles.app.repository.ReptileRepository;
import com.shareptiles.app.repository.SpeciesVarietyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataInitializerKingSnakeMigrationTest {

    @Test
    void renamesKingSnakeVarietyRecordsInPlaceAndKeepsTheirIds() {
        SpeciesVarietyRepository varietyRepository = mock(SpeciesVarietyRepository.class);
        ReptileRepository reptileRepository = mock(ReptileRepository.class);
        DataInitializer initializer = new DataInitializer();
        ReflectionTestUtils.setField(initializer, "varietyRepository", varietyRepository);
        ReflectionTestUtils.setField(initializer, "reptileRepository", reptileRepository);

        Species kingSnake = new Species("King Snake", "Lampropeltis getula", null, true);
        kingSnake.setId(7L);
        Map<String, SpeciesVariety> existing = new HashMap<>();
        addExisting(existing, kingSnake, 101L, "Blacky");
        addExisting(existing, kingSnake, 102L, "California");
        addExisting(existing, kingSnake, 103L, "Banded");
        addExisting(existing, kingSnake, 104L, "Albino King");
        addExisting(existing, kingSnake, 105L, "High White");
        addExisting(existing, kingSnake, 106L, "Desert Banded");
        addExisting(existing, kingSnake, 107L, "Lavender");
        addExisting(existing, kingSnake, 108L, "Aberrant");
        addExisting(existing, kingSnake, 109L, "Reverse Stripe");
        addExisting(existing, kingSnake, 110L, "Axanthic");

        when(varietyRepository.findBySpeciesIdAndName(eq(7L), anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(existing.get(invocation.getArgument(1))));
        when(varietyRepository.save(any(SpeciesVariety.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReflectionTestUtils.invokeMethod(initializer, "migrateVarieties", kingSnake, Map.ofEntries(
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

        assertRenamed(existing, "Blacky", "California Kingsnake", 101L,
                "/assets/img/snakes/king_california_kingsnake.jpg");
        assertRenamed(existing, "California", "Mexican Black Kingsnake", 102L,
                "/assets/img/snakes/king_mexican_black_kingsnake.jpg");
        assertRenamed(existing, "Banded", "Florida Kingsnake", 103L,
                "/assets/img/snakes/king_florida_kingsnake.jpg");
        assertRenamed(existing, "Albino King", "Speckled Kingsnake", 104L,
                "/assets/img/snakes/king_speckled_kingsnake.jpg");
        assertRenamed(existing, "High White", "Desert Kingsnake", 105L,
                "/assets/img/snakes/king_desert_kingsnake.jpg");
        assertRenamed(existing, "Desert Banded", "Eastern Kingsnake", 106L,
                "/assets/img/snakes/king_eastern_kingsnake.jpg");
        assertRenamed(existing, "Lavender", "Black Kingsnake", 107L,
                "/assets/img/snakes/king_black_kingsnake.jpg");
        assertRenamed(existing, "Aberrant", "Gray-banded Kingsnake", 108L,
                "/assets/img/snakes/king_gray_banded_kingsnake.jpg");
        assertRenamed(existing, "Reverse Stripe", "Prairie Kingsnake", 109L,
                "/assets/img/snakes/king_prairie_kingsnake.jpg");
        assertRenamed(existing, "Axanthic", "Scarlet Kingsnake", 110L,
                "/assets/img/snakes/king_scarlet_kingsnake.jpg");

        verify(varietyRepository, never()).delete(any(SpeciesVariety.class));
        verify(reptileRepository, never()).findBySpeciesVarietyId(any());
    }

    private void addExisting(Map<String, SpeciesVariety> existing, Species species, Long id, String name) {
        SpeciesVariety variety = new SpeciesVariety(species, name, true);
        variety.setId(id);
        existing.put(name, variety);
    }

    private void assertRenamed(Map<String, SpeciesVariety> existing, String oldName,
                               String expectedName, Long expectedId, String expectedPhoto) {
        SpeciesVariety variety = existing.get(oldName);
        assertEquals(expectedName, variety.getName());
        assertEquals(expectedId, variety.getId());
        assertEquals(expectedPhoto, variety.storedPhoto());
    }
}
