package com.shareptiles.app.repository;

import com.shareptiles.app.model.Reptile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReptileRepository extends JpaRepository<Reptile, Long> {
    List<Reptile> findByIsArchivedFalseOrderByCreatedAtDesc();
    List<Reptile> findByIsArchivedTrueOrderByCreatedAtDesc();
    List<Reptile> findBySpeciesIdAndIsArchivedFalse(Long speciesId);
    List<Reptile> findBySpeciesVarietyIdAndIsArchivedFalse(Long varietyId);
    List<Reptile> findBySpeciesVarietyId(Long varietyId);
    Optional<Reptile> findByReptileCode(String reptileCode);

    long countByIsArchivedFalse();
    long countByIsArchivedTrue();
    long countByStatusAndIsArchivedFalse(String status);
}
