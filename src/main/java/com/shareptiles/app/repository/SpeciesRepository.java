package com.shareptiles.app.repository;

import com.shareptiles.app.model.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpeciesRepository extends JpaRepository<Species, Long> {
    Optional<Species> findByName(String name);
    List<Species> findByCategoryId(Long categoryId);
    List<Species> findByStatusTrue();
}
