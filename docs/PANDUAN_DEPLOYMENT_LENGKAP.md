# 🚀 PANDUAN_DEPLOYMENT_LENGKAP.md — Buku Panduan Master Deployment Produksi End-to-End

Buku panduan ini memuat instruksi operasional resmi, menyeluruh, dan bertahap (*step-by-step master guide*) dari **A sampai Z** untuk mendeploy dan merilis sistem **Mari Partner Fuel** (*Klien Android Native Kotlin/Jetpack Compose, Backend PostgreSQL Supabase, Private Storage, Edge Functions, serta Integrasi Pembayaran Google Play Billing & QRISpy*).

---

## 🧭 Peta Alur Deployment Produksi

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        ALUR DEPLOYMENT PRODUKSI MARI PARTNER FUEL                      │
├─────────────────┬─────────────────┬─────────────────┬─────────────────┬────────────────┤
│ 1. Prasyarat    │ 2. Keystore     │ 3. Build R8     │ 4. Supabase DB  │ 5. Edge Funcs  │
│    Alat & JDK 21│    & Signing    │    AAB & APK    │    & Storage    │    & Secrets   │
├─────────────────┼─────────────────┼─────────────────┼─────────────────┼────────────────┤
│ 6. Layanan AI   │ 7. Play Console │ 8. Closed Test  │ 9. Staged       │ 10. Monitoring │
│    & QRISpy Gate│    Listing/Data │    20 Testers   │    Rollout 20%  │    & Rollback  │
└─────────────────┴─────────────────┴─────────────────┴─────────────────┴────────────────┘
```

---

## 🛠️ Bagian 1: Prasyarat Lingkungan & Alat Rilis

Sebelum memulai deployment, pastikan komputer/server Anda telah terpasang perangkat lunak berikut:

1. **Java Development Kit (JDK 21 LTS)**:
   - Verifikasi instalasi:
     ```powershell
     java -version
     # Pastikan output menampilkan: openjdk version "21.x.x"
     ```
2. **Android SDK & Command-Line Tools**:
   - Lokasi standar Windows: `C:\Users\%USERNAME%\AppData\Local\Android\Sdk`
   - Paket yang wajib terpasang:
     - `platforms;android-35`
     - `build-tools;35.0.0`
     - `platform-tools` (adb)
3. **Supabase CLI** (Opsional jika menggunakan CLI, atau dapat menggunakan Supabase Web Dashboard):
   - Instalasi via Scoop / NPM:
     ```powershell
     npm install -g supabase
     supabase --version
     ```
4. **Git Client**:
   - Memastikan branch `main` berada pada commit rilis stabil tanpa perubahan yang belum di-commit.

---

## 🔑 Bagian 2: Manajemen Keystore & Keamanan Kredensial Produksi

Aplikasi Android untuk Google Play Store wajib ditandatangani (*signed*) menggunakan kunci rilis resmi (*Release Keystore*).

### 2.1 Membuat Kunci Rilis Baru (Jika Belum Tersedia)
Jalankan utilitas `keytool` bawaan JDK untuk menghasilkan berkas keystore produksi:

```powershell
keytool -genkeypair -v `
  -keystore "mari_partner_release.keystore" `
  -alias "mari_partner_key" `
  -keyalg RSA `
  -keysize 2048 `
  -validity 10000
```
*Simpan berkas `mari_partner_release.keystore` dan password-nya di tempat yang sangat aman (misal: Bitwarden / 1Password / Vault perusahaan).*

> ⚠️ **PERINGATAN ZERO-SECRET**:
> Dilarang meng-commit berkas `*.keystore`, `*.jks`, atau password ke dalam repositori Git! Berkas ini telah dikecualikan di `.gitignore`.

### 2.2 Konfigurasi Signing di Proyek Lokal
Tambahkan konfigurasi berikut ke berkas `local.properties` (atau set sebagai variabel lingkungan):

```properties
# Tambahkan di baris paling bawah local.properties:
RELEASE_STORE_FILE=C:\\Path\\To\\mari_partner_release.keystore
RELEASE_STORE_PASSWORD=PasswordKeystoreAnda
RELEASE_KEY_ALIAS=mari_partner_key
RELEASE_KEY_PASSWORD=PasswordKeyAnda

# Konfigurasi Supabase Produksi Klien (Aman di APK/BuildConfig):
SUPABASE_URL=https://prod-mpf.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOi...anon_key_produksi...
GOOGLE_WEB_CLIENT_ID=xxxxxxxxxxxx.apps.googleusercontent.com
```

---

## 📦 Bagian 3: Kompilasi Paket Rilis Produksi (AAB & APK)

Proyek telah dikonfigurasi dengan **R8 Minification** dan **Resource Shrinking** (`isMinifyEnabled = true`, `isShrinkResources = true`).

### 3.1 Menjalankan Verifikasi Test Mutu
Sebelum kompilasi rilis, pastikan 100% dari 137 unit test lulus tanpa kegagalan:

```powershell
.\gradlew.bat testDebugUnitTest
```

### 3.2 Membuat Android App Bundle (AAB) untuk Google Play Store
Paket AAB adalah format wajib untuk rilis toko Google Play:

```powershell
.\gradlew.bat bundleRelease
```
- **Lokasi Artefak Keluaran**:
  `app/build/outputs/bundle/release/app-release.aab`
- **Ukuran Hasil Kompilasi**: **`7.57 MB`** *(Tereduksi ~36% dibandingkan build debug)*.

### 3.3 Membuat Universal Signed APK untuk Distribusi Langsung (Direct/Web)
Jika Anda membutuhkan berkas APK fisik untuk pengujian lokal mandiri atau distribusi web:

```powershell
.\gradlew.bat assembleRelease
```
- **Lokasi Artefak Keluaran**:
  `app/build/outputs/apk/release/app-release.apk`
- **Ukuran Hasil Kompilasi**: **`3.39 MB`** *(Tereduksi ~85.3% dibandingkan build debug)*.

---

## 🗄️ Bagian 4: Setup Backend Supabase Cloud Lengkap

### 4.1 Pembuatan Proyek Supabase Produksi
1. Buka [Supabase Dashboard](https://supabase.com/dashboard) dan klik **New project**.
2. Masukkan nama proyek: `mari-partner-fuel-prod`.
3. Pilih wilayah server (*Region*): **Singapore (`ap-southeast-1`)** *(Direkomendasikan untuk latensi tercepat pengguna Indonesia)*.
4. Buat dan simpan password database PostgreSQL yang kuat.

### 4.2 Menjalankan 8 Berkas Migrasi SQL (Secara Berurutan)
Buka menu **SQL Editor** pada Supabase Dashboard, lalu salin dan jalankan isi dari 8 berkas migrasi SQL secara berurutan:

```text
Urutan Eksekusi Wajib:
1. supabase/migrations/20261029000001_initial_schema.sql
   -> Membuat tabel: user_profiles, vehicles, fuel_transactions, maintenance_reminders.
2. supabase/migrations/20261029000002_rls_and_storage.sql
   -> Mengaktifkan RLS dasar dan membuat private storage bucket 'receipts'.
3. supabase/migrations/20261029000003_vehicles_and_transactions_tahap3.sql
   -> Mengunci foreign key komposit (vehicle_id, user_id) anti-BOLA/IDOR.
4. supabase/migrations/20261029000004_receipt_scan_jobs_policy.sql
   -> Membuat tabel dan kebijakan antrean pelacakan scan OCR struk.
5. supabase/migrations/20261029000005_maintenance_reminders_enhancement.sql
   -> Otomasi kalkulasi status jatuh tempo pengingat servis.
6. supabase/migrations/20261029000006_subscription_entitlements_and_quota.sql
   -> Katalog paket Pro, RPC atomic quota reserve/finalize, trigger batas 2 kendaraan Free.
7. supabase/migrations/20261029000007_payment_integration_and_activation.sql
   -> Tabel transaksi pembayaran, log webhook, RPC activate_subscription_for_payment.
8. supabase/migrations/20261029000008_security_hardening_and_integrity.sql
   -> Pencabutan izin publik (REVOKE EXECUTE) pada seluruh fungsi finansial;
      Akses dibatasi eksklusif hanya untuk peran 'service_role'.
```

### 4.3 Verifikasi Keamanan RLS & Private Storage Bucket
Jalankan kueri verifikasi berikut di SQL Editor:

```sql
-- 1. Pastikan Row Level Security aktif pada seluruh 13 tabel publik:
SELECT tablename, rowsecurity FROM pg_tables WHERE schemaname = 'public';

-- 2. Pastikan Storage Bucket 'receipts' berstatus private (public = false):
SELECT id, name, public FROM storage.buckets WHERE id = 'receipts';
```

---

## ⚡ Bagian 5: Deployment Supabase Edge Functions & Setup Secrets Vault

### 5.1 Deployment 7 Edge Functions Backend
Jalankan perintah Supabase CLI dari direktori proyek:

```bash
# Hubungkan CLI ke proyek Supabase produksi Anda:
supabase link --project-ref <YOUR_PRODUCTION_PROJECT_REF>

# Deploy seluruh 7 fungsi backend:
supabase functions deploy extract-fuel-receipt
supabase functions deploy get-my-entitlements
supabase functions deploy create-checkout
supabase functions deploy verify-play-purchase
supabase functions deploy get-payment-status
supabase functions deploy payment-webhook
supabase functions deploy sync-subscription
```

### 5.2 Memasukkan Kunci Rahasia ke Supabase Secrets Vault
Kunci rahasia backend **TIDAK PERNAH** ditaruh di APK klien. Masukkan rahasia langsung ke Supabase Vault:

```bash
# Kunci API Google Gemini untuk AI Fallback:
supabase secrets set GEMINI_API_KEY="AIzaSyYourProductionGeminiKey"

# Service Account Google Play untuk Verifikasi Pembelian In-App:
supabase secrets set GOOGLE_PLAY_SERVICE_ACCOUNT_JSON='{"type":"service_account","project_id":"mpf-prod",...}'

# Kredensial Live Merchant Gateway QRISpy:
supabase secrets set QRISPY_API_KEY="qrispy_live_xxxxxxxxxxxxxxxx"
supabase secrets set QRISPY_WEBHOOK_SECRET="whsec_xxxxxxxxxxxxxxxx"
```

---

## 💳 Bagian 6: Integrasi Layanan Pihak Ketiga (QRISpy & Gemini AI)

### 6.1 Pengaturan Webhook QRISpy Gateway
1. Buka portal merchant [https://dashboard.qrispy.id](https://dashboard.qrispy.id).
2. Masuk ke menu **Pengaturan** -> **Webhooks / Callback URL**.
3. Daftarkan URL webhook resmi Edge Function:
   `https://<YOUR_PRODUCTION_PROJECT_REF>.supabase.co/functions/v1/payment-webhook`
4. Pilih event: `payment.success`, `payment.failed`, `payment.expired`.
5. Salin *Webhook Signing Secret* dan masukkan ke Supabase Vault (`QRISPY_WEBHOOK_SECRET`).

### 6.2 Pengaturan Google Gemini Cloud AI
1. Buka [Google AI Studio](https://aistudio.google.com/) atau Google Cloud Console.
2. Buat API Key untuk model `gemini-1.5-flash`.
3. Pasang batas kuota harian (*Daily Budget Alert*) di Google Cloud Console untuk mencegah lonjakan tagihan tak terduga.

---

## 🏪 Bagian 7: Pengaturan Google Play Console (Closed Testing & Store Listing)

### 7.1 Pembuatan Aplikasi Baru di Google Play Console
1. Buka [Google Play Console](https://play.google.com/console).
2. Klik **Create app**:
   - App name: **Mari Partner Fuel**
   - Default language: **Indonesian (id-ID)**
   - App or game: **App**
   - Free or paid: **Free** *(Aplikasi gratis dengan pembelian in-app langganan Pro)*
3. Setujui deklarasi kepatuhan hukum dan klik **Create app**.

### 7.2 Melengkapi Formulir Kebijakan & Keamanan Data (Data Safety)
Ikuti panduan terperinci pada [`docs/PLAY_DATA_SAFETY_AND_PRIVACY.md`](PLAY_DATA_SAFETY_AND_PRIVACY.md):
- **Kebijakan Privasi (Privacy Policy)**:
  Tautkan ke URL publik: `https://maripartner.id/privacy-policy`.
- **Akses Aplikasi (App Access)**:
  Berikan instruksi login reviewer dan akun uji:
  Email: `reviewer@maripartner.id`, Password: `[PasswordUjiYangDibuat]`.
- **Keamanan Data (Data Safety)**:
  Deklarasikan pengumpulan: Alamat Email, Riwayat Pengeluaran BBM, Foto Struk, dan Log Crash. Deklarasikan bahwa **data tidak dibagikan ke pihak ketiga** dan **dienkripsi penuh dalam transit**.
- **Deklarasi Iklan**: Dinyatakan **"Tidak ada iklan"**.
- **Audiens Target**: Ditetapkan **18 tahun ke atas**.
- **Rating Konten (IARC)**: Lengkapi kuesioner rating (ekspektasi: Semua Umur / PEGI 3).

### 7.3 Konfigurasi Produk Langganan In-App (Subscriptions)
Masuk ke menu **Monetize** -> **Subscriptions** -> Buat 2 produk langganan:
1. **`mari_pro_monthly`**:
   - Nama: Mari Pro — Bulanan
   - Base plan: Auto-renewing, durasi 1 bulan
   - Harga: **Rp 29.000** (Nett)
   - Grace period: 3 Hari
2. **`mari_pro_yearly`**:
   - Nama: Mari Pro — Tahunan
   - Base plan: Auto-renewing, durasi 1 tahun
   - Harga: **Rp 290.000** (Nett)
   - Grace period: 7 Hari

### 7.4 Mengunggah Draf Daftar Toko (Store Listing)
Salin teks resmi dari [`docs/PLAY_STORE_LISTING.md`](PLAY_STORE_LISTING.md):
- **Short Description (79 Karakter)**:
  `Catat pengeluaran BBM & servis kendaraan dengan scan struk OCR cepat & akurat.`
- **Full Description**:
  Salin draf lengkap bahasa Indonesia yang telah disusun bebas dari klaim hiperbolis.
- **Aset Grafis**:
  - Unggah Ikon Aplikasi (PNG 512 x 512 px).
  - Unggah Feature Graphic (JPEG/PNG 1024 x 500 px).
  - Unggah minimal 4 tangkapan layar ponsel rasio 9:16 (1080 x 1920 atau 1080 x 2400 px).

---

## 👥 Bagian 8: Pelaksanaan Closed Testing (20 Penguji / 14 Hari)

Sesuai regulasi Google Play bagi akun pengembang personal:

1. Buka menu **Testing** -> **Closed testing** -> Jalur *Closed testing (Alpha)*.
2. Buat daftar penguji (*Email list*) berisi minimal **20 alamat email Google penguji nyata**.
3. Buat rilis baru dan unggah berkas:
   `app/build/outputs/bundle/release/app-release.aab`
4. Bagikan tautan web opt-in kepada para penguji:
   `https://play.google.com/apps/testing/id.maripartner.fuel`
5. **Jalankan Pengujian Selama Minimal 14 Hari Penuh**:
   - Pastikan 20 penguji tetap berstatus opt-in aktif dan membuka aplikasi secara berkala.
   - Kumpulkan laporan kendala menggunakan template [`docs/TESTER_FEEDBACK_TEMPLATE.md`](TESTER_FEEDBACK_TEMPLATE.md).
   - Pantau *Android Vitals* di Google Play Console (Crash-free rate wajib > 99.5%).

---

## 🚀 Bagian 9: Peluncuran Bertahap ke Publik (Staged Rollout)

Setelah periode 14 hari pengujian tertutup selesai dan permohonan akses produksi disetujui Google:

1. Buka **Release** -> **Production** -> **Create new release**.
2. Pilih AAB final dari release library.
3. Pilih metode peluncuran: **Staged rollout** -> Atur persentase awal: **`20%`**.
4. Jadwal Peningkatan Rollout:
   - **Hari 1–2 (20%)**: Pantau crash rate dan keluhan pengguna.
   - **Hari 3–4 (50%)**: Tingkatkan jika crash rate < 0.5%.
   - **Hari 5 (100%)**: Rilis penuh ke seluruh pengguna publik.

---

## 🚨 Bagian 10: Protokol Tanggap Darurat & Penghentian Rollout (Halt Rollout)

Jika terdeteksi lonjakan crash (> 1%) atau kegagalan fatal pada rilis produksi:

1. Buka Play Console -> Production -> Klik **Halt rollout** seketika.
2. Buat branch perbaikan darurat: `hotfix/v1.7.1`.
3. Perbaiki bug, perbarui nomor versi di `app/build.gradle.kts`:
   `versionCode = 9`, `versionName = "1.7.1"`.
4. Kompilasi AAB baru: `.\gradlew.bat bundleRelease`.
5. Rilis AAB hotfix baru dengan rollout 100% untuk menimpa rilis bermasalah.

---

## ⚡ Bagian 11: Menjalankan Aplikasi di Komputer Pengembang (Tanpa Android Studio)

Untuk menguji aplikasi di laptop tanpa membuka Android Studio yang berat, cukup jalankan script batch otomatis yang telah disediakan di root repositori:

```powershell
.\run-app.bat
```
Script akan:
1. Otomatis mendeteksi Android SDK dan HP fisik Anda (0% beban emulator di laptop!).
2. Atau menawarkan untuk menyalakan emulator AVD (`test_avd`) dalam mode hemat sumber daya (tanpa animasi boot, memory 2GB, 2 core CPU).
3. Mengompilasi APK debug via Gradle daemon.
4. Memasang APK dan membuka aplikasi secara instan di layar perangkat.
5. Menyediakan opsi pemantauan logcat real-time khusus Mari Partner Fuel.
