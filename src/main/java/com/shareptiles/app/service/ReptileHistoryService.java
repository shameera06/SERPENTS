package com.shareptiles.app.service;

import com.shareptiles.app.model.*;
import com.shareptiles.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReptileHistoryService {

    @Autowired
    private FeedingRecordRepository feedingRecordRepository;

    @Autowired
    private SheddingRecordRepository sheddingRecordRepository;

    @Autowired
    private PoopRecordRepository poopRecordRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private HealthAlertRepository healthAlertRepository;

    public Map<String, Object> getHistoryCounts(Reptile reptile) {
        Long id = reptile.getId();
        Map<String, Object> counts = new HashMap<>();
        counts.put("totalFeedingRecords", feedingRecordRepository.countByReptileId(id));
        counts.put("totalSheddingRecords", sheddingRecordRepository.countByReptileId(id));
        counts.put("totalPoopRecords", poopRecordRepository.countByReptileId(id));
        counts.put("totalMedicalRecords", medicalRecordRepository.countByReptileId(id));
        counts.put("totalHealthAlerts", healthAlertRepository.countByReptileId(id));
        counts.put("activeHealthAlerts", healthAlertRepository.countByReptileIdAndStatus(id, "active"));
        return counts;
    }

    public Map<String, Object> getLatestRecords(Reptile reptile) {
        Long id = reptile.getId();
        Map<String, Object> latest = new HashMap<>();
        latest.put("feeding", feedingRecordRepository.findTopByReptileIdOrderByFeedingDateDescIdDesc(id).orElse(null));
        latest.put("shedding", sheddingRecordRepository.findTopByReptileIdOrderBySheddingDateDescIdDesc(id).orElse(null));
        latest.put("poop", poopRecordRepository.findTopByReptileIdOrderByPoopDateDescIdDesc(id).orElse(null));
        latest.put("medical", medicalRecordRepository.findTopByReptileIdOrderByTreatmentDateDescIdDesc(id).orElse(null));

        List<HealthAlert> activeAlerts = healthAlertRepository.findByReptileIdAndStatus(id, "active");
        latest.put("activeAlert", activeAlerts.isEmpty() ? null : activeAlerts.get(0));

        return latest;
    }
}
