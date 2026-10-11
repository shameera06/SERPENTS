package com.shareptiles.app.controller;

import com.shareptiles.app.model.FeedingRecord;
import com.shareptiles.app.model.HealthAlert;
import com.shareptiles.app.model.Reptile;
import com.shareptiles.app.repository.*;
import com.shareptiles.app.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ReptileRepository reptileRepository;

    @Autowired
    private FeedingRecordRepository feedingRecordRepository;

    @Autowired
    private HealthAlertRepository healthAlertRepository;

    @GetMapping("/")
    public String root() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> stats = dashboardService.getDashboardStats();
        model.addAllAttributes(stats);

        List<Reptile> recentReptiles = reptileRepository.findByIsArchivedFalseOrderByCreatedAtDesc();
        model.addAttribute("recentReptiles", recentReptiles.size() > 5 ? recentReptiles.subList(0, 5) : recentReptiles);

        List<HealthAlert> activeAlerts = healthAlertRepository.findByStatusOrderByTriggeredAtDesc("active");
        Set<Long> alertedReptileIds = new HashSet<>();
        List<HealthAlert> alertsByReptile = activeAlerts.stream()
                .filter(alert -> alertedReptileIds.add(alert.getReptile().getId()))
                .toList();
        model.addAttribute("activeAlertSnakeCount", alertsByReptile.size());
        model.addAttribute("activeAlertsList", alertsByReptile.size() > 5 ? alertsByReptile.subList(0, 5) : alertsByReptile);

        return "admin/dashboard";
    }
}
