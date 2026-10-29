# SECURITY.md --- Mari Partner Fuel

**Kebijakan Keamanan, Manajemen Secret, Proteksi Data, Autentikasi & Model Ancaman**\
**Aplikasi:** Mari Partner Fuel

---

## 1. Prinsip Utama Keamanan

1. **Zero Secret Exposure:** Tidak ada kredensial backend, Supabase service-role key, ataupun API secret payment gateway (QRISpy / Midtrans / Xendit) di dalam source code, Git history, file assets, maupun APK Android.
2. **Prinsip Hak Akses Terkecil (*Least Privilege*):** Aplikasi Android hanya memegang `SUPABASE_ANON_KEY` dengan hak yang dibatasi secara ketat oleh Row Level Security (RLS).
3. **Validasi Dua Sisi (*Client & Server Validation*):** Setiap input divalidasi di Android untuk pengalaman pengguna (*fail fast*) dan divalidasi ulang di backend/PostgreSQL sebelum penyimpanan.
4. **Data Minimization untuk AI:** Ketika mengirim gambar struk ke backend Edge Function untuk pemrosesan AI, nomor polisi, profil pribadi, dan identitas pengguna tidak pernah diteruskan ke provider AI pihak ketiga.

---

## 2. Panduan Setup Supabase & Autentikasi

### 2.1 Pembuatan Proyek Supabase
1. Buat proyek baru di [Supabase Dashboard](https://supabase.com).
2. Salin **Project URL** dan **Anon Key** (publik).
3. Jalankan migrasi SQL:
   ```bash
   supabase db push
   # Atau eksekusi file SQL di SQL Editor Dashboard:
   # 1. supabase/migrations/20261029000001_initial_schema.sql
   # 2. supabase/migrations/20261029000002_rls_and_storage.sql
   # 3. supabase/migrations/20261029000003_vehicles_and_transactions_tahap3.sql
   # 4. supabase/migrations/20261029000004_receipt_scan_jobs_policy.sql
   ```

### 2.2 Konfigurasi Email Authentication
1. Buka **Authentication > Providers > Email**.
2. Aktifkan **Enable Email provider**.
3. Atur opsi **Confirm email**:
   - Disarankan aktif pada lingkungan produksi demi validitas email.
   - Pada development lokal, auto-confirm dapat diaktifkan jika pengujian tidak memerlukan verifikasi inbox nyata.

### 2.3 Konfigurasi Google Sign-In (OAuth & Credential Manager)
Integrasi Google Sign-In menggunakan **Android Credential Manager** resmi. Diperlukan dua Client ID di [Google Cloud Console](https://console.cloud.google.com/apis/credentials):
1. **Web Application Client ID (Server Client ID)**:
   - Jenis: Web Application.
   - Masukkan Client ID dan Secret ke **Supabase Dashboard > Authentication > Providers > Google**.
   - Masukkan Web Client ID ini ke `local.properties` aplikasi:
     ```properties
     GOOGLE_WEB_CLIENT_ID=xxxxxx.apps.googleusercontent.com
     ```
2. **Android Client ID**:
   - Jenis: Android.
   - Package Name: `id.maripartner.fuel`
   - **SHA-1 Fingerprint**:
     - Ambil SHA-1 Debug Keystore:
       ```powershell
       keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
       ```
     - Tambahkan juga SHA-1 Release Keystore saat rilis produksi.

---

## 3. Penyimpanan Struk & Akses Gambar Privat

1. **Private Storage Bucket:** File struk disimpan di bucket `receipts` berstatus privat (*not publicly readable*).
2. **Pemisahan Jalur Penyimpanan:**
   Path file mengikuti format: `receipts/{user_id}/{uuid}.jpg`.
3. **Signed URLs Berumur Pendek:**
   Aplikasi menampilkan struk hanya melalui URL bertanda tangan (*signed URL*) berumur pendek (default 20 menit). URL ini tidak disimpan permanen di database maupun log.

---

## 4. Model Ancaman & Mitigasi Terverifikasi

| Ancaman (*Threat*) | Vektor Serangan | Mitigasi yang Diterapkan & Teruji |
| :--- | :--- | :--- |
| **Akses Lintas Akun (*BOLA/IDOR*)** | Memanipulasi `vehicle_id` atau `receipt_file_id` milik akun lain | PostgreSQL RLS + Composite Foreign Key `(vehicle_id, user_id)` memastikan relasi hanya sah jika dimiliki `auth.uid()`. |
| **Aktivasi Pro Palsu** | Memanipulasi kolom `status` pada tabel `subscriptions` | Aturan RLS memblokir seluruh operasi INSERT/UPDATE dari klien Android; mutasi hanya dapat dieksekusi server via `service_role`. |
| **Manipulasi Harga Paket** | Mengirim nominal harga murah dari client | Harga diambil langsung dari server (`subscription_plans`); klien hanya mengirim ID paket. |
| **Penyalahgunaan Kuota AI** | Memanggil langsung endpoint provider AI | API key AI hanya disimpan di Secret Manager Edge Functions; dipanggil hanya setelah validasi JWT dan sisa kuota. |
| **Bypass Kepemilikan Struk pada AI** | Mengirim `storage_path` struk akun lain ke Edge Function | Edge Function `extract-fuel-receipt` memverifikasi token JWT dan menolak dengan status 403 jika `storage_path` tidak diawali `${user.id}/`. |
| **Kebocoran Privasi ke AI Pihak Ketiga** | Mengirim plat nomor atau data pribadi ke LLM | Data minimization: hanya gambar/teks struk yang dikirim; plat nomor, profil, dan nama pemilik ditiadakan dari payload AI. |
| **Kebocoran Struk Transaksi** | Mengakses link foto struk secara publik | Bucket `receipts` berstatus privat (`public = false`); akses membutuhkan signed URL sementara dari token user aktif. |
| **Replay Webhook Pembayaran** | Mengirim notifikasi bayar berulang kali | Tabel `payment_webhook_events` dengan *unique constraint* `(provider, event_id)` menjamin idempotensi atomik. |

---

## 5. Status Verifikasi: Teruji vs Konfigurasi Manual

| Aspek Keamanan | Status Pengujian | Keterangan |
| :--- | :---: | :--- |
| **Unit Test Validasi Input Autentikasi** | ✅ **LULUS (100%)** | Validasi email regex, panjang sandi, dan kecocokan konfirmasi sandi. |
| **Unit Test Storage Path & File Validation** | ✅ **LULUS (100%)** | Pembatasan 5 MB, filter MIME type (JPEG/PNG/WebP), path collision UUID. |
| **Unit Test Validasi Kendaraan & Transaksi** | ✅ **LULUS (100%)** | Validasi nama kendaraan, tahun (1900..2100), total > 0, volume > 0, toleransi selisih harga. |
| **Proteksi BOLA/IDOR Kendaraan & Transaksi** | ✅ **LULUS (100%)** | Foreign key komposit `(vehicle_id, user_id)` menjamin transaksi tidak dapat dihubungkan ke kendaraan akun lain. |
| **Unit Test Ekstraksi Regex & Deteksi Discrepancy OCR** | ✅ **LULUS (100%)** | Parsing struk SPBU Indonesia, toleransi pembulatan, proteksi garbled text (`ReceiptTextParserTest`). |
| **Unit Test Serialisasi & Fallback AI DTO** | ✅ **LULUS (100%)** | Deserialisasi camel/snake case, fallback graceful tanpa secret Gemini (`AiExtractionDtoTest`). |
| **Verifikasi Integritas Skema & RLS SQL (13 Tabel + Policy Tahap 4)** | ✅ **LULUS (100%)** | 13 tabel RLS aktif, composite FK, bucket privat, dan update scan job teruji via `MigrationSqlTest`. |
| **Build Android Bebas Secret** | ✅ **LULUS (100%)** | Injeksi aman BuildConfig; zero AI secret di APK; `.env` & `local.properties` diabaikan oleh Git. |
| **Kredensial Supabase & Google Cloud Riil** | ⚠️ **BLOCKED (Perlu Konfigurasi)** | Memerlukan penyediaan URL proyek, Anon Key, dan Google OAuth Client ID nyata pada environment pengguna. |
| **Kredensial Gemini API Key Riil di Supabase Edge Function** | ⚠️ **BLOCKED (Perlu Konfigurasi)** | Memerlukan pemasangan secret `GEMINI_API_KEY` via `supabase secrets set` untuk eksekusi cloud AI riil. |
