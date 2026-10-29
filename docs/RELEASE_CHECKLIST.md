# 🚀 RELEASE_CHECKLIST.md — Panduan Kesiapan Rilis & Checklist Release Candidate (RC)

Dokumen ini adalah prosedur operasional standar (SOP) pelepasan rilis (*Release Management*) untuk aplikasi **Mari Partner Fuel** (*AI-Powered Fuel Expense Tracker for Android*). Seluruh daftar periksa (*checklist*) di bawah ini wajib diverifikasi dan disetujui (*signed off*) sebelum artefak APK/AAB diunggah ke Google Play Console (Closed Testing, Open Testing, atau Production Track).

---

## 📌 Metadata Rilis

- **Versi Aplikasi**: `v1.7.0`
- **Kode Versi (Version Code)**: `8`
- **Format Paket Produksi**: Android App Bundle (`.aab`) & Universal Signed APK (`.apk`)
- **Minimum Android SDK**: API 26 (Android 8.0 Oreo)
- **Target Android SDK**: API 35 (Android 15 Vanilla Ice Cream)
- **Status Rilis**: **Release Candidate (RC-1)**
- **Artefak Terverifikasi**:
  - `app/build/outputs/bundle/release/app-release.aab` (~14.8 MB)
  - `app/build/outputs/apk/release/app-release.apk` (~15.3 MB)

---

## 📋 Ringkasan Alur Kesiapan Rilis

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        ALUR PELEPASAN RILIS MARI PARTNER FUEL                          │
├─────────────────┬─────────────────┬─────────────────┬─────────────────┬────────────────┤
│ 1. Code Freeze  │ 2. Automated QA │ 3. R8 / ProGuard│ 4. Store Policy │ 5. Go/No-Go    │
│    & SemVer     │    & Stress Test│    & Artifacts  │    & Compliance │    Decision    │
│    (100% Green) │    (137 Tests)  │    (~14.8 MB)   │    (Target 35)  │    & Deploy    │
└─────────────────┴─────────────────┴─────────────────┴─────────────────┴────────────────┘
```

---

## 🛡️ Checklist 1: Verifikasi Kode & Pengujian Mutu (Code Quality Gate)

Setiap item di bawah ini wajib bernilai `[x]` sebelum proses kompilasi rilis dilakukan:

- [x] **Zero Test Failure**: Seluruh 137 unit test dan stress benchmark lulus 100% (`.\gradlew.bat testDebugUnitTest`).
- [x] **Lint Vital Release Check**: Tidak ada kesalahan fatal (*fatal lint errors*) pada tahap `lintVitalAnalyzeRelease`.
- [x] **Performance Benchmarks Pass**:
  - [x] Ekspor 1.000 transaksi ke CSV selesai dalam < 100 ms (Aktual: 84 ms).
  - [x] Agregasi finansial 1.000 transaksi selesai dalam < 10 ms (Aktual: 3 ms).
  - [x] Kalkulasi efisiensi BBM (km/liter) 1.000 transaksi selesai dalam < 200 ms (Aktual: 54 ms).
  - [x] Pencarian & filter real-time 1.000 transaksi selesai dalam < 10 ms (Aktual: 5 ms).
  - [x] Formatter mata uang 10.000 iterasi selesai dalam < 50 ms (Aktual: 25 ms).
- [x] **Desugaring Java 8+ / `java.time`**: Pustaka `desugar_jdk_libs:2.1.2` aktif dan terverifikasi tidak memicu `NoClassDefFoundError` pada perangkat Android API 26–33.
- [x] **Clean Git Status**: Seluruh kode telah di-commit ke branch utama tanpa uncommitted modifications.

---

## 📦 Checklist 2: Konfigurasi Build & Verifikasi Artefak (Build & R8/ProGuard)

- [x] **Version Code & Name Synchronized**:
  - `app/build.gradle.kts`: `versionCode = 8`, `versionName = "1.7.0"`.
  - `AGENTS.md`, `README.md`, dan seluruh dokumen suite mencatat versi yang selaras.
- [x] **Keystore Signing Configuration**:
  - Menggunakan konfigurasi `signingConfigs.release` dengan membaca variabel lingkungan:
    - `MARI_RELEASE_KEYSTORE_PATH`
    - `MARI_RELEASE_KEYSTORE_PASSWORD`
    - `MARI_RELEASE_KEY_ALIAS`
    - `MARI_RELEASE_KEY_PASSWORD`
  - Fallback aman ke debug keystore untuk pengujian lokal/CI internal tanpa mengekspos kunci produksi.
- [x] **R8 Obfuscation & Shrinking**:
  - `isMinifyEnabled = true` dan `isShrinkResources = true` aktif pada build release.
  - Aturan `proguard-rules.pro` melindungi DTO Supabase, Model Domain, Ktor, Play Billing, dan Google ML Kit.
  - Ukuran AAB tereduksi ~36% dibandingkan build debug (Debug APK: 23.1 MB vs Release AAB: 14.8 MB).

---

## 🏛️ Checklist 3: Kepatuhan Kebijakan Google Play (Store Policy & Compliance)

- [x] **Target SDK 35 (Android 15)**:
  - Memenuhi syarat batas minimum Google Play Console (wajib target API 34+ untuk rilis 2024/2025).
- [x] **Audit Izin Manifest (Principle of Least Privilege)**:
  - `android.permission.INTERNET`: Diperlukan untuk sinkronisasi data Supabase.
  - `android.permission.ACCESS_NETWORK_STATE`: Diperlukan untuk deteksi status online/offline.
  - `android.permission.CAMERA`: Diperlukan untuk memindai struk fisik via Google ML Kit (`android.hardware.camera.any` bertanda `required="false"` agar kompatibel dengan tablet/perangkat tanpa kamera belakang).
  - `android.permission.POST_NOTIFICATIONS`: Ditangani secara runtime dengan dialog izin Material 3 (Android 13+ / API 33+).
  - `android.permission.RECEIVE_BOOT_COMPLETED` & `android.permission.SCHEDULE_EXACT_ALARM`: Dikelola secara anggun untuk pengingat servis.
  - **TIDAK ADA** izin berbahaya seperti `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `ACCESS_FINE_LOCATION`, atau `READ_CONTACTS`.
- [x] **Data Safety Form & Kebijakan Privasi**:
  - URL Kebijakan Privasi aktif dan dapat diakses publik (`https://maripartner.id/privacy-policy`).
  - Formulir Data Safety menyatakan:
    - Data keuangan (pengeluaran BBM) dikumpulkan untuk fungsionalitas aplikasi.
    - Foto struk disimpan di storage privat terenkripsi dan tidak dibagikan ke pihak ketiga.
    - Data dienkripsi dalam transit (HTTPS/TLS 1.3) dan at-rest (PostgreSQL AES-256).
- [x] **Fitur Hapus Akun & Data (Account Deletion Requirement)**:
  - Tombol hapus akun mandiri tersedia langsung di aplikasi (`ProfileScreen`).
  - URL formulir penghapusan akun berbasis web tersedia untuk pengguna yang telah mencopot pemasangan aplikasi.

---

## 💳 Checklist 4: Kepatuhan Billing & Pembayaran (Dual-Track Policy)

- [x] **Google Play Billing v7.1.1 (Store Track)**:
  - Dipakai secara eksklusif untuk distribusi aplikasi via Google Play Store.
  - Langganan Pro Bulanan (`mari_pro_monthly`) dan Pro Tahunan (`mari_pro_yearly`) terdaftar di Play Console In-App Products.
  - Verifikasi token dilakukan secara otoritatif oleh Supabase Edge Function `verify-play-purchase`.
  - Menerapkan perlindungan anti-replay token (mencegah token yang sama diklaim berulang).
- [x] **QRISpy Direct Gateway (Direct Track / Web Distribution)**:
  - Ditujukan untuk instalasi direct APK / PWA / landing page eksternal, mematuhi syarat Play Store untuk tidak menampilkan metode pembayaran pihak ketiga di dalam build Play Store tanpa izin khusus.
  - Dynamic QRISpy berbatas waktu 15 menit dengan validasi tanda tangan HMAC-SHA256 pada webhook backend.

---

## 🔍 Checklist 5: Verifikasi Staging & Smoke Test Manual

Lakukan pengujian manual pada minimal 2 perangkat fisik / emulator sebelum submit:
- **Perangkat Rendah (Low-End / Legacy)**: Android 8.0/8.1 (API 26/27) RAM 2-3 GB.
- **Perangkat Modern (High-End / Flagship)**: Android 14/15 (API 34/35) RAM 8+ GB.

| Skenario Pengujian | Hasil Pengujian | Catatan Verifikasi |
|---|:---:|---|
| **Cold App Launch** | ✅ PASS | Aplikasi terbuka tanpa crash dalam < 1.2 detik. |
| **Registrasi & Login Email** | ✅ PASS | Validasi format, pembuatan akun di Supabase Auth, sesi tersimpan. |
| **Google Sign-In** | ✅ PASS | Menggunakan Credential Manager API, graceful fallback saat ID klien belum disetel. |
| **Input Kendaraan Baru** | ✅ PASS | Penegakan batas maksimal 2 kendaraan pada paket Free Tier. |
| **Input Transaksi Manual** | ✅ PASS | Formatted Rupiah otomatis, kalkulasi harga/liter, penyimpanan sukses. |
| **Scan Struk Kamera (ML Kit)** | ✅ PASS | Deteksi nominal, tanggal, volume SPBU; alur *Human-in-the-Loop* review berfungsi. |
| **Dashboard & Riwayat** | ✅ PASS | Kartu ringkasan finansial, efisiensi km/liter, pencarian dengan debounce 300ms. |
| **Ekspor Laporan CSV** | ✅ PASS | File tersimpan di folder Downloads/Documents dengan UTF-8 BOM dan proteksi injection. |
| **Jadwal Pengingat Servis** | ✅ PASS | Notifikasi muncul tepat waktu atau saat aplikasi dibuka kembali. |
| **Katalog Pro & Kuota AI** | ✅ PASS | Status kuota AI tampil sesuai data backend (`get-my-entitlements`). |
| **Logout & Sesi Bersih** | ✅ PASS | Token dibersihkan dari memori dan EncryptedSharedPreferences. |

---

## 🚨 Checklist 6: Matriks Keputusan Go / No-Go

| Kriteria | Kondisi "GO" | Kondisi "NO-GO" (Rilis Dibatalkan) |
|---|---|---|
| **Unit Test Suite** | 100% Pass (0 Error, 0 Failure) | Ada 1 saja unit test yang gagal |
| **Crash-Free Baseline** | Tidak ada crash pada alur utama | Terjadi NullPointer / NoClassDefFoundError pada API 26 |
| **Data Safety & Secrets** | Zero secret di APK/AAB | Kunci service-role atau password ditemukan di kode |
| **RLS Multi-Akun** | Akun B tidak bisa melihat data Akun A | Ditemukan kebocoran data lintas akun |
| **Performa Scroll** | FPS stabil (sub-16ms per frame) | Terjadi lag/ANR berkepanjangan pada daftar transaksi |

---

## 🔄 Checklist 7: Prosedur Rollback & Tanggap Darurat

Jika ditemukan bug kritis (*P0 / Blocker*) setelah rilis ke publik:

1. **Penghentian Rollout Bertahap (Halt Staged Rollout)**:
   - Akses Google Play Console -> *Production* / *Testing* -> Klik **Halt rollout** untuk mencegah pengguna lain mengunduh versi bermasalah.
2. **Identifikasi & Diagnostik**:
   - Analisis jejak tumpukan (*stack trace*) di Google Play Android Vitals / Sentry / Supabase Error Logs.
3. **Penerbitan Hotfix Cepat**:
   - Buat branch darurat: `git checkout -b hotfix/v1.7.1`.
   - Perbaiki bug, perbarui nomor build: `versionCode = 9`, `versionName = "1.7.1"`.
   - Jalankan unit test: `.\gradlew.bat testDebugUnitTest`.
   - Bangun AAB baru: `.\gradlew.bat bundleRelease`.
   - Rilis 100% rollout untuk menimpa versi bermasalah.
4. **Mitigasi Sisi Server (Backend Killswitch)**:
   - Jika masalah berasal dari AI Cloud atau Payment Webhook, nonaktifkan fungsi terkait di Supabase Edge Functions tanpa perlu merilis ulang APK pengguna (fitur otomatis beralih ke fallback manual/lokal).
