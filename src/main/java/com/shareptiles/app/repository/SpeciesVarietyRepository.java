package com.shareptiles.app.repository;

import com.shareptiles.app.model.SpeciesVariety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpeciesVarietyRepository extends JpaRepository<SpeciesVariety, Long> {
    List<SpeciesVariety> findBySpeciesId(Long speciesId);
    Optional<SpeciesVariety> findBySpeciesIdAndName(Long speciesId, String name);
}
