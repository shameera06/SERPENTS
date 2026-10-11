package com.shareptiles.app.controller;

import com.shareptiles.app.model.*;
import com.shareptiles.app.repository.*;
import com.shareptiles.app.service.HealthAlertService;
import com.shareptiles.app.service.WeightMonitoringService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/feeding-records")
public class FeedingRecordController {

    @Autowired
    private FeedingRecordRepository recordRepository;

    @Autowired
    private ReptileRepository reptileRepository;

    @Autowired
    private FoodTypeRepository foodTypeRepository;

    @Autowired
    private FeedingScheduleRepository scheduleRepository;

    @Autowired
    private WeightMonitoringService weightMonitoringService;

    @Autowired
    private HealthAlertService healthAlertService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("records", recordRepository.findAll());
        return "admin/feeding-records/index";
    }

    @GetMapping("/create")
    public String create(@RequestParam(required = false) Long reptileId, Model model) {
        model.addAttribute("record", new FeedingRecord());
        model.addAttribute("reptiles", reptileRepository.findByIsArchivedFalseOrderByCreatedAtDesc());
        model.addAttribute("foodTypes", foodTypeRepository.findAll());
        model.addAttribute("schedules", scheduleRepository.findByStatus("active"));
        model.addAttribute("selectedReptileId", reptileId);
        return "admin/feeding-records/create";
    }

    @PostMapping
    public String store(@RequestParam Long reptileId,
                        @RequestParam(required = false) Long feedingScheduleId,
                        @RequestParam Long foodTypeId,
                        @RequestParam String feedingDate,
                        @RequestParam Double quantity,
                        @RequestParam String quantityUnit,
                        @RequestParam Double weightBefore,
                        @RequestParam Double weightAfter,
                        @RequestParam(required = false) Double weightAfterDefecation,
                        @RequestParam(required = false) String notes,
                        HttpSession session) {

        Reptile reptile = reptileRepository.findById(reptileId)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + reptileId));
        FoodType foodType = foodTypeRepository.findById(foodTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Food type not found: " + foodTypeId));

        FeedingRecord record = new FeedingRecord();
        record.setReptile(reptile);
        if (feedingScheduleId != null) {
            scheduleRepository.findById(feedingScheduleId).ifPresent(record::setFeedingSchedule);
        }
        record.setFoodType(foodType);
        record.setFeedingDate(LocalDateTime.parse(feedingDate));
        record.setQuantity(BigDecimal.valueOf(quantity));
        record.setQuantityUnit(quantityUnit);
        record.setWeightBefore(BigDecimal.valueOf(weightBefore));
        record.setWeightAfter(BigDecimal.valueOf(weightAfter));
        if (weightAfterDefecation != null) {
            record.setWeightAfterDefecation(BigDecimal.valueOf(weightAfterDefecation));
        }
        record.setNotes(notes);

        User user = (User) session.getAttribute("currentUser");
        record.setCreatedBy(user);

        recordRepository.save(record);

        // Recalculate reptile weight history and health alerts
        weightMonitoringService.recalculateReptileWeightHistory(reptileId);
        healthAlertService.syncWeightAlertsForReptile(reptileId);

        return "redirect:/admin/reptiles/" + reptileId;
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        FeedingRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + id));
        model.addAttribute("record", record);
        model.addAttribute("reptiles", reptileRepository.findByIsArchivedFalseOrderByCreatedAtDesc());
        model.addAttribute("foodTypes", foodTypeRepository.findAll());
        model.addAttribute("schedules", scheduleRepository.findByStatus("active"));
        return "admin/feeding-records/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam Long reptileId,
                         @RequestParam(required = false) Long feedingScheduleId,
                         @RequestParam Long foodTypeId,
                         @RequestParam String feedingDate,
                         @RequestParam Double quantity,
                         @RequestParam String quantityUnit,
                         @RequestParam Double weightBefore,
                         @RequestParam Double weightAfter,
                         @RequestParam(required = false) Double weightAfterDefecation,
                         @RequestParam(required = false) String notes) {

        FeedingRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + id));

        Reptile reptile = reptileRepository.findById(reptileId)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + reptileId));
        FoodType foodType = foodTypeRepository.findById(foodTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Food type not found: " + foodTypeId));

        record.setReptile(reptile);
        if (feedingScheduleId != null) {
            scheduleRepository.findById(feedingScheduleId).ifPresent(record::setFeedingSchedule);
        } else {
            record.setFeedingSchedule(null);
        }
        record.setFoodType(foodType);
        record.setFeedingDate(LocalDateTime.parse(feedingDate));
        record.setQuantity(BigDecimal.valueOf(quantity));
        record.setQuantityUnit(quantityUnit);
        record.setWeightBefore(BigDecimal.valueOf(weightBefore));
        record.setWeightAfter(BigDecimal.valueOf(weightAfter));
        if (weightAfterDefecation != null) {
            record.setWeightAfterDefecation(BigDecimal.valueOf(weightAfterDefecation));
        } else {
            record.setWeightAfterDefecation(null);
        }
        record.setNotes(notes);

        recordRepository.save(record);

        weightMonitoringService.recalculateReptileWeightHistory(reptileId);
        healthAlertService.syncWeightAlertsForReptile(reptileId);

        return "redirect:/admin/reptiles/" + reptileId;
    }
}
