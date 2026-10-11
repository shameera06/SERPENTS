package com.shareptiles.app.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.shareptiles.app.model.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
public class PdfExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    /**
     * Converts a local file path or static asset path into a Base64 data URI
     * for seamless offline rendering in OpenHtmlToPdf.
     */
    public String convertImageToBase64(String relativeOrAbsolutePath) {
        if (relativeOrAbsolutePath == null || relativeOrAbsolutePath.isBlank()) {
            return null;
        }
        try {
            String clean = relativeOrAbsolutePath.trim().replaceFirst("^/", "");
            File file = new File(clean);
            if (!file.exists()) {
                file = new File(System.getProperty("user.dir"), clean);
            }
            if (!file.exists()) {
                file = new File(System.getProperty("user.dir"), "uploads/" + clean);
            }
            if (!file.exists()) {
                file = new File(System.getProperty("user.dir"), "src/main/resources/static/" + clean);
            }
            if (!file.exists()) {
                file = new File(System.getProperty("user.dir"), "target/classes/static/" + clean);
            }

            if (file.exists() && file.isFile() && file.length() > 0) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String mime = "image/jpeg";
                String nameLower = file.getName().toLowerCase();
                if (nameLower.endsWith(".png")) mime = "image/png";
                else if (nameLower.endsWith(".gif")) mime = "image/gif";
                else if (nameLower.endsWith(".svg")) mime = "image/svg+xml";

                String b64 = Base64.getEncoder().encodeToString(bytes);
                return "data:" + mime + ";base64," + b64;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Generates a high-resolution PDF Dossier of the Reptile Specimen,
     * embedding the actual photo of the species/reptile.
     */
    public byte[] generateReptileHistoryPdf(Reptile reptile,
                                           List<FeedingRecord> feedings,
                                           List<SheddingRecord> sheddings,
                                           List<PoopRecord> poops,
                                           List<MedicalRecord> medicals,
                                           List<HealthAlert> alerts) throws Exception {

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/>");
        html.append("<style>");
        html.append("body { font-family: 'Helvetica', Arial, sans-serif; font-size: 11px; color: #1e293b; margin: 25px; line-height: 1.4; }");
        html.append(".header { border-bottom: 2.5px solid #047857; padding-bottom: 12px; margin-bottom: 18px; }");
        html.append(".title { font-size: 20px; font-weight: bold; color: #047857; margin: 0; letter-spacing: 0.5px; }");
        html.append(".subtitle { font-size: 10.5px; color: #64748b; margin-top: 3px; }");
        html.append(".meta-box { background: #f8fafc; border: 1px solid #cbd5e1; border-radius: 8px; padding: 12px; margin-bottom: 16px; }");
        html.append(".meta-table { width: 100%; border-collapse: collapse; }");
        html.append(".meta-table td { padding: 4px 6px; font-size: 11px; }");
        html.append(".label { color: #64748b; font-weight: bold; width: 22%; }");
        html.append(".val { font-weight: bold; color: #0f172a; }");
        html.append(".specimen-img { width: 120px; height: 120px; object-fit: cover; border-radius: 6px; border: 2px solid #059669; }");
        html.append(".section-head { font-size: 12.5px; font-weight: bold; color: #064e3b; border-bottom: 1.5px solid #cbd5e1; padding-bottom: 4px; margin-top: 18px; margin-bottom: 8px; }");
        html.append(".data-table { width: 100%; border-collapse: collapse; margin-bottom: 14px; }");
        html.append(".data-table th { background: #f1f5f9; color: #334155; font-weight: bold; font-size: 10px; text-transform: uppercase; padding: 6px 8px; border: 1px solid #cbd5e1; text-align: left; }");
        html.append(".data-table td { padding: 6px 8px; border: 1px solid #e2e8f0; font-size: 10.5px; }");
        html.append(".badge-green { color: #15803d; font-weight: bold; }");
        html.append(".badge-red { color: #b91c1c; font-weight: bold; }");
        html.append(".badge-amber { color: #b45309; font-weight: bold; }");
        html.append(".footer { border-top: 1px solid #cbd5e1; margin-top: 24px; padding-top: 8px; font-size: 9px; color: #94a3b8; text-align: center; }");
        html.append("</style></head><body>");

        // Header with SERPENTS Branding
        html.append("<div class=\"header\">");
        html.append("<table style=\"width:100%;\"><tr>");
        html.append("<td><div class=\"title\">SERPENTS CLINICAL HUSBANDRY DOSSIER</div><div class=\"subtitle\">Official Certified Specimen Clinical &amp; Growth History (Last 2 Months)</div></td>");
        html.append("<td style=\"text-align:right;\"><div style=\"font-size:15px; font-weight:bold; color:#047857;\">").append(escape(reptile.getReptileCode())).append("</div><div class=\"subtitle\">Generated: ").append(LocalDate.now().format(DATE_FMT)).append("</div></td>");
        html.append("</tr></table></div>");

        // Specimen Profile Info with Embedded Photo
        String specimenPhotoB64 = convertImageToBase64(reptile.getPhoto());

        html.append("<div class=\"meta-box\">");
        html.append("<table style=\"width:100%; border-collapse:collapse;\"><tr>");

        if (specimenPhotoB64 != null) {
            html.append("<td style=\"width: 130px; vertical-align: top; padding-right: 14px;\">");
            html.append("<img src=\"").append(specimenPhotoB64).append("\" class=\"specimen-img\" alt=\"Specimen Photo\" />");
            html.append("</td>");
        }

        html.append("<td style=\"vertical-align: top;\">");
        html.append("<table class=\"meta-table\">");
        html.append("<tr>");
        html.append("<td class=\"label\">Specimen Name:</td><td class=\"val\">").append(escape(reptile.getName())).append("</td>");
        html.append("<td class=\"label\">Species:</td><td class=\"val\">").append(escape(reptile.getSpecies() != null ? reptile.getSpecies().getName() : "N/A")).append("</td>");
        html.append("</tr><tr>");
        html.append("<td class=\"label\">Morph / Variety:</td><td class=\"val\">").append(escape(reptile.getDisplayVarietyName())).append("</td>");
        html.append("<td class=\"label\">Sex:</td><td class=\"val\">").append(escape(reptile.getSex() != null ? reptile.getSex().toUpperCase() : "UNKNOWN")).append("</td>");
        html.append("</tr><tr>");
        html.append("<td class=\"label\">Current Weight:</td><td class=\"val\">").append(reptile.getCurrentWeight() != null ? reptile.getCurrentWeight() + " " + reptile.getWeightUnit() : "N/A").append("</td>");
        html.append("<td class=\"label\">Length:</td><td class=\"val\">").append(reptile.getLength() != null ? reptile.getLength() + " cm" : "N/A").append("</td>");
        html.append("</tr><tr>");
        html.append("<td class=\"label\">Sanctuary Source:</td><td class=\"val\">").append(escape(reptile.getAcquisitionSource() != null ? reptile.getAcquisitionSource() : "SERPENTS Captive Breeding")).append("</td>");
        html.append("<td class=\"label\">Clinical Status:</td><td class=\"val\" style=\"color:#047857;\">").append(escape(reptile.getStatus() != null ? reptile.getStatus().toUpperCase() : "ACTIVE")).append("</td>");
        html.append("</tr></table>");
        html.append("</td></tr></table></div>");

        // Section 1: Feeding History
        html.append("<div class=\"section-head\">1. Feeding &amp; Weight Trajectory History (Last 2 Months)</div>");
        html.append("<table class=\"data-table\"><thead><tr>");
        html.append("<th>Date</th><th>Food Item</th><th>Quantity</th><th>Weight Before</th><th>Weight After</th><th>Delta</th><th>Trend</th>");
        html.append("</tr></thead><tbody>");
        if (feedings.isEmpty()) {
            html.append("<tr><td colspan=\"7\" style=\"text-align:center; color:#94a3b8;\">No feedings recorded in the last 2 months.</td></tr>");
        } else {
            for (FeedingRecord f : feedings) {
                html.append("<tr>");
                html.append("<td>").append(f.getFeedingDate() != null ? f.getFeedingDate().format(DATE_FMT) : "—").append("</td>");
                html.append("<td>").append(escape(f.getFoodType() != null ? f.getFoodType().getName() : "Prey Item")).append("</td>");
                html.append("<td>").append(f.getQuantity()).append(" ").append(escape(f.getQuantityUnit())).append("</td>");
                html.append("<td>").append(f.getWeightBefore() != null ? f.getWeightBefore() + "g" : "—").append("</td>");
                html.append("<td>").append(f.getWeightAfter() != null ? f.getWeightAfter() + "g" : "—").append("</td>");
                html.append("<td>");
                if (f.getWeightChange() != null) {
                    if (f.getWeightChange().doubleValue() > 0) {
                        html.append("<span class=\"badge-green\">+").append(f.getWeightChange()).append("g</span>");
                    } else if (f.getWeightChange().doubleValue() < 0) {
                        html.append("<span class=\"badge-red\">").append(f.getWeightChange()).append("g</span>");
                    } else {
                        html.append("<span class=\"badge-amber\">0.0g</span>");
                    }
                } else {
                    html.append("—");
                }
                html.append("</td>");
                html.append("<td>").append(escape(f.getWeightTrend() != null ? f.getWeightTrend().toUpperCase() : "NORMAL")).append("</td>");
                html.append("</tr>");
            }
        }
        html.append("</tbody></table>");

        // Section 2: Shedding History
        html.append("<div class=\"section-head\">2. Ecdysis / Shedding Records (Last 2 Months)</div>");
        html.append("<table class=\"data-table\"><thead><tr>");
        html.append("<th>Shed Date</th><th>Status</th><th>Observations / Notes</th>");
        html.append("</tr></thead><tbody>");
        if (sheddings.isEmpty()) {
            html.append("<tr><td colspan=\"3\" style=\"text-align:center; color:#94a3b8;\">No shedding events recorded in the last 2 months.</td></tr>");
        } else {
            for (SheddingRecord s : sheddings) {
                html.append("<tr>");
                html.append("<td>").append(s.getSheddingDate() != null ? s.getSheddingDate().format(DATE_FMT) : "—").append("</td>");
                html.append("<td>").append(escape(s.getStatus() != null ? s.getStatus().toUpperCase() : "COMPLETE")).append("</td>");
                html.append("<td>").append(escape(s.getNotes() != null ? s.getNotes() : (s.getObservations() != null ? s.getObservations() : "Normal shedding cycle"))).append("</td>");
                html.append("</tr>");
            }
        }
        html.append("</tbody></table>");

        // Section 3: Defecation History
        html.append("<div class=\"section-head\">3. Defecation &amp; Digestive Records (Last 2 Months)</div>");
        html.append("<table class=\"data-table\"><thead><tr>");
        html.append("<th>Date Observed</th><th>Consistency</th><th>Urates Status</th><th>Observations</th>");
        html.append("</tr></thead><tbody>");
        if (poops.isEmpty()) {
            html.append("<tr><td colspan=\"4\" style=\"text-align:center; color:#94a3b8;\">No defecation records logged in the last 2 months.</td></tr>");
        } else {
            for (PoopRecord p : poops) {
                html.append("<tr>");
                html.append("<td>").append(p.getPoopDate() != null ? p.getPoopDate().format(DATE_FMT) : "—").append("</td>");
                html.append("<td>").append(escape(p.getConsistency() != null ? p.getConsistency().toUpperCase() : "NORMAL")).append("</td>");
                html.append("<td>").append(escape(p.getUratesStatus() != null ? p.getUratesStatus().toUpperCase() : "NORMAL_WHITE")).append("</td>");
                html.append("<td>").append(escape(p.getNotes() != null ? p.getNotes() : (p.getObservations() != null ? p.getObservations() : "Normal digestion"))).append("</td>");
                html.append("</tr>");
            }
        }
        html.append("</tbody></table>");

        // Section 4: Veterinary & Health Alerts
        if (!alerts.isEmpty()) {
            html.append("<div class=\"section-head\" style=\"color:#b91c1c;\">4. Active Health Warnings &amp; Clinical Alerts</div>");
            html.append("<table class=\"data-table\"><thead><tr>");
            html.append("<th>Trigger Date</th><th>Alert Type</th><th>Severity</th><th>Alert Details</th>");
            html.append("</tr></thead><tbody>");
            for (HealthAlert a : alerts) {
                html.append("<tr>");
                html.append("<td>").append(a.getTriggeredAt() != null ? a.getTriggeredAt().toLocalDate().format(DATE_FMT) : "—").append("</td>");
                html.append("<td>").append(escape(a.getAlertType())).append("</td>");
                html.append("<td><span class=\"badge-red\">").append(escape(a.getSeverity() != null ? a.getSeverity().toUpperCase() : "CRITICAL")).append("</span></td>");
                html.append("<td>").append(escape(a.getMessage() != null ? a.getMessage() : a.getTitle())).append("</td>");
                html.append("</tr>");
            }
            html.append("</tbody></table>");
        }

        // Footer
        html.append("<div class=\"footer\">");
        html.append("SERPENTS Diagnostic Husbandry System &#8226; Certified Specimen Clinical Export &#8226; Page 1 of 1");
        html.append("</div>");

        html.append("</body></html>");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html.toString(), "");
        builder.toStream(os);
        builder.run();

        return os.toByteArray();
    }

    /**
     * Generates a comprehensive CNN Disease Diagnostic Report PDF,
     * including the uploaded scan photo and Grad-CAM neural attention heatmap.
     */
    public byte[] generateDiseasePredictionPdf(DiseasePrediction prediction) throws Exception {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/>");
        html.append("<style>");
        html.append("body { font-family: 'Helvetica', Arial, sans-serif; font-size: 11px; color: #1e293b; margin: 25px; line-height: 1.45; }");
        html.append(".header { border-bottom: 2.5px solid #047857; padding-bottom: 12px; margin-bottom: 16px; }");
        html.append(".title { font-size: 19px; font-weight: bold; color: #047857; margin: 0; }");
        html.append(".subtitle { font-size: 10.5px; color: #64748b; margin-top: 3px; }");
        html.append(".diag-banner { background: #f0fdf4; border: 1.5px solid #10b981; border-radius: 8px; padding: 12px 16px; margin-bottom: 16px; }");
        html.append(".diag-title { font-size: 16px; font-weight: bold; color: #064e3b; margin-bottom: 4px; }");
        html.append(".photo-table { width: 100%; border-collapse: collapse; margin-bottom: 16px; }");
        html.append(".photo-card { border: 1px solid #cbd5e1; border-radius: 8px; padding: 8px; text-align: center; background: #f8fafc; }");
        html.append(".photo-card img { max-width: 100%; height: 160px; object-fit: contain; border-radius: 4px; }");
        html.append(".photo-caption { font-size: 10px; font-weight: bold; color: #334155; margin-top: 6px; }");
        html.append(".section-head { font-size: 12.5px; font-weight: bold; color: #064e3b; border-bottom: 1.5px solid #cbd5e1; padding-bottom: 4px; margin-top: 14px; margin-bottom: 8px; }");
        html.append(".item-box { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 10px; margin-bottom: 10px; font-size: 10.5px; }");
        html.append(".footer { border-top: 1px solid #cbd5e1; margin-top: 22px; padding-top: 8px; font-size: 9px; color: #94a3b8; text-align: center; }");
        html.append("</style></head><body>");

        // Header
        html.append("<div class=\"header\">");
        html.append("<table style=\"width:100%;\"><tr>");
        html.append("<td><div class=\"title\">SERPENTS AI PATHOLOGY &amp; DIAGNOSTIC REPORT</div><div class=\"subtitle\">Convolutional Neural Network (CNN) Computer Vision Pathology Analysis</div></td>");
        html.append("<td style=\"text-align:right;\"><div style=\"font-size:15px; font-weight:bold; color:#047857;\">SCAN #").append(prediction.getId()).append("</div><div class=\"subtitle\">Generated: ").append(LocalDate.now().format(DATE_FMT)).append("</div></td>");
        html.append("</tr></table></div>");

        // Primary Diagnostic Banner
        html.append("<div class=\"diag-banner\">");
        html.append("<table style=\"width:100%;\"><tr>");
        html.append("<td>");
        html.append("<div class=\"diag-title\">").append(escape(prediction.getExactMedicalDiagnosis())).append("</div>");
        html.append("<div style=\"color:#334155; font-size:11px;\">Primary Condition: <strong>").append(escape(prediction.getPredictedDisease())).append("</strong></div>");
        html.append("</td>");
        html.append("<td style=\"text-align:right; width:140px;\">");
        html.append("<div style=\"font-size:20px; font-weight:bold; color:#047857;\">").append(prediction.getConfidence()).append("%</div>");
        html.append("<div style=\"font-size:9.5px; color:#64748b;\">CNN Confidence Rating</div>");
        html.append("<div style=\"font-size:10px; font-weight:bold; margin-top:2px; color:").append("Critical".equalsIgnoreCase(prediction.getSeverity()) ? "#b91c1c" : "#b45309").append(";\">Severity: ").append(escape(prediction.getSeverity())).append("</div>");
        html.append("</td>");
        html.append("</tr></table></div>");

        // Photographic Evidence (Uploaded Photo + Grad-CAM Heatmap)
        String uploadedPhotoB64 = convertImageToBase64(prediction.getImagePath());
        String heatmapPhotoB64 = (prediction.getHeatmapPath() != null) ? convertImageToBase64(prediction.getHeatmapPath()) : null;

        html.append("<table class=\"photo-table\"><tr>");
        if (uploadedPhotoB64 != null) {
            html.append("<td style=\"width:50%; padding-right:6px;\">");
            html.append("<div class=\"photo-card\">");
            html.append("<img src=\"").append(uploadedPhotoB64).append("\" alt=\"Original Uploaded Scan\" />");
            html.append("<div class=\"photo-caption\">Original Uploaded Specimen Photo</div>");
            html.append("</div></td>");
        }
        if (heatmapPhotoB64 != null) {
            html.append("<td style=\"width:50%; padding-left:6px;\">");
            html.append("<div class=\"photo-card\">");
            html.append("<img src=\"").append(heatmapPhotoB64).append("\" alt=\"Grad-CAM Heatmap\" />");
            html.append("<div class=\"photo-caption\">Neural Grad-CAM Attention Heatmap</div>");
            html.append("</div></td>");
        }
        html.append("</tr></table>");

        // Section 1: Detailed Clinical Pathology
        html.append("<div class=\"section-head\">1. Visual Pathology &amp; Morphological Identification</div>");
        html.append("<div class=\"item-box\">");
        html.append(escape(prediction.getDetailedPathology()));
        html.append("</div>");

        // Section 2: Detected Symptoms
        List<String> symptoms = prediction.getSymptomsDetected();
        if (!symptoms.isEmpty()) {
            html.append("<div class=\"section-head\">2. Pathological Signs Detected in Photographic Scan</div>");
            html.append("<div class=\"item-box\"><ul style=\"margin:0; padding-left:18px;\">");
            for (String s : symptoms) {
                html.append("<li style=\"margin-bottom:3px;\">").append(escape(s)).append("</li>");
            }
            html.append("</ul></div>");
        }

        // Section 3: Environmental Corrections
        html.append("<div class=\"section-head\">3. Environmental Husbandry Prescription (Temperature &amp; Humidity)</div>");
        html.append("<div class=\"item-box\" style=\"border-left: 3px solid #047857;\">");
        html.append(escape(prediction.getEnvironmentalRx()));
        html.append("</div>");

        // Section 4: First Aid Protocol & Clinical Recommendations
        html.append("<div class=\"section-head\">4. Immediate Quarantine &amp; First-Aid Clinical Protocol</div>");
        html.append("<div class=\"item-box\">");
        List<String> firstAid = prediction.getFirstAidChecklist();
        if (!firstAid.isEmpty()) {
            html.append("<ul style=\"margin:0; padding-left:18px;\">");
            for (String step : firstAid) {
                html.append("<li style=\"margin-bottom:4px;\">").append(escape(step)).append("</li>");
            }
            html.append("</ul>");
        } else if (prediction.getTreatmentRecommendations() != null && !prediction.getTreatmentRecommendations().isBlank()) {
            html.append("<p style=\"margin:0;\">").append(escape(prediction.getTreatmentRecommendations())).append("</p>");
        }
        html.append("</div>");

        // Footer
        html.append("<div class=\"footer\">");
        html.append("SERPENTS Diagnostic AI &#8226; Automated Deep Learning Pathology Report &#8226; Page 1 of 1");
        html.append("</div>");

        html.append("</body></html>");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html.toString(), "");
        builder.toStream(os);
        builder.run();

        return os.toByteArray();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
