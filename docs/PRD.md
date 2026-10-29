# PRD Final --- Mari Partner Fuel

**AI-Powered Fuel Expense Tracker for Android**\
**Versi:** 1.0 Final\
**Tanggal:** 29 Oktober 2026\
**Pemilik Produk:** Mari Partner\
**Status:** Baseline untuk implementasi MVP

------------------------------------------------------------------------

## 1. Ringkasan Produk

Mari Partner Fuel adalah aplikasi Android Native untuk mencatat,
menyimpan, dan menganalisis pengeluaran bahan bakar kendaraan. Pengguna
mengambil foto struk atau memilih gambar dari galeri. Sistem membaca
teks dengan OCR lokal, menggunakan AI cloud sebagai fallback jika hasil
lokal kurang lengkap atau ambigu, menampilkan hasil untuk diverifikasi,
lalu menyimpan transaksi ke database online.

Tujuan utama: - Mengurangi kebutuhan mengetik transaksi BBM secara
manual. - Menyediakan riwayat pengeluaran yang rapi dan dapat dicari. -
Menampilkan analitik pengeluaran dan konsumsi BBM. - Mendukung beberapa
kendaraan dalam satu akun. - Menyediakan model bisnis Free dan langganan
Pro. - Memulai tanpa mengelola VPS sendiri dan menekan biaya operasional
dengan free tier yang tersedia.

**Catatan biaya:** Free tier memiliki kuota, batas penggunaan, dan
ketentuan yang dapat berubah. Tidak ada jaminan semua biaya produksi
akan selalu Rp0. Pantau kuota database, Storage, fungsi cloud, email,
AI, dan biaya gateway.

## 2. Keputusan Final yang Dikunci

  -----------------------------------------------------------------------
  Area                                Keputusan
  ----------------------------------- -----------------------------------
  Nama aplikasi                       Mari Partner Fuel

  Platform                            Android Native

  Bahasa                              Kotlin

  UI                                  Jetpack Compose + Material 3

  Arsitektur                          MVVM, Repository, Use Case untuk
                                      logika domain

  Minimum Android                     API 26 (Android 8.0)

  Backend                             Supabase

  Database                            Supabase PostgreSQL

  Authentication                      Email/password + Google Sign-In

  File struk                          Supabase Storage private bucket

  OCR lokal                           Google ML Kit Text Recognition

  AI cloud                            Provider adapter; Gemini API dapat
                                      menjadi provider awal setelah
                                      evaluasi kuota dan kebijakan data

  Strategi OCR                        Hybrid: OCR lokal dahulu, AI cloud
                                      fallback

  Monetisasi                          Free + langganan Pro
                                      bulanan/tahunan

  Payment gateway default             QRISpy

  Gateway alternatif                  Midtrans dan Xendit melalui adapter

  VPS                                 Tidak diperlukan untuk MVP; gunakan
                                      layanan terkelola

  Bahasa antarmuka awal               Bahasa Indonesia

  Tema                                Terang dan gelap
  -----------------------------------------------------------------------

Versi Android Gradle Plugin, Kotlin, Compose BOM, Supabase SDK, dan
dependency lain harus dipilih setelah pemeriksaan kompatibilitas API 26
serta direkam dalam version catalog/Gradle lock strategy. Jangan menebak
kompatibilitas dependency tanpa build test.

## 3. Target Pengguna dan Masalah

### Target

-   Pengguna motor dan mobil pribadi.
-   Pengemudi lapangan dan pekerja dengan mobilitas tinggi.
-   Pengemudi transportasi online.
-   UMKM dengan kendaraan operasional.
-   Pengguna yang ingin mengetahui biaya BBM mingguan atau bulanan.

### Masalah yang diselesaikan

-   Pengguna lupa mencatat setelah mengisi BBM.
-   Struk fisik hilang atau memudar.
-   Catatan tidak mudah diubah menjadi laporan.
-   Pengeluaran sulit dibandingkan antarbulan atau antarkendaraan.

## 4. Tujuan dan Bukan Tujuan MVP

### Tujuan MVP

1.  Pengguna dapat membuat akun dan login.
2.  Pengguna dapat mengelola beberapa kendaraan.
3.  Pengguna dapat scan struk dari kamera atau galeri.
4.  OCR lokal mengekstrak teks; AI cloud dipanggil hanya jika diperlukan
    dan diizinkan.
5.  Pengguna dapat memeriksa dan mengedit hasil sebelum menyimpan.
6.  Data tersimpan di cloud dan hanya dapat diakses pemiliknya.
7.  Dashboard, riwayat, dan analitik dasar berfungsi.
8.  Langganan Pro dapat dibeli dan diverifikasi secara aman melalui
    QRISpy.
9.  Struktur integrasi memungkinkan Midtrans/Xendit diaktifkan kemudian.

### Bukan tujuan MVP

-   Navigasi turn-by-turn ke SPBU.
-   Klaim harga BBM real-time tanpa sumber tepercaya.
-   Rating SPBU publik dan sistem moderasi lengkap.
-   Manajemen armada perusahaan yang kompleks.
-   Prediksi sisa bensin yang presisi tanpa data cukup.
-   Penggunaan AI cloud tanpa batas.
-   Backend VPS khusus.

## 5. Alur Utama Pengguna

1.  Pengguna mendaftar dengan email/password atau Google Sign-In.
2.  Pengguna menyelesaikan profil dan menambahkan kendaraan.
3.  Pengguna menekan Scan Struk.
4.  Pengguna mengambil foto atau memilih gambar.
5.  Aplikasi memvalidasi ukuran/format gambar dan menjalankan OCR lokal.
6.  Aplikasi mengevaluasi apakah field penting sudah cukup lengkap dan
    konsisten.
7.  Jika hasil tidak lengkap/ambigu dan kuota tersedia, aplikasi meminta
    Edge Function memproses gambar dengan provider AI.
8.  Aplikasi menampilkan hasil dan field yang perlu diperiksa.
9.  Pengguna mengoreksi data dan menekan Konfirmasi.
10. Backend memvalidasi identitas, kepemilikan kendaraan, dan payload;
    file serta transaksi disimpan.
11. Dashboard dan riwayat diperbarui.

Jangan menyimpan transaksi hasil scan secara otomatis sebelum pengguna
mengonfirmasi. Jika AI tidak tersedia atau kuota habis, tetap izinkan
input manual atau koreksi hasil OCR lokal.

## 6. Spesifikasi Fitur

### 6.1 Autentikasi dan Profil

-   Email/password, verifikasi email sesuai konfigurasi Supabase.
-   Google Sign-In melalui integrasi resmi yang kompatibel dengan
    Supabase Auth.
-   Login, logout, reset password, pembaruan profil, dan pengelolaan
    sesi.
-   Jangan menyimpan password sendiri atau mengimplementasikan
    autentikasi buatan.
-   Gunakan konfigurasi OAuth yang benar untuk debug/release dan SHA
    fingerprints yang diperlukan.
-   Penghapusan akun harus mempunyai alur yang jelas untuk data database
    dan file Storage.

### 6.2 Kendaraan

Field: - ID UUID. - User ID pemilik. - Nama kendaraan. - Jenis: motor,
mobil, atau lainnya. - Merek/model opsional. - Nomor polisi opsional dan
diperlakukan sebagai data pribadi. - Jenis BBM default. - Kapasitas
tangki opsional. - Odometer terakhir opsional. - Status
aktif/diarsipkan. - Created/updated timestamp.

Fitur: tambah, ubah, lihat, arsipkan, pilih kendaraan default. Kendaraan
yang diarsipkan tetap mempertahankan transaksi historisnya.

### 6.3 Scan Struk Hybrid

Field hasil ekstraksi: - Tanggal/waktu transaksi. - Nama SPBU, jika
terbaca. - Produk/jenis BBM. - Volume liter. - Harga per liter. - Total
pembayaran. - Odometer jika tercetak atau dimasukkan pengguna. - Nomor
transaksi jika tersedia. - Confidence atau indikator kualitas per
field. - Catatan sumber nilai: OCR, AI, atau koreksi pengguna.

Persyaratan: - Mendukung kamera dan galeri. - Periksa format, ukuran,
orientasi, dan kualitas gambar. - Kompres gambar sebelum upload bila
sesuai tanpa merusak keterbacaan. - OCR lokal berjalan terlebih
dahulu. - Fallback AI cloud hanya melalui backend dan setelah
autentikasi, pemeriksaan kuota, serta kebijakan privasi. - Gunakan JSON
schema/kontrak respons yang ketat. - Validasi tanggal, volume, total,
dan harga. - Jika total dan volume tersedia, hitung perkiraan
harga/liter untuk membandingkan, bukan menimpa data sumber secara
diam-diam. - Deteksi duplikat dengan fingerprint file dan/atau kombinasi
tanggal, total, volume, kendaraan. - Tampilkan hasil dengan field ambigu
ditandai. - Pengguna dapat memperbaiki setiap field sebelum
konfirmasi. - Simpan audit nilai hasil scan dan nilai final yang
dikonfirmasi bila relevan. - Jangan mengklaim akurasi 100%; uji
menggunakan kumpulan struk representatif dengan izin yang sesuai.

### 6.4 Transaksi BBM

-   Input manual tersedia.
-   Detail transaksi menyertakan kendaraan, tanggal/waktu, volume,
    total, jenis BBM, SPBU, odometer, catatan, dan file struk.
-   Edit, hapus, pencarian, filter, dan pagination.
-   Validasi kepemilikan kendaraan di backend/database.
-   Nominal disimpan sebagai integer rupiah; volume menggunakan numeric
    presisi.
-   Jangan menghapus file atau transaksi milik pengguna lain.

### 6.5 Dashboard dan Analitik

Tampilkan: - Total biaya BBM periode terpilih. - Total liter. - Jumlah
transaksi. - Rata-rata tertimbang harga per liter. - Grafik biaya
harian/mingguan/bulanan. - Perbandingan antarbulan. - Ringkasan per
kendaraan. - Ringkasan per jenis BBM. - Transaksi terbaru.

Rumus rata-rata tertimbang:
`total seluruh biaya BBM / total seluruh liter`.

Konsumsi km/liter hanya dihitung jika terdapat sekurangnya dua pengisian
dengan odometer valid dan berurutan. Tandai anomali, odometer mundur,
pengisian parsial, dan data yang tidak cukup. Jangan menyatakan sisa BBM
aktual tanpa data yang memadai.

### 6.6 Riwayat dan Ekspor

-   Timeline transaksi.
-   Filter tanggal, kendaraan, jenis BBM, dan pencarian SPBU.
-   Detail struk melalui signed URL berumur pendek.
-   Ekspor CSV pada MVP.
-   PDF dapat ditambahkan sebagai fitur Pro/tahap lanjutan.
-   Query menggunakan pagination dan indeks yang sesuai.

### 6.7 Reminder

-   Pengingat lokal untuk isi BBM atau servis.
-   Reminder berdasarkan tanggal atau odometer.
-   Pengguna dapat mengubah, menunda, menyelesaikan, atau menghapus
    reminder.
-   Hormati izin notifikasi Android dan preferensi waktu pengguna.
-   Reminder bukan klaim bahwa tangki pasti kosong atau kendaraan pasti
    perlu servis.

### 6.8 Free dan Pro

Paket dan kuota harus dikonfigurasi di backend, tidak ditanam sebagai
satu-satunya sumber kebenaran di aplikasi.

Usulan awal untuk pengujian pasar (belum final): - Bulanan: Rp15.000. -
Tahunan: Rp120.000. - Trial 7 hari opsional.

Free: - Maksimal 2 kendaraan sebagai nilai konfigurasi awal. - Input
manual dan OCR lokal. - Riwayat transaksi dan dashboard dasar. - Kuota
AI cloud terbatas. - Fitur dasar tetap bisa dipakai setelah Pro
berakhir.

Pro: - Kuota AI lebih besar. - Analitik dan filter lanjutan. - Ekspor
lengkap. - Lebih banyak kendaraan. - Reminder lanjutan. - Detail benefit
dan kuota harus bisa diubah dari backend.

Jangan mengunci atau menghapus data pengguna ketika Pro berakhir. Jangan
menyatakan "AI tanpa batas" kecuali memang disediakan dan didanai.

## 7. Arsitektur Teknis

### Android

-   Kotlin, Jetpack Compose, Material 3.
-   MVVM dan state UI eksplisit.
-   Repository untuk abstraksi sumber data.
-   Coroutines/Flow untuk operasi asinkron.
-   Dependency injection menggunakan library yang kompatibel dengan
    versi proyek.
-   Navigation Compose.
-   ML Kit Text Recognition.
-   Kamera menggunakan solusi Android yang kompatibel dengan API 26.
-   Unit test dan UI test.
-   Gunakan secure handling untuk session/token; jangan menyimpan secret
    merchant.

### Backend

-   Supabase Auth.
-   PostgreSQL.
-   Supabase Storage private bucket.
-   Row Level Security.
-   Supabase Edge Functions untuk AI proxy, order pembayaran,
    pemeriksaan status, webhook jika didukung dan sesuai spesifikasi
    gateway, serta administrasi subscription.
-   Secrets disimpan di environment/secret manager fungsi.
-   Semua operasi sensitif diverifikasi di server.

### AI Provider Adapter

Definisikan kontrak internal, misalnya `ReceiptAiProvider`, yang
menerima input minimal dan mengembalikan data terstruktur tervalidasi.
Provider awal dapat memakai Gemini API setelah model, kuota, retensi,
dan kebijakan data diperiksa. Provider dapat diganti tanpa perubahan
layar atau skema transaksi.

Jangan mengirim data pribadi yang tidak diperlukan. Dokumentasikan bahwa
layanan AI eksternal dapat memproses foto struk. Jika kuota habis atau
layanan gagal, fallback ke OCR lokal dan koreksi manual.

### Payment Gateway Adapter

Kontrak umum: - `createPayment` - `getPaymentStatus` - `cancelPayment`
bila didukung - `verifyWebhook` - `normalizeProviderStatus`

Implementasi: 1. QRISpy default. 2. Midtrans alternatif. 3. Xendit
alternatif.

Semua token hanya berada di backend. Status subscription tidak boleh
ditentukan oleh hasil redirect/return URL dari aplikasi.

## 8. Database dan Relasi

Gunakan UUID, foreign key, timestamp dengan timezone, constraint, index,
migration yang dapat diulang di lingkungan baru, dan RLS.

### Tabel

1.  `profiles`: profil dan preferensi pengguna.
2.  `vehicles`: kendaraan pemilik.
3.  `fuel_transactions`: transaksi BBM.
4.  `receipt_files`: metadata file struk.
5.  `receipt_scan_jobs`: status OCR/AI dan error yang aman untuk
    dicatat.
6.  `maintenance_reminders`: pengingat servis/oli.
7.  `notification_preferences`: preferensi notifikasi.
8.  `subscription_plans`: konfigurasi paket, harga, kuota, dan benefit.
9.  `subscriptions`: status hak akses pengguna.
10. `payment_transactions`: order dan status pembayaran gateway.
11. `payment_webhook_events`: event untuk idempotensi/audit.
12. `ai_usage_logs`: hitungan pemakaian dan estimasi penggunaan.
13. `user_feedback`: masukan pengguna jika termasuk MVP.

### Skema inti contoh

``` sql
create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text,
  timezone text not null default 'Asia/Jakarta',
  theme_preference text not null default 'system'
    check (theme_preference in ('light','dark','system')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.vehicles (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  vehicle_type text not null
    check (vehicle_type in ('motorcycle','car','other')),
  make text,
  model text,
  license_plate text,
  default_fuel_type text,
  tank_capacity_liters numeric(8,2)
    check (tank_capacity_liters is null or tank_capacity_liters > 0),
  last_odometer_km numeric(12,1)
    check (last_odometer_km is null or last_odometer_km >= 0),
  is_archived boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (id, user_id)
);

create table public.receipt_files (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  storage_path text not null unique,
  mime_type text not null,
  size_bytes bigint not null check (size_bytes > 0),
  sha256 text,
  created_at timestamptz not null default now(),
  unique (id, user_id)
);

create table public.fuel_transactions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  vehicle_id uuid not null,
  receipt_file_id uuid,
  transaction_at timestamptz not null,
  station_name text,
  fuel_type text,
  volume_liters numeric(10,3) not null check (volume_liters > 0),
  total_amount_idr bigint not null check (total_amount_idr >= 0),
  price_per_liter_idr numeric(12,2)
    check (price_per_liter_idr is null or price_per_liter_idr >= 0),
  odometer_km numeric(12,1)
    check (odometer_km is null or odometer_km >= 0),
  scan_status text not null default 'manual'
    check (scan_status in ('manual','ocr_review','ai_review','confirmed')),
  notes text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  foreign key (vehicle_id, user_id)
    references public.vehicles(id, user_id),
  foreign key (receipt_file_id, user_id)
    references public.receipt_files(id, user_id)
);

create index idx_vehicles_user_active
  on public.vehicles(user_id, is_archived);

create index idx_fuel_transactions_user_date
  on public.fuel_transactions(user_id, transaction_at desc);

create index idx_fuel_transactions_vehicle_date
  on public.fuel_transactions(vehicle_id, transaction_at desc);

create table public.subscription_plans (
  id uuid primary key default gen_random_uuid(),
  code text not null unique,
  name text not null,
  billing_period text not null
    check (billing_period in ('monthly','yearly')),
  price_idr bigint not null check (price_idr >= 0),
  ai_scan_quota integer not null check (ai_scan_quota >= 0),
  max_vehicles integer,
  is_active boolean not null default true,
  created_at timestamptz not null default now()
);

create table public.subscriptions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  plan_id uuid not null references public.subscription_plans(id),
  status text not null
    check (status in ('pending','active','expired','cancelled','past_due')),
  starts_at timestamptz,
  expires_at timestamptz,
  provider text,
  provider_subscription_ref text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.payment_transactions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id),
  subscription_id uuid references public.subscriptions(id),
  provider text not null
    check (provider in ('qrispy','midtrans','xendit')),
  payment_reference text not null unique,
  provider_payment_id text,
  amount_idr bigint not null check (amount_idr >= 0),
  currency text not null default 'IDR' check (currency = 'IDR'),
  status text not null
    check (status in ('created','pending','paid','expired','cancelled','failed','refunded')),
  checkout_url text,
  qris_image_url text,
  expires_at timestamptz,
  paid_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (provider, provider_payment_id)
);
```

Ini adalah skema awal, bukan migration produksi lengkap. Tambahkan
migration untuk scan jobs, usage logs, webhook events, reminders,
preferences, feedback, triggers `updated_at`, constraints tambahan, dan
kebijakan RLS.

### Aturan database penting

-   Aktifkan RLS pada semua tabel yang dapat diakses client.
-   Terapkan kebijakan SELECT/INSERT/UPDATE/DELETE berdasarkan
    `auth.uid()`.
-   Validasi relasi kepemilikan dengan composite foreign key atau
    validasi backend yang setara.
-   Client tidak boleh mengubah `subscriptions.status`, payment status,
    AI quota, atau role administratif.
-   Untuk tabel sensitif, batasi penulisan melalui Edge Functions atau
    RPC dengan hak minimal.
-   Jangan pernah mengekspos service-role key ke Android.
-   Private Storage memakai path yang mengandung UUID pemilik dan
    kebijakan Storage yang sesuai.
-   Signed URL harus berumur pendek.
-   Gunakan unique constraints/idempotency untuk payment reference dan
    event gateway.
-   Terapkan retensi dan penghapusan data sesuai kebijakan privasi.

## 9. Aturan Integrasi QRISpy, Midtrans, dan Xendit

### QRISpy sebagai default

Base URL: `https://api.qrispy.id`

Endpoint sesuai dokumentasi provider: -
`POST /api/payment/qris/generate` -
`GET /api/payment/qris/{qrisId}/status` -
`POST /api/payment/qris/{qrisId}/cancel` -
`GET /api/payment/transactions` - `GET /api/payment/balance`

Gunakan header `X-API-Token` dari secret backend. Contoh request
generate berisi `amount`, `payment_reference`, dan `return_url` jika
diperlukan.

Alur: 1. Backend memvalidasi user, plan, harga aktif, dan membuat order
unik. 2. Backend memanggil QRISpy. 3. Simpan provider ID, nominal
aktual, status pending, dan waktu kedaluwarsa. 4. Client menampilkan
QRIS dari respons backend. 5. Backend memverifikasi status melalui
webhook resmi jika tersedia dan/atau endpoint status provider. 6.
Backend mengaktifkan subscription hanya setelah status pembayaran sah
dan nominal/reference cocok. 7. Jika pending/expired, jangan aktifkan
Pro. Izinkan pembuatan pembayaran baru sesuai aturan.

Masa berlaku QRIS harus diambil dari respons provider, bukan ditanam
permanen. Dokumentasi contoh menunjukkan 15 menit, tetapi implementasi
wajib menggunakan nilai aktual yang dikembalikan.

### Pembayaran aman

-   Jangan mengandalkan return URL sebagai bukti pembayaran.
-   Verifikasi signature webhook sesuai dokumentasi resmi provider
    sebelum memproses event.
-   Terapkan idempotensi dan unique event key.
-   Cocokkan referensi, provider payment ID, nominal, mata uang, dan
    status.
-   Simpan event mentah seminimal mungkin; redaksi token dan data
    sensitif.
-   Sediakan rekonsiliasi status jika webhook terlambat.
-   Jangan menganggap subscription digital eksternal otomatis sesuai
    kebijakan Google Play. Periksa kebijakan pembayaran Play yang
    berlaku untuk kategori dan wilayah distribusi sebelum rilis.
-   Midtrans/Xendit baru dianggap aktif setelah kredensial, webhook,
    sandbox test, dan verifikasi produksi selesai.

## 10. Keamanan dan Privasi

-   Tidak ada secret di source code atau APK.
-   Semua komunikasi memakai HTTPS.
-   Validasi input di client dan server.
-   Rate limit AI, scan, login sensitif, dan payment endpoints.
-   Batas ukuran dan MIME type upload; verifikasi file sesungguhnya bila
    memungkinkan.
-   RLS diuji dengan dua akun terpisah.
-   Jangan mengirim nomor polisi atau profil ke AI jika tidak
    dibutuhkan.
-   Jelaskan pemrosesan foto oleh provider AI eksternal dalam kebijakan
    privasi.
-   Sediakan penghapusan akun, data database, dan file Storage.
-   Catat audit event pembayaran dan perubahan subscription.
-   Hindari log token, URL bertanda tangan, dan data pribadi lengkap.

## 11. UI/UX dan Navigasi

Bahasa awal: Indonesia. Tema terang/gelap.

Navigasi utama: - Beranda - Riwayat - Analitik - Kendaraan - Profil

Layar MVP: 1. Splash dan onboarding. 2. Login/register/reset password.
3. Dashboard. 4. Kamera/galeri scan. 5. Proses OCR/AI. 6. Review hasil
scan. 7. Form transaksi manual. 8. Daftar/detail transaksi. 9.
Kendaraan. 10. Analitik dan filter. 11. Reminder. 12. Paket Pro. 13.
Checkout/QRIS dan status pembayaran. 14. Profil/pengaturan/privasi.

Setiap operasi jaringan harus mempunyai loading, empty, error, retry,
dan success state yang tepat. Jangan menampilkan tombol inert atau data
demo sebagai data nyata.

## 12. Struktur Repository yang Disarankan

``` text
mari-partner-fuel/
├── app/
├── core/
│   ├── common/
│   ├── designsystem/
│   ├── network/
│   └── security/
├── feature/
│   ├── auth/
│   ├── dashboard/
│   ├── receipt_scan/
│   ├── transactions/
│   ├── vehicles/
│   ├── analytics/
│   ├── reminders/
│   ├── subscription/
│   └── settings/
├── data/
│   ├── repository/
│   ├── remote/
│   └── local/
├── domain/
│   ├── model/
│   └── usecase/
├── supabase/
│   ├── migrations/
│   ├── functions/
│   └── seed.sql
├── docs/
│   ├── PRD.md
│   ├── ARCHITECTURE.md
│   ├── DATABASE.md
│   ├── SECURITY.md
│   └── PAYMENT_FLOWS.md
├── gradle/
├── AGENTS.md
└── README.md
```

Struktur boleh disederhanakan bila proyek masih kecil, tetapi pemisahan
UI, domain, repository, provider, dan keamanan harus dipertahankan.
Jangan membuat modul kosong hanya demi struktur.

## 13. Acceptance Criteria

  -------------------------------------------------------------------------
  ID                      Kriteria penerimaan       Bukti uji
  ----------------------- ------------------------- -----------------------
  AC-01                   Email/password register   Test sukses, invalid
                          dan login berfungsi       credentials, reset
                                                    password

  AC-02                   Google Sign-In berfungsi  Test OAuth dengan
                          pada konfigurasi          konfigurasi nyata
                          debug/release             

  AC-03                   User dapat membuat,       CRUD dan validasi
                          mengubah, mengarsipkan    
                          kendaraan                 

  AC-04                   Foto dapat dipilih dari   Test izin dan file
                          kamera/galeri             invalid

  AC-05                   OCR lokal menghasilkan    Test set sampel struk
                          teks untuk struk yang     
                          terbaca                   

  AC-06                   AI fallback hanya terjadi Test provider mock dan
                          jika dibutuhkan,          integrasi
                          autentikasi sah, dan      
                          kuota tersedia            

  AC-07                   Pengguna                  UI test
                          meninjau/mengoreksi hasil 
                          sebelum simpan            

  AC-08                   Total, volume, tanggal,   Unit test edge cases
                          dan harga divalidasi      

  AC-09                   Transaksi manual dapat    Integration test
                          dibuat tanpa AI           

  AC-10                   Data transaksi tersimpan  Integration test
                          online dan tampil di      
                          perangkat setelah login   

  AC-11                   Dua akun tidak dapat      RLS security test
                          membaca/mengubah data     
                          satu sama lain            

  AC-12                   Foto struk privat dan     Storage policy test
                          signed URL memiliki masa  
                          berlaku                   

  AC-13                   Dashboard memakai total   Unit/integration test
                          data yang sesuai          
                          filter/periode            

  AC-14                   Rata-rata harga per liter Unit test
                          adalah rata-rata          
                          tertimbang                

  AC-15                   Ekspor CSV mengikuti      Integration test
                          filter dan tidak mencakup 
                          akun lain                 

  AC-16                   Kuota AI habis tidak      Test
                          menghilangkan input       
                          manual/OCR lokal          

  AC-17                   QRISpy order dibuat dari  Integration test
                          harga plan server         sandbox

  AC-18                   Return URL tanpa          Security test
                          verifikasi tidak          
                          mengaktifkan Pro          

  AC-19                   Webhook berulang tidak    Idempotency test
                          menggandakan              
                          subscription/aktivasi     

  AC-20                   Pending/expired/failed    Payment state test
                          payment tidak             
                          mengaktifkan Pro          

  AC-21                   Harga/nominal/reference   Negative test
                          diverifikasi sebelum      
                          aktivasi                  

  AC-22                   Pro berakhir tetapi       Entitlement test
                          riwayat pengguna tetap    
                          dapat dibaca              

  AC-23                   App build dan berjalan    Emulator/device test
                          pada API 26 minimum       

  AC-24                   Loading, empty, error,    UI test
                          retry, dan offline states 
                          tersedia                  

  AC-25                   Tidak ada secret          Secret scan
                          merchant/API key di       
                          APK/repository            

  AC-26                   Penghapusan akun          Integration test
                          menangani data database   
                          dan file terkait          

  AC-27                   Build release berhasil    Release build test
                          dengan konfigurasi        
                          production terpisah       
  -------------------------------------------------------------------------

MVP tidak dianggap selesai hanya karena build berhasil. Acceptance
criteria keamanan, pembayaran, dan pemisahan data wajib lulus sebelum
produksi.

## 14. Strategi Pengujian

-   Unit tests: validasi OCR, perhitungan, parsing nominal rupiah,
    entitlement.
-   Repository tests: hasil sukses, error jaringan, timeout, retry.
-   Database tests: RLS dan relasi kepemilikan.
-   OCR tests: variasi format struk, blur, miring, pudar, cahaya rendah.
-   Payment tests: pending, paid, expired, cancelled, duplicate webhook,
    nominal mismatch.
-   UI tests: onboarding, scan-review-confirm, edit transaksi, filter,
    upgrade.
-   Compatibility tests: API 26 dan beberapa API lebih baru.
-   Security tests: secret scan, akses lintas akun, manipulasi request,
    akses file privat.
-   Release checks: privacy policy, store listing, izin Android, dan
    kebijakan pembayaran Google Play.

## 15. Tahapan Implementasi

1.  Audit repository, setup Gradle, dan kompatibilitas API 26.
2.  Design system, navigation, autentikasi email/Google.
3.  Supabase schema, migrations, RLS, dan Storage privat.
4.  Kendaraan dan input transaksi manual.
5.  OCR lokal, review screen, dan AI fallback.
6.  Riwayat, dashboard, analitik, CSV, reminder.
7.  Subscription plans dan entitlement server-side.
8.  QRISpy sandbox, webhook/status verification, idempotency.
9.  Adapter Midtrans/Xendit dan pengujian terpisah bila kredensial
    tersedia.
10. QA, security review, dokumentasi deployment, dan beta release.

Setiap tahap harus berakhir dengan build/test yang relevan dan catatan
pekerjaan yang belum selesai.

## 16. Risiko dan Mitigasi

-   **Free tier berubah/habis:** kuota dan pemakaian dimonitor; fallback
    manual; buat batas biaya.
-   **AI salah membaca:** review wajib, confidence, validasi matematis,
    koreksi manual.
-   **AI provider tidak tersedia:** OCR lokal tetap berfungsi.
-   **API 26 tidak didukung dependency:** lakukan compatibility spike
    awal dan pin versi yang lulus build.
-   **Pembayaran palsu/replay:** verifikasi server, signature,
    idempotency, rekonsiliasi.
-   **Kebocoran struk:** private Storage, RLS, signed URL singkat,
    minimisasi data.
-   **Kebijakan Play Store:** tinjau aturan pembayaran digital sebelum
    rilis.
-   **Skala meningkat:** pantau kuota, indeks, ukuran gambar, dan biaya;
    upgrade layanan atau pisahkan backend saat diperlukan.

## 17. Definisi Selesai

MVP selesai ketika: - Seluruh acceptance criteria wajib lulus. - Build
release berhasil dan diuji pada API 26. - Migrasi database dapat
diterapkan ulang dari repository. - RLS dan Storage security telah diuji
dengan akun berbeda. - AI fallback dan manual fallback berfungsi. -
Pembayaran sandbox diverifikasi end-to-end tanpa aktivasi palsu. - Tidak
ada secret di APK/repository. - README, setup, environment variables,
deployment, privacy, dan troubleshooting terdokumentasi. - Daftar fitur
belum selesai dinyatakan secara jujur.
