# ⛽ Panduan Setup Lengkap Mari Partner Fuel dari Awal (Langkah demi Langkah)

Buku panduan ini memandu Anda melakukan penyiapan (*setup*) aplikasi **Mari Partner Fuel** dari nol sampai aplikasi siap digunakan dan berjalan di smartphone Android fisik Anda atau emulator laptop.

---

## ⏱️ Estimasi Waktu Setup: ~10 - 15 Menit

Repositori ini menggunakan arsitektur **Monorepo** yang memisahkan aplikasi seluler pengemudi dan portal web pemilik:
- **`mobile/`**: Aplikasi Android Native (Kotlin 2.0, Jetpack Compose, Material 3) khusus untuk pengemudi dan manajemen armada.
- **`web/`**: Platform Web modern (Vue 3, Vite, Tailwind CSS) berisi **Landing Page Publik** (unduh APK 1-tap) dan **SaaS Owner Console** (`/admin/*`) untuk monitoring omset, saldo merchant QRISpy, mutasi gateway, serta konfigurasi promo harga paket.
- **OCR Struk**: **Google ML Kit (On-Device)** — *Langsung aktif otomatis di ponsel tanpa perlu API key tambahan dan tanpa memotong kuota internet!*
- **Backend & Database**: **Supabase (PostgreSQL 15+ & Storage)** — *Gratis, aman, dan online.*

---

## 📋 DAFTAR ISI

1. [🛠️ Langkah 1: Prasyarat Sistem & Alat](#️-langkah-1-prasyarat-sistem--alat)
2. [🗄️ Langkah 2: Setup Backend Supabase (Gratis)](#️-langkah-2-setup-backend-supabase-gratis)
3. [⚙️ Langkah 3: Konfigurasi Kunci di Proyek Android (.env / local.properties)](#️-langkah-3-konfigurasi-kunci-di-proyek-mobile--web-env)
   - [3.1 Cara Konfigurasi "Masuk dengan Google" (Google Sign-In OAuth)](#31-cara-konfigurasi-masuk-dengan-google-google-sign-in-oauth)
4. [🚀 Langkah 4: Menjalankan Aplikasi (Paling Ringan & Praktis)](#-langkah-4-menjalankan-aplikasi-paling-ringan--praktis)
5. [📱 Langkah 5: Uji Coba Fitur Aplikasi](#-langkah-5-uji-coba-fitur-aplikasi)
6. [🌟 Langkah 6: Pengaturan Kunci Server, Webhook QRISpy & Gateway Mutasi](#-langkah-6-opsional-pengaturan-kunci-server-supabase-service-role-gemini-ai--qrispy)
   - [6.1 Supabase Service Role Key](#1-supabase_service_role_key-superadmin-database-key)
   - [6.2 Gemini AI Key](#2-gemini_api_key-ai-cloud-fallback-google)
   - [6.3 QRISpy API Token](#3-qrispy_api_token-payment-gateway-qris-dinamis)
   - [6.4 Setup Webhook QRISpy & Verifikasi Tanda Tangan (HMAC-SHA256)](#4-qrispy_webhook_secret--konfigurasi-webhook-notifikasi-otomatis)
   - [6.5 Pembatalan Manual Transaksi QRIS](#5-pembatalan-manual-transaksi-qris-sebelum-masa-berlaku-15-menit)
   - [6.6 Integrasi Saldo & Mutasi Gateway QRISpy di Web Owner Console](#6-integrasi-saldo--mutasi-gateway-qrispy-di-web-owner-console)
7. [🌐 Langkah 7: Panduan Deploy Platform Web & Owner Console ke Vercel](#-langkah-7-panduan-deploy-platform-web--owner-console-ke-vercel)
   - [7.1 Pengaturan Project Name & Root Directory](#71-pengaturan-project-name--root-directory)
   - [7.2 Konfigurasi Framework & Build Settings](#72-konfigurasi-framework--build-settings)
   - [7.3 Pengisian Variabel Lingkungan (.env) Praktis](#73-pengisian-variabel-lingkungan-env-praktis)
   - [7.4 Melewati Optional Integrations Supabase](#74-melewati-optional-integrations-supabase)
   - [7.5 Peluncuran (Deploy) & Pengujian Live URL](#75-peluncuran-deploy--pengujian-live-url)
8. [❓ Tanya Jawab & Kendala Umum (Troubleshooting)](#-tanya-jawab--kendala-umum-troubleshooting)

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
3. Isi data proyek sesuai tampilan formulir pembuatan proyek Supabase:
   - **Organization**: Pilih organisasi Anda (misal: `Mari Partner FREE`).
   - **GitHub (optional)**: Boleh dikosongkan.
   - **Project name**: Masukkan `mari-partner-fuel`.
   - **Database password**: Klik *Generate a password*, lalu **wajib klik Copy** dan simpan password tersebut di catatan aman Anda!
   - **Region**: 
     > [!TIP]
     > Pada menu Region, klik **"Need a specific region?"** atau pilih dropdown: **Singapore (`ap-southeast-1`)**.
     > Memilih server Singapore sangat disarankan dibandingkan opsi `Auto` agar lokasi server berada paling dekat dengan Indonesia (latensi super rendah ~15–30 ms) sehingga respon data di ponsel secepat kilat.
   - **Security**:
     - ✅ **Enable Data API** (Wajib dicentang, agar klien Android Supabase Kotlin SDK dapat berkomunikasi via RESTful API).
     - ✅ **Automatically expose new tables** (Dicentang agar tabel yang dibuat otomatis terekspos ke Data API).
     - 🔲 **Enable automatic RLS** (Boleh dicentang atau dibiarkan kosong, karena seluruh skrip migrasi SQL Mari Partner Fuel sudah menyertakan perintah `ENABLE ROW LEVEL SECURITY` secara eksplisit pada setiap tabel).
   - **Advanced Configuration -> Postgres Type**:
     - Tetap pilih **`Postgres (DEFAULT)`** *(Jangan pilih OrioleDB BETA agar stabil dan kompatibel penuh)*.
4. Klik tombol hijau **Create new project** di kanan bawah dan tunggu 1–2 menit hingga status database selesai dibuat dan tampil tulisan hijau **`Healthy`** seperti pada dashboard utama.

---

### 2.2 Jalankan Migrasi Database SQL (Skema Tabel & Keamanan)
Setelah database berstatus **Healthy**, langkah berikutnya adalah membuat tabel, aturan keamanan RLS, dan storage bucket dengan menjalankan 8 berkas SQL migrasi.

Anda bisa memilih salah satu dari **dua metode** berikut (keduanya menghasilkan database yang sama persis):

---

#### 🟢 METODE 1 (Sangat Direkomendasikan & Paling Praktis): Lewat Web SQL Editor
Metode ini **100% tanpa perlu install/login tool CLI** tambahan di terminal, langsung selesai di browser:

1. Di bilah menu hitam sebelah kiri (sidebar), klik ikon ke-3 dari atas: **SQL Editor** (ikon tanda prompt terminal `>_` tepat di atas menu *Database*).
2. Klik tombol **New query** (atau ikon tanda tambah `+`).
3. Buka dan jalankan isi berkas SQL dari folder `supabase/migrations/` di proyek Anda secara berurutan:
   - **Query 1**: Buka file `supabase/migrations/20261029000001_initial_schema.sql`, salin seluruh isinya, tempel ke SQL Editor Supabase -> Tekan tombol hijau **Run** (atau `Ctrl + Enter`).
   - **Query 2**: Buka file `supabase/migrations/20261029000002_rls_and_storage.sql`, salin isinya, tempel ke SQL Editor -> Tekan **Run**. *(Ini otomatis membuat bucket penyimpanan privat `receipts` & aturan keamanannya)*.
   - **Query 3**: Buka file `supabase/migrations/20261029000003_vehicles_and_transactions_tahap3.sql` -> Tempel & Tekan **Run**.
   - **Query 4**: Buka file `supabase/migrations/20261029000004_receipt_scan_jobs_policy.sql` -> Tempel & Tekan **Run**.
   - **Query 5**: Buka file `supabase/migrations/20261029000005_maintenance_reminders_enhancement.sql` -> Tempel & Tekan **Run**.
   - **Query 6**: Buka file `supabase/migrations/20261029000006_subscription_entitlements_and_quota.sql` -> Tempel & Tekan **Run**.
   - **Query 7**: Buka file `supabase/migrations/20261029000007_payment_integration_and_activation.sql` -> Tempel & Tekan **Run**.
   - **Query 8**: Buka file `supabase/migrations/20261029000008_security_hardening_and_integrity.sql` -> Tempel & Tekan **Run**.
   - **Query 9**: Buka file `supabase/migrations/20261030000001_ephemeral_receipt_storage_policy.sql` -> Tempel & Tekan **Run**. *(Kebijakan retensi struk ephemeral: transaksi BBM disimpan murni berbasis string/angka di database tanpa menyimpan file foto struk permanen di cloud storage)*.
   - **Query 10**: Buka file `supabase/migrations/20261030000002_saas_admin_owner_dashboard.sql` -> Tempel & Tekan **Run**. *(Fungsi otoritatif analitik SaaS Owner, kolom role di tabel profiles, grafik pendaftar & mutasi status Pro manual)*.
   - **Query 11**: Buka file `supabase/migrations/20261030000003_saas_owner_plan_and_tx_management.sql` -> Tempel & Tekan **Run**. *(Manajemen katalog harga paket SaaS & audit transaksi masuk)*.
   - **Query 12**: Buka file `supabase/migrations/20261031000001_enforce_vehicle_quota_trigger.sql` -> Tempel & Tekan **Run**. *(Trigger proteksi kuota 2 kendaraan aktif paket Free di level database)*.
   - **Query 13**: Buka file `supabase/migrations/20261031000002_subscription_downgrade_and_renewal_rules.sql` -> Tempel & Tekan **Run**. *(Aturan downgrade graceful cancel_at_period_end & siklus perpanjangan paket Pro)*.

> [!TIP]
> **Mengangkat Akun Pemilik (SaaS Owner)**:
> Migration 10 secara otomatis menetapkan akun pertama yang terdaftar di database Anda sebagai `owner`. Jika Anda ingin mengangkat akun email spesifik Anda sebagai Owner secara langsung, jalankan perintah SQL berikut di SQL Editor:
> ```sql
> update public.profiles
> set role = 'owner'
> where id in (select id from auth.users where email = 'email_anda@gmail.com');
> ```

> [!NOTE]
> Pastikan setiap query menampilkan pesan sukses `Success. No rows returned` sebelum berpindah ke query berikutnya.

---

#### ⚡ METODE 2 (Otomatis Sekali Jalan): Lewat Supabase CLI di Terminal
Jika Anda ingin mengeksekusi seluruh 13 file migrasi sekaligus secara otomatis dari terminal laptop:

1. Buka terminal PowerShell di folder proyek ini.
2. Login ke akun Supabase Anda:
   ```powershell
   npx -y supabase login
   ```
   *(Browser akan terbuka otomatis untuk mengonfirmasi login dan membuat access token)*.
3. Hubungkan proyek lokal dengan proyek Supabase remote Anda:
   ```powershell
   npx -y supabase link --project-ref zcezyhgrblksfuxmszyz
   ```
   *(Masukkan Database Password yang Anda simpan saat membuat proyek)*.
4. Dorong (*push*) seluruh 13 migrasi SQL yang ada di folder `supabase/migrations/` sekaligus:
   ```powershell
   npx -y supabase db push
   ```
5. Periksa status sinkronisasi migrasi:
   ```powershell
   npx -y supabase migration list
   ```

---

---

### 2.3 Matikan Verifikasi Email (Sangat Disarankan untuk Kemudahan Login)
Secara bawaan, Supabase mewajibkan pengguna mengklik tautan verifikasi email sebelum bisa login. Pada paket gratis (*Free Tier*), Supabase memiliki kuota batas kirim email yang sangat terbatas (3-4 email per jam).

Agar Anda dan pengguna dapat langsung mendaftar dan login seketika di aplikasi Android tanpa hambatan:
1. Di bilah menu sebelah kiri, klik ikon **Authentication** (ikon dua orang / pengguna).
2. Di submenu Authentication, klik **Providers**.
3. Klik pada opsi **Email** untuk membuka pengaturannya.
4. Matikan toggle **"Confirm email"** (geser menjadi nonaktif/abu-abu).
5. Klik tombol **Save** di bagian bawah.

---

### 2.4 Ambil Kunci URL & Anon Key Supabase
Anda memerlukan 2 nilai dari Supabase untuk dimasukkan ke konfigurasi Android:

1. **Project URL**:
   - Dapat disalin langsung dari halaman **Home** / **Project Overview** dashboard (di bawah tulisan nama project Anda `mari-partner-fuel`, terdapat URL `https://<id-projek>.supabase.co` dengan tombol **Copy**).
2. **Anon Public Key**:
   - Di bilah menu kiri paling bawah, klik ikon **Project Settings (Gerigi ⚙️)**.
   - Di menu Settings, klik **API** (atau **Data API**).
   - Di bagian **Project API keys**, cari kunci bertuliskan **`anon` / `public`**.
   - Klik tombol **Copy** (kunci panjang JWT berawalan `eyJhbGciOi...`).

---

## ⚙️ Langkah 3: Konfigurasi Kunci di Proyek Mobile & Web (.env)

Proyek ini telah dikonfigurasi untuk **secara otomatis membaca berkas `.env`** di modul `mobile/` dan `web/` (atau `mobile/local.properties`):

1. **Aplikasi Android (`mobile/.env`)**:
   Salin `mobile/.env.example` menjadi `mobile/.env`, lalu masukkan URL dan Anon Key Supabase Anda:
   ```properties
   ## Konfigurasi Supabase Mari Partner Fuel (Android)
   SUPABASE_URL=https://zcezyhgrblksfuxmszyz.supabase.co
   SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

   ## Opsional: Google OAuth Web Client ID
   GOOGLE_WEB_CLIENT_ID=
   ```

2. **Platform Web & SaaS Owner Console (`web/.env`)**:
   Salin `web/.env.example` menjadi `web/.env`:
   ```properties
   ## Konfigurasi Supabase Mari Partner Fuel (Web)
   SUPABASE_URL=https://zcezyhgrblksfuxmszyz.supabase.co
   SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

   ## Info Versi & Rilis APK
   VITE_APP_VERSION=2.0.8
   VITE_APP_APK_FILENAME=mari-partner-fuel-v2.0.8.apk
   VITE_APP_APK_URL=/downloads/mari-partner-fuel-v2.0.8.apk
   ```

> [!IMPORTANT]
> **Aturan Konsistensi**: Variabel `SUPABASE_URL` dan `SUPABASE_ANON_KEY` pada `mobile/.env` dan `web/.env` wajib 1:1 identik dalam penamaan maupun isinya agar mobile dan web membaca database Supabase yang sama persis. Berkas `.env` otomatis diabaikan oleh `.gitignore` (Zero-Secret Policy).

---

### 3.1 Cara Konfigurasi "Masuk dengan Google" (Google Sign-In OAuth)
Pada layar autentikasi, tombol Google secara cerdas berstatus *"Masuk dengan Google (Perlu Konfigurasi)"* hingga Anda mengaktifkan Google OAuth. Ikuti panduan 5 menit berikut untuk mengaktifkannya:

#### 1. Buat Proyek & Konfigurasi di Google Cloud Console
1. Buka situs [Google Cloud Console](https://console.cloud.google.com/) dan login dengan akun Google Anda.
2. Buat proyek baru bernama **`Mari Partner Fuel`** (atau pilih proyek yang sudah Anda buat di bilah navigasi atas).
3. Di bilah pencarian atas atau menu sidebar kiri, buka **APIs & Services** ➡️ **Credentials**.

##### A. Konfigurasi Layar Persetujuan (OAuth Consent Screen / Google Auth Platform) — *Hanya Sekali di Awal*
> [!NOTE]
> **Tampilan Baru Google Cloud (Google Auth Platform)**:
> Jika Anda diarahkan ke halaman bertuliskan **"Google Auth Platform not configured yet"**, langsung klik tombol biru **[Get started]** di tengah layar.
> 
> Atau jika Anda melihat banner kuning bertuliskan *"Remember to configure OAuth consent screen with information about your application"*, klik tombol **Configure consent screen**.

1. Pada halaman awal atau tombol **Get started**, isi formulir identitas aplikasi:
   - **App name**: `Mari Partner Fuel`
   - **User support email**: Pilih email Google Anda dari menu dropdown.
2. Pada bagian **Audience / User Type**, pilih **External** (agar dapat digunakan oleh seluruh akun Google publik).
3. Pada bagian **Contact Information**:
   - **Email addresses**: Masukkan alamat email Anda.
4. Beri centang persetujuan kebijakan (*User Data Policy*) jika diminta, lalu klik tombol **SAVE AND CONTINUE** atau **FINISH** hingga selesai.
5. Pada langkah **Scopes** (Izin) dan **Test Users**, biarkan default (cukup klik **SAVE AND CONTINUE** terus sampai kembali ke ringkasan/dashboard).

##### B. Buat OAuth Client ID (Tipe Web Application)
1. Kembali ke menu **APIs & Services** ➡️ **Credentials**.
2. Klik tombol **+ CREATE CREDENTIALS** di bagian atas, lalu pilih **OAuth client ID** (opsi nomor 2 di menu dropdown).
3. Pada kolom **Application type**, pilih **Web application** *(⚠️ Wajib pilih 'Web application', bukan Android, karena Supabase Auth bekerja memverifikasi Google ID Token dari Web Client ID)*.
4. Kolom **Name**: Boleh dibiarkan default (misal: `Web client 1`) atau beri nama `Mari Partner Web Client`.
5. Pada bagian **Authorized redirect URIs**, klik **+ ADD URI**, lalu tempel URI callback Supabase proyek Anda:
   ```text
   https://<id-projek-anda>.supabase.co/auth/v1/callback
   ```
   *(Contoh untuk proyek Anda: `https://zcezyhgrblksfuxmszyz.supabase.co/auth/v1/callback`)*.
6. Klik tombol **CREATE** di bagian bawah.
7. Jendela pop-up akan menampilkan **Client ID** (string berakhiran `.apps.googleusercontent.com`) dan **Client Secret**. Salin kedua nilai tersebut.

#### 2. Aktifkan Google Provider di Dashboard Supabase
1. Buka Dashboard Supabase Anda, lalu klik menu **Authentication** di sidebar kiri (ikon dua orang 👥).
2. Di submenu, klik **Providers** ➡️ pilih baris **Google**.
3. Geser tombol toggle menjadi **Enable** (Aktif / Hijau).
4. Masukkan **Client ID** dan **Client Secret** yang Anda peroleh dari Google Cloud Console pada langkah sebelumnya.
5. Klik tombol **Save** di bagian bawah.

#### 3. Masukkan Client ID ke `.env` Proyek Anda
Buka berkas `.env` (atau `local.properties`) di root proyek ini, lalu tempel Client ID Anda:
```properties
GOOGLE_WEB_CLIENT_ID=client_id_anda_dari_google_cloud.apps.googleusercontent.com
```
Simpan berkas (`Ctrl + S`).

#### 4. Selesai!
Jalankan kembali aplikasi (`.\run-app.bat`). Tombol pada layar masuk akan otomatis aktif menjadi **"Masuk dengan Akun Google"** dengan alur login satu ketukan (*one-tap sign-in*) resmi Google Credential Manager!

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

## 🌟 Langkah 6 (Opsional): Pengaturan Kunci Server (Supabase Service Role, Gemini AI & QRISpy)

> 🔒 **ATURAN KEAMANAN TINGKAT TINGGI (Zero-Secret di APK)**:
> Ketiga kunci di bawah ini adalah **kunci rahasia sisi server (*server-side secrets*)**.
> Kunci ini **TIDAK PERNAH** dimasukkan ke dalam kode Android atau APK, melainkan disimpan di **Supabase Edge Functions Secrets** atau backend server.

---

### 1. `SUPABASE_SERVICE_ROLE_KEY` (Superadmin Database Key)
- **Fungsi**: Kunci master superadmin backend Supabase yang memiliki hak akses penuh membypass aturan Row Level Security (RLS). Digunakan oleh Edge Function untuk mengaktifkan status langganan Pro pengguna setelah pembayaran terverifikasi.
- **Cara Mendapatkannya**:
  1. Buka dashboard proyek Supabase Anda.
  2. Di sidebar kiri paling bawah, klik menu **Project Settings (Gerigi ⚙️)** ➡️ pilih **API** (atau **Data API**).
  3. Pada bagian **Project API keys**, cari baris **`service_role` / `secret`**.
  4. Klik tombol **Reveal** (ikon mata), lalu klik tombol **Copy**.

---

### 2. `GEMINI_API_KEY` (AI Cloud Fallback Google)
- **Fungsi**: Digunakan sebagai cadangan pintar (*fallback*) jika foto struk BBM pengguna berkerut, lecek, atau buram, sehingga model AI Google Gemini dapat membantu mengekstrak nama SPBU, volume liter, dan nominal secara akurat.
- **Cara Mendapatkannya (100% Gratis)**:
  1. Buka situs [Google AI Studio](https://aistudio.google.com/).
  2. Login dengan akun Google Anda.
  3. Klik tombol biru **Get API key** di pojok kiri atas.
  4. Klik **Create API key** ➡️ pilih proyek Google Cloud Anda (misalnya `Mari Partner Fuel` yang telah Anda buat).
  5. Salin API Key yang muncul (berawalan `AIzaSy...`).
  6. Pasang ke Supabase Edge Functions Secrets:
     ```bash
     supabase secrets set GEMINI_API_KEY="AIzaSy_key_anda"
     ```

---

### 3. `QRISPY_API_TOKEN` (Payment Gateway QRIS Dinamis)
- **Fungsi**: Digunakan untuk membuat barcode QRIS dinamis otomatis instan saat pengguna ingin berlangganan paket Pro bulanan/tahunan pada versi direct APK/web, membatalkan transaksi, dan menarik mutasi saldo merchant.
- **Cara Mendapatkannya**:
  1. Buka situs resmi penyedia QRIS: [https://qrispy.id](https://qrispy.id).
  2. Daftarkan akun merchant Anda dan login.
  3. Buka menu **Integrasi API** / **API Tokens**.
  4. Klik **Generate Token** / **Buat Token Baru**, lalu salin token API tersebut (berawalan `cki_...`).
  5. Pasang ke Supabase Edge Functions Secrets:
     ```bash
     npx supabase secrets set QRISPY_API_TOKEN="cki_token_anda"
     ```

---

### 4. `QRISPY_WEBHOOK_SECRET` & Konfigurasi Webhook Notifikasi Otomatis
- **Fungsi**: Digunakan oleh Edge Function `payment-webhook` untuk memvalidasi keaslian notifikasi pembayaran masuk dari server QRISpy menggunakan verifikasi tanda tangan kriptografis HMAC-SHA256 (`X-Qrispy-Signature`). Dengan ini, status paket Pro pengguna aktif seketika (1-2 detik) setelah pembayaran berhasil tanpa perlu verifikasi manual.
- **Langkah Setup di Dashboard QRISpy**:
  1. Login ke Dashboard [https://qrispy.id](https://qrispy.id).
  2. Buka menu **API Docs** (atau **Pengaturan** / **Webhooks**).
  3. Pada kolom **Webhook URL**, masukkan endpoint Edge Function resmi Mari Partner Fuel:
     ```text
     https://zcezyhgrblksfuxmszyz.supabase.co/functions/v1/payment-webhook
     ```
  4. Pada baris **Webhook Secret**, klik **Show** lalu salin secret key tersebut.
  5. Simpan secret tersebut ke Supabase Secrets via terminal:
     ```powershell
     cmd /c "npx -y supabase secrets set QRISPY_WEBHOOK_SECRET=whsec_secret_anda"
     ```
  6. Di dashboard QRISpy, klik tombol **Save Settings**.
  7. *(Opsional)* Tekan tombol **Test Webhook** untuk memastikan koneksi antara QRISpy dan Supabase berjalan lancar.

---

### 5. Pembatalan Manual Transaksi QRIS (Sebelum Masa Berlaku 15 Menit)
- Pengguna yang telah men-generate barcode pembayaran QRIS diberikan opsi **"Batalkan Transaksi Pembayaran"** pada layar checkout jika ingin berganti paket atau metode.
- Pembatalan ini memicu konfirmasi pengguna (*Human Gate*) dan memanggil Edge Function `cancel-payment` yang mengeksekusi pembatalan resmi ke gateway QRISpy via `POST /api/payment/qris/{qrisId}/cancel` dengan header `X-API-Token`.
- Status transaksi di basis data otomatis beralih menjadi `'cancelled'` dan pengguna dapat kembali memilih paket baru secara instan.

---

### 6. Integrasi Saldo & Mutasi Gateway QRISpy di Web Owner Console
SaaS Owner Console kini hadir sebagai portal web terisolasi yang diakses via `cd web && npm run dev` (buka `http://localhost:5173/login` dan masuk dengan akun owner):
- **Monitoring Saldo Real-Time**: Pada tab **Ringkasan (Dashboard)** Web Owner Console, tersedia kartu eksekutif **Saldo Aktif QRISpy** yang terhubung langsung ke `GET /api/payment/balance` dengan tombol refresh saldo instan.
- **Dua Jalur Pembukuan & Audit Finansial**:
  - **Audit Transaksi Database (`/admin/transactions`)**: Audit transaksi pembayaran paket SaaS yang tersimpan di database Supabase lengkap dengan rincian user dan masa aktif.
  - **Ledger Mutasi Gateway QRISpy (`/admin/qrispy-ledger`)**: Ledger transaksi real-time langsung dari server gateway QRISpy (`GET /api/payment/transactions?limit=20`) dengan status *PAID, PENDING, EXPIRED,* dan *CANCELLED*.
- **Keamanan Zero-Secret & Role Guard**: Seluruh komunikasi ke endpoint gateway QRISpy diproses oleh Edge Function `get-qrispy-merchant-data` dengan otentikasi role Owner/Admin. Tidak ada secret atau token API gateway yang tersimpan di klien browser maupun di dalam APK Android. Kunci `QRISPY_API_TOKEN` tetap 100% aman tersimpan di Supabase Edge Function Secrets.

---

## 🌐 Langkah 7: Panduan Deploy Platform Web & Owner Console ke Vercel

Panduan ini memandu Anda melakukan deployment platform web **Mari Partner Fuel** (Landing Page publik kelas dunia & SaaS Owner Console) ke **Vercel** secara gratis, cepat, dan otomatis setiap kali ada commit baru di GitHub.

---

### 7.1 Pengaturan Project Name & Root Directory
Saat mengimpor repositori GitHub `hndko/mari-partner-fuel` di dashboard Vercel (*New Project*):

1. **Importing from GitHub**:
   - Pastikan cabang (*branch*) yang dipilih adalah **`main`**.
2. **Project Name**:
   - Secara bawaan terisi `web`. Anda disarankan mengubahnya menjadi nama proyek yang representatif seperti **`mari-partner-fuel`** atau **`maripartner-fuel`**. Nama ini akan menjadi subdomain URL rilis Anda (contoh: `https://mari-partner-fuel.vercel.app`).
3. **Root Directory**:
   - Klik tombol **Edit** di samping isian Root Directory, lalu pilih folder **`web`** (seperti yang tampak pada formulir Vercel Anda).
   - > [!IMPORTANT]
     > Pemilihan Root Directory ke `web` adalah **wajib**, karena proyek ini berstruktur Monorepo (`mobile/` dan `web/`).

---

### 7.2 Konfigurasi Framework & Build Settings
Pada bagian **Build and Output Settings**:

1. **Framework Preset**:
   - Vercel akan otomatis mendeteksi **`Vite`**. Biarkan opsi ini terpilih secara default.
2. **Build Command**:
   - Biarkan tombol toggle dalam posisi **OFF (default)**. Vercel akan otomatis menjalankan:
     ```bash
     npm run build
     ```
     *(Perintah ini mengeksekusi pemeriksaan tipe data TypeScript `vue-tsc --build` dan pembentukan bundel `vite build`)*.
3. **Output Directory**:
   - Biarkan tombol toggle dalam posisi **OFF (default)** (`dist`).
4. **Install Command**:
   - Biarkan tombol toggle dalam posisi **OFF (default)** (`npm install`).

> [!NOTE]
> **Pencegahan Error 404 pada Rute SPA**:
> Repositori ini sudah menyertakan berkas `web/vercel.json` dengan konfigurasi rewrite:
> ```json
> {
>   "rewrites": [
>     { "source": "/(.*)", "destination": "/index.html" }
>   ]
> }
> ```
> Konfigurasi ini menjamin saat pengguna me-refresh halaman `/login` atau mengakses menu `/admin/*`, Vercel tidak akan menampilkan pesan error *404 NOT_FOUND*, melainkan merender aplikasi Single Page Application (SPA) secara sempurna.

---

### 7.3 Pengisian Variabel Lingkungan (.env) Praktis
Pada bagian **Environment Variables**:

> [!TIP]
> **Cara Instan (Menggunakan Tombol Import .env)**:
> Anda tidak perlu mengetik nama dan nilai variabel satu per satu secara manual!
> 1. Klik tombol **[Import .env]** (tombol di bawah kotak input Key/Value).
> 2. Salin (*copy*) teks konfigurasi berikut:
> ```properties
> SUPABASE_URL=https://zcezyhgrblksfuxmszyz.supabase.co
> SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
> VITE_APP_VERSION=2.0.8
> VITE_APP_APK_FILENAME=mari-partner-fuel-v2.0.8.apk
> VITE_APP_APK_URL=/downloads/mari-partner-fuel-v2.0.8.apk
> ```
> *(Catatan: Ganti `SUPABASE_ANON_KEY` dengan kunci anon utuh milik proyek Supabase Anda dari Langkah 2.4)*.
> 3. Tempelkan (*paste*) ke kotak dialog import Vercel, lalu konfirmasi.
> 4. Vercel akan otomatis memasukkan ke-5 variabel tersebut ke lingkungan **Production**, **Preview**, dan **Development**.

---

### 7.4 Melewati Optional Integrations Supabase
Pada bagian bawah formulir Vercel terdapat kartu bertuliskan **"Optional Integrations -> Supabase [Add]"**:

> [!CAUTION]
> **LEWATI (JANGAN KLIK ADD)!**
> Anda **TIDAK PERLU** menekan tombol *[Add]* pada integrasi Supabase Vercel Marketplace.
> 
> **Alasan Teknis**: Database backend Supabase Mari Partner Fuel Anda sudah dibuat mandiri dan berjalan online di region Singapore (`ap-southeast-1`). Menekan tombol integrasi marketplace di Vercel justru akan memicu alur OAuth untuk membuat proyek database baru yang kosong atau merusak relasi skema SQL yang sudah ada. Kredensial `SUPABASE_URL` dan `SUPABASE_ANON_KEY` yang telah Anda masukkan di Langkah 7.3 sudah lebih dari cukup untuk menghubungkan web secara langsung ke backend Supabase Anda.

---

### 7.5 Peluncuran (Deploy) & Pengujian Live URL

1. **Klik Tombol Deploy**:
   - Tekan tombol biru besar **[Deploy]** (atau **[Create Project]**) di bagian bawah layar.
   - Vercel akan menjalankan proses kloning, instalasi dependensi, kompilasi TypeScript, dan pembuatan bundel produksi (~30 detik).
   - Setelah sukses, Anda akan melihat tampilan perayaan rilis lengkap dengan domain publik gratis (misal: `https://mari-partner-fuel.vercel.app`).

2. **Pengujian Fungsional di Browser**:
   - **Halaman Utama / Landing Page (`/`)**:
     - Buka URL publik Vercel Anda.
     - Periksa tampilan Hero Petrol Teal, Bento Grid fitur unggulan, dan tabel perbandingan paket Free vs Pro.
     - Klik tombol **Download APK Android** ➡️ browser akan langsung mengunduh file installer APK versi terbaru (`/downloads/mari-partner-fuel-v2.0.8.apk`) yang tersaji secara statis dari Vercel CDN!
   - **Login Khusus Pemilik SaaS (`/login`)**:
     - Buka `https://<domain-anda>.vercel.app/login`.
     - Lakukan pengujian refresh browser (tekan `F5`). Pastikan halaman tetap termuat rapi dan **tidak terjadi error 404**.
     - Masuk dengan akun Owner Anda (`kyuoko13@gmail.com`).
   - **Monitoring SaaS Owner Console (`/admin/dashboard`)**:
     - Masuk ke dashboard eksekutif untuk melihat total omset, pengguna terdaftar, dan kartu saldo live QRISpy.
     - Buka menu **Plans** (`/admin/plans`) untuk mengatur harga promo paket langganan secara real-time.

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
