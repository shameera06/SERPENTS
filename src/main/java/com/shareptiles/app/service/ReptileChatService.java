package com.shareptiles.app.service;

import com.shareptiles.app.model.*;
import com.shareptiles.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ReptileChatService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Autowired
    private ReptileRepository reptileRepository;

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

    public String generateChatResponse(Long reptileId, String userMessage) {
        Reptile reptile = reptileRepository.findById(reptileId).orElse(null);
        if (reptile == null) {
            return "Specimen profile not found. Please select a valid reptile from your catalog.";
        }

        String msg = (userMessage != null) ? userMessage.toLowerCase(Locale.ROOT).trim() : "";
        String speciesName = (reptile.getSpecies() != null) ? reptile.getSpecies().getName() : "Snake";
        String varietyName = reptile.getDisplayVarietyName();
        String snakeName = reptile.getName();
        String code = reptile.getReptileCode();
        BigDecimal weight = reptile.getCurrentWeight();
        String weightStr = (weight != null) ? (weight + " " + reptile.getWeightUnit()) : "Not recorded";

        // Fetch recent context
        List<FeedingRecord> feedings = feedingRecordRepository.findByReptileIdOrderByFeedingDateDescIdDesc(reptileId);
        List<SheddingRecord> sheds = sheddingRecordRepository.findByReptileIdOrderBySheddingDateDescIdDesc(reptileId);
        List<PoopRecord> poops = poopRecordRepository.findByReptileIdOrderByPoopDateDescIdDesc(reptileId);
        List<HealthAlert> alerts = healthAlertRepository.findByReptileIdOrderByTriggeredAtDesc(reptileId);
        List<MedicalRecord> medicals = medicalRecordRepository.findByReptileIdOrderByTreatmentDateDescIdDesc(reptileId);

        StringBuilder sb = new StringBuilder();

        // 1. Temperature & Humidity / Enclosure Specs
        if (msg.contains("temp") || msg.contains("humidity") || msg.contains("habitat") ||
                msg.contains("enclosure") || msg.contains("tank") || msg.contains("basking") ||
                msg.contains("heat") || msg.contains("environment")) {

            sb.append("🌡️ **Husbandry & Habitat Parameters for ").append(snakeName).append(" (").append(speciesName).append("):**\n\n");
            if (speciesName.toLowerCase().contains("python")) {
                sb.append("• **Basking / Warm Hide**: 88°F - 92°F (31°C - 33°C)\n");
                sb.append("• **Cool End**: 78°F - 80°F (25°C - 27°C)\n");
                sb.append("• **Ambient Humidity**: 60% - 65% (boost to 70% - 80% during active shed cycles)\n");
                sb.append("• **Substrate**: Cypress mulch, coconut husk (Coco Coir), or clean unprinted paper towels.\n");
                sb.append("• **Notes for ").append(snakeName).append("**: Pythons are prone to dehydration and respiratory infections if humidity drops below 50%. Ensure two snug hides (one warm, one cool).\n");
            } else if (speciesName.toLowerCase().contains("king")) {
                sb.append("• **Basking / Warm Zone**: 85°F - 88°F (29°C - 31°C)\n");
                sb.append("• **Cool Zone**: 75°F - 78°F (24°C - 26°C)\n");
                sb.append("• **Ambient Humidity**: 40% - 55% (moderate; avoid stagnant dampness to prevent scale rot)\n");
                sb.append("• **Substrate**: Shredded Aspen shavings or coconut fiber. King snakes love burrowing!\n");
                sb.append("• **Notes for ").append(snakeName).append("**: King snakes are active, curious, and voracious feeders. Keep enclosure strictly secure with lock clips.\n");
            } else if (speciesName.toLowerCase().contains("corn")) {
                sb.append("• **Basking / Warm Zone**: 85°F - 88°F (29°C - 31°C)\n");
                sb.append("• **Cool End**: 75°F - 78°F (24°C - 25°C)\n");
                sb.append("• **Ambient Humidity**: 45% - 55% (provide a humid moss box when eyes go opaque/blue)\n");
                sb.append("• **Substrate**: Dust-free Aspen bedding or paper towels.\n");
                sb.append("• **Notes for ").append(snakeName).append("**: Corn snakes are docile, hardy, and benefit from climbing branches and multiple hides.\n");
            } else {
                sb.append("• **Warm Gradient**: 85°F - 90°F\n• **Cool Gradient**: 75°F - 80°F\n• **Relative Humidity**: 50% - 65%\n");
            }
            sb.append("\n💡 *Tip*: Monitor temperature gradients daily using a digital probe thermometer or infrared temp gun.");
            return sb.toString();
        }

        // 2. Weight & Growth Analysis
        if (msg.contains("weight") || msg.contains("growth") || msg.contains("heavy") ||
                msg.contains("thin") || msg.contains("gain") || msg.contains("loss") ||
                msg.contains("grams") || msg.contains("scale")) {

            sb.append("⚖️ **Weight Trajectory Assessment for ").append(snakeName).append(" (").append(code).append("):**\n\n");
            sb.append("• **Current Registered Weight**: **").append(weightStr).append("**\n");

            if (!feedings.isEmpty()) {
                sb.append("• **Recent Weigh-in Records**:\n");
                int limit = Math.min(3, feedings.size());
                double totalGain = 0;
                for (int i = 0; i < limit; i++) {
                    FeedingRecord f = feedings.get(i);
                    String date = (f.getFeedingDate() != null) ? f.getFeedingDate().format(DATE_FMT) : "Recent";
                    BigDecimal delta = f.getWeightChange();
                    sb.append("  - ").append(date).append(": Pre-feed ")
                            .append(f.getWeightBefore() != null ? f.getWeightBefore() + "g" : "—")
                            .append(" ➔ Post-feed ")
                            .append(f.getWeightAfter() != null ? f.getWeightAfter() + "g" : "—")
                            .append(" (").append(delta != null && delta.doubleValue() >= 0 ? "+" : "").append(delta != null ? delta + "g" : "0g").append(")\n");
                    if (delta != null) totalGain += delta.doubleValue();
                }

                if (totalGain < 10.0 && limit >= 3) {
                    sb.append("\n⚠️ **Clinical Note**: ").append(snakeName).append("'s cumulative weight gain over the last ")
                            .append(limit).append(" feedings is **").append(String.format("%.1f", totalGain)).append("g**, which is below the target threshold of 10g. An automated health alert has been flagged. A veterinary consultation or prey size adjustment is advised.");
                } else {
                    sb.append("\n✅ **Clinical Note**: ").append(snakeName).append(" is exhibiting steady mass development suitable for a ").append(varietyName).append(" ").append(speciesName).append(".");
                }
            } else {
                sb.append("• *No consecutive feeding weights logged yet for trend analysis.*");
            }
            return sb.toString();
        }

        // 3. Feeding & Diet Guidance
        if (msg.contains("feed") || msg.contains("diet") || msg.contains("eat") ||
                msg.contains("mouse") || msg.contains("rat") || msg.contains("food") ||
                msg.contains("hunger") || msg.contains("meal") || msg.contains("prey")) {

            sb.append("🍽️ **Nutritional & Feeding Protocol for ").append(snakeName).append(":**\n\n");
            if (speciesName.toLowerCase().contains("python")) {
                sb.append("• **Recommended Prey**: Appropriate-sized frozen-thawed rat pups or weaned rats (prey girth should be 1.0x to 1.25x the snake's widest body circumference, or 10-15% of body weight).\n");
                sb.append("• **Feeding Schedule**: Every 7 to 10 days for juveniles; every 14 to 21 days for mature adults.\n");
                sb.append("• **Thawing Protocol**: Thaw thoroughly in warm water (100°F - 105°F / 38°C - 40°C) before presentation with tongs.\n");
            } else if (speciesName.toLowerCase().contains("king")) {
                sb.append("• **Recommended Prey**: Frozen-thawed fuzzy or hopper mice (10-12% body weight).\n");
                sb.append("• **Feeding Schedule**: Every 5 to 7 days for growing juveniles; every 7 to 10 days for adults.\n");
                sb.append("• **Handling Note**: Do not handle ").append(snakeName).append(" for 48 hours post-feeding to prevent regurgitation.\n");
            } else {
                sb.append("• **Recommended Prey**: Appropriately sized frozen-thawed hopper or adult mice.\n");
                sb.append("• **Feeding Schedule**: Every 7 days.\n");
            }

            if (!feedings.isEmpty()) {
                FeedingRecord last = feedings.get(0);
                String lastDate = (last.getFeedingDate() != null) ? last.getFeedingDate().format(DATE_FMT) : "Recently";
                String item = (last.getFoodType() != null) ? last.getFoodType().getName() : "prey item";
                sb.append("\n📋 **Last Meal Recorded**: ").append(lastDate).append(" — Offered ").append(item)
                        .append(" (Status: ").append(last.getWeightTrend() != null ? last.getWeightTrend() : "consumed").append(").");
            }
            return sb.toString();
        }

        // 4. Shedding & Skin Care
        if (msg.contains("shed") || msg.contains("skin") || msg.contains("dysecdysis") ||
                msg.contains("eyecap") || msg.contains("blue") || msg.contains("peel") ||
                msg.contains("ecdysis")) {

            sb.append("🐍 **Shedding (Ecdysis) Advisory for ").append(snakeName).append(":**\n\n");
            sb.append("• When ").append(snakeName).append(" enters the 'blue' phase (milky eyes and dull ventral scales), elevate relative vivarium humidity to **70% - 80%**.\n");
            sb.append("• Provide a dedicated **humidity hide box** filled with clean, damp Sphagnum moss.\n");
            sb.append("• **Crucial Precaution**: NEVER forcefully strip stuck eye caps or dry shed from the tail tip, as this risks corneal tearing or tail tip necrosis.\n");

            if (!sheds.isEmpty()) {
                SheddingRecord lastShed = sheds.get(0);
                String sDate = (lastShed.getSheddingDate() != null) ? lastShed.getSheddingDate().format(DATE_FMT) : "Recorded";
                sb.append("\n📅 **Most Recent Shed**: ").append(sDate)
                        .append(" (Status: ").append(lastShed.getStatus() != null ? lastShed.getStatus() : "complete")
                        .append(", Notes: ").append(lastShed.getNotes() != null ? lastShed.getNotes() : "Normal shed").append(").");
            }
            return sb.toString();
        }

        // 5. Poop, Digestion & Urates
        if (msg.contains("poop") || msg.contains("stool") || msg.contains("feces") ||
                msg.contains("urate") || msg.contains("digest") || msg.contains("diarrhea") ||
                msg.contains("enteritis")) {

            sb.append("💩 **Digestive & Elimination Profile for ").append(snakeName).append(":**\n\n");
            sb.append("• Healthy snake defecation consists of well-formed, dark brown/black fecal matter accompanied by firm, chalky white urates.\n");
            sb.append("• Stools should occur 3 to 7 days following prey ingestion.\n");
            sb.append("• **Warning Signs**: Watery unformed stools, foul putrid odor, green bile staining, or red streaks warrant veterinary fecal flotation testing.\n");

            if (!poops.isEmpty()) {
                PoopRecord lastP = poops.get(0);
                String pDate = (lastP.getPoopDate() != null) ? lastP.getPoopDate().format(DATE_FMT) : "Recorded";
                sb.append("\n🚽 **Last Defecation**: ").append(pDate).append(" — Consistency: ")
                        .append(lastP.getConsistency() != null ? lastP.getConsistency() : "normal")
                        .append(", Urates Status: ").append(lastP.getUratesStatus() != null ? lastP.getUratesStatus() : "Normal white").append(".");
            }
            return sb.toString();
        }

        // 6. Health Alerts & Medical Status
        if (msg.contains("alert") || msg.contains("health") || msg.contains("sick") ||
                msg.contains("vet") || msg.contains("disease") || msg.contains("medical") ||
                msg.contains("medicine") || msg.contains("infection")) {

            sb.append("🚨 **Clinical & Diagnostic Overview for ").append(snakeName).append(":**\n\n");
            if (!alerts.isEmpty()) {
                sb.append("⚠️ **Active Health Alerts (").append(alerts.size()).append(")**:\n");
                for (HealthAlert a : alerts) {
                    sb.append("• [").append(a.getSeverity() != null ? a.getSeverity().toUpperCase() : "ALERT")
                            .append("] **").append(a.getAlertType()).append("**: ")
                            .append(a.getMessage() != null ? a.getMessage() : a.getTitle())
                            .append(" (Status: ").append(a.getStatus()).append(")\n");
                }
            } else {
                sb.append("✅ **Clean Bill of Health**: No open clinical health alerts currently flagged for this specimen.\n");
            }

            if (!medicals.isEmpty()) {
                sb.append("\n💊 **Recent Medical Logs**:\n");
                for (MedicalRecord m : medicals.stream().limit(2).toList()) {
                    sb.append("• ").append(m.getTreatmentDate() != null ? m.getTreatmentDate().format(DATE_FMT) : "Past")
                            .append(": ").append(m.getSymptomsOrCondition()).append(" — Rx: ")
                            .append(m.getMedicineName() != null ? m.getMedicineName() : "General care").append("\n");
                }
            }
            return sb.toString();
        }

        // 7. Morph & Variety Specifics
        if (msg.contains("morph") || msg.contains("variety") || msg.contains("spider") ||
                msg.contains("banana") || msg.contains("axanthic") || msg.contains("lavender") ||
                msg.contains("piebald") || msg.contains("ghost") || msg.contains("wobble")) {

            sb.append("🧬 **Genetic & Morph Insights for ").append(snakeName).append(":**\n\n");
            sb.append("• **Specimen**: ").append(snakeName).append(" (").append(code).append(")\n");
            sb.append("• **Variety / Morph**: **").append(varietyName).append("** (").append(speciesName).append(")\n\n");

            if (varietyName.toLowerCase().contains("spider")) {
                sb.append("🔬 **Spider Morph Considerations**: The Spider gene is linked to an inner-ear neurological trait known as 'head wobble'. Most Spider snakes thrive normally, but stress or elevated temperatures can exacerbate wobbling. Maintain a quiet environment, avoid excessive handling, and feed in the enclosure.\n");
            } else if (varietyName.toLowerCase().contains("banana")) {
                sb.append("🔬 **Banana Morph Considerations**: Characterized by beautiful soft yellow pigments and developing dark freckling as the snake matures. Banana is an incomplete dominant, sex-linked trait.\n");
            } else if (varietyName.toLowerCase().contains("axanthic")) {
                sb.append("🔬 **Axanthic Morph Considerations**: A striking recessive mutation that eliminates yellow pigmentation, leaving a high-contrast monochromatic silver, gray, and black aesthetic.\n");
            } else {
                sb.append("🔬 **Husbandry Note**: ").append(varietyName).append(" is a prized captive-bred phenotypic variety known for distinct patterns and docile temperament.\n");
            }
            return sb.toString();
        }

        // Default: Comprehensive Specimen Profile Overview
        sb.append("👋 **Greetings! I am the AI Care Specialist for ").append(snakeName).append("!**\n\n");
        sb.append("Here is a quick snapshot of this specimen's husbandry profile:\n");
        sb.append("• **Species**: ").append(speciesName).append(" (").append(varietyName).append(")\n");
        sb.append("• **Specimen ID**: ").append(code).append("\n");
        sb.append("• **Current Weight**: ").append(weightStr).append("\n");
        sb.append("• **Sex / Status**: ").append(reptile.getSex() != null ? reptile.getSex().toUpperCase() : "UNKNOWN")
                .append(" &bull; ").append(reptile.getStatus() != null ? reptile.getStatus().toUpperCase() : "ACTIVE").append("\n\n");

        if (!alerts.isEmpty()) {
            sb.append("⚠️ **Notice**: There is an active health alert regarding weight monitoring for this specimen.\n\n");
        }

        sb.append("Feel free to ask me anything about **").append(snakeName).append("**, such as:\n");
        sb.append("1. *\"What is the best temperature and humidity setup?\"*\n");
        sb.append("2. *\"Analyze ").append(snakeName).append("'s weight and feeding history\"*\n");
        sb.append("3. *\"What prey size and feeding schedule should I use?\"*\n");
        sb.append("4. *\"How should I prepare the vivarium for shedding?\"*\n");
        sb.append("5. *\"Check recent health alerts and veterinary notes\"*");

        return sb.toString();
    }
}
