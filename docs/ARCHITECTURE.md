# ARCHITECTURE.md --- Mari Partner Fuel

**Arsitektur Sistem & Spesifikasi Teknis**\
**Aplikasi:** Mari Partner Fuel (Android Native)\
**Versi:** 1.0 (MVP Baseline)\
**Minimum SDK:** Android API 26 (Android 8.0 Oreo)\
**Target SDK:** Android API 35 (Android 15)

---

## 1. Prinsip Arsitektur

Mari Partner Fuel dirancang menggunakan **Clean Architecture** dengan pola **MVVM (Model-View-ViewModel)** dan **Unidirectional Data Flow (UDF)**. Pemisahan tanggung jawab (*separation of concerns*) ditegakkan secara ketat agar setiap komponen mudah diuji (*testable*), andal (*reliable*), dan dapat dirawat (*maintainable*).

```
┌─────────────────────────────────────────────────────────────┐
│                         UI LAYER                            │
│  Jetpack Compose + Material 3 Screens, Components, Theme    │
│  ViewModels, UiState (Sealed/Data class), UiEvent (Sealed)  │
└──────────────────────────────▲──────────────────────────────┘
                               │ Observes Flow / StateFlow
                               │ Dispatches Intent / Event
┌──────────────────────────────┴──────────────────────────────┐
│                        DOMAIN LAYER                         │
│  Use Cases (Pure Business Rules, Calculations, Validations) │
│  Domain Models (Clean POJOs/Data Classes)                   │
│  Repository & Provider Interfaces                           │
└──────────────────────────────▲──────────────────────────────┘
                               │ Implements
┌──────────────────────────────┴──────────────────────────────┐
│                         DATA LAYER                          │
│  Repository Implementations                                 │
│  Remote Data Sources: Supabase (Auth, PostgREST, Storage)   │
│  Local Hardware/Engines: Google ML Kit Text Recognition     │
│  Payment Gateway Adapters (QRISpy, Midtrans, Xendit)        │
│  Local Preferences / Cache                                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Struktur Package Standar

Struktur package modular dalam modul `:app` disusun sebagai berikut:

```text
id.maripartner.fuel
├── core
│   ├── common
│   │   ├── result          // ResultWrapper, AppException, NetworkState
│   │   ├── util            // DateUtils, CurrencyFormatter, NumberUtils
│   │   └── config          // AppConfig (Supabase URL, Anon Key, Flavors)
│   ├── designsystem
│   │   ├── theme           // Color, Type, Shape, Theme (Dark/Light)
│   │   └── component       // PrimaryButton, StateView, FuelCard, TopBar
│   ├── navigation          // NavRoute, AppNavHost, BottomNavBar
│   └── security            // KeystoreManager, TokenStorage
├── data
│   ├── local               // Preferences, Cache
│   ├── remote
│   │   ├── supabase        // SupabaseClientProvider, DTOs
│   │   └── payment         // QRISpyClient, GatewayAdapters
│   ├── ocr                 // MlKitOcrEngine
│   └── repository          // VehicleRepoImpl, TransactionRepoImpl, etc.
├── domain
│   ├── model               // Vehicle, FuelTransaction, Subscription, etc.
│   ├── repository          // AuthRepository, VehicleRepository, etc.
│   └── usecase             // CalculateFuelAverageUseCase, ValidateScanUseCase
└── feature
    ├── auth                // LoginScreen, RegisterScreen, ForgotPasswordScreen
    ├── dashboard           // DashboardScreen, DashboardViewModel
    ├── receipt_scan        // CameraScreen, ScanReviewScreen, OcrReviewViewModel
    ├── transactions        // TransactionListScreen, TransactionFormScreen
    ├── vehicles            // VehicleListScreen, VehicleFormScreen
    ├── analytics           // AnalyticsScreen, AnalyticsViewModel
    ├── reminders           // RemindersScreen, ReminderViewModel
    ├── subscription        // ProPlansScreen, CheckoutScreen, QrisView
    └── settings            // SettingsScreen, ProfileScreen
```

---

## 3. Aliran Data dan State Management (UDF)

Setiap layar memiliki siklus unidirectional:
1. **User Action / Event**: Pengguna berinteraksi dengan Composable (klik tombol, ubah teks).
2. **Event Dispatch**: Composable memanggil method ViewModel via `onEvent(event)`.
3. **Use Case Execution**: ViewModel memanggil domain Use Case yang mengeksekusi aturan bisnis.
4. **Repository Fetch**: Use Case berinteraksi dengan Repository untuk mengambil/menyimpan data.
5. **State Emitted**: ViewModel memperbarui `MutableStateFlow<UiState>` secara immutable.
6. **UI Render**: Jetpack Compose me-render ulang UI berdasarkan `UiState` terbaru (`Loading`, `Success`, `Error`, `Empty`).

### Standar UiState
```kotlin
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>
    data class Empty(val message: String) : UiState<Nothing>
}
```

---

## 4. Design System & Theming

Aplikasi mengadopsi **Material 3 (M3)** dengan skema warna ramah otomotif/BBM:
- **Primary Color:** Petrol Teal (`#006D77` Light, `#83C5BE` Dark)
- **Secondary Color:** Amber Fuel (`#E29578` Light, `#FFDDD2` Dark)
- **Neutral / Surface:** Clean slate and off-white background
- **Accessibility:** Mendukung tema Gelap (*Dark Mode*) dan Terang (*Light Mode*) dengan rasio kontras WCAG AA (minimal 4.5:1 untuk teks normal).
- **Komponen Inti:**
  - `FuelPrimaryButton`: Tombol aksi utama dengan state loading terintegrasi (mencegah double-tap).
  - `FuelSecondaryButton`: Tombol aksi sekunder outline.
  - `FuelStateView`: Penangan standar untuk Loading spinner, Empty state (ilustrasi + pesan), dan Error state (pesan + tombol coba lagi / retry).
  - `FuelTopBar`: App bar konsisten dengan navigasi kembali dan judul yang jelas.

---

## 5. Navigasi

Navigasi menggunakan **Jetpack Navigation Compose**:
- **Single Activity:** `MainActivity.kt` bertindak sebagai container tunggal.
- **Root Graph (`AppNavHost`):** Mengelola transisi antar alur (Auth Graph -> Main Graph).
- **Bottom Navigation Bar:** Menampilkan 5 tab utama bagi pengguna login:
  1. **Beranda (`dashboard`)**
  2. **Riwayat (`transactions`)**
  3. **Analitik (`analytics`)**
  4. **Kendaraan (`vehicles`)**
  5. **Profil (`settings`)**
- **Modal / Detail Screens:**
  - Scan Struk (`receipt_scan` -> `scan_review`)
  - Form Transaksi BBM (`transaction_form`)
  - Detail Transaksi (`transaction_detail/{id}`)
  - Langganan Pro (`pro_plans` -> `checkout/{planId}`)

---

## 6. Kompatibilitas Android Minimum API 26

Untuk menjamin keandalan 100% pada Android 8.0 (API 26) hingga Android 15 (API 35):
1. **Core Library Desugaring (`com.android.tools:desugar_jdk_libs:2.1.2`):**
   Memungkinkan penggunaan API Java modern seperti `java.time.Instant`, `OffsetDateTime`, `LocalDate`, dan Stream API di API 26 tanpa crash runtime.
2. **Notification Channels:** Seluruh pengingat servis/BBM wajib menggunakan Notification Channel yang didaftarkan pada level Application untuk memenuhi syarat Android O (API 26+).
3. **Runtime Permissions:** Pengelolaan izin runtime (`CAMERA`, `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE`, `POST_NOTIFICATIONS`) menggunakan `rememberLauncherForActivityResult`.
4. **Vector Drawables & Icons:** Seluruh aset ikon menggunakan VectorDrawable / Compose ImageVectors untuk mencegah degradasi resolusi pada berbagai kepadatan layar (mdpi hingga xxxhdpi).

---

## 7. Keamanan dan Konfigurasi Environment

1. **Zero Secret Policy:** Tidak ada token rahasia, Supabase service-role key, ataupun API secret payment gateway di source code maupun APK client.
2. **Klien Supabase:** Hanya menggunakan `SUPABASE_URL` dan `SUPABASE_ANON_KEY` publik. Semua mutasi data dilindungi oleh PostgreSQL Row Level Security (RLS) berbasis `auth.uid()`.
3. **Injeksi Konfigurasi:** Nilai environment diinjeksikan saat build melalui `BuildConfig` yang bersumber dari `gradle.properties` / `local.properties` / environment variable CI/CD.
4. **Template Aman:** Menyediakan file `.env.example` yang hanya berisi placeholder tanpa nilai nyata.
