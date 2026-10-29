# ⛽ Panduan Setup Lengkap Mari Partner Fuel dari Awal (Langkah demi Langkah)

Buku panduan ini memandu Anda melakukan penyiapan (*setup*) aplikasi **Mari Partner Fuel** dari nol sampai aplikasi siap digunakan dan berjalan di smartphone Android fisik Anda atau emulator laptop.

---

## ⏱️ Estimasi Waktu Setup: ~10 - 15 Menit

Aplikasi ini menggunakan:
- **Klien**: Android Native (Kotlin 2.0, Jetpack Compose, Material 3).
- **OCR Struk**: **Google ML Kit (On-Device)** — *Langsung aktif otomatis di ponsel tanpa perlu API key tambahan dan tanpa memotong kuota internet!*
- **Backend & Database**: **Supabase (PostgreSQL & Storage)** — *Gratis, aman, dan online.*

---

## 📋 DAFTAR ISI

1. [🛠️ Langkah 1: Prasyarat Sistem & Alat](#️-langkah-1-prasyarat-sistem--alat)
2. [🗄️ Langkah 2: Setup Backend Supabase (Gratis)](#️-langkah-2-setup-backend-supabase-gratis)
3. [⚙️ Langkah 3: Konfigurasi Kunci di Proyek Android](#️-langkah-3-konfigurasi-kunci-di-proyek-android)
4. [🚀 Langkah 4: Menjalankan Aplikasi (Paling Ringan & Praktis)](#-langkah-4-menjalankan-aplikasi-paling-ringan--praktis)
5. [📱 Langkah 5: Uji Coba Fitur Aplikasi](#-langkah-5-uji-coba-fitur-aplikasi)
6. [🌟 Langkah 6 (Opsional): Pengaturan AI Cloud & Pembayaran](#-langkah-6-opsional-pengaturan-ai-cloud--pembayaran)
7. [❓ Tanya Jawab & Kendala Umum (Troubleshooting)](#-tanya-jawab--kendala-umum-troubleshooting)

---

## 🛠️ Langkah 1: Prasyarat Sistem & Alat

Pastikan komputer/laptop Anda telah terpasang:

1. **Java JDK 21 (LTS)**:
   - Cek versi Java di PowerShell/Terminal:
     ```powershell
     java -version
     ```
   - *Jika belum ada, unduh OpenJDK 21 dari [Adoptium Temurin](https://adoptium.net/temurin/releases/?version=21).*

2. **Android SDK & Platform Tools**:
   - Jika Anda sudah memiliki Android Studio atau Command Line Tools, lokasi standar di Windows adalah:
     `C:\Users\<Username>\AppData\Local\Android\Sdk`

3. **Perangkat untuk Menjalankan Aplikasi (Pilih Salah Satu)**:
   - **Opsi A (Sangat Direkomendasikan)**: **HP Android Fisik** dihubungkan via kabel data USB. Aktifkan **USB Debugging** di menu *Developer Options* HP Anda. Ini adalah **opsi paling ringan (0% beban emulator di laptop)**, laptop tetap dingin dan lancar!
   - **Opsi B**: **Emulator AVD** lokal (misalnya `test_avd`).

---

## 🗄️ Langkah 2: Setup Backend Supabase (Gratis)

Supabase bertindak sebagai basis data online, autentikasi pengguna, dan penyimpanan foto struk.

### 2.1 Buat Akun & Proyek Baru
1. Buka situs [supabase.com](https://supabase.com) dan klik **Start your project** (Bisa login dengan akun GitHub).
2. Di Dashboard Supabase, klik **New project**.
3. Isi data proyek:
   - **Name**: `mari-partner-fuel`
   - **Database Password**: Buat password yang kuat (dan simpan di catatan aman).
   - **Region**: Pilih **Singapore (`ap-southeast-1`)** *(Lokasi server terdekat dengan Indonesia agar respon secepat kilat)*.
   - **Pricing Plan**: Pilih **Free Tier** ($0).
4. Klik **Create new project** dan tunggu 1-2 menit hingga status database selesai dibuat.

---

### 2.2 Jalankan Migrasi Database SQL (Skema Tabel & Keamanan)
Di dalam repositori ini, telah disediakan **8 file SQL migrasi** di folder `supabase/migrations/`. Anda cukup menyalin dan menjalankannya di Supabase:

1. Di Dashboard Supabase, klik menu **SQL Editor** (ikon tanda kurung siku `_>` di sebelah kiri).
2. Klik **New query**.
3. Buka dan jalankan isi berkas SQL berikut secara berurutan (bisa buka file-nya di VS Code / Notepad, lalu salin isinya ke SQL Editor Supabase, lalu tekan tombol **Run**):
   - **Query 1**: Salin isi `supabase/migrations/20261029000001_initial_schema.sql` -> Tekan **Run**.
   - **Query 2**: Salin isi `supabase/migrations/20261029000002_rls_and_storage.sql` -> Tekan **Run**.
   - **Query 3**: Salin isi `supabase/migrations/20261029000003_vehicles_and_transactions_tahap3.sql` -> Tekan **Run**.
   - **Query 4**: Salin isi `supabase/migrations/20261029000004_receipt_scan_jobs_policy.sql` -> Tekan **Run**.
   - **Query 5**: Salin isi `supabase/migrations/20261029000005_maintenance_reminders_enhancement.sql` -> Tekan **Run**.
   - **Query 6**: Salin isi `supabase/migrations/20261029000006_subscription_entitlements_and_quota.sql` -> Tekan **Run**.
   - **Query 7**: Salin isi `supabase/migrations/20261029000007_payment_integration_and_activation.sql` -> Tekan **Run**.
   - **Query 8**: Salin isi `supabase/migrations/20261029000008_security_hardening_and_integrity.sql` -> Tekan **Run**.

*(Seluruh tabel `vehicles`, `fuel_transactions`, sistem paket Free/Pro, serta keamanan Row Level Security RLS kini telah aktif 100%!)*

---

### 2.3 Ambil Kunci URL & Anon Key Supabase
1. Di Dashboard Supabase, buka menu **Project Settings** (ikon gerigi di kiri bawah) -> **API**.
2. Salin 2 nilai berikut:
   - **Project URL** (contoh: `https://xyzprojectid.supabase.co`)
   - **anon / public key** (kunci panjang berawalan `eyJhbGciOi...`)

---

## ⚙️ Langkah 3: Konfigurasi Kunci di Proyek Android

Buka folder proyek ini di komputer Anda, lalu buat atau buka berkas `local.properties`:

1. Buka berkas `local.properties` (atau duplikasi dari `.env.example`).
2. Masukkan URL dan Anon Key Supabase yang Anda salin pada Langkah 2.3:

```properties
## Konfigurasi Supabase Mari Partner Fuel
SUPABASE_URL=https://xyzprojectid.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.salin_anon_key_anda_disini

## Opsional: Google Sign-In Client ID (bisa dikosongkan jika pakai login email)
GOOGLE_WEB_CLIENT_ID=
```

3. Simpan berkas `local.properties`.

> 🔒 **Aman**: Berkas `local.properties` otomatis terdaftar di `.gitignore` dan tidak akan pernah ter-upload ke publik.

---

## 🚀 Langkah 4: Menjalankan Aplikasi (Paling Ringan & Praktis)

Anda **tidak perlu membuka Android Studio** yang memakan banyak RAM! Di root proyek ini telah disediakan script otomatis `run-app.bat`.

### Cara Menjalankan:
1. Hubungkan HP Android Anda via kabel data USB (atau pastikan emulator AVD sudah siap).
2. Buka terminal PowerShell di folder proyek ini, lalu ketik:
   ```powershell
   .\run-app.bat
   ```
   *(Atau klik dua kali berkas `run-app.bat` di File Explorer Windows Anda)*.

3. Script otomatis akan:
   - Mendeteksi Android SDK di laptop Anda secara otomatis.
   - Mendeteksi HP fisik Anda *(Pilih nomor [1] untuk menjalankan langsung di HP fisik Anda)*.
   - Mengompilasi aplikasi dengan Gradle.
   - Memasang APK (`app-debug.apk`) ke HP Anda dan langsung membuka aplikasi di layar!
   - Menyediakan opsi untuk memantau logcat real-time jika Anda ingin melihat log aplikasi.

---

## 📱 Langkah 5: Uji Coba Fitur Aplikasi

Setelah aplikasi terbuka di layar smartphone Anda:

1. **Daftar Akun Baru**:
   - Tekan tautan **Daftar Akun Baru**.
   - Masukkan alamat email Anda dan password (minimal 6 karakter).
   - Akun langsung aktif dan Anda akan otomatis masuk ke Beranda!

2. **Tambah Kendaraan Pertama**:
   - Masuk ke tab **Kendaraan** (atau tekan tombol tambah di beranda).
   - Masukkan Nama Kendaraan (contoh: *Honda Vario 160* atau *Toyota Avanza*), Jenis, Nomor Plat, dan Odometer awal (km).
   - Tekan **Simpan Kendaraan**.

3. **Uji Coba Input Transaksi Manual**:
   - Tekan tombol **Input Manual**.
   - Masukkan total nominal Rupiah (misal `50000` -> otomatis diformat jadi `Rp 50.000`).
   - Masukkan volume liter (misal `5` liter).
   - Masukkan angka odometer saat isi bensin.
   - Tekan **Simpan Transaksi**.

4. **Uji Coba Scan Struk BBM (Google ML Kit On-Device)**:
   - Tekan tombol **Scan Struk** (berwarna Teal).
   - Berikan izin Kamera, lalu arahkan ke struk SPBU Pertamina, Shell, atau BP (atau pilih foto struk dari galeri).
   - Teks struk akan terbaca **instan secara lokal** oleh teknologi Google ML Kit.
   - Periksa angka di **Layar Peninjauan (Review)**, lalu tekan **Konfirmasi & Simpan Transaksi**.

5. **Lihat Dashboard & Ekspor Laporan**:
   - Buka layar **Beranda / Dashboard** untuk melihat total belanja bulanan, total liter, dan rata-rata harga/liter.
   - Buka tab **Analitik** untuk melihat grafik konsumsi kilometer per liter (km/liter).
   - Buka tab **Riwayat**, lalu tekan ikon **Unduh CSV** untuk mengunduh laporan Excel yang rapi!

---

## 🌟 Langkah 6 (Opsional): Pengaturan AI Cloud & Pembayaran

Aplikasi sudah 100% berfungsi normal dengan fitur gratis di atas. Jika Anda ingin mengaktifkan fitur tambahan:

### A. Fitur AI Cloud Fallback (Google Gemini)
*Digunakan jika ada struk yang sangat buram/berkerut dan ingin dibantu ekstraksi oleh Gemini AI:*
1. Dapatkan API Key gratis di [Google AI Studio](https://aistudio.google.com/).
2. Di Supabase CLI / Secrets, pasang:
   ```bash
   supabase secrets set GEMINI_API_KEY="AIzaSy..."
   ```

### B. Fitur Langganan Pro (Google Play Billing / QRISpy)
- Pada versi Google Play Store, langganan menggunakan in-app billing resmi Google Play.
- Pada versi direct APK, aplikasi dapat menerima pembayaran QRIS dinamis via gateway [qrispy.id](https://qrispy.id).

---

## ❓ Tanya Jawab & Kendala Umum (Troubleshooting)

### Q1: Saat menjalankan `.\run-app.bat`, muncul pesan `Android SDK tidak ditemukan`?
- **Solusi**: Pastikan Android SDK sudah terpasang. Lokasi standarnya adalah:
  `C:\Users\<NamaUserAnda>\AppData\Local\Android\Sdk`.
  Saat script meminta input path, cukup masukkan lokasi path folder SDK Anda tersebut.

### Q2: HP Android saya tidak terdeteksi di `run-app.bat`?
- **Solusi**:
  1. Pastikan Anda mengaktifkan **USB Debugging** di HP Anda (*Settings -> About Phone -> Ketuk 'Build Number' 7 kali -> Buka Developer Options -> Aktifkan USB Debugging*).
  2. Gunakan kabel data USB yang mendukung transfer data (bukan kabel charger biasa).
  3. Saat muncul pop-up *"Allow USB debugging?"* di layar HP, pilih **Always allow** dan tekan **OK**.

### Q3: Foto struk gagal disimpan atau tidak muncul?
- **Solusi**: Pastikan Anda sudah menjalankan SQL migrasi ke-2 (`20261029000002_rls_and_storage.sql`) yang membuat bucket penyimpanan bernama `receipts`.

### Q4: Apakah butuh kuota internet untuk scan struk?
- **TIDAK**: Pemindaian struk menggunakan **Google ML Kit Text Recognition** yang berjalan 100% lokal di prosesor smartphone Anda tanpa internet. Internet hanya digunakan saat aplikasi menyinkronkan data transaksi ke akun Supabase Anda.

---

🎉 **Selamat! Aplikasi Mari Partner Fuel Anda kini telah siap digunakan sepenuhnya.**
