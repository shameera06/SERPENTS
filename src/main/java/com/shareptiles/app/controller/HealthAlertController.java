package com.shareptiles.app.controller;

import com.shareptiles.app.model.HealthAlert;
import com.shareptiles.app.model.User;
import com.shareptiles.app.repository.HealthAlertRepository;
import com.shareptiles.app.service.HealthAlertService;
import com.shareptiles.app.service.EmailNotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Controller
@RequestMapping("/admin/health-alerts")
public class HealthAlertController {

    @Autowired
    private HealthAlertRepository alertRepository;

    @Autowired
    private HealthAlertService healthAlertService;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @GetMapping
    public String index(@RequestParam(required = false) String status, Model model) {
        if (status != null && !status.isBlank()) {
            model.addAttribute("alerts", alertRepository.findByStatusOrderByTriggeredAtDesc(status));
            model.addAttribute("currentStatus", status);
        } else {
            model.addAttribute("alerts", alertRepository.findAllByOrderByTriggeredAtDesc());
            model.addAttribute("currentStatus", "all");
        }
        return "admin/health-alerts/index";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        HealthAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Health alert not found: " + id));
        model.addAttribute("alert", alert);
        return "admin/health-alerts/show";
    }

    @PostMapping("/{id}/resolve")
    public String resolve(@PathVariable Long id,
                          @RequestParam(required = false) String notes,
                          HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user != null && user.isAdmin()) {
            healthAlertService.resolveAlert(id, user, notes);
        }
        return "redirect:/admin/health-alerts/" + id;
    }

    @PostMapping("/{id}/dismiss")
    public String dismiss(@PathVariable Long id,
                          @RequestParam(required = false) String notes,
                          HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user != null && user.isAdmin()) {
            healthAlertService.dismissAlert(id, user, notes);
        }
        return "redirect:/admin/health-alerts/" + id;
    }

    @Autowired
    private EmailNotificationService emailNotificationService;

    @PostMapping("/{id}/evidence")
    public String uploadEvidence(@PathVariable Long id,
                                 @RequestParam(required = false) String notes,
                                 @RequestParam(required = false) MultipartFile evidenceFile) throws Exception {

        String evidencePath = null;
        if (evidenceFile != null && !evidenceFile.isEmpty()) {
            evidencePath = com.shareptiles.app.util.FileUploadHelper.saveUploadedFile(evidenceFile, uploadDir, "evidence");
        }

        healthAlertService.updateEvidence(id, evidencePath, notes);
        return "redirect:/admin/health-alerts/" + id;
    }

    @PostMapping("/{id}/send-email")
    public String sendEmail(@PathVariable Long id) {
        HealthAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Health alert not found: " + id));
        emailNotificationService.sendHealthAlertNotification(alert);
        return "redirect:/admin/health-alerts/" + id + "?emailSent=true";
    }
}
