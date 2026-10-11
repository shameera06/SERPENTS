package com.shareptiles.app.controller;

import com.shareptiles.app.model.*;
import com.shareptiles.app.repository.*;
import com.shareptiles.app.service.PdfExportService;
import com.shareptiles.app.service.ReptileDeletionService;
import com.shareptiles.app.service.ReptileChatService;
import com.shareptiles.app.service.WeightMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin/reptiles")
public class ReptileController {

    @Autowired
    private ReptileRepository reptileRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired
    private SpeciesVarietyRepository varietyRepository;

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
    private WeightMonitoringService weightMonitoringService;

    @Autowired
    private PdfExportService pdfExportService;

    @Autowired
    private ReptileChatService reptileChatService;

    @Autowired
    private ReptileDeletionService reptileDeletionService;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @GetMapping
    public String index(@RequestParam(defaultValue = "false") boolean archived, Model model) {
        List<Reptile> reptiles = archived ?
                reptileRepository.findByIsArchivedTrueOrderByCreatedAtDesc() :
                reptileRepository.findByIsArchivedFalseOrderByCreatedAtDesc();
        model.addAttribute("reptiles", reptiles);
        model.addAttribute("showingArchived", archived);
        return "admin/reptiles/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("reptile", new Reptile());
        model.addAttribute("speciesList", speciesRepository.findAll());
        model.addAttribute("varieties", varietyRepository.findAll());
        return "admin/reptiles/create";
    }

    @PostMapping
    public String store(@RequestParam Long speciesId,
                        @RequestParam(required = false) Long speciesVarietyId,
                        @RequestParam String name,
                        @RequestParam String sex,
                        @RequestParam(required = false) Double currentWeight,
                        @RequestParam(required = false) Double length,
                        @RequestParam(required = false) String dateOfBirth,
                        @RequestParam(required = false) String acquisitionDate,
                        @RequestParam(required = false) String acquisitionSource,
                        @RequestParam(required = false) String notes,
                        @RequestParam(required = false) MultipartFile photoFile,
                        @RequestParam(required = false) MultipartFile certificateFile) throws Exception {

        Species species = speciesRepository.findById(speciesId)
                .orElseThrow(() -> new IllegalArgumentException("Species not found: " + speciesId));

        Reptile reptile = new Reptile();
        reptile.setName(name);
        reptile.setSpecies(species);
        if (speciesVarietyId != null) {
            varietyRepository.findById(speciesVarietyId).ifPresent(reptile::setSpeciesVariety);
        }
        reptile.setSex(sex);
        if (currentWeight != null) reptile.setCurrentWeight(BigDecimal.valueOf(currentWeight));
        if (length != null) reptile.setLength(BigDecimal.valueOf(length));
        if (dateOfBirth != null && !dateOfBirth.isBlank()) reptile.setDateOfBirth(LocalDate.parse(dateOfBirth));
        if (acquisitionDate != null && !acquisitionDate.isBlank()) reptile.setAcquisitionDate(LocalDate.parse(acquisitionDate));
        reptile.setAcquisitionSource(acquisitionSource);
        reptile.setNotes(notes);

        if (photoFile != null && !photoFile.isEmpty()) {
            String path = com.shareptiles.app.util.FileUploadHelper.saveUploadedFile(photoFile, uploadDir, "reptiles");
            reptile.setPhoto(path);
        }
        reptile.setPariveshCertificatePath(saveCertificate(certificateFile));

        reptile = reptileRepository.save(reptile);
        reptile.setReptileCode(String.format("REP-%06d", reptile.getId()));
        reptileRepository.save(reptile);

        return "redirect:/admin/reptiles";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        Reptile reptile = reptileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + id));
        model.addAttribute("reptile", reptile);
        return "admin/reptiles/show";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Reptile reptile = reptileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + id));
        model.addAttribute("reptile", reptile);
        model.addAttribute("speciesList", speciesRepository.findAll());
        model.addAttribute("varieties", varietyRepository.findAll());
        return "admin/reptiles/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam Long speciesId,
                         @RequestParam(required = false) Long speciesVarietyId,
                         @RequestParam String name,
                         @RequestParam String sex,
                         @RequestParam(required = false) Double currentWeight,
                         @RequestParam(required = false) Double length,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String notes,
                         @RequestParam(required = false) MultipartFile certificateFile) throws IOException {

        Reptile reptile = reptileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + id));

        Species species = speciesRepository.findById(speciesId)
                .orElseThrow(() -> new IllegalArgumentException("Species not found: " + speciesId));

        reptile.setName(name);
        reptile.setSpecies(species);
        if (speciesVarietyId != null) {
            varietyRepository.findById(speciesVarietyId).ifPresent(reptile::setSpeciesVariety);
        } else {
            reptile.setSpeciesVariety(null);
        }
        reptile.setSex(sex);
        if (currentWeight != null) reptile.setCurrentWeight(BigDecimal.valueOf(currentWeight));
        if (length != null) reptile.setLength(BigDecimal.valueOf(length));
        if (status != null) reptile.setStatus(status);
        reptile.setNotes(notes);
        if (certificateFile != null && !certificateFile.isEmpty()) {
            reptile.setPariveshCertificatePath(saveCertificate(certificateFile));
        }

        reptileRepository.save(reptile);
        return "redirect:/admin/reptiles/" + id;
    }

    @GetMapping("/{id}/certificate")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long id) throws IOException {
        Reptile reptile = reptileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reptile not found"));
        if (reptile.getPariveshCertificatePath() == null || reptile.getPariveshCertificatePath().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No PARIVESH certificate has been uploaded for this reptile");
        }

        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path certificate = uploadRoot.resolve(reptile.getPariveshCertificatePath()).normalize();
        if (!certificate.startsWith(uploadRoot) || !Files.isRegularFile(certificate)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PARIVESH certificate file not found");
        }

        String reptileCode = reptile.getReptileCode() != null ? reptile.getReptileCode() : "reptile-" + id;
        String filename = reptileCode + "_PARIVESH_certificate.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(Files.readAllBytes(certificate));
    }

    @PostMapping("/{id}/certificate")
    public String uploadCertificate(@PathVariable Long id,
                                    @RequestParam MultipartFile certificateFile) throws IOException {
        Reptile reptile = reptileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + id));
        reptile.setPariveshCertificatePath(saveCertificate(certificateFile));
        reptileRepository.save(reptile);
        return "redirect:/admin/reptiles/" + id;
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !user.isAdmin()) {
            return "redirect:/admin/reptiles?error=unauthorized";
        }
        Reptile reptile = reptileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + id));
        reptile.setArchived(!reptile.isArchived());
        reptileRepository.save(reptile);
        return "redirect:/admin/reptiles";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !user.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator access required");
        }
        reptileDeletionService.deleteReptile(id);
        return "redirect:/admin/reptiles?deleted";
    }

    @GetMapping("/{id}/weight-history")
    public String weightHistory(@PathVariable Long id, Model model) {
        Reptile reptile = reptileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + id));
        List<FeedingRecord> records = feedingRecordRepository.findByReptileIdOrderByFeedingDateDescIdDesc(id);

        model.addAttribute("reptile", reptile);
        model.addAttribute("records", records);
        return "admin/reptiles/weight-history";
    }

    private String saveCertificate(MultipartFile certificateFile) throws IOException {
        if (certificateFile == null || certificateFile.isEmpty()) {
            return null;
        }
        if (certificateFile.getSize() > 25L * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PARIVESH certificate must be 25 MB or smaller");
        }
        String originalFilename = certificateFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PARIVESH certificate must be a PDF file");
        }
        try (var input = certificateFile.getInputStream()) {
            byte[] signature = input.readNBytes(5);
            if (!Arrays.equals(signature, "%PDF-".getBytes(StandardCharsets.US_ASCII))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is not a valid PDF document");
            }
        }
        return com.shareptiles.app.util.FileUploadHelper.saveUploadedFile(certificateFile, uploadDir, "reptile-certificates");
    }

    @GetMapping({"/{id}/pdf", "/{id}/history/pdf", "/{id}/print"})
    public String printPdf(@PathVariable Long id, Model model) {
        Reptile reptile = reptileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + id));

        java.time.LocalDateTime twoMonthsAgo = java.time.LocalDateTime.now().minusMonths(2);
        java.time.LocalDate twoMonthsAgoDate = java.time.LocalDate.now().minusMonths(2);

        List<FeedingRecord> allFeedings = feedingRecordRepository.findByReptileIdOrderByFeedingDateDescIdDesc(id);
        List<FeedingRecord> feedings2m = allFeedings.stream()
                .filter(f -> f.getFeedingDate() != null && !f.getFeedingDate().isBefore(twoMonthsAgo))
                .toList();
        if (feedings2m.isEmpty() && !allFeedings.isEmpty()) {
            feedings2m = allFeedings.stream().limit(5).toList();
        }

        List<PoopRecord> allPoops = poopRecordRepository.findByReptileIdOrderByPoopDateDescIdDesc(id);
        List<PoopRecord> poops2m = allPoops.stream()
                .filter(p -> p.getPoopDate() != null && !p.getPoopDate().isBefore(twoMonthsAgo))
                .toList();
        if (poops2m.isEmpty() && !allPoops.isEmpty()) {
            poops2m = allPoops.stream().limit(5).toList();
        }

        List<SheddingRecord> allSheds = sheddingRecordRepository.findByReptileIdOrderBySheddingDateDescIdDesc(id);
        List<SheddingRecord> sheds2m = allSheds.stream()
                .filter(s -> s.getSheddingDate() != null && !s.getSheddingDate().isBefore(twoMonthsAgoDate))
                .toList();
        if (sheds2m.isEmpty() && !allSheds.isEmpty()) {
            sheds2m = allSheds.stream().limit(5).toList();
        }

        List<MedicalRecord> allMedicals = medicalRecordRepository.findByReptileIdOrderByTreatmentDateDescIdDesc(id);
        List<MedicalRecord> medicals2m = allMedicals.stream()
                .filter(m -> m.getTreatmentDate() != null && !m.getTreatmentDate().isBefore(twoMonthsAgoDate))
                .toList();
        if (medicals2m.isEmpty() && !allMedicals.isEmpty()) {
            medicals2m = allMedicals.stream().limit(5).toList();
        }

        List<HealthAlert> alerts = healthAlertRepository.findByReptileIdOrderByTriggeredAtDesc(id);

        model.addAttribute("reptile", reptile);
        model.addAttribute("twoMonthsAgo", twoMonthsAgo);
        model.addAttribute("feedings", feedings2m);
        model.addAttribute("poops", poops2m);
        model.addAttribute("sheds", sheds2m);
        model.addAttribute("medicals", medicals2m);
        model.addAttribute("alerts", alerts);
        model.addAttribute("totalFeedings", feedings2m.size());
        model.addAttribute("totalPoops", poops2m.size());
        model.addAttribute("totalSheds", sheds2m.size());

        return "admin/reptiles/print-pdf";
    }

    @GetMapping({"/{id}/pdf/download", "/{id}/download-pdf"})
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Reptile reptile = reptileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + id));

        java.time.LocalDateTime twoMonthsAgo = java.time.LocalDateTime.now().minusMonths(2);
        java.time.LocalDate twoMonthsAgoDate = java.time.LocalDate.now().minusMonths(2);

        List<FeedingRecord> allFeedings = feedingRecordRepository.findByReptileIdOrderByFeedingDateDescIdDesc(id);
        List<FeedingRecord> feedings2m = allFeedings.stream()
                .filter(f -> f.getFeedingDate() != null && !f.getFeedingDate().isBefore(twoMonthsAgo))
                .toList();
        if (feedings2m.isEmpty() && !allFeedings.isEmpty()) feedings2m = allFeedings.stream().limit(5).toList();

        List<PoopRecord> allPoops = poopRecordRepository.findByReptileIdOrderByPoopDateDescIdDesc(id);
        List<PoopRecord> poops2m = allPoops.stream()
                .filter(p -> p.getPoopDate() != null && !p.getPoopDate().isBefore(twoMonthsAgo))
                .toList();
        if (poops2m.isEmpty() && !allPoops.isEmpty()) poops2m = allPoops.stream().limit(5).toList();

        List<SheddingRecord> allSheds = sheddingRecordRepository.findByReptileIdOrderBySheddingDateDescIdDesc(id);
        List<SheddingRecord> sheds2m = allSheds.stream()
                .filter(s -> s.getSheddingDate() != null && !s.getSheddingDate().isBefore(twoMonthsAgoDate))
                .toList();
        if (sheds2m.isEmpty() && !allSheds.isEmpty()) sheds2m = allSheds.stream().limit(5).toList();

        List<MedicalRecord> allMedicals = medicalRecordRepository.findByReptileIdOrderByTreatmentDateDescIdDesc(id);
        List<MedicalRecord> medicals2m = allMedicals.stream()
                .filter(m -> m.getTreatmentDate() != null && !m.getTreatmentDate().isBefore(twoMonthsAgoDate))
                .toList();
        if (medicals2m.isEmpty() && !allMedicals.isEmpty()) medicals2m = allMedicals.stream().limit(5).toList();

        List<HealthAlert> alerts = healthAlertRepository.findByReptileIdOrderByTriggeredAtDesc(id);

        try {
            byte[] pdfBytes = pdfExportService.generateReptileHistoryPdf(reptile, feedings2m, sheds2m, poops2m, medicals2m, alerts);
            String filename = reptile.getReptileCode() + "_" + reptile.getName().replaceAll("[^a-zA-Z0-9.-]", "_") + "_Clinical_History.pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/chat")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> chatWithReptileAi(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        String message = (payload != null) ? payload.getOrDefault("message", "") : "";
        String reply = reptileChatService.generateChatResponse(id, message);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "reply", reply,
                "timestamp", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        ));
    }
}
