package com.shareptiles.app.controller;

import com.shareptiles.app.model.Reptile;
import com.shareptiles.app.model.SheddingRecord;
import com.shareptiles.app.model.User;
import com.shareptiles.app.repository.ReptileRepository;
import com.shareptiles.app.repository.SheddingRecordRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/shedding-records")
public class SheddingRecordController {

    @Autowired
    private SheddingRecordRepository recordRepository;

    @Autowired
    private ReptileRepository reptileRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("records", recordRepository.findAll());
        return "admin/shedding-records/index";
    }

    @GetMapping("/create")
    public String create(@RequestParam(required = false) Long reptileId, Model model) {
        model.addAttribute("record", new SheddingRecord());
        model.addAttribute("reptiles", reptileRepository.findByIsArchivedFalseOrderByCreatedAtDesc());
        model.addAttribute("selectedReptileId", reptileId);
        return "admin/shedding-records/create";
    }

    @PostMapping
    public String store(@RequestParam Long reptileId,
                        @RequestParam String sheddingDate,
                        @RequestParam String status,
                        @RequestParam(required = false) String observations,
                        @RequestParam(required = false) String notes,
                        HttpSession session) {

        Reptile reptile = reptileRepository.findById(reptileId)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + reptileId));

        SheddingRecord record = new SheddingRecord();
        record.setReptile(reptile);
        record.setSheddingDate(LocalDate.parse(sheddingDate));
        record.setStatus(status);
        record.setObservations(observations);
        record.setNotes(notes);

        User user = (User) session.getAttribute("currentUser");
        record.setCreatedBy(user);

        recordRepository.save(record);
        return "redirect:/admin/reptiles/" + reptileId;
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        SheddingRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + id));
        model.addAttribute("record", record);
        model.addAttribute("reptiles", reptileRepository.findByIsArchivedFalseOrderByCreatedAtDesc());
        return "admin/shedding-records/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam Long reptileId,
                         @RequestParam String sheddingDate,
                         @RequestParam String status,
                         @RequestParam(required = false) String observations,
                         @RequestParam(required = false) String notes) {

        SheddingRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + id));

        Reptile reptile = reptileRepository.findById(reptileId)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + reptileId));

        record.setReptile(reptile);
        record.setSheddingDate(LocalDate.parse(sheddingDate));
        record.setStatus(status);
        record.setObservations(observations);
        record.setNotes(notes);

        recordRepository.save(record);
        return "redirect:/admin/reptiles/" + reptileId;
    }
}
