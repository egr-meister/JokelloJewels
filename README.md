# Jokello Jewels

A fully offline Android game in Kotlin + Jetpack Compose with two modes — **Quiz** and **Matching Pairs** — set in a jester's velvet jewel box.

- **applicationId / namespace:** `com.jokellojewels.game`
- **minSdk 24 · compileSdk 36 · targetSdk 36 · JDK 17**
- Portrait only, edge-to-edge, standard Android Back.

---

## Features

**Quiz — "Circles & Spheres"** — 3 difficulty levels (*Easy*, *Medium*, *Hard*) × 10 questions about rings, balls, planets, pearls and the geometry of circles; 4 options each, exactly one correct. One question at a time on the ornate question panel with a progress bar and `n/10`. Tapping an answer turns the correct option green with ✓ and a wrong pick red with ✗, then auto-advances after ~0.75 s. Best score saved per difficulty. On normal phones the quiz never scrolls; the free space under the answers is filled with the jester mascots. Pause panel: Continue / Restart / Menu. Results panel: `Score: X/10`, Continue / Restart / Menu.

**Matching Pairs** — 9 levels:

| Level | Pairs | Columns | Seconds |
|---|---|---|---|
| 1 | 2 | 2 | 60 |
| 2 | 3 | 2 | 70 |
| 3 | 4 | 2 | 70 |
| 4 | 6 | 3 | 80 |
| 5 | 6 | 3 | 80 |
| 6 | 8 | 4 | 100 |
| 7 | 8 | 4 | 100 |
| 8 | 10 | 4 | 110 |
| 9 | 12 | 4 | 120 |

All cards of every level (up to 24) fit on screen at once — the grid is sized from the available width **and** height (portrait cards, 0.76 aspect), no scrolling. A deck of `pairs` items is drawn from 12 themed icons, duplicated and shuffled. Flip two cards; matches stay open (✓ badge), mismatches flip back after 0.7 s. Countdown per level, paused by the pause panel and when the app goes to background. All pairs before time → **Well Done**; timeout → **Out of Time**. Best matched pairs saved per level. Level N unlocks when level N-1 is cleared (level 1 always open).

**Screens:** Main · Menu · Quiz difficulty · Quiz · Quiz results · Levels (9 bubbles + Game Rules panel) · Pairs game · Pairs results · Results summary · Settings · Game Rules.

## Offline only

No `INTERNET` permission, no runtime permissions, no services, receivers, WorkManager, alarms or notifications. No networking, ads, analytics, payments, maps, Firebase or attribution/tracking SDKs. "Privacy Policy" and "Rate the Game" only hand an `ACTION_VIEW` intent to other apps (browser / Play Store); if none is installed a toast is shown.

## Local storage & DataStore

`GameRepository` keeps a single JSON string (`game_data_json`) in DataStore Preferences (`jokello_game.preferences_pb`):

```kotlin
@Serializable data class GameData(
    val quizBest: Map<String, Int> = emptyMap(),   // categoryId -> 0..10
    val pairsBest: Map<String, Int> = emptyMap(),  // "1".."9"  -> best matched pairs
    val settings: Settings = Settings(),
)
@Serializable data class Settings(val soundEnabled: Boolean = true)
```

`GameSerializer` (context-free, `ignoreUnknownKeys = true`, `coerceInputValues = true`) never throws: null / empty / corrupted / wrong-typed JSON → defaults, out-of-range values are clamped. A corrupted DataStore file is replaced via `ReplaceFileCorruptionHandler`; IO errors on read emit defaults.

## Privacy

The app collects nothing. Progress lives only on the device (`allowBackup="false"`). Replace `PRIVACY_POLICY_URL` in `ui/screens/SettingsScreen.kt` with your hosted policy before publishing (Play requires a policy URL in the listing).

## Missing integrations (intentionally not included)

Networking (Retrofit/OkHttp/Ktor), Firebase, ads, analytics, crash reporting, in-app purchases, maps, push (OneSignal), attribution (AppsFlyer), WebView, Room, DI frameworks, Play Games, cloud save. Quiz texts are replaceable (see Art assets).

## Visual concept & unique layout

**Concept: "Royal Jester Stage"** — built from the supplied art pack: velvet curtain stage, royal-purple plates with gold rims, ruby / emerald / amethyst jewels and three jester mascots.

| HEX | Name | Usage |
|---|---|---|
| `#1A0620` | Night | window background, scrims |
| `#2C0D3A` | Plum | surfaces; `#2C0D26` splash (average tone of the stage) |
| `#4A1786` | Velvet | royal purple: panels, card backs, HUD pills |
| `#22073F` | VelvetDeep | panel / card gradient bottom |
| `#C8102E` | Ruby | accents |
| `#19A35B` | Emerald | matched cards, score pill |
| `#F6C453` | Gold | titles, borders, rims |
| `#FFF3DC` | Cream | body text |
| `#D2BFEA` | Lavender | secondary text |
| `#1E8E4E` | CorrectGreen | correct answer + ✓ |
| `#C62839` | WrongRed | wrong answer / low timer + ✗ / "!" |

**Home — "Royal Jester Stage"** (not the mascot → title → stats → button-stack dashboard): the logo hangs under the curtain valance and floats; a crown-shaped arc of jewels twinkles beneath it; the greeting jester and the bowing jester stand on the stage floor at either side, holding the ornate **Start** sign between them. Sized from the screen height — no scrolling.

**Buttons:** image plates with a white bold label drawn in code — ornate jester-scroll plate `btn_menu` (menus, Start, with art icons) and the slim gold-rimmed `btn_plate` (answers, panel actions), both kept at their own aspect ratio. **Question panel:** the panel art at its own aspect ratio with the question drawn on it. **Modal panels** (pause / results / rules) are drawn in Compose (rounded rectangle, purple gradient, gold border) with an art badge on top — never a stretched frame image.

**Pairs cards** follow the reference: royal-purple gradient back with gold rim and the jester-hat emblem; face shows the jewel; matched cards turn emerald with a ✓ badge.

**Typography:** Material 3 with system fonts only — serif bold for titles, sans-serif body; text has a soft shadow over the art.

**Accessibility:** white text on purple plates (≥ 4.5:1), ≥ 48 dp targets (plates ≥ 63 dp tall, icons 56 dp), content descriptions on image buttons, cards, level bubbles and mascots; state never by color alone (✓/✗, ★, padlock, "!" on low timer); headings and live regions; text scales with the system setting (labels on fixed-shape art capped at 1.2–1.3×; screens fall back to scrolling on very small displays).

## App icon

From the art pack: adaptive icon with the curtain stage (logo removed) as background layer and the Jokello Jewels logo as foreground inside the visible mask area; legacy `ic_launcher` (rounded `icon.jpg`) and `ic_launcher_round` for API 24–25. `store/play_store_icon_512.png` is ready for the Play listing.

## Splash

`androidx.core:core-splashscreen`: `Theme.JokelloJewels.Splash` with the stage tone `#2C0D26` and the logo (`ic_splash_logo`), then `Theme.JokelloJewels` (night background, no white flash). No heavy asset.

## Art assets

All art is wired through `ui/Assets.kt` with fixed names in `res/drawable-nodpi/`, produced from the art pack by:

```bash
python3 tools/import_art.py "/path/to/Jokello Jewels"   # Pillow + numpy
```

| Resource | Source art | Used on |
|---|---|---|
| `bg_main.jpg` | background.jpg (logo removed, curtains kept, 9:16) | every screen |
| `logo.png` | logo.png | home, splash, launcher |
| `mascot_a/b/c.png` | greeting jester / jester with emerald / bowing jester | home, menu, quiz, results, rules, settings, fallback |
| `btn_menu.png` | jester plaque | menu, Start, Rules, Rate |
| `btn_plate.png` | middle of the jester plaque (pill ends redrawn) | answers, panel buttons |
| `panel_question.png` | game panel | quiz question |
| `plate_round.png` | empty round button | level bubbles |
| `icon_back.png`, `icon_pause.png` | back / pause buttons | top bars |
| `item_01..12.png` | ruby, emerald, amethyst, star brooch, bell, goblet, gift, chest, lyre, key, potion, fan | pair cards, decorations |
| `deco_hat/mask/book/hourglass/scepter/ticket.png` | jester hat, mask, book, hourglass, scepter, ticket | card backs, menu icons, timer, panels |

Replace a source file and re-run the script — no code change. If a plate's proportions change, update `Assets.*_ASPECT`. SFX are synthesized by `python3 tools/generate_placeholders.py` (`--art` rebuilds simple fallback art and overwrites the imported art).

The quiz texts in `logic/QuizContent.kt` are replaceable; keep the ids `easy` / `medium` / `hard` (they key saved best scores).

## Tech stack

Kotlin 2.0.21 · Jetpack Compose (BOM 2024.12.01) · Material 3 · Navigation Compose 2.8.5 (type-safe `@Serializable` routes) · Lifecycle/ViewModel 2.8.7 · Coroutines 1.9.0 · Flow · DataStore Preferences 1.1.1 · Kotlinx Serialization 1.7.3 · core-splashscreen 1.0.1 · AGP 8.7.3 · Gradle 8.9 · Version catalog `gradle/libs.versions.toml`. Every package imported in code is a direct dependency (Compose `foundation`, `foundation-layout`, `animation`, `animation-core`, `ui-*`, `runtime`, `runtime-saveable`, `lifecycle-viewmodel`, `annotation` declared explicitly).

## Architecture

```
JokelloApp (Application)  ── creates once ──▶ AppContainer(GameRepository, SoundManager)
        │                                         │ syncs soundEnabled → SoundManager (startup + live)
MainActivity ─▶ JokelloTheme ─▶ AppNavHost (typed routes, safe arg decoding, fallbacks)
                                   │
          Screens (Compose, collectAsStateWithLifecycle)
                                   │
   ProgressViewModel · QuizViewModel · PairsViewModel   (immutable UiState via StateFlow,
                                   │                     plain ViewModelProvider.Factory)
              GameRepository (DataStore, one JSON string) ── GameSerializer (pure)
              GameLogic (pure: levels, deck, unlock, completed count) · QuizContent (pure)
```

Navigation safety: every route arg has a default; `toRoute()` is wrapped; unknown category, invalid level/score, locked level, empty or reset storage all show a friendly fallback with Back. Double-tap navigation is ignored unless the source screen is resumed; game-finished navigation happens only after the result is saved (`NonCancellable`). Process death restores the back stack and ViewModels rebuild from args + DataStore.

## Open in Android Studio

1. Android Studio Ladybug (2024.2) or newer. *File → Open* the project folder.
2. Gradle JDK: **17** (*Settings → Build Tools → Gradle → Gradle JDK*).
3. SDK Manager: install **Android SDK Platform 36** and **Build-Tools 36.0.0**.
4. Wrapper: `gradlew` + `gradle/wrapper/gradle-wrapper.jar` (Gradle 8.9) are included. If you prefer to regenerate: `gradle wrapper --gradle-version 8.9`.

### API 36 note

AGP 8.7.x predates official API 36 support, so `gradle.properties` contains `android.suppressUnsupportedCompileSdk=36`. If your toolchain still rejects compileSdk 36, bump to AGP **8.9.x+** with Gradle **8.11.1+** in `libs.versions.toml` / wrapper — no code changes needed.

### 16 KB page size

Pure Kotlin/Compose, no native libraries (`.so`) → compatible with 16 KB memory pages. Still verify the release: Play Console → App bundle explorer, or `zipalign -c -P 16 -v 4 app-release.apk` (CI runs this check), or Android Studio *Build → Analyze APK*.

## Build

**Debug:** `./gradlew :app:assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`

**Unit tests:** `./gradlew :app:testDebugUnitTest` (deck building, unlock/completion, completed count, JSON null/empty/corrupted/unknown keys/missing fields, quiz content shape: 3 levels × 10 questions × 4 options).

**Non-minified release** (first shipped build — see *Staged R8*):

```bash
./gradlew :app:assembleRelease :app:bundleRelease
# APK: app/build/outputs/apk/release/app-release.apk
# AAB: app/build/outputs/bundle/release/app-release.aab
```

Any release task **fails immediately** (`verifyReleaseSigning`) if the four signing variables are missing or the keystore file does not exist. Release is never signed with the debug key.

## PKCS12 keystore generation

```bash
keytool -genkeypair -v -storetype PKCS12 \
  -keystore jokello-release.p12 -alias jokellojewels \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Jokello Jewels, OU=Games, O=Jokello Jewels, C=BY"
```

PKCS12 uses one password for store and key — set `ANDROID_KEY_PASSWORD` equal to `ANDROID_KEYSTORE_PASSWORD`. Back the keystore up safely (with Play App Signing it becomes your **upload key**). Never commit it (`.gitignore` blocks `*.p12/*.jks/*.keystore`).

## Local signing

```bash
export ANDROID_KEYSTORE_PATH="/absolute/path/jokello-release.p12"
export ANDROID_KEYSTORE_PASSWORD='...'
export ANDROID_KEY_ALIAS='jokellojewels'
export ANDROID_KEY_PASSWORD='...'
./gradlew :app:assembleRelease :app:bundleRelease
```

## GitHub Secrets

*Repo → Settings → Secrets and variables → Actions → New repository secret:*

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | `base64 -w0 jokello-release.p12` (macOS: `base64 -i jokello-release.p12`) |
| `ANDROID_KEYSTORE_PASSWORD` | keystore password |
| `ANDROID_KEY_ALIAS` | `jokellojewels` |
| `ANDROID_KEY_PASSWORD` | key password (same as store password for PKCS12) |

## GitHub Actions

`.github/workflows/android-build.yml` runs on push to `main` and manually (*workflow_dispatch*): JDK 17 → SDK Platform 36 + Build Tools 36.0.0 → Gradle 8.9 via `gradle/actions/setup-gradle` (fixed version, so a committed wrapper is not required) → decode keystore to `$RUNNER_TEMP` → unit tests → signed `assembleRelease` + `bundleRelease` → `apksigner verify --print-certs` (fails on verification error or `CN=Android Debug`) → `jarsigner` check of the AAB → 16 KB alignment check → upload **APK** (test artifact) and **AAB** (Google Play artifact) → delete keystore. Secrets are never echoed.

### apksigner

```bash
$ANDROID_HOME/build-tools/36.0.0/apksigner verify --verbose --print-certs app-release.apk
```

Must print `Verified using v2 scheme ... true` and your own certificate DN — never `CN=Android Debug`.

### APK vs AAB

- **AAB** (`app-release.aab`) — the **only** artifact uploaded to Google Play.
- **APK** (`app-release.apk`) — for local testing / sideloading only.

> **A green CI build is not proof the app launches.** Always verify the release on a device.

## Local release verification

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
adb logcat --pid=$(adb shell pidof -s com.jokellojewels.game)
```

Checklist: splash shows plum + jewel mark, then home carousel · Start → Menu · each quiz category plays 10 questions, green/red + ✓/✗, auto-advance, pause Continue/Restart/Menu, result panel and best score · Levels: only 1 open; clear 1 → 2 unlocks; mismatch flips back; timer pauses on pause and on Home button; win and timeout panels · Results summary numbers · Settings: sound toggle persists across restart, privacy/rate links, reset dialog clears progress · Back on every screen · rotate device (stays portrait) · kill app in background (`adb shell am kill com.jokellojewels.game`) and reopen · no crashes in logcat.

## Staged R8

1. Ship the first release with `isMinifyEnabled = false` and `isShrinkResources = false` (current setting), verify it on a device.
2. Then set both to `true` in `app/build.gradle.kts`. `proguard-rules.pro` already contains Kotlinx-Serialization keep-rules plus keeps for `data.*` models and `ui.nav.*` typed routes.
3. Rebuild, then **repeat the full local release verification** (navigation with typed args, DataStore read/write, reset) before uploading the new AAB.

## Data reset

In-app: *Settings → Reset Progress → Reset* (clears quiz bests, pairs bests and settings). Or: *Android Settings → Apps → Jokello Jewels → Storage → Clear data*, or `adb shell pm clear com.jokellojewels.game`.

## Known limitations

- Synthesized SFX are simple tones (drop real `.wav` files into `res/raw` with the same names).
- English only (strings in `res/values/strings.xml`; quiz content in Kotlin).
- No cloud backup (`allowBackup="false"`); uninstalling removes progress.
- On large screens Android 16 (API 36) may ignore the portrait lock for apps targeting 36 (letterboxing/resizing rules); layouts scroll and cap widths to stay usable.
- AGP 8.7.x + compileSdk 36 relies on `android.suppressUnsupportedCompileSdk` (see API 36 note).
- The privacy URL is a placeholder until you host a policy.
