# ⛽ Mari Partner Fuel

> **AI-Powered Fuel Expense Tracker for Android**  
> Aplikasi Android Native Kotlin cerdas untuk mencatat, menganalisis, dan mengoptimalkan pengeluaran bahan bakar kendaraan dengan OCR hybrid (Google ML Kit lokal + AI cloud fallback), Supabase online backend, autentikasi email & Google, serta langganan Pro via QRISpy.

---

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android_Native-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform Android" />
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin 2.0.21" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Compose M3" />
  <img src="https://img.shields.io/badge/Minimum_SDK-API_26_(Android_8.0)-006D77?style=for-the-badge" alt="Min SDK 26" />
  <img src="https://img.shields.io/badge/Backend-Supabase-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white" alt="Supabase Backend" />
  <img src="https://img.shields.io/badge/OCR-Google_ML_Kit-EA4335?style=for-the-badge&logo=google&logoColor=white" alt="Google ML Kit" />
  <img src="https://img.shields.io/badge/Payment-Play_Billing_%26_QRISpy-FF8F00?style=for-the-badge" alt="Payment Dual-Track" />
  <img src="https://img.shields.io/badge/Tests-137_Passing-brightgreen?style=for-the-badge" alt="Tests 137 Passing" />
  <img src="https://img.shields.io/badge/Release_Candidate-RC--1-blue?style=for-the-badge" alt="Release Candidate RC-1" />
  <img src="https://img.shields.io/badge/Production_Readiness-Ready_for_Closed_Testing-3ECF8E?style=for-the-badge" alt="Ready for Closed Testing" />
  <img src="https://img.shields.io/badge/AAB_Size-7.57_MB-blue?style=for-the-badge" alt="AAB Size 7.57 MB" />
  <img src="https://img.shields.io/badge/Version-v1.7.0-blue?style=for-the-badge" alt="Version 1.7.0" />
</p>

---

## 📑 Daftar Isi

- [📖 Tentang Proyek](#-tentang-proyek)
- [✨ Fitur Utama](#-fitur-utama)
- [🛠️ Teknologi & Library](#️-teknologi--library)
- [🏗️ Arsitektur & Pola Desain](#️-arsitektur--pola-desain)
- [🗄️ Struktur Basis Data](#️-struktur-basis-data)
- [👥 Hak Akses & Model Akun (Free vs Pro)](#-hak-akses--model-akun-free-vs-pro)
- [📋 Prasyarat Sistem](#-prasyarat-sistem)
- [🚀 Panduan Instalasi & Setup Lokal](#-panduan-instalasi--setup-lokal)
- [🧪 Perintah Testing & Build](#-perintah-testing--build)
- [📚 Suite Dokumentasi Lengkap](#-suite-dokumentasi-lengkap)
- [🤝 Panduan Kontribusi & Aturan Agen](#-panduan-kontribusi--aturan-agen)
- [📄 Lisensi](#-lisensi)

---

## 📖 Tentang Proyek

Pencatatan manual pengeluaran BBM sering kali terlewat, struk fisik SPBU mudah memudar dan hilang, serta perhitungan konsumsi kilometer per liter rumit jika dilakukan manual di spreadsheet.

**Mari Partner Fuel** hadir sebagai solusi komprehensif bagi pengemudi motor, mobil pribadi, pengemudi transportasi online, hingga staf operasional armada UMKM. Mengusung pendekatan **Local-First Hybrid OCR**, pemindaian struk dilakukan instan secara lokal di perangkat menggunakan Google ML Kit tanpa kuota internet, dengan fallback cerdas ke Cloud AI hanya saat struk rusak atau buram. Seluruh data disinkronkan ke PostgreSQL Supabase berkeamanan tinggi dengan Row Level Security (RLS) serta private cloud storage untuk foto struk.

---

## ✨ Fitur Utama

- ⚡ **Scan Struk Cepat (Hybrid OCR)**: Deteksi otomatis nama SPBU, tanggal, volume liter, jenis BBM, dan total pembayaran dalam hitungan detik.
- 👁️ **Review Transparan (Human-in-the-Loop)**: Pengguna selalu meninjau dan dapat mengoreksi angka sebelum disimpan.
- ✍️ **Input Transaksi Manual**: Catat pengeluaran BBM dengan mudah saat tidak memiliki struk fisik.
- 🚗 **Multi-Kendaraan**: Kelola sepeda motor, mobil pribadi, maupun armada kendaraan kantor dalam satu akun.
- 📊 **Dashboard & Analitik Pintar**:
  - Total pengeluaran bulanan.
  - Total volume liter dan frekuensi pengisian.
  - Rata-rata tertimbang biaya per liter (`total pengeluaran / total liter`).
  - Analitik efisiensi bahan bakar (km/liter) berbasis odometer berurutan.
- 📜 **Riwayat & Pencarian**: Filter rentang waktu, filter kendaraan, dan pencarian SPBU secara real-time dengan debounce.
- 💎 **Paket Free & Pro Otoritatif (Server-Side Entitlement)**:
  - Katalog paket backend resmi: **Mari Free** (Rp 0, 2 kendaraan, 5 AI scans/bln), **Mari Pro Bulanan** (Rp 29.000, kendaraan tanpa batas, 100 AI scans/bln), dan **Mari Pro Tahunan** (Rp 290.000, hemat 17%).
  - Layanan backend `get-my-entitlements` sebagai Single Source of Truth (klien Android tidak dapat memalsukan status `is_pro`).
  - Kuota ekstraksi AI cloud ditegakkan secara atomik melalui PostgreSQL RPC (`reserve_ai_quota` & `finalize_ai_quota`) dengan proteksi idempotensi dan refund otomatis saat provider gagal.
  - Batas maksimal kendaraan aktif ditegakkan secara permanen di database via trigger PostgreSQL `trg_enforce_vehicle_limit`.
  - Halaman visual perbandingan paket (`ProPlansScreen`) dengan meter kuota AI dan kepatuhan Google Play Payments Policy.
- 💳 **Integrasi Pembayaran & Aktivasi Pro (Dual-Track)**:
  - Jalur 1 (Google Play Store): Google Play Billing v7.1.1 untuk auto-renewing subscriptions, verifikasi purchase token sisi server via Edge Function `verify-play-purchase`, dan anti-replay token reuse prevention.
  - Jalur 2 (Direct/Web Distribution): Dynamic QRISpy dengan countdown 15 menit, verifikasi signature webhook, dan polling status fallback.
  - Aktivasi Langganan Atomik: RPC `activate_subscription_for_payment` memperpanjang `expires_at` secara akumulatif jika paket masih aktif.
  - Idempotensi Mutlak: RPC `record_payment_webhook_event` mencegah perpanjangan ganda akibat webhook berulang.
- 🛡️ **Audit Keamanan Menyeluruh & Integritas Data (Tahap 8)**:
  - Migrasi 008: Pencabutan hak eksekusi default PostgreSQL (`REVOKE EXECUTE ... FROM public, authenticated`) pada seluruh fungsi pembayaran dan kuota; eksekusi dibatasi eksklusif untuk peran `service_role`.
  - Guard internal fungsi basis data `auth.role() = 'service_role'` untuk proteksi BOLA/IDOR dan bypass hak istimewa.
  - Mitigasi Replay Purchase Token: penolakan pembelian lama yang sudah berstatus `paid` (`HTTP 409 PURCHASE_TOKEN_ALREADY_REDEEMED`).
  - Verifikasi Tanda Tangan Webhook: validasi signature header HMAC-SHA256 untuk webhook gateway pihak ketiga.
  - Anti-Prompt Injection AI: isolasi ketat tag `<untrusted_receipt_text>` dan pembatasan maksimal 8.000 karakter pada teks struk mentah.
  - Sanitasi Error 500: pencegahan kebocoran nama tabel atau skema SQL pada respons API backend.
- 🧪 **Pengujian Mutu Menyeluruh, Kinerja & Release Candidate (Tahap 9)**:
  - 137 pengujian otomatis (100% pass rate di 29 test suites) mencakup unit test, regression prevention, dan stress test.
  - Benchmark performa terukur: ekspor CSV 1.000 transaksi dalam 84 ms, agregasi finansial 1.000 data dalam 3 ms, efisiensi BBM sequential odometer dalam 54 ms.
  - Kepatuhan penuh Google Play Store: Target SDK API 35 (Android 15), Minimum SDK API 26 (Android 8.0), desugaring JDK Java 8+ / `java.time`, Least Privilege permissions, dan audit privasi.
- 🛡️ **Kesiapan Produksi & Audit Rilis Final (Tahap 10)**:
  - **Status Resmi: READY FOR CLOSED TESTING** (Layak masuk pengujian Google Play Closed Track).
  - Optimasi R8 Minification & Resource Shrinking aktif: Universal Signed APK tereduksi **85.3%** menjadi **`3.39 MB`**, Android App Bundle (AAB) tereduksi menjadi **`7.57 MB`**.
  - Prosedur Pemulihan Bencana (*Disaster Recovery*) dengan SLA target RPO 1 jam & RTO 2 jam.
  - Standar Pemantauan & Observability dengan ambang batas alert P0/P1/P2 dan zero-secret logging.
  - Rencana Tanggap Darurat mencakup mitigasi terstruktur untuk skenario insiden kritis.
- 🏪 **Google Play Closed Testing, Store Listing & Publikasi (Tahap 11)**:
  - Kepatuhan Target SDK API 35 (Android 15), paket AAB `7.57 MB`, Play App Signing, dan 137 unit tests 100% pass.
  - Draf Lengkap Daftar Toko (*Store Listing*) dalam bahasa Indonesia beserta spesifikasi aset grafis (ikon 512x512, feature graphic 1024x500, screenshot 9:16).
  - Kepatuhan Pembayaran Google Play: Penegakan in-app subscription Play Billing v7.1.1, SKU bulanan/tahunan, dan lisensi pengujian bebas biaya riil.
  - Rencana Pengujian Tertutup: Regulasi 20 penguji / 14 hari akun personal dan runbook rilis publikasi bertahap (*staged rollout*).
- 🔒 **Zero-Secret & Isolasi RLS**: Proteksi akses lintas akun (anti-BOLA/IDOR) dan tanda tangan URL struk berumur pendek.

---

## 🛠️ Teknologi & Library

| Kategori | Teknologi / Pustaka | Keterangan & Peran |
| :--- | :--- | :--- |
| **Bahasa & Runtime** | Kotlin 2.0.21 & Java 21 (LTS) | Bahasa pemrograman utama dengan compiler Compose generasi baru |
| **Arsitektur UI** | Jetpack Compose (BOM 2024.10.00) | UI deklaratif modern, reactive state management |
| **Design System** | Material 3 (M3) | Skema warna kustom *Petrol Teal* & *Fuel Amber* (Terang & Gelap) |
| **Navigasi** | Navigation Compose 2.8.3 | Navigasi deklaratif dengan rute bertipe aman |
| **Kompatibilitas** | Android Gradle Plugin 8.5.2 & Desugaring 2.1.2 | Mendukung penuh Android API 26 (Android 8.0) hingga API 35 (Android 15) |
| **Local OCR** | Google ML Kit Text Recognition 19.0.1 | Pembacaan teks struk SPBU di perangkat (*on-device*) |
| **Backend & Cloud** | Supabase SDK 3.0.1 (Auth, Postgrest, Storage, Functions) | Otentikasi, basis data relasional online, dan private storage |
| **HTTP Engine** | Ktor Client OkHttp 3.0.0 | Client jaringan asinkron berkecepatan tinggi |
| **Play Billing** | Google Play Billing Library 7.1.1 | Integrasi pembelian langganan digital in-app Google Play Store |
| **Serialisasi** | Kotlinx Serialization 1.7.3 | Parsing JSON ketat dan type-safe |
| **Testing** | JUnit 4, Kotlinx Coroutines Test, Espresso | Suite pengujian otomatis logika domain dan UI State |

---

## 🏗️ Arsitektur & Pola Desain

Mari Partner Fuel menerapkan **Clean Architecture** berlapis tiga:
1. **UI Layer (`app/src/main/java/id/maripartner/fuel/feature/`)**: Composable murni, ViewModel, immutable `UiState` (Idle, Loading, Success, Error, Empty), dan handling event.
2. **Domain Layer (`domain/`)**: Pure Kotlin Use Cases (kalkulasi rata-rata tertimbang, efisiensi km/liter, validasi struk) bebas dependensi platform.
3. **Data Layer (`data/`)**: Repository implementations, Supabase Client Data Source, Google ML Kit OCR engine, dan adapter pembayaran.

```
[UI Layer (Compose)] ──(Event)──► [ViewModel] ──► [Domain UseCase] ──► [Repository] ──► [Supabase / ML Kit]
         ▲                              │
         └────────(UiState Flow)────────┘
```

---

## 🗄️ Struktur Basis Data

Data dikelola oleh PostgreSQL Supabase dengan 7 tabel relasional utama berindeks komposit:
- `profiles`: Profil dan preferensi tema pengguna (terikat `auth.users`).
- `vehicles`: Data kendaraan (motor/mobil, kapasitas tangki, status arsip).
- `fuel_transactions`: Transaksi pengisian BBM (volume liter, nominal Rupiah, odometer, scan status).
- `receipt_files`: Metadata file struk di private bucket storage.
- `subscription_plans`: Master konfigurasi paket Pro (harga server-authoritative & kuota AI).
- `subscriptions`: Hak akses aktif pengguna.
- `payment_transactions` & `payment_webhook_events`: Catatan order gateway & log idempotensi webhook.

---

## 👥 Hak Akses & Model Akun (Free vs Pro)

| Fitur | Free Tier (Gratis) | Pro Tier (Berlangganan) |
| :--- | :---: | :---: |
| **Pencatatan Manual** | ✅ Tersedia | ✅ Tersedia |
| **OCR Lokal ML Kit** | ✅ Tanpa Batas | ✅ Tanpa Batas |
| **Bantuan Cloud AI** | ⚡ Kuota Dasar Bulanan | ⚡ Kuota Prioritas Besar |
| **Maksimal Kendaraan** | Maksimal 2 Kendaraan | Tanpa Batas |
| **Riwayat & Laporan** | ✅ Lengkap | ✅ Lengkap + Ekspor CSV Semua Periode |
| **Saat Pro Berakhir** | Data riwayat & kendaraan **TIDAK AKAN DIKUNCI ATAU DIHAPUS** |

---

## 📋 Prasyarat Sistem

- **Sistem Operasi**: Windows 10/11, macOS, atau Linux
- **JDK**: Java 21 LTS (direkomendasikan JetBrains Runtime atau Eclipse Temurin)
- **Android SDK**:
  - Minimum SDK: **API 26** (Android 8.0 Oreo)
  - Target SDK: **API 35** (Android 15)
  - Compile SDK: **API 35**
- **Android Studio**: Ladybug / Meerkat / versi 2024.1+

---

## 🚀 Panduan Instalasi & Setup Lokal

### 1. Klon Repositori
```bash
git clone https://github.com/hndko/mari-partner-fuel.git
cd mari-partner-fuel
```

### 2. Konfigurasi `local.properties`
Pastikan direktori Android SDK telah terdefinisi di `local.properties` (file ini otomatis diabaikan oleh Git):
```properties
sdk.dir=C\:\\Users\\<Username>\\AppData\\Local\\Android\\Sdk
```

### 3. Konfigurasi Kredensial Lingkungan (Opsional untuk Development)
Salin contoh template [.env.example](.env.example):
```bash
cp .env.example .env
```
Variabel publik `SUPABASE_URL` dan `SUPABASE_ANON_KEY` dapat dimasukkan ke `local.properties` atau dibiarkan menggunakan placeholder saat pengujian lokal murni.

---

## 🧪 Perintah Testing & Build

### ⚡ Menjalankan Aplikasi di HP / Emulator Tanpa Android Studio (Mode Ringan)
Untuk menguji aplikasi langsung di smartphone fisik Android (USB/Wi-Fi) atau emulator AVD lokal (`test_avd`) tanpa membuka Android Studio yang berat di laptop:
```powershell
.\run-app.bat
```
Script batch otomatis ini akan:
1. Mendeteksi lokasi Android SDK dan perangkat fisik Anda secara otomatis.
2. Memberikan pilihan untuk menggunakan HP fisik (0% beban emulator di laptop) atau menyalakan AVD dengan parameter hemat RAM/CPU.
3. Mengompilasi APK debug via Gradle daemon, memasangnya ke perangkat, dan membuka aplikasi secara instan.
4. Menyediakan opsi pemantauan Logcat real-time khusus filter `id.maripartner.fuel`.

### Menjalankan Unit Test (100% Lulus)
```powershell
.\gradlew.bat testDebugUnitTest
```
Hasil uji: **137 tests lulus 100%** (29 test suites):
- Benchmark performa & stress testing (`PerformanceAndStressTest` - 5 tests)
- Hardening keamanan & integritas data (`SecurityHardeningAndIntegrityTest` - 8 tests)
- Verifikasi skema DDL, migrasi & trigger SQL (`MigrationSqlTest` - 8 tests)
- Alur Use Case & model pembayaran (`PaymentModelTest`, `PaymentDtoTest`, `PaymentUseCaseTest` - 11 tests)
- Entitlement & penegakan kuota AI (`UserEntitlementTest`, `CheckFeatureAccessUseCaseTest`, `EntitlementDtoTest` - 14 tests)
- Agregasi dashboard, efisiensi BBM km/L & ekspor CSV (`DashboardSummaryCalculationTest`, `CalculateFuelEfficiencyTest`, `ExportTransactionsCsvTest`, `MaintenanceReminderValidationTest` - 18 tests)
- Pembacaan & parsing OCR struk ML Kit serta validasi transaksi (`ReceiptTextParserTest`, `AiExtractionDtoTest`, `FuelTransactionValidationTest`, `VehicleValidationTest` - 26 tests)
- Autentikasi, navigasi & utilitas (`AuthUseCaseTest`, `AuthValidationTest`, `CurrencyFormatterTest`, `DateUtilsTest`, `NavigationTest`, `UiStateTest`, `AppConfigTest` - 47 tests).

### Membangun APK Debug
```powershell
.\gradlew.bat assembleDebug
```
Artefak APK keluaran: `app/build/outputs/apk/debug/app-debug.apk` (~23.1 MB).

### Menjalankan Fitur Tahap 7 (Integrasi Pembayaran & Aktivasi Pro)
1. **Pilihan Paket & Checkout (`/pro_plans` -> `/checkout/{planId}`)**:
   - Dari menu Akun atau Banner Pro di Beranda, buka katalog paket Pro.
   - Ketuk paket pilihan (Bulanan Rp 29.000 atau Tahunan Rp 290.000) untuk membuka layar Checkout.
   - Sistem menampilkan rincian biaya otoritatif server, PPN/biaya administrasi (Rp 0), dan metode pembayaran yang tersedia.
2. **Jalur Google Play Billing (Play Store Build)**:
   - Ketuk "Bayar via Google Play" untuk memicu bottom sheet Google Play Billing v7.1.1.
   - Setelah pembelian disetujui, token dikirim ke Edge Function `verify-play-purchase` untuk divalidasi ke Google Developer API.
   - Status Pro dan kuota AI langsung aktif secara atomik.
3. **Jalur Dynamic QRISpy (Direct/Web Distribution)**:
   - Pilih opsi pembayaran QRISpy, sistem akan men-generate QRIS dinamis unik dengan masa berlaku 15 menit.
   - Layar `QrisPaymentScreen` menampilkan QR string, timer countdown, dan tombol unduh/simpan QR.
   - Polling status otomatis mengecek konfirmasi webhook dari gateway.
4. **Layar Status & Rekonsiliasi (`/payment_status/{paymentReference}`)**:
   - Menampilkan konfirmasi status real-time: Pending (Kuning), Berhasil (Hijau/Teal), atau Kedaluwarsa (Merah).
   - Hak Pro langsung aktif tanpa restart aplikasi.

### Menjalankan Fitur Tahap 5 (Dashboard, Analitik, Ekspor CSV, & Pengingat Servis)
1. **Dashboard Statistik Real-Time (`/dashboard`)**:
   - Total pengeluaran bulan berjalan, total liter, rata-rata tertimbang biaya per liter, dan total transaksi.
   - Perbandingan pengeluaran terhadap bulan lalu dengan persentase dan selisih nominal (mencegah persentase menyesatkan bila pembanding nol).
   - Kartu kendaraan dengan pengeluaran tertinggi bulan ini dan grafik ringkasan distribusi per kendaraan.
   - Daftar 5 transaksi terkini dengan rincian SPBU, kendaraan, dan nominal.
2. **Analitik Finansial & Konsumsi BBM (`/analytics`)**:
   - Filter periode: Bulan Ini, Bulan Lalu, 3 Bulan, 6 Bulan, Tahun Ini, dan Rentang Khusus.
   - Grafik tren pengeluaran time-series harian/bulanan berbasis Compose Canvas.
   - Distribusi persentase pengeluaran per kendaraan.
   - Estimasi efisiensi BBM (km/liter) berbasis selisih odometer berurutan per kendaraan dengan penanganan anomali odometer.
3. **Ekspor Riwayat CSV Aman (`/transactions`)**:
   - Ketuk ikon Download pada bilah atas layar Riwayat untuk mengekspor riwayat transaksi ke format CSV standar RFC 4180.
   - Dilengkapi **OWASP Formula Injection Mitigation**: menetralkan input berbahaya berawalan `=`, `+`, `-`, `@`, `\t` dengan petik tunggal (`'`) tanpa merusak format angka numerik yang sah.
   - Menyertakan **UTF-8 BOM (`\uFEFF`)** sehingga file dapat langsung dibuka dengan rapi di Microsoft Excel maupun Google Sheets.
   - Menggunakan Android Storage Access Framework (SAF) agar pengguna bebas memilih direktori penyimpanan.
4. **Pengingat Perawatan & Servis Kendaraan (`/reminders`)**:
   - Catat pengingat ganti oli mesin, servis berkala, atau pemeriksaan rem.
   - Pemicu jatuh tempo ganda: berdasarkan tanggal target, batas kilometer odometer, atau kombinasi keduanya.
   - Status visual dinamis: **Sudah Jatuh Tempo** (Merah), **Mendekati Jatuh Tempo** (Kuning/Amber), **Mendatang** (Teal), dan **Selesai** (Hijau).
   - Penjadwalan alarm lokal Android (`AlarmManager` & `ReminderNotificationReceiver`) pada pukul 08:00 pagi saat tanggal target tiba.

### Menjalankan Fitur Tahap 4 (Hybrid OCR & AI Receipt Extraction)
1. **Pemindaian Struk On-Device (`/receipt_scan`)**:
   - Ambil foto struk menggunakan kamera atau pilih dari galeri foto melalui PhotoPicker bawaan.
   - Text recognition berjalan instan di perangkat (*local on-device*) menggunakan **Google ML Kit Text Recognition** tanpa kuota internet.
   - Sistem mengekstrak nama SPBU (Pertamina, Shell, BP, AKR), tanggal/waktu, nomor struk, volume liter, harga per liter, dan total nominal Rupiah.
2. **Peninjauan Transparan (*Human-in-the-Loop*) (`/scan_review`)**:
   - Pengguna meninjau hasil ekstraksi sebelum transaksi disimpan.
   - Peringatan ramah otomatis muncul jika ditemukan selisih matematis antara volume x harga per liter dengan total pembayaran.
   - Semua kolom kandidat (kendaraan, nominal, volume, harga, SPBU, tanggal) dapat disunting manual.
3. **Optimasi AI Cloud (*Fallback Cerdas*)**:
   - Jika struk buram atau berkerut, pengguna dapat mengetuk tombol "Optimalkan dengan AI Cloud" yang memicu dialog persetujuan privasi.
   - Permintaan diteruskan ke Supabase Edge Function `extract-fuel-receipt` yang memanggil Google Gemini 1.5 Flash tanpa mengekspos API key di dalam APK.
4. **Penyimpanan Transaksi**:
   - Transaksi **hanya** disimpan ke database setelah pengguna secara sadar mengetuk tombol **Konfirmasi & Simpan Transaksi**.

### Menjalankan Fitur Tahap 3 (Kendaraan & Transaksi Manual)
1. **Navigasi Tab Kendaraan (`/vehicles`)**:
   - Ketuk FAB `+` untuk membuka form tambah kendaraan.
   - Isi Nama (wajib), Jenis (Motor/Mobil/Lainnya), Merek, Model, Tahun, Plat Nomor, dan Odometer awal.
   - Kendaraan yang tidak lagi digunakan dapat diarsipkan agar tidak muncul sebagai pilihan default pada transaksi baru.
2. **Pencatatan Transaksi BBM Manual (`/transaction_form`)**:
   - Pilih kendaraan aktif dari dropdown.
   - Masukkan Total Pembayaran (format otomatis Rupiah, misal `Rp 50.000`).
   - Masukkan Volume Liter dan Harga per Liter (sistem akan otomatis memverifikasi kecocokan nominal dan memberi peringatan ramah jika terdapat selisih > 5%).
   - Lampirkan foto struk fisik secara opsional menggunakan Android PhotoPicker bawaan (disimpan aman di private storage bucket `receipts`).
3. **Riwayat & Detail Transaksi (`/transactions`)**:
   - Cari SPBU secara real-time dengan debounce 300ms.
   - Filter cepat berdasarkan kendaraan aktif dan ubah urutan (Terbaru/Terlama).
   - Ketuk transaksi untuk melihat detail lengkap, waktu pencatatan, dan signed URL foto struk.

---

## 📚 Dokumentasi & Panduan Setup

Panduan setup aplikasi dari awal telah dirangkum lengkap dalam satu panduan terpadu yang praktis di direktori [docs/](docs/):
- 🧭 [**Panduan Setup Lengkap dari Awal (Langkah demi Langkah)**](docs/README.md) — Panduan mulai dari nol: Prasyarat Java & Android SDK, setup backend Supabase gratis (SQL migrasi 001-008 & storage), konfigurasi `local.properties`, menjalankan via `run-app.bat` (HP fisik USB 0% emulator overhead atau AVD), pengujian fitur, hingga troubleshooting FAQ.
- 🚀 [**Script Pengujian Cepat (`run-app.bat`)**](run-app.bat) — Script otomatis interaktif satu klik untuk mendeteksi HP USB fisik dan emulator AVD tanpa membuka Android Studio yang berat.
- 🤖 [**AGENTS.md**](AGENTS.md) — Sumber acuan hukum dan 10 pilar aturan rekayasa agen.

---

## 🤝 Panduan Kontribusi & Aturan Agen

Setiap kontribusi wajib mematuhi [AGENTS.md](AGENTS.md):
1. **Conventional Commits**: Format commit `<type>(<scope>): <subject>` (contoh: `feat(ocr): add mlkit receipt scanner engine`).
2. **Strict Verification**: Wajib menjalankan `.\gradlew.bat testDebugUnitTest` sebelum commit.
3. **Zero Secret Exposure**: Dilarang meng-commit file `.env` atau token backend asli.
4. **SemVer Synchronization**: Sinkronkan nomor versi serempak pada `app/build.gradle.kts`, `README.md`, dan `AGENTS.md`.

---

## 📄 Lisensi

Hak Cipta © 2026 **Mari Partner**. Seluruh hak cipta dilindungi undang-undang.
