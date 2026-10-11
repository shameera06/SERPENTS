package com.shareptiles.app.service;

import com.shareptiles.app.model.Reptile;
import com.shareptiles.app.repository.DiseasePredictionRepository;
import com.shareptiles.app.repository.FeedingRecordRepository;
import com.shareptiles.app.repository.FeedingScheduleAdjustmentRepository;
import com.shareptiles.app.repository.FeedingScheduleRepository;
import com.shareptiles.app.repository.HealthAlertRepository;
import com.shareptiles.app.repository.MedicalRecordRepository;
import com.shareptiles.app.repository.PoopRecordRepository;
import com.shareptiles.app.repository.ReptileRepository;
import com.shareptiles.app.repository.SheddingRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReptileDeletionServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void deletesDependentRecordsBeforeTheReptile() {
        ReptileRepository reptiles = mock(ReptileRepository.class);
        HealthAlertRepository alerts = mock(HealthAlertRepository.class);
        DiseasePredictionRepository predictions = mock(DiseasePredictionRepository.class);
        MedicalRecordRepository medical = mock(MedicalRecordRepository.class);
        FeedingRecordRepository feedings = mock(FeedingRecordRepository.class);
        FeedingScheduleAdjustmentRepository adjustments = mock(FeedingScheduleAdjustmentRepository.class);
        FeedingScheduleRepository schedules = mock(FeedingScheduleRepository.class);
        SheddingRecordRepository shedding = mock(SheddingRecordRepository.class);
        PoopRecordRepository poop = mock(PoopRecordRepository.class);
        Reptile reptile = new Reptile();

        when(reptiles.findById(9L)).thenReturn(Optional.of(reptile));
        when(alerts.findByReptileIdOrderByTriggeredAtDesc(9L)).thenReturn(List.of());
        when(predictions.findByReptileIdOrderByCreatedAtDesc(9L)).thenReturn(List.of());
        when(medical.findByReptileIdOrderByTreatmentDateDescIdDesc(9L)).thenReturn(List.of());
        when(feedings.findByReptileIdOrderByFeedingDateDescIdDesc(9L)).thenReturn(List.of());
        when(adjustments.findByReptileIdOrderByAdjustedAtDesc(9L)).thenReturn(List.of());
        when(schedules.findByReptileId(9L)).thenReturn(List.of());
        when(shedding.findByReptileIdOrderBySheddingDateDescIdDesc(9L)).thenReturn(List.of());
        when(poop.findByReptileIdOrderByPoopDateDescIdDesc(9L)).thenReturn(List.of());

        ReptileDeletionService service = new ReptileDeletionService(
                reptiles, alerts, predictions, medical, feedings, adjustments, schedules, shedding, poop);
        service.deleteReptile(9L);

        InOrder deletionOrder = inOrder(alerts, predictions, medical, feedings, adjustments, schedules,
                shedding, poop, reptiles);
        deletionOrder.verify(alerts).deleteAll(List.of());
        deletionOrder.verify(predictions).deleteAll(List.of());
        deletionOrder.verify(medical).deleteAll(List.of());
        deletionOrder.verify(feedings).deleteAll(List.of());
        deletionOrder.verify(adjustments).deleteAll(List.of());
        deletionOrder.verify(schedules).deleteAll(List.of());
        deletionOrder.verify(shedding).deleteAll(List.of());
        deletionOrder.verify(poop).deleteAll(List.of());
        deletionOrder.verify(reptiles).delete(reptile);
        verify(reptiles).findById(9L);
    }

    @Test
    void removesTheReptilesUploadedPhotoFromTheUploadDirectory() throws Exception {
        ReptileRepository reptiles = mock(ReptileRepository.class);
        Reptile reptile = new Reptile();
        reptile.setPhoto("reptiles/sally.jpg");
        when(reptiles.findById(9L)).thenReturn(Optional.of(reptile));

        Path photo = temporaryDirectory.resolve("reptiles/sally.jpg");
        Files.createDirectories(photo.getParent());
        Files.createFile(photo);
        ReptileDeletionService service = new ReptileDeletionService(
                reptiles,
                mock(HealthAlertRepository.class),
                mock(DiseasePredictionRepository.class),
                mock(MedicalRecordRepository.class),
                mock(FeedingRecordRepository.class),
                mock(FeedingScheduleAdjustmentRepository.class),
                mock(FeedingScheduleRepository.class),
                mock(SheddingRecordRepository.class),
                mock(PoopRecordRepository.class));
        ReflectionTestUtils.setField(service, "uploadDir", temporaryDirectory.toString());

        service.deleteReptile(9L);

        assertFalse(Files.exists(photo));
    }
}
