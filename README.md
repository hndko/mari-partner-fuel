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
  <img src="https://img.shields.io/badge/Payment-QRISpy-FF8F00?style=for-the-badge" alt="Payment QRISpy" />
  <img src="https://img.shields.io/badge/Version-v1.4.0-blue?style=for-the-badge" alt="Version 1.4.0" />
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
- 💎 **Langganan Pro via QRISpy**: Upgrade ke tier Pro secara aman menggunakan QRIS dinamis dari QRISpy dengan verifikasi server terenkripsi dan webhook idempotent.
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

### Menjalankan Unit Test (100% Lulus)
```powershell
.\gradlew.bat testDebugUnitTest
```
Hasil uji: **99 tests lulus** (21 test suites):
- Agregasi dashboard & perbandingan bulan (`DashboardSummaryCalculationTest` - 4 tests)
- Estimasi efisiensi konsumsi BBM km/L (`CalculateFuelEfficiencyTest` - 3 tests)
- Ekspor CSV & mitigasi formula injection (`ExportTransactionsCsvTest` - 4 tests)
- Validasi & status jatuh tempo pengingat servis (`MaintenanceReminderValidationTest` - 7 tests)
- Pembacaan & parsing teks struk SPBU (`ReceiptTextParserTest` - 5 tests)
- DTO ekstraksi AI & fallback serialisasi (`AiExtractionDtoTest` - 3 tests)
- Validasi data kendaraan (`ValidateVehicleInputUseCaseTest` - 7 tests)
- Validasi transaksi manual (`ValidateTransactionInputUseCaseTest` - 6 tests)
- Estimasi volume & toleransi selisih harga (`EstimateFuelCalculationUseCaseTest` - 6 tests)
- Pemetaan Postgrest DTO & domain model (`VehicleDtoMappingTest` - 3 tests, `FuelTransactionDtoMappingTest` - 2 tests)
- Alur Use Case kendaraan & transaksi (`VehicleUseCaseTest` - 4 tests, `TransactionUseCaseTest` - 4 tests)
- Verifikasi skema DDL & trigger SQL (`MigrationSqlTest` - 9 tests)
- Pengujian autentikasi & validasi input (`AuthUseCaseTest` - 9 tests, `AuthValidationTest` - 6 tests)
- Manajemen private bucket storage (`ReceiptStorageManagerTest` - 6 tests)
- Formatting & utilitas (`CurrencyFormatterTest` - 5 tests, `NavRouteTest` - 3 tests, `UiStateTest` - 2 tests, `AppConfigTest` - 2 tests).

### Membangun APK Debug
```powershell
.\gradlew.bat assembleDebug
```
Artefak APK keluaran: `app/build/outputs/apk/debug/app-debug.apk` (~23.1 MB).

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

## 📚 Suite Dokumentasi Lengkap

Dokumentasi terperinci telah disusun sesuai standar enterprise di direktori [docs/](docs/):
- 🧭 [**Portal Dokumentasi**](docs/README.md) — Hub navigasi seluruh panduan proyek.
- ⚡ [**01. Daftar Perintah Wajib & Operasional**](docs/01-daftar-command.md) — Referensi CLI Gradle, ADB, dan troubleshooting.
- 📖 [**02. Buku Panduan Pengguna (Manual Book)**](docs/02-buku-panduan-pengguna.md) — Panduan visual dan alur fitur untuk pengguna & QA.
- 🛠️ [**03. Panduan Pengembang & Arsitektur**](docs/03-developer-guide.md) — Panduan arsitektur kode, Clean MVVM, dan UDF.
- 🚀 [**04. Panduan Deployment & Distribusi**](docs/04-panduan-deployment.md) — Keystore signing, R8/Proguard, dan rilis Google Play.
- 📋 [**PRD Baseline**](docs/PRD.md) — Spesifikasi produk asli Mari Partner Fuel.
- 📐 [**Spesifikasi Arsitektur**](docs/ARCHITECTURE.md) — Desain teknis komponen dan aliran data.
- 🗄️ [**Desain Basis Data & RLS**](docs/DATABASE.md) — Skema tabel Supabase dan aturan keamanan PostgreSQL.
- 🛡️ [**Kebijakan Keamanan & Privasi**](docs/SECURITY.md) — Threat model dan perlindungan secret.
- 💳 [**Alur Pembayaran QRISpy**](docs/PAYMENT_FLOWS.md) — Siklus pembayaran QRIS dan webhook idempotensi.
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
