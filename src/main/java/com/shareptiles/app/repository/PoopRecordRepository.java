package com.shareptiles.app.repository;

import com.shareptiles.app.model.PoopRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoopRecordRepository extends JpaRepository<PoopRecord, Long> {
    List<PoopRecord> findByReptileIdOrderByPoopDateDescIdDesc(Long reptileId);
    Optional<PoopRecord> findTopByReptileIdOrderByPoopDateDescIdDesc(Long reptileId);
    long countByReptileId(Long reptileId);
}
