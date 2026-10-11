# Sha Reptiles AI Diagnostic & Husbandry System (Java Edition)

A full-stack enterprise reptile husbandry, health tracking, and AI-powered dermatological diagnostic web application built with **Java (Spring Boot 3)**, **Spring Data JPA**, **Thymeleaf**, and **PyTorch CNN with Grad-CAM Explainable AI**.

> **Note for College Evaluation**: This system is a 100% faithful, module-by-module Java rewrite of the original PHP/Laravel application. Every single module, data model, business logic rule (including weight stagnation monitoring and Grad-CAM diagnosis), route, and UI view has been preserved with zero omissions.

---

## 🚀 Key Modules & Architecture

1. **Reptile Management & Inventory Module**:
   - Complete profiles for each specimen (Identification code, morph, sex, birth date, microchip ID, quarantine status, enclosure number).
   - 360-degree aggregated health and husbandry record view.
   - Comprehensive Weight History and Growth Trend Analytics.
   - Admin-only actions to archive or permanently delete an individual reptile and its associated care history.
   - Admin-only JSON export of application records; user account and password data are excluded.

2. **Hierarchical Taxonomy & Classification**:
   - **Species Category** (e.g. Snake, Lizard, Gecko)
   - **Species Type** (e.g. Ball Python, Corn Snake)
   - **Species Variety / Morph** (e.g. Pastel, Albino, Banana)
   - Interactive Visual Breadcrumb / Tree View navigation.

3. **Feeding Management & Automated Scheduling**:
   - Multi-tier food inventory (Mouse, Rat, Pinky, Fuzzy, Hopper).
   - Interval-based recurring feeding scheduler with calendar projection.
   - Pre-feeding vs. post-feeding and post-defecation weight logging.
   - Feeding Schedule Adjustments with full audit trail.

4. **Clinical Defecation & Urates Monitoring**:
   - Defecation logging with stool consistency (Normal, Loose, Hard, Mucus, Bloody).
   - Urates hydration tracking (Chalky White, Yellowish, Greenish, None).
   - Post-defecation true weight tracking.

5. **Shedding (Ecdysis) Quality Module**:
   - Log shed cycles (Complete, Incomplete/Dysecdysis, Stuck).
   - Detailed clinical observations (retained eye caps, tail tip constrictions).

6. **Veterinary & Medical Treatment Records**:
   - Medical condition logging, prescribed medication, dosage, and veterinarian contact.
   - Secure digital prescription upload and storage.

7. **Rule-Based Weight Health Alert Engine**:
   - **Automated Algorithm**: Calculates effective weight (`weightAfterDefecation ?? weightAfter ?? weightBefore`).
   - Tracks net weight delta across chronological feeding events.
   - **Clinical Trigger**: Flags an alert if a reptile fails to gain weight (gain ≤ 0.0g) across **3 consecutive feedings** or experiences unexpected weight loss.
   - Automated status resolution upon subsequent documented weight gain.

8. **AI Deep Learning Disease Diagnostic Portal**:
   - **PyTorch Convolutional Neural Network (CNN)** trained for snake skin conditions.
   - **Grad-CAM (Gradient-weighted Class Activation Mapping)**: Generates visual heatmaps highlighting exact lesion areas.
   - 7 Clinical Classes:
     1. Respiratory Infection
     2. Dysecdysis (Retained Shed)
     3. Mouth Rot (Infectious Stomatitis)
     4. Scale Rot (Ulcerative Dermatitis)
     5. Snake Mites (*Ophionyssus natricis*)
     6. Enteritis / Gastrointestinal
     7. Healthy / Normal
   - Dual interface: Administrative Diagnostic Workbench + Public Customer Diagnostic Portal with printable PDF-ready clinical health reports.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Backend Language** | Java 25 (LTS) |
| **Framework** | Spring Boot 3.5.16 (Spring MVC, Spring Data JPA) |
| **View Engine** | Thymeleaf 3 (Server-Side Rendering) |
| **Frontend UI** | Bootstrap 5, Bootstrap Icons, Sneat Theme, Boxicons |
| **Database** | Embedded File-Persistent H2 (MySQL Mode, zero install required) |
| **Deep Learning** | PyTorch CNN, Torchvision, OpenCV / PIL (`ml/predict.py`) |
| **Build Tool** | Apache Maven |

---

## 🏃 How to Run the Project

### Option 1: Double-Click Batch File
Run `run.bat` located in this folder. It will automatically check your environment and launch the Spring Boot server.

### Option 2: Using VS Code (Recommended)
1. Open **VS Code**.
2. Go to **File → Open Folder** and select `sha-reptiles-java`.
3. If not already installed, install the **Extension Pack for Java** in VS Code.
4. Navigate to `src/main/java/com/shareptiles/app/ShaReptilesApplication.java`.
5. Click **Run** (or press `F5`).

### Option 3: Command Line (Maven)
```bash
mvn spring-boot:run
```

The application will start on:
👉 **http://localhost:8000**

### Hugging Face Profile Image Generation (Optional)

When the `HF_TOKEN` environment variable is set to a Hugging Face token with the **Inference Providers** permission, the app can generate species-level profile images in the background. Hugging Face text-to-image generation is not used for morphs because generated images cannot be guaranteed to represent the exact morph. Morphs use the species-and-variety-specific local image catalog where a verified local image exists; otherwise the UI shows an “Image not available” placeholder or an available species-level image. Generated species files are stored under `uploads/generated-profiles`; existing user-uploaded images are kept.

The curated mappings live in `src/main/java/com/shareptiles/app/util/SnakeImageCatalog.java`; their image files are under `src/main/resources/static/assets/img/snakes/`. Individual photos were cropped from the user-provided, labeled Kingsnake, Corn Snake, and Ball Python collages and named after the pictured varieties. The mapping uses both the species name and variety name. On the next application startup, the existing King Snake, Corn Snake, and Python variety rows are renamed in place where needed and assigned their matching collage photos. Existing variety IDs and linked reptile records are preserved; other reptile records and histories are not rewritten. The Species & Morphs sidebar link opens the species-card page directly. Species cards retain their existing image/fallback behavior. If no verified matching file exists, leave the mapping absent—the placeholder or an available species-level photo is safer than showing a different morph. To add another image, place the file in that folder and add a species-plus-variety mapping.

On Windows, set `HF_TOKEN` as a user environment variable using **Edit environment variables for your account** in the Windows Start menu. Alternatively, set it just for the current PowerShell window before starting the app:

```powershell
$env:HF_TOKEN = "your-token"
.\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```

Restart VS Code or your terminal after setting a persistent environment variable, then restart the application so it can read the variable.

Create a fine-grained Hugging Face token with **Inference Providers** permission in [Hugging Face token settings](https://huggingface.co/settings/tokens). Never put the token in source code, `application.properties`, or a committed file. If a token has been pasted into chat or otherwise exposed, revoke it in Hugging Face settings and create a replacement before configuring the app. The default model is `stabilityai/stable-diffusion-3-medium-diffusers`; optionally override it with the `HF_IMAGE_MODEL` environment variable.

---

## 🔑 Default Credentials

- **Admin Portal**: `http://localhost:8000/admin/login`
  - **Email**: `admin@example.com`
  - **Password**: `ChangeMe123!`
  - *(Alternate seeded admin: `shaspets06@gmail.com` / `ChangeMe123!`)*
- **Public Customer AI Scan**: `http://localhost:8000/disease-prediction`
- **H2 Database Console**: `http://localhost:8000/h2-console`
  - JDBC URL: `jdbc:h2:file:./data/sha_reptiles;DB_CLOSE_DELAY=-1;MODE=MySQL`
  - User: `sa`
  - Password: `password`

---

## 📂 Project Directory Structure

```
sha-reptiles-java/
├── pom.xml                               # Maven project dependencies & build configuration
├── run.bat                               # Windows launcher script
├── data/                                 # Persistent database storage
├── uploads/                              # Uploaded medical prescriptions and scan images
├── ml/                                   # PyTorch CNN models and prediction script
│   └── predict.py
└── src/
    └── main/
        ├── java/com/shareptiles/app/
        │   ├── ShaReptilesApplication.java
        │   ├── config/                   # WebConfig & AuthInterceptor
        │   ├── controller/               # 17 Spring MVC Controllers
        │   ├── model/                    # 14 JPA Domain Entities
        │   ├── repository/               # 14 Spring Data Repositories
        │   └── service/                  # AI, Weight Analysis & Business Logic
        └── resources/
            ├── application.properties    # Configuration settings
            ├── static/                   # CSS, JS, Images, Vendor assets
            └── templates/                # Thymeleaf views (Admin & Customer)
```
