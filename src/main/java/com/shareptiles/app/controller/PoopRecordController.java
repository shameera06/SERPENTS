package com.shareptiles.app.controller;

import com.shareptiles.app.model.PoopRecord;
import com.shareptiles.app.model.Reptile;
import com.shareptiles.app.model.User;
import com.shareptiles.app.repository.PoopRecordRepository;
import com.shareptiles.app.repository.ReptileRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/poop-records")
public class PoopRecordController {

    @Autowired
    private PoopRecordRepository recordRepository;

    @Autowired
    private ReptileRepository reptileRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("records", recordRepository.findAll());
        return "admin/poop-records/index";
    }

    @GetMapping("/create")
    public String create(@RequestParam(required = false) Long reptileId, Model model) {
        model.addAttribute("record", new PoopRecord());
        model.addAttribute("reptiles", reptileRepository.findByIsArchivedFalseOrderByCreatedAtDesc());
        model.addAttribute("selectedReptileId", reptileId);
        return "admin/poop-records/create";
    }

    @PostMapping
    public String store(@RequestParam Long reptileId,
                        @RequestParam String poopDate,
                        @RequestParam(defaultValue = "normal") String consistency,
                        @RequestParam(defaultValue = "normal_white") String uratesStatus,
                        @RequestParam(required = false) Double weightAfterPoop,
                        @RequestParam(required = false) String observations,
                        @RequestParam(required = false) String notes,
                        HttpSession session) {

        Reptile reptile = reptileRepository.findById(reptileId)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + reptileId));

        PoopRecord record = new PoopRecord();
        record.setReptile(reptile);
        record.setPoopDate(LocalDateTime.parse(poopDate));
        record.setConsistency(consistency);
        record.setUratesStatus(uratesStatus);
        if (weightAfterPoop != null) {
            record.setWeightAfterPoop(BigDecimal.valueOf(weightAfterPoop));
        }
        record.setObservations(observations);
        record.setNotes(notes);

        User user = (User) session.getAttribute("currentUser");
        record.setCreatedBy(user);

        recordRepository.save(record);
        return "redirect:/admin/reptiles/" + reptileId;
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        PoopRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + id));
        model.addAttribute("record", record);
        model.addAttribute("reptiles", reptileRepository.findByIsArchivedFalseOrderByCreatedAtDesc());
        return "admin/poop-records/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam Long reptileId,
                         @RequestParam String poopDate,
                         @RequestParam(defaultValue = "normal") String consistency,
                         @RequestParam(defaultValue = "normal_white") String uratesStatus,
                         @RequestParam(required = false) Double weightAfterPoop,
                         @RequestParam(required = false) String observations,
                         @RequestParam(required = false) String notes) {

        PoopRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + id));

        Reptile reptile = reptileRepository.findById(reptileId)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + reptileId));

        record.setReptile(reptile);
        record.setPoopDate(LocalDateTime.parse(poopDate));
        record.setConsistency(consistency);
        record.setUratesStatus(uratesStatus);
        if (weightAfterPoop != null) {
            record.setWeightAfterPoop(BigDecimal.valueOf(weightAfterPoop));
        } else {
            record.setWeightAfterPoop(null);
        }
        record.setObservations(observations);
        record.setNotes(notes);

        recordRepository.save(record);
        return "redirect:/admin/reptiles/" + reptileId;
    }
}
