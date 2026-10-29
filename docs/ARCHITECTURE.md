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

---

## 8. Arsitektur Autentikasi & Supabase Data Source

1. **Supabase Client Provider (`SupabaseClientProvider.kt`):**
   - Menginisialisasi plugin `Auth` (dengan session auto-refresh dan local storage recovery), `Postgrest`, `Storage`, dan `Functions`.
   - Menggunakan Ktor OkHttp Engine yang stabil dan hemat memori pada Android.
2. **Abstraksi Repository & Use Cases:**
   - Layer domain tidak memiliki ketergantungan langsung ke SDK Supabase.
   - `AuthRepository` diimplementasikan oleh `AuthRepositoryImpl` yang menerjemahkan model Supabase menjadi model domain bersih (`UserSession`, `UserProfile`).
   - Use Case murni mengeksekusi aturan validasi input *Fail-Fast* (`ValidateAuthInputUseCase`) sebelum memicu request jaringan.
3. **Siklus Pemulihan Sesi Otomatis (*Auto-Session Recovery*):**
   - Saat aplikasi dibuka, `SplashScreen` memanggil `GetCurrentSessionUseCase()`.
   - Jika sesi pengguna aktif dan valid, aplikasi langsung beralih ke `Dashboard`.
   - Jika sesi kosong atau kedaluwarsa, aplikasi mengarahkan pengguna ke layar `Login`.
4. **Pengelolaan Foto Struk Privat (`ReceiptStorageManager.kt`):**
   - Validasi ketat di sisi klien: batas ukuran 5 MB dan tipe MIME (`image/jpeg`, `image/png`, `image/webp`).
   - Penamaan berkas terisolasi per user ID: `receipts/{userId}/{UUID}.{ext}` untuk mencegah konflik nama berkas.
   - Akses foto hanya melalui *Signed URL* berumur 20 menit yang dibuat secara dinamis.

---

## 9. Arsitektur Hybrid OCR & AI Extraction (Tahap 4)

Pendekatan **Local-First Hybrid OCR** menjamin privasi, kecepatan pengenalan teks, dan ketersediaan offline tanpa bergantung pada layanan cloud:

```
[Kamera / Galeri]
       │
       ▼ (Bitmap <= 1920px)
[ReceiptOcrProcessor] ──► Google ML Kit (On-Device Local OCR)
       │
       ▼ (Raw Text Lines)
[ReceiptTextParser] ────► Ekstraksi Regex SPBU, Total Rupiah, Liter, Harga/L
       │
       ▼ (ReceiptCandidateData)
[ScanReviewScreen] ◄──── (Human-in-the-Loop Review & Discrepancy Banner)
       │
       ├─ (Opsional: User Minta Optimasi AI Cloud)
       │       │
       │       ▼ (Consent Dialog Diterima)
       │  [AiReceiptExtractor] ──(JWT Token)──► [Edge Function: extract-fuel-receipt]
       │                                                    │
       │                                                    ▼ (Gemini 1.5 Flash API)
       │  [Merge Candidate Data] ◄──────────────────────────┘
       │
       ▼ (User Menekan "Konfirmasi & Simpan")
[Save Fuel Transaction] ──► [PostgreSQL & Private Storage]
```

1. **Local-First On-Device Engine (`ReceiptOcrProcessor.kt` & `ReceiptTextParser.kt`):**
   - Gambar distandarisasi dan dikompresi agar dimensi maksimal 1920 piksel guna mencegah `OutOfMemoryError`.
   - Menggunakan Google ML Kit Text Recognition lokal tanpa transmisi internet.
   - Parser regex mengenali format struk SPBU Indonesia (Pertamina, Shell, BP-AKR), menangani pemisah ribuan titik pada Rupiah dan desimal koma pada liter, serta mendiferensiasikan nominal pembayaran dari uang tunai/kembalian (*cash/change*).
   - Menghitung diskrepansi matematis otomatis: `abs(total - (volume * harga)) > toleransi`.
2. **Cloud AI Fallback via Supabase Edge Function (`extract-fuel-receipt`):**
   - Fallback cerdas berbasis cloud AI hanya dijalankan jika diminta secara sadar oleh pengguna (*explicit user action*) melalui dialog persetujuan privasi.
   - Tidak ada API key AI yang disimpan di aplikasi Android. Permintaan memanggil Edge Function dengan menyertakan token JWT pengguna.
   - Edge Function memverifikasi kepemilikan file di private storage (`storage_path.startsWith("${user.id}/")`), memanggil Google Gemini 1.5 Flash dengan JSON schema terstruktur, dan mencatat metrik ke tabel `receipt_scan_jobs`.
3. **Prinsip Human-in-the-Loop Tanpa Auto-Save:**
   - Hasil ekstraksi OCR lokal maupun AI hanya disajikan sebagai kandidat (`ReceiptCandidateData`).
   - Seluruh kolom dapat disunting oleh pengguna di layar `ScanReviewScreen`.
   - Transaksi baru disimpan ke database jika dan hanya jika pengguna menekan tombol **Konfirmasi & Simpan Transaksi**.

---

## 10. Arsitektur Dashboard, Analitik, Ekspor CSV, & Pengingat Servis (Tahap 5)

### 10.1 Agregasi Dashboard & Analitik Pengeluaran
- **Zero N+1 Query:** Pengambilan data transaksi menggunakan filter rentang tanggal server-side (`TransactionFilter`) pada Postgrest Supabase. Seluruh kalkulasi agregasi (total biaya, total volume, rata-rata tertimbang, perbandingan antarbulan, persentase pengeluaran per kendaraan) dieksekusi secara instan dan efisien di Domain Layer (`GetDashboardSummaryUseCase` & `GetAnalyticsReportUseCase`).
- **Penanganan Data Volume Tidak Lengkap:** Transaksi tanpa volume tidak dianggap 0 saat menghitung rata-rata harga per liter; hanya transaksi dengan volume yang dimasukkan dalam kalkulasi weighted average, disertai flag informatif `hasIncompleteVolumeData`.
- **Mencegah Angka Menyesatkan (*Misleading Comparisons*):** Jika periode pembanding belum memiliki data (Rp 0), persentase perubahan dinyatakan sebagai `null` (bukan tak hingga / Infinity).

### 10.2 Estimasi Efisiensi BBM (km/L) Berbasis Odometer
- `CalculateFuelEfficiencyUseCase` memproses transaksi berurutan per kendaraan berdasarkan odometer naik.
- Validasi selisih jarak: `delta_km = curr_odo - prev_odo`. Jika `delta_km <= 0` (odometer menurun akibat reset atau kesalahan ketik) atau `delta_km > 5000 km`, sistem menandainya sebagai anomali dan mengabaikannya agar tidak merusak akurasi rata-rata.
- Memerlukan minimal 2 pencatatan transaksi berurutan dengan odometer dan volume lengkap untuk menyajikan estimasi km/liter.

### 10.3 Ekspor CSV & Mitigasi Formula Injection (OWASP Compliance)
- `ExportTransactionsCsvUseCase` mengonversi seluruh riwayat transaksi pengguna ke format CSV standar RFC 4180.
- **Proteksi Formula Injection:** Nilai teks yang diawali karakter eksekusi spreadsheet (`=`, `+`, `-`, `@`, `\t`, `\r`) dinetralkan dengan menyisipkan awalan tanda petik tunggal (`'`). Angka murni negatif (`-50`) atau positif (`+100`) dipertahankan sebagai numerik sah.
- **Enkoding UTF-8 dengan BOM:** Menyertakan karakter Byte Order Mark (`\uFEFF`) di awal berkas agar Microsoft Excel di Windows dapat membaca aksen dan pemisah secara otomatis.
- **Integrasi SAF:** Menggunakan Android Storage Access Framework (`CreateDocument("text/csv")`) untuk memberikan kendali penuh lokasi berkas kepada pengguna tanpa meminta izin storage berlebih.

### 10.4 Pengingat Perawatan Kendaraan & Alarm Notifikasi
- Dikelola oleh `MaintenanceReminderRepositoryImpl` dengan relasi Composite FK `(vehicle_id, user_id)` untuk mencegah IDOR.
- Pemicu ganda: Tanggal kalender (`target_date`), batas kilometer odometer (`target_odometer_km`), atau kombinasi keduanya.
- Notifikasi lokal dijadwalkan secara aman menggunakan `AlarmManager.setAndAllowWhileIdle()` pada pukul 08:00 pagi melalui `ReminderNotificationReceiver` pada channel `fuel_service_reminders`. Alarm dibatalkan secara atomik saat pengingat diselesaikan atau dihapus.

---

## 11. Arsitektur Entitlement & Penegakan Kuota AI Otoritatif (Tahap 6)

### 11.1 Prinsip Zero-Client-Trust
1. **Status Pro & Hak Akses:** Klien Android **TIDAK PERNAH** dipercaya untuk menentukan status `is_pro` atau sisa kuota. Backend PostgreSQL & Supabase Edge Functions adalah satu-satunya sumber kebenaran (*Single Source of Truth*).
2. **Katalog Paket Backend Otoritatif:**
   - **Mari Free:** Rp 0, 2 kendaraan aktif maksimal, 5 pemindaian AI per bulan, OCR lokal on-device tanpa batas, ekspor CSV standar.
   - **Mari Pro Bulanan:** Rp 29.000 / bulan, kendaraan tanpa batas, 100 pemindaian AI per bulan, prioritas AI tinggi, analitik lanjutan.
   - **Mari Pro Tahunan:** Rp 290.000 / tahun (hemat 17%), seluruh fitur Pro lengkap.
3. **Penyedia Layanan Hak Akses (`get-my-entitlements`):**
   - Edge Function memvalidasi JWT pengguna.
   - Menjalankan fungsi PostgreSQL `get_user_effective_entitlement(user.id)` yang memverifikasi status langganan (`active` atau `grace_period`) terhadap waktu server (`now() <= expires_at`).
   - Menghitung pemakaian kuota bulan berjalan dari tabel `ai_usage_logs` (`status in ('reserved', 'confirmed')`).
   - Mengembalikan payload terstruktur: paket aktif, daftar fitur yang diizinkan, sisa kuota AI, dan timestamp reset otomatis awal bulan depan.

### 11.2 Alur Reservasi Kuota AI Atomik & Idempotensi
```
[Android Client]                 [Edge Function: extract-fuel-receipt]              [PostgreSQL Database]
       │                                          │                                          │
       ├── 1. POST (path / text, idempotency_key)►│                                          │
       │                                          ├── 2. RPC reserve_ai_quota ──────────────►│
       │                                          │      (Cek limit & pemakaian bulan ini)   ├─ Lock & Insert 'reserved'
       │                                          │◄───── 3. Result { allowed, limit, used }─┤
       │                                          │                                          │
       │                               (Jika Kuota Habis: allowed = false)                    │
       │◄── 4. 403 AI_QUOTA_EXCEEDED ─────────────┤                                          │
       │    (Prompt Upgrade Pro, Fallback Lokal)  │                                          │
       │                                          │                                          │
       │                               (Jika Kuota Tersedia: allowed = true)                 │
       │                                          ├── 5. Panggil Gemini 1.5 Flash API        │
       │                                          │                                          │
       │                                          ├─ (Sukses: finalize_ai_quota(true)) ─────►│ Update 'confirmed'
       │                                          │  (Gagal: finalize_ai_quota(false)) ─────►│ Update 'released' (Refund)
       │◄── 6. Return Structured Fields ──────────┤                                          │
```

### 11.3 Penegakan Batas Kendaraan di Tingkat Database
- Trigger PostgreSQL `trg_enforce_vehicle_limit` secara otomatis memeriksa jumlah kendaraan aktif pengguna terhadap batas `max_vehicles` dari paket yang sedang aktif saat operasi `INSERT` atau `UPDATE` status arsip.
- Klien Android (`VehicleFormViewModel`) menyediakan validasi dini ramah pengguna melalui `CheckFeatureAccessUseCase.canAddVehicle()` sebelum pengiriman formulir.

---

## 12. Arsitektur Pembayaran Terpadu (Dual-Track Payments) & Aktivasi Langganan (Tahap 7)

### 12.1 Regulasi Dual-Track (Google Play Billing vs QRISpy)
1. **Google Play Store Build**: Untuk build yang didistribusikan melalui Play Store, aplikasi wajib menggunakan **Google Play Billing Library v7.1.1** (`com.android.billingclient:billing-ktx`). Pembelian paket langganan in-app ditagihkan secara otomatis via Google Play Account.
2. **Direct/Web Distribution**: Untuk distribusi di luar Play Store (website resmi/sideload), gateway **QRISpy** (`https://api.qrispy.id`) digunakan dengan dynamic QRIS yang kedaluwarsa dalam 15 menit.
3. Klien Android mengabstraksi kedua jalur di balik `PaymentRepository` dan use cases domain tanpa membocorkan logika platform ke UI layar Pro.

### 12.2 Alur Google Play Billing & Anti-Replay Token
```text
[PlayBillingManager] ──(Launch Flow)──► [Google Play Store Sheet]
        │                                       │
        │◄──────(Purchase Completed)────────────┘
        ▼
[Edge Function: verify-play-purchase]
        ├── 1. Validasi Keunikan Token di payment_transactions (Anti-Replay)
        ├── 2. Verifikasi Token & Package Name ke Google Play Developer API
        ├── 3. Konfirmasi Status Pembayaran 'paid'
        └── 4. RPC activate_subscription_for_payment() (Atomik)
```

### 12.3 Alur Dynamic QRISpy & Idempotensi Webhook
- Pembuatan order transaksi via `create-checkout` dengan `payment_reference` berformat unik `MPF-YYYYMMDD-XXXX`.
- Gateway QRISpy memanggil Edge Function `payment-webhook`.
- Payload diverifikasi melalui signature HMAC-SHA256, kemudian dicatat secara atomik ke tabel `payment_webhook_events` via RPC `record_payment_webhook_event`.
- Duplikasi webhook diidentifikasi berdasarkan `event_id` dan direspons dengan HTTP 200 tanpa mengeksekusi mutasi kedua.

### 12.4 Perpanjangan Langganan Atomik (Cumulative Extension)
Dalam RPC `activate_subscription_for_payment`:
- Jika pengguna telah memiliki langganan Pro aktif untuk paket yang sama, tanggal kedaluwarsa baru dihitung dari sisa masa aktif yang ada:
  `new_expires_at = current_subscription.expires_at + plan_duration`.
- Kuota pemakaian AI bulan berjalan di-reset atau ditambahkan kuota prioritas Pro secara atomik.



