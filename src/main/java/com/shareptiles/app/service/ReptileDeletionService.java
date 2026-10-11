package com.shareptiles.app.service;

import com.shareptiles.app.model.Reptile;
import com.shareptiles.app.model.DiseasePrediction;
import com.shareptiles.app.model.HealthAlert;
import com.shareptiles.app.model.MedicalRecord;
import com.shareptiles.app.repository.DiseasePredictionRepository;
import com.shareptiles.app.repository.FeedingRecordRepository;
import com.shareptiles.app.repository.FeedingScheduleAdjustmentRepository;
import com.shareptiles.app.repository.FeedingScheduleRepository;
import com.shareptiles.app.repository.HealthAlertRepository;
import com.shareptiles.app.repository.MedicalRecordRepository;
import com.shareptiles.app.repository.PoopRecordRepository;
import com.shareptiles.app.repository.ReptileRepository;
import com.shareptiles.app.repository.SheddingRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReptileDeletionService {

    private static final Logger logger = LoggerFactory.getLogger(ReptileDeletionService.class);

    private final ReptileRepository reptileRepository;
    private final HealthAlertRepository healthAlertRepository;
    private final DiseasePredictionRepository diseasePredictionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final FeedingRecordRepository feedingRecordRepository;
    private final FeedingScheduleAdjustmentRepository scheduleAdjustmentRepository;
    private final FeedingScheduleRepository feedingScheduleRepository;
    private final SheddingRecordRepository sheddingRecordRepository;
    private final PoopRecordRepository poopRecordRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public ReptileDeletionService(ReptileRepository reptileRepository,
                                  HealthAlertRepository healthAlertRepository,
                                  DiseasePredictionRepository diseasePredictionRepository,
                                  MedicalRecordRepository medicalRecordRepository,
                                  FeedingRecordRepository feedingRecordRepository,
                                  FeedingScheduleAdjustmentRepository scheduleAdjustmentRepository,
                                  FeedingScheduleRepository feedingScheduleRepository,
                                  SheddingRecordRepository sheddingRecordRepository,
                                  PoopRecordRepository poopRecordRepository) {
        this.reptileRepository = reptileRepository;
        this.healthAlertRepository = healthAlertRepository;
        this.diseasePredictionRepository = diseasePredictionRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.feedingRecordRepository = feedingRecordRepository;
        this.scheduleAdjustmentRepository = scheduleAdjustmentRepository;
        this.feedingScheduleRepository = feedingScheduleRepository;
        this.sheddingRecordRepository = sheddingRecordRepository;
        this.poopRecordRepository = poopRecordRepository;
    }

    @Transactional
    public void deleteReptile(Long reptileId) {
        Reptile reptile = reptileRepository.findById(reptileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reptile not found"));

        List<HealthAlert> alerts = healthAlertRepository.findByReptileIdOrderByTriggeredAtDesc(reptileId);
        List<DiseasePrediction> predictions = diseasePredictionRepository.findByReptileIdOrderByCreatedAtDesc(reptileId);
        List<MedicalRecord> medicalRecords = medicalRecordRepository.findByReptileIdOrderByTreatmentDateDescIdDesc(reptileId);
        List<String> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(reptile.storedPhoto());
        uploadedFiles.add(reptile.getPariveshCertificatePath());
        alerts.stream().map(HealthAlert::getEvidencePath).forEach(uploadedFiles::add);
        predictions.stream().map(DiseasePrediction::getImagePath).forEach(uploadedFiles::add);
        predictions.stream().map(DiseasePrediction::getHeatmapPath).forEach(uploadedFiles::add);
        medicalRecords.stream().map(MedicalRecord::getPrescriptionPath).forEach(uploadedFiles::add);

        healthAlertRepository.deleteAll(alerts);
        diseasePredictionRepository.deleteAll(predictions);
        medicalRecordRepository.deleteAll(medicalRecords);
        feedingRecordRepository.deleteAll(feedingRecordRepository.findByReptileIdOrderByFeedingDateDescIdDesc(reptileId));
        scheduleAdjustmentRepository.deleteAll(scheduleAdjustmentRepository.findByReptileIdOrderByAdjustedAtDesc(reptileId));
        feedingScheduleRepository.deleteAll(feedingScheduleRepository.findByReptileId(reptileId));
        sheddingRecordRepository.deleteAll(sheddingRecordRepository.findByReptileIdOrderBySheddingDateDescIdDesc(reptileId));
        poopRecordRepository.deleteAll(poopRecordRepository.findByReptileIdOrderByPoopDateDescIdDesc(reptileId));
        reptileRepository.delete(reptile);
        deleteUploadedFilesAfterCommit(uploadedFiles);
    }

    private void deleteUploadedFilesAfterCommit(List<String> storedPaths) {
        Runnable cleanup = () -> storedPaths.stream()
                .map(this::resolveManagedUpload)
                .filter(path -> path != null)
                .forEach(this::deleteManagedUpload);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cleanup.run();
                }
            });
        } else {
            cleanup.run();
        }
    }

    private Path resolveManagedUpload(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) return null;
        String relativePath = storedPath.replace('\\', '/');
        if (relativePath.startsWith("/uploads/")) {
            relativePath = relativePath.substring("/uploads/".length());
        } else if (relativePath.startsWith("uploads/")) {
            relativePath = relativePath.substring("uploads/".length());
        }
        if (relativePath.isBlank() || relativePath.startsWith("/")) return null;

        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path candidate = root.resolve(relativePath).normalize();
        if (!candidate.startsWith(root)) return null;
        try {
            if (!Files.exists(candidate)) return null;
            Path realRoot = root.toRealPath();
            Path realCandidate = candidate.toRealPath();
            return realCandidate.startsWith(realRoot) ? realCandidate : null;
        } catch (IOException e) {
            logger.warn("Unable to resolve uploaded file for deleted reptile: {}", storedPath, e);
            return null;
        }
    }

    private void deleteManagedUpload(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            logger.warn("Unable to remove uploaded file for deleted reptile: {}", path, e);
        }
    }
}
