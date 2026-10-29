# 📚 Portal Dokumentasi Resmi --- Mari Partner Fuel

Selamat datang di pusat dokumentasi resmi **Mari Partner Fuel** (*AI-Powered Fuel Expense Tracker for Android*). Portal ini memandu seluruh pengembang, staf operasional, penguji mutu (QA), dan auditor keamanan dalam memahami, mengoperasikan, mengembangkan, dan mendistribusikan aplikasi.

---

## 🧭 Peta Navigasi Dokumen

Gunakan indeks di bawah ini untuk menuju panduan yang sesuai dengan kebutuhan Anda:

| Dokumen | Target Pembaca | Deskripsi & Topik Utama |
| :--- | :--- | :--- |
| [**01. Daftar Perintah Wajib & Operasional**](01-daftar-command.md) | Developer / DevOps | Referensi perintah CLI Gradle, eksekusi unit test, build APK, ADB debugging, dan pemeriksaan secret. |
| [**02. Buku Panduan Pengguna (Manual Book)**](02-buku-panduan-pengguna.md) | Pengguna / QA / Tim Bisnis | Panduan lengkap langkah demi langkah fitur aplikasi: Beranda, Scan Struk, Riwayat, Analitik, Kendaraan, dan Langganan Pro. |
| [**03. Panduan Pengembang & Arsitektur**](03-developer-guide.md) | Software Engineer / Maintainer | Arsitektur MVVM + Repository, Clean Layering, UDF UiState, Google ML Kit OCR lokal, Supabase SDK & RLS, serta panduan menambah fitur baru. |
| [**04. Panduan Deployment & Distribusi**](04-panduan-deployment.md) | Release Engineer / Lead Dev | Standar build release, keystore signing, optimasi Proguard/R8, setup Supabase Edge Functions, dan kepatuhan kebijakan Google Play. |
| [**PRD Baseline**](PRD.md) | Product Manager / Seluruh Tim | Spesifikasi produk asli, target audiens, model bisnis, dan kriteria penerimaan MVP. |
| [**Spesifikasi Arsitektur**](ARCHITECTURE.md) | Arsitek Sistem / Lead Dev | Spesifikasi teknis komponen internal, pemetaan data flow, design system, dan mitigasi API 26. |
| [**Desain Basis Data & RLS**](DATABASE.md) | Backend Engineer / DBA | Skema relasional PostgreSQL Supabase, composite foreign keys, indeks kinerja, dan kebijakan RLS. |
| [**Kebijakan Keamanan & Privasi**](SECURITY.md) | Security Auditor / DevOps | Model ancaman (threat model), isolasi kredensial, private storage signed URL, dan proteksi BOLA/IDOR. |
| [**Alur Pembayaran QRISpy**](PAYMENT_FLOWS.md) | FinTech / Integrator | Siklus pesanan QRISpy, alur QRIS dinamis, verifikasi webhook tanda tangan, dan idempotensi database. |
| [**Sistem Pembayaran & Aktivasi (Dual-Track)**](PAYMENTS.md) | FinTech / Billing Dev | Arsitektur Dual-Track Google Play Billing & QRISpy, verifikasi server, anti-replay token, dan siklus webhook. |
| [**Laporan Audit Keamanan (Tahap 8)**](SECURITY_AUDIT.md) | Security Auditor / QA | Hasil audit komprehensif, mitigasi temuan Critical/High, RLS RPC hardening, dan hasil quality gate. |
| [**Panduan Pengujian & Kasus Uji**](TESTING.md) | QA / Tester | Rekap 137 unit test, 29 test suites, dan skenario pengujian E2E (Free, Pro, Pembayaran, Multi-Akun). |
| [**Rencana Pengujian Mutu (QA Test Plan)**](QA_TEST_PLAN.md) | QA Lead / Tester | Matriks pengujian menyeluruh, kompatibilitas perangkat API 26-35, cakupan fungsional, dan SLA performa. |
| [**Laporan Hasil Pengujian QA (Tahap 9)**](QA_TEST_RESULTS.md) | QA / Lead Dev | Rekap hasil 137 unit test, 29 test suites, verifikasi 10 modul fungsional, dan smoke test checklist. |
| [**Laporan Tolok Ukur Kinerja (Performance)**](PERFORMANCE.md) | Performance / DevOps | Benchmark kinerja: throughput CSV 1.000 data, agregasi finansial, efisiensi BBM, ukuran artefak R8. |
| [**Daftar Periksa Kesiapan Rilis (Release Checklist)**](RELEASE_CHECKLIST.md) | Release Lead / DevOps | SOP pelepasan rilis, Google Play target SDK 35, izin least privilege, verifikasi AAB/APK, dan prosedur rollback. |
| [**Batasan Teknis & Known Issues**](KNOWN_ISSUES.md) | Seluruh Tim | Catatan kredensial produksi eksternal, batasan alarm reboot Android, dan mitigasi Doze mode. |
| [**Panduan Deployment Produksi**](DEPLOYMENT.md) | DevOps / Release Lead | Urutan migrasi database Supabase 001-008, deploy Edge Functions, dan panduan signing rilis. |

---

## ⚡ Pintasan Cepat (Quick Shortcuts)

### 💻 Untuk Pengembang Baru
1. Pastikan Java 21 terpasang dan JDK terpetakan di `gradle.properties`.
2. Jalankan test otomatis:
   ```powershell
   .\gradlew.bat testDebugUnitTest
   ```
3. Bangun APK debug untuk instalasi ke emulator/perangkat:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
4. Baca selengkapnya di [03-developer-guide.md](03-developer-guide.md).

### 🔍 Untuk Tim QA & Pengujian Fitur
1. Ambil APK debug terbaru dari `app/build/outputs/apk/debug/app-debug.apk`.
2. Pasang ke perangkat uji minimum API 26 (Android 8.0) atau emulator Android 14/15.
3. Ikuti skenario pengujian di [02-buku-panduan-pengguna.md](02-buku-panduan-pengguna.md).

### 🚀 Untuk Release & Deployment
1. Ikuti checklist kesiapan rilis di [04-panduan-deployment.md](04-panduan-deployment.md).
2. Verifikasi 9 pilar audit di [AGENTS.md](../AGENTS.md).
