try:
    import torch
    import torch.nn as nn
    import torch.nn.functional as F
    TORCH_AVAILABLE = True
except ImportError:
    TORCH_AVAILABLE = False
    torch = None
    nn = None
    F = None

CLASSES = [
    "Respiratory Infection (RI)",
    "Incomplete Shedding (Dysecdysis)",
    "Loose Motion / Enteritis",
    "Infectious Stomatitis (Mouth Rot)",
    "Scale Rot (Ulcerative Dermatitis)",
    "Mite Infestation (Ophionyssus)",
    "Healthy / Normal Condition"
]

CLASS_SYMPTOMS = {
    "Respiratory Infection (RI)": [
        "Excessive oral mucus & bubbling secretions around glottis",
        "Open-mouth breathing (dyspneic respiratory distress)",
        "Postural dyspnea (elevated stargazing neck posturing)",
        "Audible wheezing, popping, or clicking during inhalation",
        "Labial and nasal discharge clogging sensory pits"
    ],
    "Incomplete Shedding (Dysecdysis)": [
        "Retained cloudy spectacle (eye cap) impairing ocular vision",
        "Patches of dry, unshed keratinized skin adhered across crown & dorsal scales",
        "Constriction rings around tail tip risking ischemic tail necrosis",
        "Wrinkled, dull skin texture indicating insufficient enclosure humidity",
        "Focal scale rubbing excoriations around snout from shedding attempts"
    ],
    "Loose Motion / Enteritis": [
        "Watery, unformed liquid stool with complete loss of normal fecal structure",
        "Excessive bile staining, yellowish/greenish mucoid discharge",
        "Foul, pungent ammonia or putrid intestinal odor",
        "Vent/cloacal erythema and wet substrate staining",
        "Severe post-defecation weight drop and dehydration skin tenting"
    ],
    "Infectious Stomatitis (Mouth Rot)": [
        "Caseous yellow necrotic exudate along gum lines and labial margins",
        "Gingival erythema, edema, and mucosal hemorrhaging",
        "Inability to fully close mouth due to oral tissue swelling",
        "Partial tooth loss and purulent oral debris"
    ],
    "Scale Rot (Ulcerative Dermatitis)": [
        "Discolored, necrotic ventral belly scales (scutes)",
        "Fluid-filled subscale blisters (vesicular dermatitis)",
        "Ulcerative skin lesions with raw subcutaneous exposure",
        "Bacterial breakdown of keratinized skin layers"
    ],
    "Mite Infestation (Ophionyssus)": [
        "Tiny dark oval ectoparasites clustered under chin scales and gular fold",
        "Parasites visibly crawling around eye spectacle rims and labial pits",
        "Frequent, prolonged soaking in water bowl trying to drown mites",
        "Raised, rough scales and excessive rubbing against vivarium furnishings",
        "Lethargy and anemia caused by hematophagous blood-feeding parasites"
    ],
    "Healthy / Normal Condition": [
        "Firmly sealed oral cavity with zero mucus accumulation",
        "Clear, alert eyes and clean heat sensory labial pits",
        "Smooth, intact dorsal and ventral scales with normal sheen",
        "Formed stool with distinct brown fecal portion and solid white urate",
        "Complete one-piece sheds and natural, relaxed exploratory posture"
    ]
}

CLASS_SEVERITY = {
    "Respiratory Infection (RI)": "Critical / High",
    "Incomplete Shedding (Dysecdysis)": "Moderate",
    "Loose Motion / Enteritis": "High",
    "Infectious Stomatitis (Mouth Rot)": "High",
    "Scale Rot (Ulcerative Dermatitis)": "Moderate to High",
    "Mite Infestation (Ophionyssus)": "Moderate",
    "Healthy / Normal Condition": "Normal / Healthy"
}

CLASS_EXACT_NAMES = {
    "Respiratory Infection (RI)": "Bacterial Bronchopneumonia & Upper/Lower Respiratory Tract Infection (Dyspneic Stage)",
    "Incomplete Shedding (Dysecdysis)": "Severe Cutaneous Dysecdysis & Pathological Retained Ocular Spectacles",
    "Loose Motion / Enteritis": "Infectious Colitis, Enteritis & Severe Gastrointestinal Dysbiosis",
    "Infectious Stomatitis (Mouth Rot)": "Infectious Ulcerative Stomatitis & Periodontal Caseous Necrosis (Mouth Rot)",
    "Scale Rot (Ulcerative Dermatitis)": "Bacterial Necrotizing Ulcerative Dermatitis & Vesicular Ventral Scale Rot",
    "Mite Infestation (Ophionyssus)": "Acute Ophionyssus natricis Acariasis & Hematophagous Ectoparasitic Infestation",
    "Healthy / Normal Condition": "Epithelial Intactness & Normal Physiological Equilibrium (Healthy Condition)"
}

CLASS_PATHOLOGY = {
    "Respiratory Infection (RI)": (
        "Gram-negative bacterial colonization (predominantly Pseudomonas fluorescens, Aeromonas hydrophila, or Klebsiella pneumoniae) "
        "proliferating within the tracheal mucosa and faveolar pulmonary chambers. Visual features detect glistening oral mucoid secretions, "
        "respiratory labial bubbling, and open-mouth postural elevation."
    ),
    "Incomplete Shedding (Dysecdysis)": (
        "Pathological failure of the stratum corneum to detach cleanly during the ecdysis cycle, caused by chronic ambient humidity deficits (<50%) "
        "or systemic dehydration. Keratinized scales remain adhered across the crown and tail tip; retained cloudy spectacles present an immediate risk of corneal ulceration."
    ),
    "Loose Motion / Enteritis": (
        "Acute intestinal mucosal compromise and loss of solid fecal architecture triggered by protozoal flagellates, Cryptosporidium serpentis, "
        "or pathogenic bacteria. Visual cues show unformed watery excretion with bilious discolored exudate, indicating acute metabolic dehydration."
    ),
    "Infectious Stomatitis (Mouth Rot)": (
        "Necrotizing bacterial invasion along the labial margin and gingival sulcus. Photographic features highlight caseous yellow curd-like necrotic plaques, "
        "severe gingival hyperemia, mucosal swelling, and inability to fully close oral margins."
    ),
    "Scale Rot (Ulcerative Dermatitis)": (
        "Bacterial necrotizing infection of the ventral scutes (vesicular dermatitis) caused by prolonged exposure to damp, contaminated, or unsanitary substrate. "
        "The photo shows subscale fluid-filled blister formation, brown necrotic scale discoloration, and ulcerated dermal exposure."
    ),
    "Mite Infestation (Ophionyssus)": (
        "Hematophagous ectoparasitic infestation by Ophionyssus natricis mites. Visual indicators reveal micro-speckling clustered beneath the chin (gular fold) "
        "and ocular spectacles, leading to scale lifting, acute pruritus, and blood-loss anemia."
    ),
    "Healthy / Normal Condition": (
        "Zero clinical pathology detected. Keratinized scale layers display normal lipid sheen, oral mucosa is dry and sealed, eyes and heat pits are clear, and muscle tone is robust."
    )
}

CLASS_ENVIRONMENT_RX = {
    "Respiratory Infection (RI)": "Warm Basking: 88°F - 90°F (31°C - 32°C) | Ambient Warm: 85°F (29°C) | Cool: 78°F (26°C) | Target Humidity: 50% - 55% | Action: Eliminate drafts and dusty substrates; line with paper towels.",
    "Incomplete Shedding (Dysecdysis)": "Ambient Temperature: 84°F - 86°F (29°C - 30°C) | Target Relative Humidity: 70% - 80% | Action: Install a dedicated humidity hide filled with damp Chilean sphagnum moss immediately.",
    "Loose Motion / Enteritis": "Hospital Tub Temp: 84°F - 86°F (29°C - 30°C) | Humidity: 50% | Action: House strictly on sterile white paper towels; sanitize and replace enclosure daily.",
    "Infectious Stomatitis (Mouth Rot)": "Basking Zone: 88°F - 90°F (31°C - 32°C) | Ambient: 84°F | Target Humidity: 50% - 60% | Action: Remove all rough furnishings; maintain sterile paper towel bedding.",
    "Scale Rot (Ulcerative Dermatitis)": "Hospital Enclosure Temp: 84°F - 86°F (29°C - 30°C) | Target Humidity: 40% - 45% (Dry conditions essential) | Action: Strictly remove damp substrate; use dry sterile paper towels.",
    "Mite Infestation (Ophionyssus)": "Quarantine Room Isolation | Ambient Temp: 80°F - 82°F | Action: Paper towel bedding only; isolate water bowl to prevent drowning spillover.",
    "Healthy / Normal Condition": "Standard Species Husbandry: Kingsnakes 75°-85°F / 40-50% RH | Corn Snakes 75°-85°F / 40-60% RH | Ball Pythons 78°-88°F / 60-70% RH."
}

CLASS_FIRST_AID = {
    "Respiratory Infection (RI)": [
        "1. Immediately isolate specimen in a dedicated, warm hospital vivarium.",
        "2. Elevate ambient enclosure temperature to 85°F-88°F to boost immune metabolism.",
        "3. Remove all loose/damp substrate; line with clean, dry paper towels.",
        "4. Prepare for veterinary antibiotic nebulization (F10SC 1:250 or injectable Ceftazidime)."
    ],
    "Incomplete Shedding (Dysecdysis)": [
        "1. NEVER peel dry stuck skin or eye caps with force to avoid tissue damage.",
        "2. Provide a 25-minute lukewarm bath (82°F-84°F) with a textured wet washcloth.",
        "3. Prepare a humid hide box with fresh, damp sphagnum moss.",
        "4. Apply sterile ophthalmic lubricant drops if eye cap remains adhered after 2 soaks."
    ],
    "Loose Motion / Enteritis": [
        "1. Collect fresh fecal sample in sterile container and refrigerate for vet fecal float/smear.",
        "2. Temporarily withhold prey feeding to allow GI tract mucosal resting.",
        "3. Administer lukewarm electrolyte soak (50% unflavored Pedialyte, 50% water).",
        "4. Sterilize enclosure and hides with veterinary F10SC disinfectant."
    ],
    "Infectious Stomatitis (Mouth Rot)": [
        "1. Inspect oral cavity gently using a soft silicone probe; do not force teeth.",
        "2. Swab oral mucosal margins with dilute 0.05% Chlorhexidine gluconate solution.",
        "3. Gently debride loose caseous necrotic pus plaques under veterinary guidance.",
        "4. Administer veterinary systemic antibiotics and anti-inflammatories as prescribed."
    ],
    "Scale Rot (Ulcerative Dermatitis)": [
        "1. Relocate to a strictly dry hospital enclosure lined with fresh paper towels.",
        "2. Administer daily 15-minute lukewarm diluted Betadine soak (tea-colored dilution).",
        "3. Pat ventral belly scutes completely dry with sterile surgical gauze.",
        "4. Apply topical Silver Sulfadiazine (Silvadene 1%) cream to ulcerated lesions."
    ],
    "Mite Infestation (Ophionyssus)": [
        "1. Place in separate quarantine room far from other collection reptiles.",
        "2. Soak snake in warm water with 1 drop of gentle soap for 30 minutes to drown mites.",
        "3. Strip enclosure and treat with Permethrin premise spray (dry 24h before re-entry).",
        "4. Repeat soak cycles every 7-10 days to disrupt new hatching egg cycles."
    ],
    "Healthy / Normal Condition": [
        "1. Maintain standard scheduled feeding intervals and clean water bowl.",
        "2. Continue recording routine weight, shed, and defecation entries."
    ]
}

CLASS_RECOMMENDATIONS = {
    "Respiratory Infection (RI)": (
        "Immediate veterinary consultation required. Quarantine reptile immediately in a clean, "
        "dry hospital enclosure. Elevate ambient temperature to the optimal upper thermal zone (85°F - 88°F / 29°C - 31°C) "
        "to stimulate the reptile's immune response. Eliminate cold drafts. Discontinue dusty substrates and replace with paper towels. "
        "Prepare for veterinary nebulization therapy and injectable antibiotics (e.g. Ceftazidime or Enrofloxacin) upon culture."
    ),
    "Incomplete Shedding (Dysecdysis)": (
        "Do NOT forcefully peel dry stuck skin or eye caps, as this can cause permanent corneal or scale damage. "
        "Provide a dedicated humidity hide box containing damp sphagnum moss. Place the snake in a shallow lukewarm water soak "
        "(82°F - 85°F / 28°C - 29°C) for 20-30 minutes with a textured washcloth to let the snake slither through and gently loosen stuck shed. "
        "Increase vivarium ambient humidity to 65%-75% during shed cycles. If spectacle cap remains stuck after 2 soaks, seek veterinary assistance for ophthalmic lubricant removal."
    ),
    "Loose Motion / Enteritis": (
        "Collect a fresh fecal sample in a sterile container and refrigerate immediately for an exotic vet fecal floatation and direct smear "
        "(tests for Cryptosporidium, Flagellates, Amoebiasis, and Coccidia). Quarantine snake on clean white paper towels to monitor stool consistency. "
        "Discontinue feeding temporarily to rest the GI tract. Rehydrate reptile with lukewarm electrolyte soaks (unflavored Pedialyte 1:1 with water). "
        "Disinfect entire vivarium with veterinary F10 disinfectant."
    ),
    "Infectious Stomatitis (Mouth Rot)": (
        "Schedule an exotic reptile veterinary appointment. Gently swab oral cavity with dilute chlorhexidine solution (0.05%) "
        "or povidone-iodine if instructed by a vet. Remove purulent caseous debris under professional guidance. "
        "Adjust vivarium temperature and improve overall sanitation to prevent opportunistic bacterial reinfection."
    ),
    "Scale Rot (Ulcerative Dermatitis)": (
        "Move reptile to a completely dry quarantine enclosure with clean paper towels. Discontinue wet bedding or moss. "
        "Perform daily warm diluted Betadine/povidone-iodine soaks (tea-colored dilution) for 15-20 minutes, then gently dry. "
        "Apply topical silver sulfadiazine cream to affected ventral scutes. Consult an exotic vet for antibiotic therapy if blisters rupture."
    ),
    "Mite Infestation (Ophionyssus)": (
        "Isolate reptile in a separate room from other collections. Discard all wood and porous enclosure decor. "
        "Clean enclosure with diluted F10SC or Permethrin-based reptile premise spray (e.g., Provent-a-Mite — NEVER spray directly on snake). "
        "Soak snake in warm water with a drop of gentle dish soap to break surface tension and drown active mites. "
        "Treat snake with veterinary-approved topical ivermectin spray or olive oil wipe as directed by vet. Repeat treatment at 10-day intervals to break egg cycle."
    ),
    "Healthy / Normal Condition": (
        "No clinical pathology detected. Continue maintaining standard species-specific husbandry parameters, "
        "including temperature gradient, appropriate humidity cycles, scheduled feedings, and fresh water."
    )
}

if TORCH_AVAILABLE:
    class SnakeDiseaseCNN(nn.Module):
        """
        4-Stage Convolutional Neural Network for Reptile Multi-Disease Classification.
        Extracts multi-scale spatial features (scale textures, stuck shed, fecal liquidity, oral bubbling, blisters, ectoparasites).
        """
        def __init__(self, num_classes=len(CLASSES)):
            super(SnakeDiseaseCNN, self).__init__()
        
        # Block 1: Low-level edges, scale textures (3 -> 32)
        self.conv1 = nn.Conv2d(3, 32, kernel_size=3, padding=1)
        self.bn1 = nn.BatchNorm2d(32)
        self.pool1 = nn.MaxPool2d(2, 2)

        # Block 2: Mid-level shapes, oral margins, skin flaking (32 -> 64)
        self.conv2 = nn.Conv2d(32, 64, kernel_size=3, padding=1)
        self.bn2 = nn.BatchNorm2d(64)
        self.pool2 = nn.MaxPool2d(2, 2)

        # Block 3: High-level pathology: mucus bubbles, necrotic plaques, blisters, fecal pooling (64 -> 128)
        self.conv3 = nn.Conv2d(64, 128, kernel_size=3, padding=1)
        self.bn3 = nn.BatchNorm2d(128)
        self.pool3 = nn.MaxPool2d(2, 2)

        # Block 4: Contextual features: stargazing posture, cloudy eye caps, liquid halos (128 -> 256)
        self.conv4 = nn.Conv2d(128, 256, kernel_size=3, padding=1)
        self.bn4 = nn.BatchNorm2d(256)
        self.pool4 = nn.MaxPool2d(2, 2)

        # Adaptive average pooling for consistent dense input
        self.adaptive_pool = nn.AdaptiveAvgPool2d((4, 4))

        # Fully Connected Classification Head
        self.classifier = nn.Sequential(
            nn.Flatten(),
            nn.Linear(256 * 4 * 4, 512),
            nn.ReLU(inplace=True),
            nn.Dropout(0.4),
            nn.Linear(512, 128),
            nn.ReLU(inplace=True),
            nn.Dropout(0.2),
            nn.Linear(128, num_classes)
        )

        self.gradients = None
        self.activations = None

    def activations_hook(self, grad):
        self.gradients = grad

    def forward(self, x):
        x = self.pool1(F.relu(self.bn1(self.conv1(x))))
        x = self.pool2(F.relu(self.bn2(self.conv2(x))))
        x = self.pool3(F.relu(self.bn3(self.conv3(x))))
        x = self.conv4(x)
        x = self.bn4(x)
        x = F.relu(x)
        
        # Save activations for Grad-CAM
        self.activations = x
        if x.requires_grad:
            x.register_hook(self.activations_hook)

        x_pooled = self.pool4(x)
        features = self.adaptive_pool(x_pooled)
        logits = self.classifier(features)
        return logits

    def get_activations_gradient(self):
        return self.gradients

    def get_activations(self):
        return self.activations
else:
    class SnakeDiseaseCNN:
        def __init__(self, num_classes=len(CLASSES)):
            pass

