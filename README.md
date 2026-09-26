# The Lost Kingdom (The Blade of Ancient Realms)

A 2D fantasy side-scrolling action-adventure game built with Kotlin and Jetpack Compose.

---

## 🎮 Key Features

- **Smooth Camera Follow System**: Bounded platformer camera tracking with lead-ahead player positioning, damping, vertical look-ahead, and screen shake.
- **Multi-Layer Parallax Environment**: 8 distinct biome regions (Whispering Woods, Oakhaven Hamlet, Deeproot Caverns, Ancient Ruins, Frostpeak Summit, Hidden Lunar Shrine, Stormgate Keep, and Throne of the Ruin Colossus) with background horizons, midgrounds, near foregrounds, and dynamic weather particle effects.
- **Local Room Database Player Inventory**:
  - Full local SQLite persistence utilizing Android Room + KSP.
  - Entities, DAOs, and repository pattern with reactive `Flow` updates.
  - Stacking inventory items across multiple categories (*Consumables*, *Key Items*, *Materials*, *Relics*, *Equipment*, *Treasure*).
  - Usable potions and crystals that dynamically affect player health and speed in real-time.
  - In-game interactive Backpack dialog with category filtering, search, lore inspection, and direct consumption.
- **100% Offline Gameplay**: Local persistent storage with no external dependencies required.

---

## 🚀 Automated GitHub Actions & APK Downloads

This repository includes pre-configured **GitHub Actions CI/CD workflows** (`.github/workflows/build-and-release.yml` and `pr-check.yml`) for automated building and direct APK downloading:

### 1. Direct APK Download via GitHub Actions Artifacts
Every push to `main` (or `master`) automatically compiles the project using Gradle and uploads the APK:
1. Go to the **Actions** tab on your GitHub repository.
2. Click the latest workflow run: **Build Android APK & GitHub Release**.
3. Scroll down to the **Artifacts** section at the bottom.
4. Click **TheLostKingdom-Android-APK** to download the ready-to-install `.apk` directly!

### 2. Direct APK Download via GitHub Releases
You can generate a direct public release with the APK attached in two easy ways:

#### Option A: Tag a Release (Recommended)
Push a git tag starting with `v` (e.g. `v1.0.0`):
```bash
git tag v1.0.0
git push origin v1.0.0
```
GitHub Actions will automatically build the APK and publish a new **GitHub Release** with the `.apk` attached as a direct download asset.

#### Option B: Manual Dispatch (1-Click in GitHub UI)
1. Go to the **Actions** tab on your GitHub repository.
2. Select **Build Android APK & GitHub Release** in the left sidebar.
3. Click the **Run workflow** dropdown button.
4. Select `Build Type` (`debug` or `release`).
5. Check `Publish a GitHub Release with downloadable APK`.
6. Click **Run workflow**.

---

## 🛠️ Pushing to Your GitHub Repository

If you haven't linked a remote repository yet, run the following commands in your terminal:

```bash
# Add your GitHub remote
git remote add origin https://github.com/<YOUR_USERNAME>/<YOUR_REPOSITORY>.git

# Push the code and workflows to main
git push -u origin main
```
*(You can also use the **Export to GitHub** option directly from the AI Studio Settings menu).*

---

## 📱 Local Building & Testing

To compile or test locally via Gradle:
```bash
# Build Debug APK
./gradlew assembleDebug

# Run Unit & Room Database Tests
./gradlew testDebugUnitTest
```
