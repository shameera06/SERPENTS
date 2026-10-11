package com.shareptiles.app.controller;

import com.shareptiles.app.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
public class AdminDataExportController {

    private static final Pattern SAFE_TABLE_NAME = Pattern.compile("[A-Za-z0-9_]+");

    private final DataSource dataSource;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public AdminDataExportController(DataSource dataSource,
                                     com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/admin/data/export")
    public ResponseEntity<byte[]> exportData(HttpSession session) throws Exception {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !user.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator access required");
        }

        Map<String, Object> export = new LinkedHashMap<>();
        export.put("generatedAt", OffsetDateTime.now());
        export.put("excludedTables", List.of("users"));

        Map<String, List<Map<String, Object>>> tables = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            var metadata = connection.getMetaData();
            String schema = connection.getSchema();
            try (var result = metadata.getTables(connection.getCatalog(), schema, "%", new String[]{"TABLE"})) {
                while (result.next()) {
                    String tableName = result.getString("TABLE_NAME");
                    String tableSchema = result.getString("TABLE_SCHEM");
                    if (tableName == null || !SAFE_TABLE_NAME.matcher(tableName).matches()
                            || "users".equalsIgnoreCase(tableName)
                            || (schema != null && tableSchema != null && !schema.equalsIgnoreCase(tableSchema))) {
                        continue;
                    }

                    String quotedTableName = "\"" + tableName.replace("\"", "\"\"") + "\"";
                    try (Statement statement = connection.createStatement();
                         ResultSet rows = statement.executeQuery("SELECT * FROM " + quotedTableName)) {
                        ResultSetMetaData columns = rows.getMetaData();
                        List<Map<String, Object>> tableRows = new java.util.ArrayList<>();
                        while (rows.next()) {
                            Map<String, Object> row = new LinkedHashMap<>();
                            for (int column = 1; column <= columns.getColumnCount(); column++) {
                                row.put(columns.getColumnLabel(column), rows.getObject(column));
                            }
                            tableRows.add(row);
                        }
                        tables.put(tableName, tableRows);
                    }
                }
            }
        }
        export.put("tables", tables);

        byte[] body = objectMapper.writeValueAsBytes(export);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("sha-reptiles-data-export-" + LocalDate.now() + ".json", StandardCharsets.UTF_8)
                .build());
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}
