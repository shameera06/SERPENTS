package com.shareptiles.app.controller;

import com.shareptiles.app.model.*;
import com.shareptiles.app.repository.*;
import com.shareptiles.app.service.ReptileHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reptiles/{reptileId}/history")
public class ReptileHistoryController {

    @Autowired
    private ReptileRepository reptileRepository;

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

    @Autowired
    private ReptileHistoryService reptileHistoryService;

    @GetMapping
    public String index(@PathVariable Long reptileId, Model model) {
        Reptile reptile = reptileRepository.findById(reptileId)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + reptileId));

        model.addAttribute("reptile", reptile);
        model.addAttribute("counts", reptileHistoryService.getHistoryCounts(reptile));
        model.addAttribute("latest", reptileHistoryService.getLatestRecords(reptile));

        model.addAttribute("feedingRecords", feedingRecordRepository.findByReptileIdOrderByFeedingDateDescIdDesc(reptileId));
        model.addAttribute("sheddingRecords", sheddingRecordRepository.findByReptileIdOrderBySheddingDateDescIdDesc(reptileId));
        model.addAttribute("poopRecords", poopRecordRepository.findByReptileIdOrderByPoopDateDescIdDesc(reptileId));
        model.addAttribute("medicalRecords", medicalRecordRepository.findByReptileIdOrderByTreatmentDateDescIdDesc(reptileId));
        model.addAttribute("healthAlerts", healthAlertRepository.findByReptileIdOrderByTriggeredAtDesc(reptileId));

        return "admin/reptiles/history";
    }

    @GetMapping("/feeding-data")
    @ResponseBody
    public Map<String, Object> feedingData(@PathVariable Long reptileId) {
        List<FeedingRecord> list = feedingRecordRepository.findByReptileIdOrderByFeedingDateDescIdDesc(reptileId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("data", list);
        return resp;
    }

    @GetMapping("/shedding-data")
    @ResponseBody
    public Map<String, Object> sheddingData(@PathVariable Long reptileId) {
        List<SheddingRecord> list = sheddingRecordRepository.findByReptileIdOrderBySheddingDateDescIdDesc(reptileId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("data", list);
        return resp;
    }

    @GetMapping("/medical-data")
    @ResponseBody
    public Map<String, Object> medicalData(@PathVariable Long reptileId) {
        List<MedicalRecord> list = medicalRecordRepository.findByReptileIdOrderByTreatmentDateDescIdDesc(reptileId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("data", list);
        return resp;
    }

    @GetMapping("/health-alerts-data")
    @ResponseBody
    public Map<String, Object> healthAlertsData(@PathVariable Long reptileId) {
        List<HealthAlert> list = healthAlertRepository.findByReptileIdOrderByTriggeredAtDesc(reptileId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("data", list);
        return resp;
    }

    @GetMapping("/poop-data")
    @ResponseBody
    public Map<String, Object> poopData(@PathVariable Long reptileId) {
        List<PoopRecord> list = poopRecordRepository.findByReptileIdOrderByPoopDateDescIdDesc(reptileId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("data", list);
        return resp;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long reptileId) {
        Reptile reptile = reptileRepository.findById(reptileId)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + reptileId));

        String content = "<html><body style='font-family:sans-serif; padding:30px;'>"
                + "<h1>Sha Reptiles - Medical &amp; Complete History</h1>"
                + "<hr/>"
                + "<h3>Reptile: " + reptile.getName() + " (" + reptile.getReptileCode() + ")</h3>"
                + "<p>Species: " + reptile.getSpecies().getName() + " | Variety: " + reptile.getDisplayVarietyName() + "</p>"
                + "<p>Current Weight: " + reptile.getCurrentWeight() + " " + reptile.getWeightUnit() + "</p>"
                + "<p>Status: " + reptile.getStatus().toUpperCase() + "</p>"
                + "<h4>Report generated on: " + java.time.LocalDateTime.now() + "</h4>"
                + "</body></html>";

        byte[] pdfBytes = content.getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"history_" + reptile.getReptileCode() + ".html\"")
                .contentType(MediaType.TEXT_HTML)
                .body(pdfBytes);
    }
}
