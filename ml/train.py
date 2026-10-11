import os
import random
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms
from PIL import Image
from model import SnakeDiseaseCNN, CLASSES

# Ensure deterministic behavior
torch.manual_seed(42)
random.seed(42)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
SAMPLES_DIR = os.path.join(os.path.dirname(BASE_DIR), "public", "samples")

class SnakeDataset(Dataset):
    def __init__(self, samples, transform=None):
        self.samples = samples  # List of (image_path, class_idx)
        self.transform = transform

    def __len__(self):
        return len(self.samples)

    def __getitem__(self, idx):
        path, label = self.samples[idx]
        image = Image.open(path).convert('RGB')
        if self.transform:
            image = self.transform(image)
        return image, label

def get_base_samples():
    class_folders = {
        0: os.path.join(SAMPLES_DIR, "respiratory_infection"),
        1: os.path.join(SAMPLES_DIR, "incomplete_shedding"),
        2: os.path.join(SAMPLES_DIR, "loose_motion"),
        3: os.path.join(SAMPLES_DIR, "mouth_rot"),
        4: os.path.join(SAMPLES_DIR, "scale_rot"),
        5: os.path.join(SAMPLES_DIR, "snake_mites"),
        6: os.path.join(SAMPLES_DIR, "healthy")
    }

    raw_samples = []
    for class_idx, folder in class_folders.items():
        if not os.path.exists(folder):
            continue
        for fname in os.listdir(folder):
            if fname.lower().endswith(('.jpg', '.jpeg', '.png', '.webp')):
                full_path = os.path.join(folder, fname)
                raw_samples.append((full_path, class_idx))
    return raw_samples

def train():
    print("=" * 60)
    print("Training SnakeDiseaseCNN on Reptile Health Dataset")
    print("=" * 60)

    raw_samples = get_base_samples()
    print(f"Loaded {len(raw_samples)} authentic base samples across {len(CLASSES)} classes:")
    for idx, cname in enumerate(CLASSES):
        count = sum(1 for _, l in raw_samples if l == idx)
        print(f"  Class {idx}: {cname} -> {count} base images")

    # Data augmentation pipeline
    train_transforms = transforms.Compose([
        transforms.Resize((224, 224)),
        transforms.RandomHorizontalFlip(p=0.5),
        transforms.RandomRotation(degrees=20),
        transforms.ColorJitter(brightness=0.2, contrast=0.2, saturation=0.2),
        transforms.RandomAffine(degrees=10, translate=(0.08, 0.08), scale=(0.9, 1.1)),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
    ])

    val_transforms = transforms.Compose([
        transforms.Resize((224, 224)),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
    ])

    # Replicate raw samples to build augmented batches
    augmented_samples = []
    for item in raw_samples:
        multiplier = 10
        augmented_samples.extend([item] * multiplier)

    random.shuffle(augmented_samples)
    split_idx = int(0.85 * len(augmented_samples))
    train_data = augmented_samples[:split_idx]
    val_data = augmented_samples[split_idx:]

    train_loader = DataLoader(SnakeDataset(train_data, transform=train_transforms), batch_size=16, shuffle=True)
    val_loader = DataLoader(SnakeDataset(val_data, transform=val_transforms), batch_size=16, shuffle=False)

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Training on device: {device}", flush=True)

    model = SnakeDiseaseCNN(num_classes=len(CLASSES)).to(device)
    criterion = nn.CrossEntropyLoss()
    optimizer = optim.AdamW(model.parameters(), lr=0.001, weight_decay=1e-4)

    epochs = 5
    best_acc = 0.0

    for epoch in range(1, epochs + 1):
        model.train()
        running_loss = 0.0
        correct = 0
        total = 0

        for images, labels in train_loader:
            images, labels = images.to(device), labels.to(device)
            optimizer.zero_grad()
            outputs = model(images)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()

            running_loss += loss.item() * images.size(0)
            _, predicted = torch.max(outputs, 1)
            total += labels.size(0)
            correct += (predicted == labels).sum().item()

        train_loss = running_loss / total
        train_acc = (correct / total) * 100.0

        # Validation
        model.eval()
        val_loss = 0.0
        val_correct = 0
        val_total = 0
        with torch.no_grad():
            for images, labels in val_loader:
                images, labels = images.to(device), labels.to(device)
                outputs = model(images)
                loss = criterion(outputs, labels)
                val_loss += loss.item() * images.size(0)
                _, predicted = torch.max(outputs, 1)
                val_total += labels.size(0)
                val_correct += (predicted == labels).sum().item()

        val_acc = (val_correct / val_total) * 100.0 if val_total > 0 else 0.0
        print(f"Epoch [{epoch:02d}/{epochs:02d}] - Train Loss: {train_loss:.4f}, Train Acc: {train_acc:.2f}% | Val Loss: {val_loss / max(1, val_total):.4f}, Val Acc: {val_acc:.2f}%")

        if val_acc > best_acc:
            best_acc = val_acc

    # Save model weights
    save_path = os.path.join(BASE_DIR, "snake_disease_cnn.pth")
    torch.save(model.state_dict(), save_path)
    print(f"\nModel successfully trained and saved to: {save_path}")
    print(f"Peak Validation Accuracy: {best_acc:.2f}%")

if __name__ == "__main__":
    train()
