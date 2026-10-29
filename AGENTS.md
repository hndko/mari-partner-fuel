# 🤖 AGENTS.md — Panduan & Aturan Utama Pengembangan Proyek Mari Partner Fuel

Dokumen ini adalah sumber acuan hukum dan aturan rekayasa perangkat lunak tertinggi bagi seluruh AI Coding Assistant dan pengembang yang bekerja pada repositori **Mari Partner Fuel**. Seluruh instruksi dalam dokumen ini bersifat mengikat dan wajib dipatuhi tanpa pengecualian. Jika aturan bertentangan, urutan prioritasnya adalah: **keamanan dan integritas data**, **instruksi pengguna saat ini**, **dokumen ini (AGENTS.md)**, **PRD (`docs/PRD.md`)**, lalu **pola kode lama**.

---

## 📌 Metadata Proyek

- **Nama Aplikasi**: **Mari Partner Fuel** (AI-Powered Fuel Expense Tracker for Android)
- **Versi Saat Ini**: `v1.6.0`
- **Platform**: Android Native (Minimum SDK API 26 Android 8.0 Oreo, Target SDK API 35 Android 15)
- **Bahasa & UI**: Kotlin 2.0.21, Jetpack Compose BOM 2024.10.00, Material 3
- **Backend & Basis Data**: Supabase PostgreSQL 15+, Private Storage, Row Level Security (RLS), Edge Functions
- **OCR & AI Strategy**: Google ML Kit Text Recognition (lokal) + Cloud AI Fallback via Edge Function
- **Payment Gateway**: Dual-Track (Google Play Billing v7.1.1 untuk Play Store, QRISpy/Midtrans untuk Direct/Web)
- **Dokumentasi Proyek**: [Portal Dokumentasi](docs/README.md) & [Buku Panduan Pengembang](docs/03-developer-guide.md)
- **Histori Rilis**: `README.md`
- **Sumber Versi Runtime**: `app/build.gradle.kts` (`versionName = "1.6.0"`, `versionCode = 7`)

---

## 📋 Daftar Isi

1. [🏛️ 10 Pilar Aturan Wajib AI Assistant & Pengembang](#-10-pilar-aturan-wajib-ai-assistant--pengembang)
   - [🎯 1. Peran & Prinsip Kerja Utama (Role & Prime Directives)](#-1-peran--prinsip-kerja-utama-role--prime-directives)
   - [🏗️ 2. Arsitektur Android MVVM + Repository & Clean Layering](#-2-arsitektur-android-mvvm--repository--clean-layering)
   - [🎨 3. UI Murni Jetpack Compose & Material 3 (Zero DB Query in View)](#-3-ui-murni-jetpack-compose--material-3-zero-db-query-in-view)
   - [🏢 4. Isolasi Kepemilikan Data & PostgreSQL RLS (Scoped Ownership)](#-4-isolasi-kepemilikan-data--postgresql-rls-scoped-ownership)
   - [⚡ 5. Strategi OCR Hybrid & Batasan Cloud AI (Local First)](#-5-strategi-ocr-hybrid--batasan-cloud-ai-local-first)
   - [💳 6. Standar Pembayaran QRISpy, Webhook & Idempotensi Langganan](#-6-standar-pembayaran-qrispy-webhook--idempotensi-langganan)
   - [🔒 7. Standar Keamanan OWASP, Private Storage & Zero-Secret](#-7-standar-keamanan-owasp-private-storage--zero-secret)
   - [🧪 8. Standar Pengujian Mutu (Quality Gates) & Definition of Done](#-8-standar-pengujian-mutu-quality-gates--definition-of-done)
   - [📦 9. Aturan Git Commit & Push Otomatis & SemVer (Conventional Commits)](#-9-aturan-git-commit--push-otomatis--semver-conventional-commits)
   - [🔄 10. Kewajiban Pengkinian Dokumen (Mandatory Sync)](#-10-kewajiban-pengkinian-dokumen-mandatory-sync)
2. [📁 Konsep Tata Letak Direktori (Folder-Foldering Hierarchy)](#-konsep-tata-letak-direktori-folder-foldering-hierarchy)
   - [🌳 Diagram Pohon Direktori](#-diagram-pohon-direktori)
   - [🧩 Penjelasan Modul Direktori Proyek](#-penjelasan-modul-direktori-proyek)
3. [🛡️ Standar Wajib Audit Kualitas & Kesiapan Produksi (Quality Gate)](#-standar-wajib-audit-kualitas--kesiapan-produksi-quality-gate)

---

## 🏛️ 10 Pilar Aturan Wajib AI Assistant & Pengembang

```text
┌──────────────────────────────────────────────────────────────────────────────────┐
│              10 PILAR ATURAN WAJIB PENGEMBANGAN MARI PARTNER FUEL                │
├───────────────────┬───────────────────┬───────────────────┬──────────────────────┤
│ 1. Prime Directive│ 2. Clean MVVM     │ 3. Compose M3 UDF │ 4. PostgreSQL RLS    │
│ 5. Local OCR First│ 6. QRISpy Gateway │ 7. Zero Secret    │ 8. Strict Quality    │
│ 9. SemVer AutoPush│ 10. Mandatory Sync│                   │                      │
└───────────────────┴───────────────────┴───────────────────┴──────────────────────┘
```

---

### 🎯 1. Peran & Prinsip Kerja Utama (Role & Prime Directives)

1. **Senior Engineering Mindset**: Bertindak sebagai Senior Android Engineer, Security Auditor, dan Tech Lead. Selalu utamakan solusi jangka panjang, arsitektur bersih, dan minim dependensi redundan.
2. **Nol Asumsi & Non-Halusinasi**: Selalu baca berkas langsung di sistem file sebelum melakukan edit atau memberikan rekomendasi. Dilarang berasumsi nama fungsi, versi library, atau skema tabel ada tanpa verifikasi nyata.
3. **Preservasi Invarian Domain**: Jangan merusak logika domain bisnis (perhitungan rata-rata tertimbang biaya per liter, konsumsi km/liter, validasi angka odometer, saldo kuota AI, dan status langganan Pro).
4. **Verifikasi Fungsional Nyata**: Dilarang mengklaim modul fungsional hanya karena ViewModel atau Composable sudah dibuat. Verifikasi kompilasi Gradle, unit test, dan validasi data secara riil.

---

### 🏗️ 2. Arsitektur Android MVVM + Repository & Clean Layering

1. **Pemisahan Tiga Layer Bersih**:
   - **UI Layer (`feature/*`, `core/designsystem/*`)**: Jetpack Compose Composable murni, ViewModel, UI State (`UiState`), UI Event (`UiEvent`).
   - **Domain Layer (`domain/*`)**: Pure Kotlin Use Cases (kalkulasi konsumsi BBM, validasi scan struk, agregasi analitik) dan domain model bersih tanpa dependensi Android Framework.
   - **Data Layer (`data/*`)**: Repository implementations, Supabase client data sources, ML Kit OCR Engine, Local Storage / Preferences.
2. **Unidirectional Data Flow (UDF)**:
   - UI mengobservasi `StateFlow<UiState<T>>` dari ViewModel secara immutable.
   - UI memancarkan user intent/event ke ViewModel melalui fungsi event terstandar.
3. **Dilarang Blocking I/O pada Main Thread**: Seluruh operasi jaringan (Supabase), disk I/O, dan pengolahan gambar ML Kit wajib berjalan pada background dispatcher (`Dispatchers.IO` / `Dispatchers.Default`).

---

### 🎨 3. UI Murni Jetpack Compose & Material 3 (Zero DB Query in View)

1. **Presentation Layer Only**: Composable dilarang memuat query jaringan langsung, parsing database mentah, atau logika bisnis kalkulasi berat.
2. **Standar Penanganan 5 Status UI Wajib**:
   Setiap layar yang berinteraksi dengan data asinkron wajib menyediakan state:
   - `Idle`: Keadaan awal sebelum proses berjalan.
   - `Loading`: Menampilkan indikator loading / spinner yang jelas.
   - `Success`: Menampilkan data riil pengguna.
   - `Empty`: Tampilan ramah pengguna saat data kosong disertai tombol ajakan aksi (*call to action*).
   - `Error`: Pesan kendala yang informatif dilengkapi tombol *Coba Lagi* (*retry*).
3. **Pencegahan Double-Submit & Formatted Input**:
   - Seluruh tombol simpan/aksi formulir wajib menampilkan *loading spinner* dan nonaktif (`disabled`) saat proses berjalan.
   - Input nominal BBM wajib otomatis diformat dengan pemisah ribuan Rupiah (`Rp 50.000`) dan dinormalisasi menjadi `Long` sebelum disimpan.
4. **Larangan Dialog Browser / Toast Liar**:
   - Gunakan komponen Material 3 `SnackbarHost` atau dialog konfirmasi Compose kustom. Dilarang menggunakan alert blocking.

---

### 🏢 4. Isolasi Kepemilikan Data & PostgreSQL RLS (Scoped Ownership)

1. **Row Level Security (RLS) Wajib Aktif**: Seluruh tabel di Supabase (`public.*`) yang dapat diakses klien Android wajib memiliki RLS aktif berbasis `auth.uid() = user_id`.
2. **Validasi Composite Foreign Key**: Relasi kepemilikan wajib menyertakan `user_id` (misal: `foreign key (vehicle_id, user_id) references vehicles(id, user_id)`) untuk mencegah referensi ID lintas akun (*BOLA/IDOR*).
3. **Dilarang Percaya Client untuk Kolom Privileged**: Klien Android tidak memiliki hak untuk mengubah langsung status langganan (`subscriptions.status`), kuota AI, role administratif, atau mencatat transaksi pembayaran berhasil tanpa verifikasi server.
4. **Pengujian Multi-Akun**: Setiap kebijakan RLS wajib diuji dengan minimal dua akun pengguna yang berbeda.

---

### ⚡ 5. Strategi OCR Hybrid & Batasan Cloud AI (Local First)

1. **Google ML Kit Berjalan Pertama Kali**: Ekstraksi teks struk selalu diproses di perangkat lokal (*on-device*) terlebih dahulu tanpa membutuhkan kuota internet.
2. **Fallback Cloud AI Hanya Melalui Backend**:
   - Dilarang keras menaruh API key provider AI (Gemini/OpenAI) di dalam APK Android.
   - Panggilan AI cloud wajib melalui Supabase Edge Function setelah memvalidasi JWT user, batas rate-limit, dan sisa kuota paket.
3. **Konfirmasi Wajib Pengguna (Human-in-the-Loop)**:
   - Hasil scan struk (tanggal, volume liter, total rupiah, SPBU, jenis BBM) **TIDAK PERNAH** disimpan secara otomatis ke database. Pengguna wajib memeriksa dan menyetujui pada layar review.
4. **Graceful Fallback**: Jika kuota AI habis atau koneksi gagal, sistem tetap mengizinkan pengisian manual atau koreksi teks hasil OCR lokal tanpa memblokir pengguna.
5. **Privasi Foto Struk**: Nomor polisi kendaraan dan identitas pribadi tidak pernah dikirim ke AI cloud provider.

---

### 💳 6. Standar Pembayaran QRISpy, Webhook & Idempotensi Langganan

1. **QRISpy Provider Default**: Menggunakan endpoint resmi `https://api.qrispy.id`. Adapter Midtrans dan Xendit harus mengimplementasikan interface kontrak yang sama (`PaymentGatewayProvider`).
2. **Server-Authoritative Pricing**: Nominal pembayaran diambil langsung dari konfigurasi server (`subscription_plans`), bukan dari angka yang dikirim klien.
3. **Return URL Bukan Bukti Bayar**: Redirect URL atau kembali dari WebView hanya memicu polling status; aktivasi Pro hanya terjadi setelah status `paid` diverifikasi oleh backend.
4. **Idempotensi Webhook Mutlak**: Event webhook pembayaran diverifikasi dan dicatat pada tabel `payment_webhook_events`. Event duplikat tidak akan memperpanjang atau menggandakan status Pro.
5. **Graceful Expiry**: Ketika langganan Pro berakhir, riwayat pengeluaran BBM dan data kendaraan pengguna **TIDAK BOLEH** dikunci atau dihapus. Akun bertransisi kembali ke batas Free Tier secara aman.

---

### 🔒 7. Standar Keamanan OWASP, Private Storage & Zero-Secret

1. **Zero Secret Policy**:
   - Dilarang melakukan commit terhadap file `.env`, kredensial JSON Google Services, Supabase service-role key, ataupun merchant secret QRISpy/Midtrans.
   - File konfigurasi lokal (`local.properties`, `.env`) wajib terdaftar di `.gitignore`.
2. **Private Storage untuk Struk**:
   - Foto struk disimpan dalam Supabase Storage private bucket (`receipts/{user_id}/{filename}`).
   - Akses foto hanya menggunakan *signed URL* berumur pendek (15-30 menit).
3. **Hak Hapus Akun & Data**:
   - Sediakan alur penghapusan akun yang menghapus seluruh data relasional dan berkas struk terkait di Storage bucket.

---

### 🧪 8. Standar Pengujian Mutu (Quality Gates) & Definition of Done

Sebuah fase atau fitur dinyatakan selesai (*Definition of Done*) jika dan hanya jika:
- Seluruh unit test terkait lulus 100% tanpa kegagalan (`Zero Test Failure`);
- Build debug berhasil dikompilasi (`assembleDebug`);
- Desugaring API 26 terbukti aktif dan bebas crash;
- Tidak ada secret atau token yang terekspos di kode sumber maupun manifest;
- Dokumentasi terkait dimutakhirkan.

**Perintah Verifikasi Wajib**:
```powershell
# Menjalankan unit tests
.\gradlew.bat testDebugUnitTest

# Membangun APK debug
.\gradlew.bat assembleDebug
```

---

### 📦 9. Aturan Git Commit & Push Otomatis & SemVer (Conventional Commits)

1. **Format Pesan Commit Wajib**:
   ```text
   <type>(<scope>): <subject>
   ```
   - `feat`: Menambahkan fitur baru.
   - `fix`: Memperbaiki bug atau kesalahan logika.
   - `docs`: Mengubah atau memperbarui dokumentasi.
   - `style`: Penyesuaian tampilan/formatting UI.
   - `refactor`: Restrukturisasi kode tanpa mengubah fungsionalitas.
   - `test`: Menambah atau memperbaiki automated tests.
   - `chore`: Perawatan dependensi atau konfigurasi build.
2. **Aturan Penulisan**:
   - Gunakan kata kerja imperatif (`add`, `fix`, `update`, `refactor`).
   - Judul subjek maksimal 100 karakter dan **tanpa tanda titik** di akhir.
3. **Semantic Versioning (SemVer)**:
   - Versi aplikasi dicatat serempak pada `app/build.gradle.kts` (`versionName`), `README.md`, dan `AGENTS.md`.

---

### 🔄 10. Kewajiban Pengkinian Dokumen (Mandatory Sync)

1. **Pengkinian `AGENTS.md`**: Setiap ada aturan arsitektur baru atau penyesuaian aturan, langsung dicatat pada dokumen ini.
2. **Pengkinian `README.md`**: Setiap fitur baru selesai, perbarui tabel fitur dan panduan build di `README.md`.
3. **Pengkinian `docs/` Suite**: Jaga sinkronisasi antara `docs/README.md`, `01-daftar-command.md`, `02-buku-panduan-pengguna.md`, `03-developer-guide.md`, dan `04-panduan-deployment.md`.

---

## 📁 Konsep Tata Letak Direktori (Folder-Foldering Hierarchy)

### 🌳 Diagram Pohon Direktori

```text
mari-partner-fuel/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/id/maripartner/fuel/
│       │   │   ├── MariPartnerFuelApp.kt
│       │   │   ├── MainActivity.kt
│       │   │   ├── core/
│       │   │   │   ├── common/
│       │   │   │   │   ├── config/       # AppConfig (injeksi aman BuildConfig)
│       │   │   │   │   ├── result/       # UiState, AppResult, AppException
│       │   │   │   │   └── util/         # CurrencyFormatter, DateUtils
│       │   │   │   ├── designsystem/
│       │   │   │   │   ├── component/    # FuelButton, FuelStateView, FuelTopBar, FuelCard
│       │   │   │   │   └── theme/        # Color, Type, Shape, Theme (Light & Dark)
│       │   │   │   ├── navigation/       # NavRoute, BottomNavItem, AppBottomBar, AppNavHost
│       │   │   │   └── security/         # TokenStorage, KeystoreManager
│       │   │   ├── data/
│       │   │   │   ├── local/            # DataStore, Preferences, Cache
│       │   │   │   ├── ocr/              # Google ML Kit Text Recognition Engine
│       │   │   │   ├── remote/           # Supabase Client Provider, Edge Function Callers
│       │   │   │   └── repository/       # Repository Implementations
│       │   │   ├── domain/
│       │   │   │   ├── model/            # Vehicle, FuelTransaction, SubscriptionPlan, etc.
│       │   │   │   ├── repository/       # Repository Interfaces
│       │   │   │   └── usecase/          # Pure Business Rules & Calculations
│       │   │   └── feature/
│       │   │       ├── analytics/        # AnalyticsScreen, AnalyticsViewModel
│       │   │       ├── auth/             # LoginScreen, RegisterScreen, AuthViewModel
│       │   │       ├── dashboard/        # DashboardScreen, DashboardViewModel
│       │   │       ├── profile/          # ProfileScreen, ProfileViewModel
│       │   │       ├── receipt_scan/     # CameraScanScreen, ScanReviewScreen, OcrViewModel
│       │   │       ├── splash/           # SplashScreen
│       │   │       ├── subscription/     # ProPlansScreen, CheckoutScreen, QrisView
│       │   │       ├── transactions/     # TransactionsScreen, TransactionFormScreen
│       │   │       └── vehicles/         # VehiclesScreen, VehicleFormScreen
│       │   └── res/
│       │       ├── drawable/             # Vector icons & drawables
│       │       ├── mipmap-anydpi-v26/    # Adaptive launcher icons
│       │       └── values/               # strings.xml, themes.xml, colors.xml
│       └── test/java/id/maripartner/fuel/
│           └── core/                     # Unit test suites (Currency, Nav, State, Config)
├── docs/
│   ├── README.md                         # Documentation Hub
│   ├── 01-daftar-command.md              # CLI Reference & Daily Operations
│   ├── 02-buku-panduan-pengguna.md       # User Manual Book
│   ├── 03-developer-guide.md             # Developer & Architecture Guide
│   ├── 04-panduan-deployment.md          # Deployment & Store Release Guide
│   ├── PRD.md                            # Product Requirement Document Baseline
│   ├── ARCHITECTURE.md                   # Clean Architecture Specification
│   ├── DATABASE.md                       # PostgreSQL Schema & RLS Policies
│   ├── SECURITY.md                       # Threat Model & Secret Protection
│   └── PAYMENT_FLOWS.md                  # QRISpy Payment & Webhook Lifecycle
├── gradle/
│   ├── libs.versions.toml                # Gradle Version Catalog
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── .env.example                          # Safe Environment Template
├── .gitignore                            # Secret & Build Artifact Exclusions
├── AGENTS.md                             # Acuan Hukum AI & Aturan Proyek
├── build.gradle.kts                      # Root Gradle Script
├── gradle.properties                     # JVM & Android Flags
├── gradlew                               # Linux/macOS Wrapper
├── gradlew.bat                           # Windows Wrapper
├── README.md                             # Portal Utama Repositori
└── settings.gradle.kts                   # Plugin & Dependency Repositories
```

---

### 🧩 Penjelasan Modul Direktori Proyek

- **`app/`**: Modul aplikasi Android tunggal yang memuat logika presentasi, domain, dan integrasi backend.
- **`core/common/`**: Komponen lintas fitur seperti konfigurasi lingkungan aman, utilitas tanggal/waktu berbasis desugaring API 26, dan formatter nominal Rupiah.
- **`core/designsystem/`**: Desain visual terpadu Material 3, tema warna BBM *Petrol Teal*, dan komponen reusable anti-flicker dengan loading states.
- **`core/navigation/`**: Penanganan navigasi deklaratif menggunakan Jetpack Navigation Compose dengan rute bertipe aman.
- **`data/`**: Abstraksi data sumber ganda (Supabase remote API + ML Kit on-device hardware OCR).
- **`domain/`**: Inti logika bisnis murni tanpa dependensi framework luar agar mudah diuji dengan unit test secepat kilat.
- **`feature/`**: Modul layar interaktif terpisah per domain fungsional (*Bounded Context*).
- **`docs/`**: Suite dokumentasi menyeluruh untuk staf operasional, pengembang, dan auditor keamanan.

---

## 🛡️ Standar Wajib Audit Kualitas & Kesiapan Produksi (Quality Gate)

Sebelum kode dinyatakan siap rilis (*production-ready*) atau beralih ke tahap berikutnya, AI Assistant wajib memverifikasi 9 pilar audit berikut:

| No | Pilar Kualitas | Aspek yang Wajib Diverifikasi | Standar Wajib Mari Partner Fuel |
|:--:|---|---|---|
| **1** | **Concurrency & Race Condition** | Pembuatan order pembayaran, mutasi kuota AI, simpan transaksi struk | Idempotensi payment webhook, transaksi atomik PostgreSQL |
| **2** | **UX & Usability Bottleneck** | Double submit form, review scan struk, empty state, keyboard resize | Tombol loading spinner `disabled`, layar review wajib sebelum simpan, `adjustResize` aktif |
| **3** | **OWASP Security** | Secret leak, akses lintas akun (BOLA/IDOR), manipulasi nominal plan | Zero-secret di APK, RLS `auth.uid() = user_id`, server-authoritative pricing |
| **4** | **Scalability & Resource** | Query transaksi riwayat, pembacaan foto struk resolusi tinggi | Pagination limit/offset, kompresi foto lokal sebelum unggah |
| **5** | **Database & Indexing** | Pencarian SPBU, filter kendaraan & tanggal transaksi | Indeks komposit pada `(user_id, transaction_at desc)` & `(vehicle_id, transaction_at desc)` |
| **6** | **Memory Leaks & Bloat** | Siklus hidup kamera, detektor ML Kit, coroutine scope | Coroutine terikat `viewModelScope`, pelepasan resource image analyzer |
| **7** | **Timeout & External Fallback** | AI cloud failure, payment gateway offline, internet lambat | ML Kit lokal tetap jalan, input transaksi manual tetap tersedia saat offline/AI down |
| **8** | **Observability & Error Log** | Log debug aplikasi, jejak audit webhook | Redaksi kunci otentikasi via `AppConfig.redactedKey`, zero-secret di log |
| **9** | **Real-time Debounce** | Pencarian riwayat transaksi BBM, filter kendaraan | Debounce minimal 300ms pada seluruh input query pencarian |
