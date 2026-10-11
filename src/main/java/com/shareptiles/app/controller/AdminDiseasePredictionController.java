package com.shareptiles.app.controller;

import com.shareptiles.app.model.*;
import com.shareptiles.app.repository.*;
import com.shareptiles.app.service.CnnDiseasePredictionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/disease-predictions")
public class AdminDiseasePredictionController {

    @Autowired
    private DiseasePredictionRepository predictionRepository;

    @Autowired
    private ReptileRepository reptileRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private CnnDiseasePredictionService cnnService;

    @Autowired
    private com.shareptiles.app.service.PdfExportService pdfExportService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("predictions", predictionRepository.findAllByOrderByCreatedAtDesc());
        return "admin/disease-predictions/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("reptiles", reptileRepository.findByIsArchivedFalseOrderByCreatedAtDesc());
        return "admin/disease-predictions/create";
    }

    @PostMapping
    public String store(@RequestParam(required = false) Long reptileId,
                        @RequestParam(required = false) String speciesHint,
                        @RequestParam(required = false) String userNotes,
                        @RequestParam(required = false) MultipartFile imageFile,
                        @RequestParam(required = false) String samplePath) throws Exception {

        Reptile reptile = (reptileId != null) ? reptileRepository.findById(reptileId).orElse(null) : null;

        DiseasePrediction pred = cnnService.predictAndSave(
                imageFile,
                samplePath,
                reptile,
                null,
                null,
                null,
                speciesHint,
                userNotes
        );

        return "redirect:/admin/disease-predictions/" + pred.getId();
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        DiseasePrediction prediction = predictionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prediction not found: " + id));
        model.addAttribute("prediction", prediction);
        return "admin/disease-predictions/show";
    }

    @PostMapping("/{id}/convert-medical-record")
    public String convertToMedicalRecord(@PathVariable Long id,
                                         @RequestParam(required = false) Long reptileId,
                                         HttpSession session) {

        DiseasePrediction prediction = predictionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prediction not found: " + id));

        Reptile reptile = prediction.getReptile();
        if (reptile == null && reptileId != null) {
            reptile = reptileRepository.findById(reptileId).orElse(null);
            prediction.setReptile(reptile);
        }

        if (reptile != null && prediction.getMedicalRecord() == null) {
            MedicalRecord med = new MedicalRecord();
            med.setReptile(reptile);
            med.setTreatmentDate(LocalDate.now());
            med.setSymptomsOrCondition("AI Diagnostic Detection: " + prediction.getPredictedDisease() + " (Confidence: " + prediction.getConfidence() + "%)");
            med.setTreatmentDetails(prediction.getTreatmentRecommendations());
            med.setNotes("Generated automatically from AI Disease Prediction Scan #" + prediction.getId() + ". User notes: " + prediction.getUserNotes());
            med.setCreatedBy((User) session.getAttribute("currentUser"));

            med = medicalRecordRepository.save(med);
            prediction.setMedicalRecord(med);
            prediction.setStatus("converted_to_medical");
            predictionRepository.save(prediction);
        }

        return "redirect:/admin/disease-predictions/" + id;
    }

    @GetMapping({"/{id}/pdf", "/{id}/download-pdf"})
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        DiseasePrediction pred = predictionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prediction not found: " + id));

        try {
            byte[] pdfBytes = pdfExportService.generateDiseasePredictionPdf(pred);
            String safeDisease = (pred.getPredictedDisease() != null) ?
                    pred.getPredictedDisease().replaceAll("[^a-zA-Z0-9.-]", "_") : "Diagnostic_Report";
            String filename = "SERPENTS_Diagnostic_Report_#" + pred.getId() + "_" + safeDisease + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            String fallback = "<html><body style='font-family:sans-serif; padding:30px;'><h1>Diagnostic Report #" + pred.getId() + "</h1><p>" + e.getMessage() + "</p></body></html>";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report_" + pred.getId() + ".html\"")
                    .contentType(MediaType.TEXT_HTML)
                    .body(fallback.getBytes());
        }
    }
}
