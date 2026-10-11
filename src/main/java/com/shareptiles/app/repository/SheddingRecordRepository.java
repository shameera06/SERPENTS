package com.shareptiles.app.repository;

import com.shareptiles.app.model.SheddingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SheddingRecordRepository extends JpaRepository<SheddingRecord, Long> {
    List<SheddingRecord> findByReptileIdOrderBySheddingDateDescIdDesc(Long reptileId);
    Optional<SheddingRecord> findTopByReptileIdOrderBySheddingDateDescIdDesc(Long reptileId);
    long countByReptileId(Long reptileId);
}
