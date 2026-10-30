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
3. [⚙️ Langkah 3: Konfigurasi Kunci di Proyek Android (.env / local.properties)](#️-langkah-3-konfigurasi-kunci-di-proyek-android-env--localproperties)
   - [3.1 Cara Konfigurasi "Masuk dengan Google" (Google Sign-In OAuth)](#31-cara-konfigurasi-masuk-dengan-google-google-sign-in-oauth)
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

> [!NOTE]
> Pastikan setiap query menampilkan pesan sukses `Success. No rows returned` sebelum berpindah ke query berikutnya.

---

#### ⚡ METODE 2 (Otomatis Sekali Jalan): Lewat Supabase CLI di Terminal
Jika Anda ingin mengeksekusi ke-8 file migrasi sekaligus secara otomatis dari terminal laptop:

1. Buka terminal PowerShell di folder proyek ini.
2. Login ke akun Supabase Anda:
   ```powershell
   supabase login
   ```
   *(Browser akan terbuka otomatis untuk mengonfirmasi login dan membuat access token)*.
3. Hubungkan proyek lokal dengan proyek Supabase remote Anda:
   ```powershell
   supabase link --project-ref zcezyhgrblksfuxmszyz
   ```
   *(Masukkan Database Password yang Anda simpan saat membuat proyek)*.
4. Dorong (*push*) seluruh 8 migrasi SQL yang sudah ada di folder `supabase/migrations/` sekaligus:
   ```powershell
   supabase db push
   ```
   *(Jangan jalankan perintah `supabase migration new` karena seluruh 8 berkas SQL migrasi sudah tersedia lengkap di repositori ini)*.

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

## ⚙️ Langkah 3: Konfigurasi Kunci di Proyek Android (.env / local.properties)

Proyek ini telah dikonfigurasi untuk **secara otomatis membaca berkas `.env`** di root repositori (atau `local.properties`):

1. Buka berkas `.env` (atau duplikasi dari `.env.example`).
2. Masukkan URL dan Anon Key Supabase yang Anda peroleh pada Langkah 2.4:

```properties
## Konfigurasi Supabase Mari Partner Fuel
SUPABASE_URL=https://zcezyhgrblksfuxmszyz.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

## Opsional: Google OAuth Web Client ID (Jika ingin mengaktifkan tombol Masuk dengan Google)
GOOGLE_WEB_CLIENT_ID=
```

3. Simpan berkas `.env`.

> 🔒 **Aman**: Berkas `.env` dan `local.properties` otomatis terdaftar di `.gitignore` dan tidak akan pernah ter-upload ke publik.

---

### 3.1 Cara Konfigurasi "Masuk dengan Google" (Google Sign-In OAuth)
Pada layar autentikasi, tombol Google secara cerdas berstatus *"Masuk dengan Google (Perlu Konfigurasi)"* hingga Anda mengaktifkan Google OAuth. Ikuti panduan 5 menit berikut untuk mengaktifkannya:

#### 1. Buat Proyek & Konfigurasi di Google Cloud Console
1. Buka situs [Google Cloud Console](https://console.cloud.google.com/) dan login dengan akun Google Anda.
2. Buat proyek baru bernama **`Mari Partner Fuel`** (atau pilih proyek yang sudah Anda buat di bilah navigasi atas).
3. Di bilah pencarian atas atau menu sidebar kiri, buka **APIs & Services** ➡️ **Credentials**.

##### A. Konfigurasi Layar Persetujuan (OAuth Consent Screen) — *Hanya Sekali di Awal*
> [!NOTE]
> Jika Anda melihat banner kuning bertuliskan *"Remember to configure OAuth consent screen with information about your application"*, Google Cloud mewajibkan pengaturan consent screen terlebih dahulu sebelum membuat Client ID.

1. Klik tombol **Configure consent screen** pada banner kuning (atau klik menu **OAuth consent screen** di sidebar kiri).
2. Pada pilihan **User Type**, pilih **External** (agar dapat digunakan oleh akun Google publik), lalu klik **CREATE**.
3. Isi data dasar aplikasi:
   - **App name**: `Mari Partner Fuel`
   - **User support email**: Pilih email Google Anda dari menu dropdown.
   - **Developer contact information**: Masukkan alamat email Anda.
4. Klik tombol **SAVE AND CONTINUE** di bagian bawah.
5. Pada langkah **Scopes** (Izin) dan **Test Users**, biarkan default (langsung klik **SAVE AND CONTINUE** hingga kembali ke ringkasan/dashboard).

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
