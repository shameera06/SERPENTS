package com.shareptiles.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shareptiles.app.model.User;
import com.shareptiles.app.repository.DiseasePredictionRepository;
import com.shareptiles.app.repository.FeedingRecordRepository;
import com.shareptiles.app.repository.FeedingScheduleAdjustmentRepository;
import com.shareptiles.app.repository.FeedingScheduleRepository;
import com.shareptiles.app.repository.HealthAlertRepository;
import com.shareptiles.app.repository.MedicalRecordRepository;
import com.shareptiles.app.repository.PoopRecordRepository;
import com.shareptiles.app.repository.ReptileRepository;
import com.shareptiles.app.repository.SheddingRecordRepository;
import com.shareptiles.app.service.ReptileDeletionService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.h2.jdbcx.JdbcDataSource;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminOnlyActionsTest {

    @Test
    void staffCannotDeleteAReptile() {
        ReptileRepository reptiles = mock(ReptileRepository.class);
        ReptileDeletionService deletionService = new ReptileDeletionService(
                reptiles,
                mock(HealthAlertRepository.class),
                mock(DiseasePredictionRepository.class),
                mock(MedicalRecordRepository.class),
                mock(FeedingRecordRepository.class),
                mock(FeedingScheduleAdjustmentRepository.class),
                mock(FeedingScheduleRepository.class),
                mock(SheddingRecordRepository.class),
                mock(PoopRecordRepository.class));
        ReptileController controller = new ReptileController();
        ReflectionTestUtils.setField(controller, "reptileDeletionService", deletionService);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("currentUser")).thenReturn(new User("Staff", "staff@example.com", "hash", "staff"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> controller.delete(9L, session));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(reptiles, never()).findById(9L);
    }

    @Test
    void staffCannotExportDatabaseRecords() {
        AdminDataExportController controller = new AdminDataExportController(null, new ObjectMapper());
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("currentUser")).thenReturn(new User("Staff", "staff@example.com", "hash", "staff"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> controller.exportData(session));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void adminExportIncludesApplicationRowsButExcludesUserCredentials() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:admin_export;DB_CLOSE_DELAY=-1");
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE users (id BIGINT PRIMARY KEY, email VARCHAR(200), password VARCHAR(200))");
            statement.execute("INSERT INTO users VALUES (1, 'admin@example.com', 'secret-hash')");
            statement.execute("CREATE TABLE reptiles (id BIGINT PRIMARY KEY, name VARCHAR(100))");
            statement.execute("INSERT INTO reptiles VALUES (9, 'Sally')");
        }

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        AdminDataExportController controller = new AdminDataExportController(dataSource, objectMapper);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("currentUser")).thenReturn(new User("Admin", "admin@example.com", "hash", "admin"));

        var response = controller.exportData(session);
        String json = new String(response.getBody(), StandardCharsets.UTF_8);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(json.contains("\"REPTILES\""));
        assertTrue(json.contains("\"Sally\""));
        assertFalse(json.contains("\"USERS\""));
        assertFalse(json.contains("secret-hash"));
    }
}
