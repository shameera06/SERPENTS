package com.shareptiles.app.repository;

import com.shareptiles.app.model.SpeciesCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpeciesCategoryRepository extends JpaRepository<SpeciesCategory, Long> {
    Optional<SpeciesCategory> findByName(String name);
    List<SpeciesCategory> findAllByOrderByStatusDescNameAsc();
}
