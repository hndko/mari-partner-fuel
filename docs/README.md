# 📚 Portal Dokumentasi Resmi — Mari Partner Fuel

Selamat datang di pusat dokumentasi resmi **Mari Partner Fuel** (*AI-Powered Fuel Expense Tracker for Android*). Seluruh dokumentasi telah distrukturisasi secara ramping, modular, dan bebas redundansi ke dalam **14 dokumen rujukan utama**.

---

## 🧭 Peta Navigasi Dokumen (14 Dokumen Utama)

| No | Dokumen | Target Pembaca | Deskripsi & Cakupan Topik Utama |
|:--:|:--- | :--- | :--- |
| **1** | [**PRD Baseline**](PRD.md) | Product Manager / Tim Bisnis | Spesifikasi produk asli, target pengguna, model bisnis Free vs Pro, dan kriteria penerimaan MVP. |
| **2** | [**Spesifikasi Arsitektur**](ARCHITECTURE.md) | Arsitek Sistem / Lead Dev | Arsitektur Clean MVVM + Repository, Unidirectional Data Flow (`UiState`), dan mitigasi Android API 26. |
| **3** | [**Desain Basis Data & RLS**](DATABASE.md) | Backend Engineer / DBA | Skema relasional PostgreSQL Supabase, relasi foreign key komposit anti-IDOR, dan urutan migrasi SQL 001-008. |
| **4** | [**Kebijakan Keamanan & Audit**](SECURITY.md) | Security Auditor / DevOps | Model ancaman (*Threat Model*), Zero-Secret Policy, perlindungan private storage signed URL, mitigasi formula injection, dan hardening migrasi 008. |
| **5** | [**Panduan QA & Testing Hub**](TESTING.md) | QA Lead / Tester | Rekap 137 unit test (29 test suites, 100% lulus), tolok ukur performa 1.000 data transaksi, dan matriks kompatibilitas Android API 26–35. |
| **6** | [**Sistem Pembayaran Dual-Track**](PAYMENTS.md) | FinTech / Billing Dev | Arsitektur Dual-Track Play Billing v7.1.1 & QRISpy Dynamic Gateway, verifikasi purchase token sisi server, anti-replay, dan prosedur refund. |
| **7** | [**Panduan Master Deployment (A-Z)**](DEPLOYMENT.md) | DevOps / Release Lead | **Panduan operasional rilis terpadu dari A sampai Z**: Keystore signing, kompilasi R8 AAB/APK, setup Supabase, 7 Edge Functions, hingga Staged Rollout. |
| **8** | [**Panduan Closed Testing**](CLOSED_TESTING.md) | QA / Release Specialist | Rencana pengujian tertutup Google Play (aturan 20 tester / 14 hari), checklist 6 fase pengujian, dan formulir umpan balik penguji. |
| **9** | [**Metadata Toko & Privasi**](STORE_LISTING.md) | Product / Marketing / Legal | Metadata lengkap Play Store (nama, deskripsi singkat & lengkap bahasa Indonesia), kuesioner Keamanan Data (*Data Safety*), draf Kebijakan Privasi, dan akun reviewer. |
| **10**| [**Manual Operasional & SRE**](OPERATIONS.md) | DevOps / SRE / SRE Lead | Audit kesiapan produksi, SOP Backup & DR (RPO 1 jam / RTO 2 jam), standar monitoring P0/P1/P2, respons darurat 10 insiden, dan analisis biaya kapasitas. |
| **11**| [**Batasan Teknis & Known Issues**](KNOWN_ISSUES.md) | Seluruh Tim | Catatan kredensial produksi eksternal, penanganan Doze mode OEM, dan prasyarat verifikasi lingkungan live. |
| **12**| [**Buku Panduan Pengguna**](USER_MANUAL.md) | Pengguna / Tim QA | Panduan visual langkah demi langkah penggunaan fitur aplikasi: Beranda, Scan Struk OCR, Input Manual, Kendaraan, Analitik km/liter, dan Ekspor CSV. |
| **13**| [**Panduan Pengembang & CLI**](DEVELOPER_GUIDE.md) | Software Engineer | Standar coding Clean Architecture, injeksi konfigurasi aman, referensi perintah CLI Gradle/ADB, dan panduan pengujian ringan via `run-app.bat`. |

---

## ⚡ Pintasan Cepat (Quick Shortcuts)

### 💻 Untuk Pengembang Baru
1. Pasang Java JDK 21 dan pastikan Android SDK terpetakan.
2. Jalankan unit test otomatis:
   ```powershell
   .\gradlew.bat testDebugUnitTest
   ```
3. Uji coba aplikasi langsung di HP fisik atau emulator ringan tanpa membuka Android Studio:
   ```powershell
   .\run-app.bat
   ```
4. Baca selengkapnya di [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md).

### 🚀 Untuk Release & Deployment Produksi
1. Ikuti langkah demi langkah di [DEPLOYMENT.md](DEPLOYMENT.md).
2. Verifikasi kesiapan Google Play di [STORE_LISTING.md](STORE_LISTING.md) dan [CLOSED_TESTING.md](CLOSED_TESTING.md).
3. Pantau parameter operasional dan backup di [OPERATIONS.md](OPERATIONS.md).
