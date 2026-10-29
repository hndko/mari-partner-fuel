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
   # 5. supabase/migrations/20261029000005_maintenance_reminders_enhancement.sql
   # 6. supabase/migrations/20261029000006_subscription_entitlements_and_quota.sql
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
| **Spreadsheet Formula Injection (CSV)** | Menyisipkan perintah formula `=`, `+`, `-`, `@` pada kolom catatan atau SPBU | Netralisasi otomatis dengan awalan tanda petik tunggal (`'`) pada `ExportTransactionsCsvUseCase`, angka numerik sah dipertahankan. |
| **Manipulasi Pengingat Lintas Akun** | Menghubungkan pengingat servis ke `vehicle_id` milik akun lain | Composite Foreign Key `fk_maintenance_reminders_vehicle_user (vehicle_id, user_id)` menjamin referensi ditolak jika kendaraan bukan milik `user_id`. |
| **Replay Webhook Pembayaran** | Mengirim notifikasi bayar berulang kali | Tabel `payment_webhook_events` dengan *unique constraint* `(provider, event_id)` menjamin idempotensi atomik. |
| **Bypass Kuota AI Bersamaan (*Race Condition*)** | Menembak request AI paralel melebihi batas kuota bulanan | PostgreSQL RPC `reserve_ai_quota` atomik mengunci dan mencatat status `reserved`; tolak HTTP 403 `AI_QUOTA_EXCEEDED` sebelum AI eksternal dipanggil. |
| **Pemalsuan Hak Akses Pro (*Client Claim*)** | Klien memanipulasi payload `is_pro: true` atau status subscription | Edge Function `get-my-entitlements` dan RPC `get_user_effective_entitlement` memvalidasi masa aktif dari database via server time (`now() <= expires_at`). |
| **Pelanggaran Batas Kendaraan Free Tier** | Menambahkan lebih dari 2 kendaraan aktif tanpa Pro | PostgreSQL Trigger `trg_enforce_vehicle_limit` menolak `INSERT`/`UPDATE` kendaraan aktif melebihi batas paket di database. |
| **Replay Purchase Token Play Store** | Menyerahkan `purchase_token` yang sama dari akun berbeda atau transaksi lama | Edge Function `verify-play-purchase` memeriksa keunikan token pada `payment_transactions(provider, provider_payment_id)` dan menolak dengan HTTP 403 `PURCHASE_TOKEN_ALREADY_USED`. |
| **Bypass Google Play Billing Verification** | Mengirim claim pembelian sukses tanpa verifikasi store | Edge Function `verify-play-purchase` memvalidasi token langsung ke Google Play Developer API via Service Account sebelum memanggil RPC aktivasi. |
| **Pemalsuan Signature Webhook QRISpy** | Mengirim HTTP callback palsu ke endpoint webhook | Edge Function `payment-webhook` memvalidasi signature HMAC-SHA256 / X-API-Token secara timing-safe sebelum memproses event. |
| **Duplikasi Webhook Pembayaran (Double Activation)**| Gateway mengirim webhook sukses berulang kali | PostgreSQL RPC `record_payment_webhook_event` mengunci `event_id` secara atomik; duplikat diabaikan tanpa memperpanjang langganan ganda. |

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
| **Unit Test Formula Injection Mitigation & CSV RFC 4180** | ✅ **LULUS (100%)** | Netralisasi formula injeksi spreadsheet, UTF-8 BOM, zero credentials di CSV (`ExportTransactionsCsvTest`). |
| **Unit Test Validasi Pengingat & Evaluasi Jatuh Tempo** | ✅ **LULUS (100%)** | Pemicu tanggal/odometer, deteksi status overdue/due soon, proteksi null trigger (`MaintenanceReminderValidationTest`). |
| **Unit Test Agregasi Finansial & Efisiensi Odometer** | ✅ **LULUS (100%)** | Perhitungan dashboard tanpa N+1, rata-rata tertimbang, penanganan incomplete volume & anomali odometer. |
| **Unit Test Entitlement, Batas Kendaraan, & Kuota AI (Tahap 6)** | ✅ **LULUS (100%)** | Perhitungan batas 2 kendaraan Free, status kedaluwarsa/grace period, kuota habis (`UserEntitlementTest`, `CheckFeatureAccessUseCaseTest`). |
| **Unit Test Deserialisasi Entitlement & Plan DTO (Tahap 6)** | ✅ **LULUS (100%)** | Deserialisasi JSON respons `UserEntitlementDto` dan fitur `SubscriptionPlanDto` (`EntitlementDtoTest`). |
| **Unit Test Model & State Pembayaran (Tahap 7)** | ✅ **LULUS (100%)** | Validasi status pembayaran, parsing provider type, status kelayakan Pro (`PaymentModelTest`). |
| **Unit Test Serialisasi DTO Checkout & Status (Tahap 7)** | ✅ **LULUS (100%)** | Deserialisasi order QRISpy, verifikasi purchase token, dan respons transaksi (`PaymentDtoTest`). |
| **Unit Test Use Case Pembayaran & Aktivasi (Tahap 7)** | ✅ **LULUS (100%)** | Pembuatan order, validasi purchase token Google Play, polling status pembayaran (`PaymentUseCaseTest`). |
| **Verifikasi Integritas Skema SQL & Migrasi 007 (Tahap 7)** | ✅ **LULUS (100%)** | Validasi constraint provider, RPC `activate_subscription_for_payment`, dan idempotensi webhook via `MigrationSqlTest`. |
| **Unit Test Keamanan & Hardening Data (Tahap 8)** | ✅ **LULUS (100%)** | Uji isolasi akun A vs B, mitigasi formula injection, anti-replay token, dan zero-secret (`SecurityHardeningAndIntegrityTest`). |
| **Verifikasi Migrasi 008 & Pengetatan Izin RPC (Tahap 8)** | ✅ **LULUS (100%)** | Pencabutan izin `EXECUTE` fungsi pembayaran/kuota dari public/authenticated ke `service_role` murni (`MigrationSqlTest`). |
| **Build Android Bebas Secret** | ✅ **LULUS (100%)** | Injeksi aman BuildConfig; zero AI/payment secret di APK; `.env` & `local.properties` diabaikan oleh Git. |
| **Kepatuhan Regulasi Google Play Payments** | ✅ **LULUS (100%)** | Dual-track payment memisahkan Play Billing resmi dengan direct web distribution (`docs/PAYMENTS.md`). |
| **Kredensial Supabase & Google Cloud Riil** | ⚠️ **BLOCKED (Perlu Konfigurasi)** | Memerlukan penyediaan URL proyek, Anon Key, dan Google OAuth Client ID nyata pada environment pengguna. |
| **Kredensial Gemini API Key Riil di Supabase Edge Function** | ⚠️ **BLOCKED (Perlu Konfigurasi)** | Memerlukan pemasangan secret `GEMINI_API_KEY` via `supabase secrets set` untuk eksekusi cloud AI riil. |
| **Kredensial Google Play Service Account & Merchant QRISpy Riil** | ⚠️ **BLOCKED (Perlu Konfigurasi)** | Memerlukan `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` dan `QRISPY_API_KEY` pada Supabase Secrets untuk transaksi moneter riil. |
