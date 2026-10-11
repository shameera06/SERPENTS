package com.shareptiles.app.service;

import com.shareptiles.app.model.FeedingRecord;
import com.shareptiles.app.model.Reptile;
import com.shareptiles.app.repository.FeedingRecordRepository;
import com.shareptiles.app.repository.PoopRecordRepository;
import com.shareptiles.app.repository.ReptileRepository;
import com.shareptiles.app.repository.SheddingRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:snake-history-test;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
class SeededSnakeHistoryTest {

    @Autowired
    private ReptileRepository reptileRepository;

    @Autowired
    private FeedingRecordRepository feedingRecordRepository;

    @Autowired
    private PoopRecordRepository poopRecordRepository;

    @Autowired
    private SheddingRecordRepository sheddingRecordRepository;

    @Test
    void seedsThirtySnakesWithCompleteHusbandryHistoryAndRisingWeights() {
        List<Reptile> snakes = reptileRepository.findByIsArchivedFalseOrderByCreatedAtDesc();
        assertEquals(30, snakes.size());

        for (Reptile snake : snakes) {
            assertNotNull(snake.getCurrentWeight());
            assertTrue(snake.getCurrentWeight().compareTo(BigDecimal.valueOf(200)) >= 0);
            assertTrue(snake.getCurrentWeight().compareTo(BigDecimal.valueOf(1500)) <= 0);
            assertTrue(feedingRecordRepository.countByReptileId(snake.getId()) >= 10);
            assertTrue(poopRecordRepository.countByReptileId(snake.getId()) >= 10);
            assertTrue(sheddingRecordRepository.countByReptileId(snake.getId()) >= 2);

            List<FeedingRecord> feedings =
                    feedingRecordRepository.findByReptileIdOrderByFeedingDateAscIdAsc(snake.getId());
            BigDecimal previousWeight = null;
            for (FeedingRecord feeding : feedings) {
                BigDecimal effectiveWeight = feeding.getEffectiveWeight();
                assertNotNull(effectiveWeight);
                if (previousWeight != null) {
                    assertTrue(effectiveWeight.compareTo(previousWeight) > 0,
                            "Seeded feeding weights should rise for " + snake.getName());
                }
                previousWeight = effectiveWeight;
            }
            assertTrue(feedings.getFirst().getWeightBefore().compareTo(BigDecimal.valueOf(200)) >= 0);
            assertTrue(feedings.getFirst().getWeightBefore().compareTo(BigDecimal.valueOf(600)) <= 0);
        }
    }
}
