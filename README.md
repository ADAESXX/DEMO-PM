# ShareChat - Android Sharesheet & Firebase Demo Application

A modern, production-grade Android demonstration application developed with **Jetpack Compose**, **Material 3 Adaptive**, **Firebase Firestore**, and **Firebase Crashlytics**.

---

## 🚀 Key Features

1. **Android Sharesheet Integration**:
   - Outbound sharing via `Intent.ACTION_SEND` and `Intent.createChooser()`.
   - Rich previews including message text, sender details, and attached camera photos.
   - Secure temporary URI permissions with `FileProvider` and `FLAG_GRANT_READ_URI_PERMISSION`.
   - Inbound receiver in `MainActivity` accepting text and images shared from other Android apps into the chat.

2. **Camera Capture & Runtime Permission**:
   - Camera access managed via `rememberLauncherForActivityResult` with `ActivityResultContracts.RequestPermission()`.
   - High-resolution capture via `ActivityResultContracts.TakePicture()`.
   - Clean permission rationale and fallback handling.

3. **Multi-User Real-Time Chat & Persistence**:
   - Built on Cloud Firestore with reactive Kotlin `callbackFlow`.
   - Instant in-memory demo resilience (works seamlessly even before remote Firebase configuration).
   - Fast participant switcher (Alice, Bob, Clara) for 2-way conversation demonstration on a single device.

4. **Firebase Crashlytics Observability**:
   - User identification tracking (`crashlytics.setUserId`).
   - Custom key-value diagnostics (`crashlytics.setCustomKey`).
   - Breadcrumb logging (`crashlytics.log`).
   - Interactive "Trigger Crashlytics Crash" button for live presentation verification.

5. **Adaptive UI & State Resilience**:
   - Dynamically adapts between **Single-Pane** (portrait phones) and **Dual-Pane** (landscape, tablets, foldables).
   - Unidirectional Data Flow (UDF) managed by `ChatViewModel`, `ChatUiState`, and `ChatUiEvent`.
   - State persists across configuration changes and device rotation via `SavedStateHandle`.
   - Background tasks distributed across Kotlin Coroutines (`Dispatchers.IO`).

---

## 📁 Package Architecture

```
com.example.sharechat/
├── data/
│   ├── model/
│   │   ├── ChatMessage.kt           # Immutable message entity for UI and Firestore
│   │   └── ChatUser.kt              # Participant representation
│   └── repository/
│       ├── ChatRepository.kt        # Abstraction interface for messaging operations
│       └── FirebaseChatRepository.kt# Firestore + reactive Flow implementation
├── ui/
│   ├── chat/
│   │   ├── ChatScreen.kt            # Main Compose chat screen
│   │   ├── ChatUiEvent.kt           # UDF user action events
│   │   ├── ChatUiState.kt           # Immutable single source of truth state
│   │   └── ChatViewModel.kt         # Centralized ViewModel with Coroutines & SavedState
│   ├── components/
│   │   ├── AdaptiveChatScaffold.kt  # Responsive single/dual pane scaffold
│   │   ├── ChatBubble.kt            # Message bubble with image and share action
│   │   └── MessageInputBar.kt       # Composer with camera and sharesheet triggers
│   └── theme/
│       ├── Color.kt                 # Material 3 color definitions
│       ├── Theme.kt                 # Dynamic/dark/light theme setup
│       └── Type.kt                  # Typography definitions
├── util/
│   ├── CameraUtils.kt               # Runtime permissions and FileProvider URI generator
│   ├── CrashlyticsHelper.kt         # Firebase Crashlytics logging and test triggers
│   └── ShareSheetHelper.kt          # Android Sharesheet launch utilities
├── MainActivity.kt                  # Edge-to-edge Activity with incoming share receiver
└── ShareChatApplication.kt          # Application initialization & Firebase setup
```

---

## 🛠️ Requirements & Setup

1. **Android Studio**: Android Studio Koala / Ladybug (or Hedgehog+).
2. **JDK**: Java 17.
3. **Android SDK**: `minSdk = 26`, `targetSdk = 35`, `compileSdk = 35`.

### Opening in Android Studio
1. Open Android Studio.
2. Select **Open** and browse to:
   `C:\Users\DELL\.gemini\antigravity\scratch\ShareChatApp`
3. Allow Gradle to sync dependencies.

### Connecting to your Firebase Project
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create an Android App with package name `com.example.sharechat`.
3. Download your official `google-services.json` and replace the placeholder in `app/google-services.json`.
4. Enable **Cloud Firestore** and **Crashlytics** in the Firebase Console.

---

## ⏱️ Presentation Guide
For the 5-minute timed presentation script, refer to [DEMO_GUIDE.md](file:///C:/Users/DELL/.gemini/antigravity/scratch/ShareChatApp/DEMO_GUIDE.md).
