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
| [**🌟 Buku Panduan Master Deployment Lengkap (A-Z)**](PANDUAN_DEPLOYMENT_LENGKAP.md) | DevOps / Release Lead / Seluruh Tim | **Panduan terpadu satu pintu dari A sampai Z**: Keystore, Build R8 AAB/APK, setup Supabase 8 migrasi, Storage, 7 Edge Functions, QRISpy, Gemini AI, Closed Testing 20 tester 14 hari, hingga Staged Rollout. |
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
| [**Audit Kesiapan Produksi (Production Readiness)**](PRODUCTION_READINESS.md) | Lead Architect / Auditor | Audit kesiapan produksi menyeluruh (Tahap 10), evaluasi arsitektur, isolasi data, dan status Closed Testing. |
| [**Audit Rilis Google Play (Tahap 11)**](PLAY_RELEASE_AUDIT.md) | Release Specialist / QA | Audit kesiapan rilis Google Play Console, regulasi target SDK 35, verifikasi bundle AAB, dan mitigasi risiko. |
| [**Draf Daftar Toko (Store Listing)**](PLAY_STORE_LISTING.md) | Product / Marketing | Metadata lengkap Play Store (nama, deskripsi singkat & lengkap bahasa Indonesia, checklist aset grafis). |
| [**Keamanan Data & Privasi (Data Safety)**](PLAY_DATA_SAFETY_AND_PRIVACY.md) | Security / Legal | Kuesioner Data Safety Play Console, draf Kebijakan Privasi resmi, dan instruksi akun peninjau (App Access). |
| [**Kepatuhan Pembayaran (Play Billing)**](PLAY_BILLING_COMPLIANCE.md) | Billing Specialist / Legal | Kepatuhan Google Play Payments Policy, integrasi Play Billing v7.1.1, SKU paket Pro, dan license testing. |
| [**Rencana Pengujian Tertutup (Closed Testing)**](CLOSED_TESTING_PLAN.md) | QA Lead / Community | Strategi closed testing akun personal (20 penguji / 14 hari), roadmap pengujian harian, dan kuesioner produksi. |
| [**Daftar Periksa Pengujian Tertutup**](CLOSED_TESTING_CHECKLIST.md) | QA / Testers | Panduan uji langkah demi langkah fungsional, edge cases, offline handling, dan kriteria kelulusan exit criteria. |
| [**Formulir Umpan Balik Penguji**](TESTER_FEEDBACK_TEMPLATE.md) | Testers / Support | Template pengumpulan laporan kendala, bug triage, rating kemudahan fitur, dan masukan penguji. |
| [**Runbook Publikasi Produksi (Staged Rollout)**](PLAY_PRODUCTION_RELEASE_RUNBOOK.md) | Release Lead / DevOps | SOP transisi ke produksi publik, rollout bertahap 20% -> 50% -> 100%, protokol halt rollout, dan mitigasi darurat. |
| [**Master Checklist Rilis Google Play**](PLAY_RELEASE_CHECKLIST.md) | Release Lead / QA | Checklist terpadu kesiapan teknis, store listing, kebijakan privasi, billing, dan stopping gate produksi. |
| [**Pencadangan & Pemulihan Bencana (Backup & DR)**](BACKUP_RESTORE.md) | DevOps / DBA | Prosedur backup database Supabase, private storage receipts, target RPO (1 jam) & RTO (2 jam), dan restore runbook. |
| [**Pemantauan & Notifikasi (Monitoring & Alerting)**](MONITORING_ALERTING.md) | DevOps / SRE | Observability native, ambang batas waspada P0/P1/P2, investigasi error log, dan sensor zero-PII. |
| [**Tanggap Darurat Insiden (Incident Response)**](INCIDENT_RESPONSE.md) | Incident Commander / Sec | Prosedur darurat penanganan 10 skenario insiden (kebocoran secret, BOLA/IDOR, AI outage, gagal bayar, crash spike). |
| [**Operasional Pembayaran (Payment Operations)**](PAYMENT_OPERATIONS.md) | FinTech / Billing Dev | SOP operasional langganan, rekonsiliasi manual pending orders, siklus auto-renew Play Billing, dan refund. |
| [**Analisis Biaya & Kapasitas (Cost & Capacity)**](COST_CAPACITY.md) | Product Owner / DevOps | Struktur biaya infrastruktur aktual (Supabase, Gemini AI, Play Console), proyeksi kapasitas 100 s.d. 10.000 user. |
| [**Batasan Teknis & Known Issues**](KNOWN_ISSUES.md) | Seluruh Tim | Catatan kredensial produksi eksternal, batasan alarm reboot Android, dan mitigasi Doze mode. |
| [**Panduan Deployment Produksi**](DEPLOYMENT.md) | DevOps / Release Lead | Runbook 15 langkah deployment produksi, urutan migrasi SQL 001-008, deploy Edge Functions, dan rollback. |

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
