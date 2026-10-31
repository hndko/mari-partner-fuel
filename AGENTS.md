# 🤖 AGENTS.md — Panduan & Aturan Utama Pengembangan Proyek Mari Partner Fuel

Dokumen ini adalah sumber acuan hukum dan aturan rekayasa perangkat lunak tertinggi bagi seluruh AI Coding Assistant dan pengembang yang bekerja pada repositori **Mari Partner Fuel**. Seluruh instruksi dalam dokumen ini bersifat mengikat dan wajib dipatuhi tanpa pengecualian. Jika aturan bertentangan, urutan prioritasnya adalah: **keamanan dan integritas data**, **instruksi pengguna saat ini**, **dokumen ini (AGENTS.md)**, lalu **pola kode lama**.

---

## 📌 Metadata Proyek

- **Nama Aplikasi**: **Mari Partner Fuel** (AI-Powered Fuel Expense Tracker for Android)
- **Versi Saat Ini**: `v1.9.0`
- **Platform**: Android Native (Minimum SDK API 26 Android 8.0 Oreo, Target SDK API 35 Android 15)
- **Bahasa & UI**: Kotlin 2.0.21, Jetpack Compose BOM 2024.10.00, Material 3
- **Backend & Basis Data**: Supabase PostgreSQL 15+, Private Storage, Row Level Security (RLS), Edge Functions
- **OCR & AI Strategy**: Google ML Kit Text Recognition (lokal) + Cloud AI Fallback via Edge Function
- **Payment Gateway**: Dual-Track (Google Play Billing v7.1.1 untuk Play Store, QRISpy/Midtrans untuk Direct/Web)
- **Dokumentasi Proyek**: [Panduan Setup Lengkap](docs/README.md)
- **Histori Rilis**: `README.md`
- **Sumber Versi Runtime**: `app/build.gradle.kts` (`versionName = "1.9.0"`, `versionCode = 10`)

---

## 📋 Daftar Isi

1. [🏛️ 10 Pilar Aturan Wajib AI Assistant & Pengembang](#-10-pilar-aturan-wajib-ai-assistant--pengembang)
   - [🎯 1. Peran & Prinsip Kerja Utama (Role & Prime Directives)](#-1-peran--prinsip-kerja-utama-role--prime-directives)
   - [🏗️ 2. Arsitektur Android MVVM + Repository & Clean Layering](#-2-arsitektur-android-mvvm--repository--clean-layering)
   - [🎨 3. UI Murni Jetpack Compose & Material 3 (Zero DB Query in View)](#-3-ui-murni-jetpack-compose--material-3-zero-db-query-in-view)
   - [🏢 4. Isolasi Kepemilikan Data, PostgreSQL RLS & Eksekusi Migrasi CLI](#-4-isolasi-kepemilikan-data-postgresql-rls--eksekusi-migrasi-cli)
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
4. **Invarian Siklus Langganan (Renewal, Upgrade, & Downgrade)**:
   - **Kumulatif Renewal**: Perpanjangan paket Pro yang sedang aktif wajib menambahkan durasi ke sisa masa aktif berjalan (`expires_at + duration`), bukan mengganti atau mereset tanggal.
   - **Graceful Downgrade ke Free (No Immediate Revocation)**: Permohonan downgrade dari paket Pro ke Free tidak boleh memutus benefit seketika. Seluruh fitur Pro tetap aktif hingga masa berlaku yang telah dibayar (`expires_at`) berakhir secara alami (*cancel at period end*).
   - **Zero Data Loss Guarantee**: Data historis transaksi BBM, odometer, dan log pengguna tidak boleh dihapus atau dikunci ketika akun berpindah ke paket Free. Kendaraan aktif yang melebihi batas paket Free (2 unit) wajib diarsipkan secara aman tanpa kehilangan riwayat pengeluarannya.
5. **Verifikasi Fungsional Nyata**: Dilarang mengklaim modul fungsional hanya karena ViewModel atau Composable sudah dibuat. Verifikasi kompilasi Gradle, unit test, dan validasi data secara riil.

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

### 🏢 4. Isolasi Kepemilikan Data, PostgreSQL RLS & Eksekusi Migrasi CLI

1. **Row Level Security (RLS) Wajib Aktif**: Seluruh tabel di Supabase (`public.*`) yang dapat diakses klien Android wajib memiliki RLS aktif berbasis `auth.uid() = user_id`.
2. **Validasi Composite Foreign Key**: Relasi kepemilikan wajib menyertakan `user_id` (misal: `foreign key (vehicle_id, user_id) references vehicles(id, user_id)`) untuk mencegah referensi ID lintas akun (*BOLA/IDOR*).
3. **Dilarang Percaya Client untuk Kolom Privileged**: Klien Android tidak memiliki hak untuk mengubah langsung status langganan (`subscriptions.status`), kuota AI, role administratif, atau mencatat transaksi pembayaran berhasil tanpa verifikasi server.
4. **Pengujian Multi-Akun**: Setiap kebijakan RLS wajib diuji dengan minimal dua akun pengguna yang berbeda.
5. **Eksekusi Migrasi Baru Wajib Manual via Supabase CLI**:
   - Setiap kali membuat atau memodifikasi berkas migrasi di `supabase/migrations/`, AI Assistant dan pengembang **WAJIB** mengeksekusi/mendorong migrasi secara manual ke database remote via Supabase CLI:
     ```powershell
     cmd /c "npx -y supabase db push"
     ```
   - Dilarang keras berasumsi atau menganggap migrasi otomatis berjalan di database server tanpa verifikasi eksekusi nyata.
   - Seluruh status migrasi lokal dan remote wajib divalidasi hingga sinkron 100% menggunakan:
     ```powershell
     cmd /c "npx -y supabase migration list"
     ```
   - Setiap berkas migrasi SQL baru wajib bersifat idempoten (`create or replace`, `if not exists`, `drop trigger if exists`) agar aman dieksekusi ulang tanpa merusak integritas data eksisting.

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
6. **Kebijakan Ephemeral Scan & Nol File Permanen**: Foto struk hanya digunakan sebagai input visual sementara untuk pembacaan teks otomatis (Google ML Kit on-device / Cloud AI fallback). Data transaksi historis di database Supabase disimpan murni sebagai atribut relasional teks & angka (nominal Rp, volume liter, SPBU, jenis BBM, odometer, tanggal). Aplikasi Android tidak mengunggah atau menyimpan berkas foto struk permanen di cloud storage server demi menjaga privasi pengguna dan menghemat kuota cloud storage secara maksimal.

---

### 💳 6. Standar Pembayaran QRISpy, Webhook & Idempotensi Langganan

1. **QRISpy Provider Default**: Menggunakan endpoint resmi `https://api.qrispy.id`. Adapter Midtrans dan Xendit harus mengimplementasikan interface kontrak yang sama (`PaymentGatewayProvider`).
2. **Server-Authoritative Pricing**: Nominal pembayaran diambil langsung dari konfigurasi server (`subscription_plans`), bukan dari angka yang dikirim klien.
3. **Return URL Bukan Bukti Bayar**: Redirect URL atau kembali dari WebView hanya memicu polling status; aktivasi Pro hanya terjadi setelah status `paid` diverifikasi oleh backend.
4. **Idempotensi Webhook Mutlak**: Event webhook pembayaran diverifikasi dan dicatat pada tabel `payment_webhook_events`. Event duplikat tidak akan memperpanjang atau menggandakan status Pro.
5. **Graceful Expiry**: Ketika langganan Pro berakhir, riwayat pengeluaran BBM dan data kendaraan pengguna **TIDAK BOLEH** dikunci atau dihapus. Akun bertransisi kembali ke batas Free Tier secara aman.
6. **Aturan Perpanjangan (Renewal) Paket Aktif**:
   - Jika pengguna memilih kembali paket yang sama saat langganan Pro masih berjalan, sistem menyajikan opsi "Perpanjang Masa Aktif".
   - Sistem wajib menambahkan masa aktif secara kumulatif (`expires_at = current_expires_at + duration`). Hari aktif yang tersisa tidak boleh hangus atau di-reset.
7. **Aturan Upgrade Paket (Bulanan ke Tahunan)**:
   - Pengguna dengan paket aktif Bulanan dapat memilih paket Tahunan untuk upgrade dengan potongan hemat tahunan (17%).
   - Aktivasi paket Tahunan akan mengakumulasikan durasi baru ke sisa masa aktif bulanan yang sedang berjalan.
8. **Aturan Downgrade Paket (Pro ke Free)**:
   - Kartu paket Free untuk pengguna Pro wajib menyajikan aksi "Downgrade ke Paket Free" yang interaktif.
   - Mengklik tombol ini wajib memicu dialog konfirmasi edukatif (*Human Confirmation Gate*) yang menjelaskan secara transparan:
     - Masa aktif Pro tetap dinikmati secara penuh sampai tanggal kedaluwarsa (`expires_at`).
     - Kuota scan AI bulanan baru akan kembali ke 5 scan/bulan setelah masa aktif Pro berakhir.
     - Batas kendaraan aktif baru akan dibatasi maksimal 2 unit setelah masa aktif berakhir; kendaraan selebihnya diarsipkan otomatis dengan aman tanpa kehilangan riwayat transaksi BBM.
     - Seluruh riwayat transaksi pengeluaran BBM yang telah dicatat tersimpan permanen dan dijamin 100% aman (*Zero Data Loss*).
   - Konfirmasi downgrade menandai status langganan pada backend dengan `cancel_at_period_end = true` dan `auto_renew = false`.
9. **Aturan Peralihan Siklus Antar Paket Pro (Tahunan ke Bulanan)**:
   - Jika pengguna Pro Tahunan memilih paket Bulanan, sistem memberikan edukasi bahwa siklus bulanan baru akan berlaku efektif setelah masa aktif tahunan berakhir, sehingga pengguna tidak dirugikan atas biaya tahunan yang telah dibayarkan di muka.
10. **Aturan Pengingat Masa Aktif Pro & Transisi Otomatis ke Free (Auto-Expire & Graceful Fallback)**:
   - Sistem mendeteksi sisa masa aktif Pro $\le 7$ hari (`is_expiring_soon = true`) dan menyajikan pengingat proaktif visual (In-App Banner di Beranda & Badge Peringatan di Layar Paket) serta Android System Notification.
   - Apabila masa aktif telah melewati jatuh tempo (`now() > expires_at`) tanpa diperpanjang, status langganan di backend (`process_expired_subscriptions`) otomatis beralih menjadi `'expired'` dan akun bertransisi ke Paket Free (kuota 5 scan AI/bln & maks 2 kendaraan aktif).
   - Seluruh kendaraan aktif ke-3 dan seterusnya diarsipkan otomatis (`is_archived = true`) dengan jaminan mutlak *Zero Data Loss Guarantee* (seluruh riwayat transaksi BBM dan log odometer tetap tersimpan 100% utuh).
11. **Aturan Harga Promo & Batasan Pembelian per Pengguna (Promotional Pricing & Per-User Limit)**:
   - Paket langganan eksisting dapat memiliki harga promosi khusus (`is_promo_active`, `promo_price_idr`, `promo_max_per_user`, `promo_label`).
   - Batas pembelian per user (`promo_max_per_user`) mengontrol frekuensi klaim promo per akun: jika bernilai 1, pengguna hanya dapat membeli harga promo 1 kali; jika `null` atau `0`, promosi berlaku tanpa batas klaim (*unlimited*).
   - Hak klaim promo dihitung secara server-authoritative via RPC `get_available_subscription_plans` dan diverifikasi ulang saat pembuatan order di Edge Function `create-checkout` dengan mencatat `is_promo = true` pada tabel `payment_transactions`.
   - Apabila kuota klaim promo pengguna telah habis, tampilan dan checkout beralih secara anggun (*graceful fallback*) ke harga reguler tanpa error sistem.
   - Owner/Admin dapat mengaktifkan/menonaktifkan promo, mengubah harga diskon, batas pembelian, dan tag promo secara real-time dari Owner Console SaaS.

---

### 🔒 7. Standar Keamanan OWASP, Private Storage & Zero-Secret

1. **Zero Secret Policy**:
   - Dilarang melakukan commit terhadap file `.env`, kredensial JSON Google Services, Supabase service-role key, ataupun merchant secret QRISpy/Midtrans.
   - File konfigurasi lokal (`local.properties`, `.env`) wajib terdaftar di `.gitignore`.
2. **Ephemeral Storage untuk Struk**:
   - Foto struk berstatus sementara (ephemeral) untuk pemrosesan teks. Server database hanya menyimpan data string/angka terstruktur.
   - Jika terdapat file sementara yang pernah tersimpan di Storage bucket `receipts`, file tersebut berumur pendek dan dibersihkan otomatis via fungsi `cleanup_ephemeral_receipts`.
3. **Hak Hapus Akun & Data**:
   - Sediakan alur penghapusan akun yang menghapus seluruh data relasional dan berkas struk terkait di Storage bucket.

---

### 🧪 8. Standar Pengujian Mutu (Quality Gates) & Definition of Done

Sebuah fase atau fitur dinyatakan selesai (*Definition of Done*) jika dan hanya jika:
- Seluruh unit test terkait lulus 100% tanpa kegagalan (`Zero Test Failure`);
- Build debug berhasil dikompilasi (`assembleDebug`);
- Desugaring API 26 terbukti aktif dan bebas crash;
- Seluruh migrasi database baru telah dieksekusi manual ke remote via Supabase CLI (`cmd /c "npx -y supabase db push"`) dan terverifikasi sinkron 100% (`cmd /c "npx -y supabase migration list"`);
- Tidak ada secret atau token yang terekspos di kode sumber maupun manifest;
- Dokumentasi terkait dimutakhirkan.

**Perintah Verifikasi Wajib**:
```powershell
# Menjalankan unit tests
.\gradlew.bat testDebugUnitTest

# Membangun APK debug
.\gradlew.bat assembleDebug

# Memeriksa dan mengeksekusi migrasi Supabase ke remote database
cmd /c "npx -y supabase migration list"
cmd /c "npx -y supabase db push"
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
3. **Pengkinian Dokumentasi**: Jaga sinkronisasi dengan panduan di `docs/README.md`.

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
│       │   │       ├── owner/            # OwnerConsoleScreen, multi-tab console (Dashboard, Plans, Users, Transactions)
│       │   │       ├── profile/          # ProfileScreen, EditProfileScreen, ProfileViewModel
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
│   └── README.md                         # Panduan Setup Lengkap dari Nol & Operasional
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
├── run-app.bat                           # Script Pengujian Cepat HP USB & Emulator
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
- **`docs/`**: Panduan setup menyeluruh dari awal, konfigurasi database, dan petunjuk operasional.

---

## 🛡️ Standar Wajib Audit Kualitas & Kesiapan Produksi (Quality Gate)

Sebelum kode dinyatakan siap rilis (*production-ready*) atau beralih ke tahap berikutnya, AI Assistant wajib memverifikasi 9 pilar audit berikut:

| No | Pilar Kualitas | Aspek yang Wajib Diverifikasi | Standar Wajib Mari Partner Fuel |
|:--:|---|---|---|
| **1** | **Concurrency & Race Condition** | Pembuatan order pembayaran, mutasi kuota AI, simpan transaksi struk | Idempotensi payment webhook, transaksi atomik PostgreSQL |
| **2** | **UX & Usability Bottleneck** | Double submit form, review scan struk, empty state, keyboard resize | Tombol loading spinner `disabled`, layar review wajib sebelum simpan, `adjustResize` aktif |
| **3** | **OWASP Security** | Secret leak, akses lintas akun (BOLA/IDOR), manipulasi nominal plan | Zero-secret di APK, RLS `auth.uid() = user_id`, server-authoritative pricing |
| **4** | **Scalability & Resource** | Query transaksi riwayat, pembacaan foto struk resolusi tinggi | Pagination limit/offset, kompresi foto lokal sebelum unggah |
| **5** | **Database, Indexing & Migrations** | Pencarian SPBU, filter kendaraan & eksekusi skema migrasi | Indeks komposit pada `(user_id, transaction_at desc)` & eksekusi migrasi manual via Supabase CLI (`npx -y supabase db push`) |
| **6** | **Memory Leaks & Bloat** | Siklus hidup kamera, detektor ML Kit, coroutine scope | Coroutine terikat `viewModelScope`, pelepasan resource image analyzer |
| **7** | **Timeout & External Fallback** | AI cloud failure, payment gateway offline, internet lambat | ML Kit lokal tetap jalan, input transaksi manual tetap tersedia saat offline/AI down |
| **8** | **Observability & Error Log** | Log debug aplikasi, jejak audit webhook | Redaksi kunci otentikasi via `AppConfig.redactedKey`, zero-secret di log |
| **9** | **Real-time Debounce** | Pencarian riwayat transaksi BBM, filter kendaraan | Debounce minimal 300ms pada seluruh input query pencarian |
