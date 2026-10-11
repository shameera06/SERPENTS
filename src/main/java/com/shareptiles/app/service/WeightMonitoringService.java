package com.shareptiles.app.service;

import com.shareptiles.app.model.FeedingRecord;
import com.shareptiles.app.model.Reptile;
import com.shareptiles.app.repository.FeedingRecordRepository;
import com.shareptiles.app.repository.ReptileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class WeightMonitoringService {

    public static final double MINIMUM_WEIGHT_GAIN_THRESHOLD = 10.0; // grams
    public static final String TECHNICAL_TERM = "Failure to Thrive (Weight Stagnation)";
    public static final String ALERT_MESSAGE = "Health Alert - Failure to Thrive (Weight Stagnation): Snake weight has not increased by minimum 10-15g after 3 consecutive feedings. Please consult a veterinarian.";

    @Autowired
    private FeedingRecordRepository feedingRecordRepository;

    @Autowired
    private ReptileRepository reptileRepository;

    public BigDecimal getEffectiveWeight(FeedingRecord record) {
        if (record.getWeightAfterDefecation() != null) {
            return record.getWeightAfterDefecation();
        }
        if (record.getWeightAfter() != null) {
            return record.getWeightAfter();
        }
        return record.getWeightBefore();
    }

    public BigDecimal calculateWeightChange(FeedingRecord record) {
        if (record.getWeightBefore() == null) {
            return null;
        }
        if (record.getWeightAfterDefecation() != null) {
            return record.getWeightAfterDefecation().subtract(record.getWeightBefore()).setScale(2, RoundingMode.HALF_UP);
        }
        if (record.getWeightAfter() != null) {
            return record.getWeightAfter().subtract(record.getWeightBefore()).setScale(2, RoundingMode.HALF_UP);
        }
        return null;
    }

    public String determineWeightTrend(FeedingRecord record, FeedingRecord previous) {
        if (previous == null) {
            return null;
        }
        BigDecimal current = getEffectiveWeight(record);
        BigDecimal prev = getEffectiveWeight(previous);
        if (current == null || prev == null) {
            return null;
        }

        int cmp = current.compareTo(prev);
        if (cmp > 0) {
            return "gain";
        } else if (cmp < 0) {
            return "loss";
        } else {
            return "no_gain";
        }
    }

    public boolean hasThreeConsecutivePoorGainRecords(List<FeedingRecord> records) {
        if (records == null || records.size() < 3) {
            return false;
        }

        List<FeedingRecord> three = records.subList(records.size() - 3, records.size());

        // 1. Check if all three have non-gain trends (no_gain or loss)
        boolean allPoorTrends = true;
        for (FeedingRecord r : three) {
            String trend = r.getWeightTrend();
            if ("gain".equalsIgnoreCase(trend)) {
                allPoorTrends = false;
                break;
            }
        }
        if (allPoorTrends) {
            return true;
        }

        // 2. Check total weight gain across the 3 consecutive feedings
        List<FeedingRecord> sorted = new ArrayList<>(three);
        sorted.sort(Comparator.comparing(FeedingRecord::getFeedingDate).thenComparing(FeedingRecord::getId));

        FeedingRecord oldest = sorted.get(0);
        FeedingRecord newest = sorted.get(sorted.size() - 1);

        BigDecimal startWeight = oldest.getWeightBefore() != null ? oldest.getWeightBefore() : getEffectiveWeight(oldest);
        BigDecimal endWeight = getEffectiveWeight(newest);

        if (startWeight != null && endWeight != null) {
            BigDecimal totalGain = endWeight.subtract(startWeight);
            return totalGain.doubleValue() < MINIMUM_WEIGHT_GAIN_THRESHOLD;
        }

        return false;
    }

    @Transactional
    public void recalculateReptileWeightHistory(Long reptileId) {
        List<FeedingRecord> records = feedingRecordRepository.findByReptileIdOrderByFeedingDateAscIdAsc(reptileId);
        List<FeedingRecord> processed = new ArrayList<>();

        for (FeedingRecord record : records) {
            FeedingRecord previous = processed.isEmpty() ? null : processed.get(processed.size() - 1);

            record.setWeightChange(calculateWeightChange(record));
            record.setWeightTrend(determineWeightTrend(record, previous));

            processed.add(record);

            boolean alert = hasThreeConsecutivePoorGainRecords(processed);
            record.setWeightAlertTriggered(alert);
            record.setWeightAlertMessage(alert ? ALERT_MESSAGE : null);

            feedingRecordRepository.save(record);
        }

        if (!processed.isEmpty()) {
            FeedingRecord latest = processed.get(processed.size() - 1);
            reptileRepository.findById(reptileId).ifPresent(reptile -> {
                reptile.setCurrentWeight(getEffectiveWeight(latest));
                reptileRepository.save(reptile);
            });
        }
    }
}
