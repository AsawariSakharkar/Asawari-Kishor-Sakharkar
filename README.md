# Overthink

**Overthink** is a private, on-device native Android mindfulness application built using Kotlin, Jetpack Compose, Material 3, Navigation Compose, Room Database, and Jetpack DataStore.

The app provides quality-of-life pause sessions and standalone cognitive reframing tools to help interrupt mental spirals. It is strictly private, offline-first, and never uploads personal thoughts to external servers or cloud AI models.

---

## 🌟 Key Features

### 1. Guided Full Pause Session
- **Pre-session Check-In**: Identify the current emotion (Anxious, Overwhelmed, Sad, Frustrated, etc.), intensity (1–5), and optional triggers (Work, Social, Relationship, Decisions, etc.).
- **Progress Tracking**: Clear step indicators (`Exercise 2 of 5`, estimated time remaining, and non-judgmental skip/end early options).
- **Exercise 1 — Slow Breathing**: 5-minute expandable breathing visualizer with cadence cues, optional procedural ambient sound, and gentle 1-minute continue prompt.
- **Exercise 2 — First Thought Capture**: 50-second countdown to record up to 5 single words with explicit privacy control (*"Include in insights"* or *"Keep private"*).
- **Exercise 3 — Let It Pass**: Visual thought defusion where thoughts drift away inside a cloud graphic without struggling to fix them.
- **Exercise 4 — Grounding**: Automatic recommendation of **3-3-3** or **5-4-3-2-1** sensory grounding based on user-rated helpfulness, with instant switching.
- **Exercise 5 — Five-Minute Walk & Movement**: Countdown timer with optional on-device step counting and low-movement alternatives (stretching, standing by a window, slow water sip).
- **Post-session Check-In & Gentle Support**: Shift tracking (*Worse*, *The same*, *A little lighter*, *Much lighter*), most helpful part attribution, and gentle grounded support if feeling worse.

### 2. Standalone Tool Library
- **Calm the Body**: Belly Breathing (Diaphragmatic cadence), Mindful Meditation (Breath, Body Scan, Sounds focus with 3/5/10 min options).
- **Return to the Present**: 3-3-3 Grounding Rule, 5-4-3-2-1 Sensory Grounding.
- **Create Space from Thoughts**: Standalone "Let It Pass" thought release.
- **Understand the Loop**: Identify Your Overthinking (theme mapping & dynamic tool suggestion).
- **Examine a Thought**: Question the Thought (separating facts from predictions/assumptions) and Challenge a Thought (CBT-inspired balanced perspective builder).

### 3. Private Journal
- Daily/weekly check-in prompt without streak pressure or shaming.
- Voice entry using Android `SpeechRecognizer` with dynamic runtime microphone permission handling.
- Edit, delete, and browse journal records stored securely in local SQLite/Room.

### 4. Local On-Device Insights
- 7-day pause activity chart.
- Most frequent emotions and end feeling distributions.
- Highest-rated exercises computed strictly on-device.
- Recurring thought themes (from user-confirmed inclusions only).
- Instant pause/resume toggle for pattern tracking and one-tap history wipe.

### 5. Settings & Privacy
- Theme customization (System, Light, Dark mode).
- Ambient audio defaults.
- Transparent local storage status and one-tap **Delete All App Data** action.

---

## 🛠️ Architecture & Tech Stack

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Persistence**: Room SQLite Database (4 entities: `PauseSession`, `ExerciseCompletion`, `JournalEntry`, `ThoughtCapture`)
- **Key-Value Storage**: Jetpack DataStore Preferences
- **Concurrency**: Kotlin Coroutines & Flow (`StateFlow`, `collectAsStateWithLifecycle`)
- **Testing**: JUnit4, Robolectric, and Roborazzi

---

## 🚀 Setup & Run Instructions

1. **Prerequisites**:
   - Android Studio Ladybug (or newer)
   - JDK 17+
   - Android SDK API 34+

2. **Open the Project**:
   - Clone or export this repository.
   - In Android Studio, select **File > Open** and choose the root project folder.

3. **Build & Run**:
   - Allow Gradle to sync dependencies.
   - Select an emulator or connected physical Android device (Android 8.0+ / API 26+).
   - Press **Run** (`Shift + F10`) or execute `./gradlew installDebug`.

4. **Running Tests**:
   ```bash
   gradle :app:testDebugUnitTest
   ```
