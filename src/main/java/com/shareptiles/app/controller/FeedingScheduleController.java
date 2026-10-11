package com.shareptiles.app.controller;

import com.shareptiles.app.model.*;
import com.shareptiles.app.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin/feeding-schedules")
public class FeedingScheduleController {

    @Autowired
    private FeedingScheduleRepository scheduleRepository;

    @Autowired
    private FeedingScheduleAdjustmentRepository adjustmentRepository;

    @Autowired
    private ReptileRepository reptileRepository;

    @Autowired
    private FoodTypeRepository foodTypeRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("schedules", scheduleRepository.findAll());
        return "admin/feeding-schedules/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("schedule", new FeedingSchedule());
        model.addAttribute("reptiles", reptileRepository.findByIsArchivedFalseOrderByCreatedAtDesc());
        model.addAttribute("foodTypes", foodTypeRepository.findAll());
        return "admin/feeding-schedules/create";
    }

    @PostMapping
    public String store(@RequestParam Long reptileId,
                        @RequestParam Long foodTypeId,
                        @RequestParam Double quantity,
                        @RequestParam(defaultValue = "piece") String quantityUnit,
                        @RequestParam Integer intervalDays,
                        @RequestParam String startDate,
                        @RequestParam(required = false) String nextDueAt,
                        @RequestParam(required = false) String notes) {

        Reptile reptile = reptileRepository.findById(reptileId)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + reptileId));
        FoodType foodType = foodTypeRepository.findById(foodTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Food type not found: " + foodTypeId));

        FeedingSchedule schedule = new FeedingSchedule();
        schedule.setReptile(reptile);
        schedule.setFoodType(foodType);
        schedule.setQuantity(BigDecimal.valueOf(quantity));
        schedule.setQuantityUnit(quantityUnit);
        schedule.setIntervalDays(intervalDays);

        LocalDate sDate = LocalDate.parse(startDate);
        schedule.setStartDate(sDate);

        LocalDateTime firstDue = sDate.atTime(12, 0);
        schedule.setFirstDueAt(firstDue);

        if (nextDueAt != null && !nextDueAt.isBlank()) {
            schedule.setNextDueAt(LocalDateTime.parse(nextDueAt));
        } else {
            schedule.setNextDueAt(firstDue.plusDays(intervalDays));
        }

        schedule.setStatus("active");
        schedule.setNotes(notes);

        scheduleRepository.save(schedule);
        return "redirect:/admin/feeding-schedules";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        FeedingSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + id));
        model.addAttribute("schedule", schedule);
        model.addAttribute("reptiles", reptileRepository.findByIsArchivedFalseOrderByCreatedAtDesc());
        model.addAttribute("foodTypes", foodTypeRepository.findAll());
        return "admin/feeding-schedules/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam Long reptileId,
                         @RequestParam Long foodTypeId,
                         @RequestParam Double quantity,
                         @RequestParam String quantityUnit,
                         @RequestParam Integer intervalDays,
                         @RequestParam String status,
                         @RequestParam(required = false) String notes) {

        FeedingSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + id));

        Reptile reptile = reptileRepository.findById(reptileId)
                .orElseThrow(() -> new IllegalArgumentException("Reptile not found: " + reptileId));
        FoodType foodType = foodTypeRepository.findById(foodTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Food type not found: " + foodTypeId));

        schedule.setReptile(reptile);
        schedule.setFoodType(foodType);
        schedule.setQuantity(BigDecimal.valueOf(quantity));
        schedule.setQuantityUnit(quantityUnit);
        schedule.setIntervalDays(intervalDays);
        schedule.setStatus(status);
        schedule.setNotes(notes);

        scheduleRepository.save(schedule);
        return "redirect:/admin/feeding-schedules";
    }

    @GetMapping("/{id}/adjust-next-feeding")
    public String adjustNextFeeding(@PathVariable Long id, Model model) {
        FeedingSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + id));
        List<FeedingScheduleAdjustment> history = adjustmentRepository.findByFeedingScheduleIdOrderByAdjustedAtDesc(id);

        model.addAttribute("schedule", schedule);
        model.addAttribute("history", history);
        return "admin/feeding-schedules/adjust-next-feeding";
    }

    @PostMapping("/{id}/adjust-next-feeding")
    public String storeAdjustment(@PathVariable Long id,
                                  @RequestParam String adjustedNextDueAt,
                                  @RequestParam String reason,
                                  @RequestParam(required = false) String notes,
                                  HttpSession session) {

        FeedingSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + id));

        User user = (User) session.getAttribute("currentUser");

        LocalDateTime orig = schedule.getNextDueAt();
        LocalDateTime adj = LocalDateTime.parse(adjustedNextDueAt);

        FeedingScheduleAdjustment record = new FeedingScheduleAdjustment();
        record.setFeedingSchedule(schedule);
        record.setReptile(schedule.getReptile());
        record.setOriginalNextDueAt(orig);
        record.setAdjustedNextDueAt(adj);
        record.setReason(reason);
        record.setNotes(notes);
        record.setAdjustedBy(user);
        record.setAdjustedAt(LocalDateTime.now());
        adjustmentRepository.save(record);

        schedule.setNextDueAt(adj);
        scheduleRepository.save(schedule);

        return "redirect:/admin/feeding-schedules/" + id + "/adjust-next-feeding";
    }
}
