package com.shareptiles.app.controller;

import com.shareptiles.app.model.MedicalRecord;
import com.shareptiles.app.model.Reptile;
import com.shareptiles.app.model.User;
import com.shareptiles.app.repository.MedicalRecordRepository;
import com.shareptiles.app.repository.ReptileRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/admin/medical-records")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordRepository recordRepository;

    @Autowired
    private ReptileRepository reptileRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("records", recordRepository.findAll());
        return "admin/medical-records/index";
    }

    @GetMapping("/create")
    public String create(@RequestParam(required = false) Long reptileId, Model model) {
        model.addAttribute("record", new MedicalRecord());
        model.addAttribute("reptiles", reptileRepository.findByIsArchivedFalseOrderByCreatedAtDesc());
        model.addAttribute("selectedReptileId", reptileId);
        return "admin/medical-records/create";
    }

    @PostMapping
    public String store(@RequestParam Long reptileId,
                        @RequestParam String treatmentDate,
                        @RequestParam String symptomsOrCondition,
                        @RequestParam(required = false) String medicineName,
                        @RequestParam(required = false) String dosage,
                        @RequestParam(required = false) String treatmentDetails,
                        @RequestParam(required = false) String veterinarianName,
                        @RequestParam(required = false) String veterinarianContact,
                        @RequestParam(required = false) String notes,
                        @RequestParam(required = false) MultipartFile prescriptionFile,
                        HttpSession session) throws Exception {

        Reptile reptile = reptileRepository.findById(reptileId)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + reptileId));

        MedicalRecord record = new MedicalRecord();
        record.setReptile(reptile);
        record.setTreatmentDate(LocalDate.parse(treatmentDate));
        record.setSymptomsOrCondition(symptomsOrCondition);
        record.setMedicineName(medicineName);
        record.setDosage(dosage);
        record.setTreatmentDetails(treatmentDetails);
        record.setVeterinarianName(veterinarianName);
        record.setVeterinarianContact(veterinarianContact);
        record.setNotes(notes);

        if (prescriptionFile != null && !prescriptionFile.isEmpty()) {
            String path = com.shareptiles.app.util.FileUploadHelper.saveUploadedFile(prescriptionFile, uploadDir, "prescriptions");
            record.setPrescriptionPath(path);
        }

        User user = (User) session.getAttribute("currentUser");
        record.setCreatedBy(user);

        recordRepository.save(record);
        return "redirect:/admin/reptiles/" + reptileId;
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        MedicalRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + id));
        model.addAttribute("record", record);
        return "admin/medical-records/show";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        MedicalRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + id));
        model.addAttribute("record", record);
        model.addAttribute("reptiles", reptileRepository.findByIsArchivedFalseOrderByCreatedAtDesc());
        return "admin/medical-records/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam Long reptileId,
                         @RequestParam String treatmentDate,
                         @RequestParam String symptomsOrCondition,
                         @RequestParam(required = false) String medicineName,
                         @RequestParam(required = false) String dosage,
                         @RequestParam(required = false) String treatmentDetails,
                         @RequestParam(required = false) String veterinarianName,
                         @RequestParam(required = false) String veterinarianContact,
                         @RequestParam(required = false) String notes) {

        MedicalRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + id));

        Reptile reptile = reptileRepository.findById(reptileId)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + reptileId));

        record.setReptile(reptile);
        record.setTreatmentDate(LocalDate.parse(treatmentDate));
        record.setSymptomsOrCondition(symptomsOrCondition);
        record.setMedicineName(medicineName);
        record.setDosage(dosage);
        record.setTreatmentDetails(treatmentDetails);
        record.setVeterinarianName(veterinarianName);
        record.setVeterinarianContact(veterinarianContact);
        record.setNotes(notes);

        recordRepository.save(record);
        return "redirect:/admin/reptiles/" + reptileId;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user != null && user.isAdmin()) {
            recordRepository.deleteById(id);
        }
        return "redirect:/admin/medical-records";
    }
}
