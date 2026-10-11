package com.shareptiles.app.repository;

import com.shareptiles.app.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByReptileIdOrderByTreatmentDateDescIdDesc(Long reptileId);
    Optional<MedicalRecord> findTopByReptileIdOrderByTreatmentDateDescIdDesc(Long reptileId);
    long countByReptileId(Long reptileId);
}
