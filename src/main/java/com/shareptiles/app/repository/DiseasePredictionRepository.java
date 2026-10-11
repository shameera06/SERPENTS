package com.shareptiles.app.repository;

import com.shareptiles.app.model.DiseasePrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiseasePredictionRepository extends JpaRepository<DiseasePrediction, Long> {
    List<DiseasePrediction> findAllByOrderByCreatedAtDesc();
    List<DiseasePrediction> findByReptileIdOrderByCreatedAtDesc(Long reptileId);
    List<DiseasePrediction> findByStatusOrderByCreatedAtDesc(String status);

    long countByStatus(String status);
    long countByIsRespiratoryInfectionTrue();
}
