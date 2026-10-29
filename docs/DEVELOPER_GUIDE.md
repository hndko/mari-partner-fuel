# 🛠️ DEVELOPER_GUIDE.md — Panduan Pengembang, Arsitektur & Referensi CLI

Dokumen ini ditujukan bagi Software Engineer, Code Reviewer, dan Kontributor teknis yang mengembangkan dan merawat codebase **Mari Partner Fuel** (*AI-Powered Fuel Expense Tracker for Android*).

---

## 🏛️ 1. Prinsip Arsitektur Inti (Clean Architecture + MVVM + UDF)

Codebase Mari Partner Fuel menerapkan pemisahan tiga layer bersih:

```text
┌─────────────────────────────────────────────────────────────┐
│                         UI LAYER                            │
│  - Jetpack Compose + Material 3 Screens & Components        │
│  - ViewModels mengelola StateFlow<UiState<T>>               │
│  - UI Event / User Intent (Unidirectional Data Flow)        │
└──────────────────────────────▲──────────────────────────────┘
                               │ StateFlow<UiState<T>>
                               │ Dispatches onEvent(...)
┌──────────────────────────────┴──────────────────────────────┐
│                        DOMAIN LAYER                         │
│  - Pure Kotlin Use Cases (bebas framework Android)          │
│  - Business Invariants: Kalkulasi biaya BBM, validasi scan  │
│  - Domain Models & Repository Interfaces                    │
└──────────────────────────────▲──────────────────────────────┘
                               │ Implements Interfaces
┌──────────────────────────────┴──────────────────────────────┐
│                         DATA LAYER                          │
│  - Repository Implementations                               │
│  - Remote: Supabase (Auth, Postgrest, Storage, Functions)   │
│  - Hardware Engine: Google ML Kit Text Recognition          │
│  - Local: TokenStorage Keystore & SharedPreferences         │
└─────────────────────────────────────────────────────────────┘
```

### Standar Penanganan Status UI (`UiState<T>`)
Setiap layar mengonsumsi `UiState<T>` terstandar untuk menjamin tidak ada layar yang *hang* atau *flicker*:
```kotlin
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>
    data class Empty(val message: String) : UiState<Nothing>
}
```

---

## ⚡ 2. Uji Coba Cepat Tanpa Android Studio (`run-app.bat`)

Untuk menguji aplikasi di laptop tanpa membuka Android Studio yang berat, gunakan script otomatis yang disediakan di root repositori:

```powershell
.\run-app.bat
```
Script akan otomatis:
1. Mendeteksi lokasi Android SDK dan mendeteksi apakah ada HP fisik yang terhubung (USB / Wi-Fi).
2. Jika memilih emulator AVD (`test_avd`), menjalankannya dengan parameter hemat daya:
   `-no-boot-anim -netdelay none -netspeed full -cores 2 -memory 2048 -gpu host`.
3. Mengompilasi APK debug via Gradle daemon (`assembleDebug`).
4. Memasang APK ke perangkat dan langsung membuka `MainActivity`.
5. Menyediakan opsi pemantauan logcat real-time khusus filter `id.maripartner.fuel`.

---

## 💻 3. Referensi Perintah CLI Harian (CLI Command Reference)

### 3.1 Menjalankan Unit Tests (137 Tests / 29 Test Suites)
```powershell
# Jalankan seluruh unit test:
.\gradlew.bat testDebugUnitTest

# Jalankan suite spesifik:
.\gradlew.bat testDebugUnitTest --tests "id.maripartner.fuel.core.performance.PerformanceAndStressTest"
```

### 3.2 Kompilasi Build
```powershell
# Build APK Debug:
.\gradlew.bat assembleDebug

# Build Release APK (R8 Minified ~3.39 MB):
.\gradlew.bat assembleRelease

# Build Release AAB untuk Google Play Store (R8 Minified ~7.57 MB):
.\gradlew.bat bundleRelease
```

### 3.3 Pemeriksaan Kualitas & Kebersihan Rahasia (Zero-Secret)
```powershell
# Analisis Lint rilis:
.\gradlew.bat lintVitalRelease

# Pindai kebocoran kunci rahasia di kode sumber:
git grep -n -E "(service_role|sbp_|sk_live|secret_key|private_key)" app/src/main
```

---

## 🔒 4. Bootstrap Lingkungan Aman (Zero-Secret Policy)

Sesuai aturan di [`AGENTS.md`](../AGENTS.md):
- Kunci rahasia backend (`GEMINI_API_KEY`, `QRISPY_API_KEY`, `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`) **TIDAK PERNAH** ditaruh di APK klien; hanya di Supabase Secrets Vault backend.
- Konfigurasi publik klien dibaca secara aman melalui `app/build.gradle.kts`:
  - `SUPABASE_URL`
  - `SUPABASE_ANON_KEY`
  - `GOOGLE_WEB_CLIENT_ID`
- Nilai lokal disimpan di `local.properties` (terdaftar di `.gitignore`).
- Redaksi kunci aktif via `AppConfig.redactedKey()` saat mencetak ke debug logger.
