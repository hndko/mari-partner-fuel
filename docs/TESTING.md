# TESTING.md — Panduan & Rekap Pengujian Mutu (QA) Mari Partner Fuel

Dokumen ini memuat seluruh metodologi pengujian, laporan eksekusi pengujian otomatis, cakupan pengujian, dan panduan verifikasi manual untuk aplikasi **Mari Partner Fuel**.

---

## 📌 1. Ringkasan Eksekusi Pengujian Mutu (Quality Gate Tahap 8)

- **Status Build**: ✅ **BUILD SUCCESSFUL**
- **Total Unit Test**: **132 Tests** (28 Test Suites)
- **Tingkat Kelulusan (Pass Rate)**: **100% (0 Gagal, 0 Error, 0 Diabaikan)**
- **Waktu Eksekusi**: ~23 detik
- **Artefak Build Terverifikasi**: `app/build/outputs/apk/debug/app-debug.apk` (Ukuran: ~23.1 MB)
- **Kompatibilitas Runtime**: Min SDK API 26 (Android 8.0 Oreo), Target SDK API 35 (Android 15), Desugaring JDK Java 8+ / `java.time` aktif.

---

## 🧪 2. Rincian Rangkaian Unit Test (28 Test Suites)

### A. Keamanan, Integritas Data & Hardening (Tahap 8)
1. **`SecurityHardeningAndIntegrityTest` (8 tests)**:
   - Isolasi kepemilikan multi-akun (`Akun A` vs `Akun B`): penolakan penyamaran identitas kendaraan.
   - Proteksi foreign key komposit: penolakan modifikasi transaksi BBM lintas akun.
   - Aturan direktori private storage: penolakan akses folder berkas akun lain.
   - Sanitasi OWASP Spreadsheet Formula Injection: penjinakan payload berbahaya (`=`, `+`, `-`, `@`, `\t`, `\r`) dengan awalan tanda petik tunggal (`'`).
   - Preservasi angka numerik murni pada CSV: angka sah bertanda positif/negatif tidak dirusak.
   - Anti-replay purchase token Google Play: deteksi token `paid` agar tidak dapat diklaim berulang.
   - Parsing tipe provider pembayaran resmi (`google_play`, `qrispy`).
   - Zero-Secret Policy: pengujian sensor redaksi kunci rahasia pada debug logger (`AppConfig.redactedKey`).
2. **`MigrationSqlTest` (8 tests)**:
   - Keberadaan dan sintaksis 8 berkas migrasi SQL Supabase (001 s.d. 008).
   - Validasi aktivasi Row Level Security (RLS) pada 13 tabel publik.
   - Validasi konfigurasi bucket privat `receipts` (5 MB, MIME JPEG/PNG/WebP).
   - Validasi trigger batas kendaraan `trg_enforce_vehicle_limit`.
   - Validasi RPC `activate_subscription_for_payment`, `handle_subscription_lifecycle_event`, dan `record_payment_webhook_event`.
   - Validasi pencabutan izin (REVOKE) fungsi otoritatif dari `public, anon, authenticated` dan pembatasan khusus bagi `service_role`.

### B. Pembayaran & Langganan Dual-Track (Tahap 7)
1. **`PaymentModelTest` (4 tests)**:
   - Evaluasi status transaksi pembayaran (`isPending`, `isPaid`, `isExpired`, `isFailed`).
   - Penentuan tanggal aktif Pro dari transaksi finansial.
2. **`PaymentDtoTest` (3 tests)**:
   - Serialisasi dan deserialisasi response DTO `CreateCheckoutResponseDto`.
   - Deserialisasi payload verifikasi pembelian Google Play `VerifyPlayPurchaseRequestDto`.
   - Deserialisasi status pembayaran `PaymentStatusResponseDto`.
3. **`PaymentUseCaseTest` (4 tests)**:
   - Eksekusi `CreateCheckoutOrderUseCase` dengan provider Play Billing dan QRISpy.
   - Eksekusi `VerifyPlayBillingPurchaseUseCase` dengan validasi server.
   - Eksekusi `GetPaymentStatusUseCase` untuk polling status pembayaran.
   - Eksekusi `GetPaymentHistoryUseCase` untuk riwayat invoice pengguna.

### C. Entitlement, Kuota AI, & Paket Free/Pro (Tahap 6)
1. **`UserEntitlementTest` (6 tests)**:
   - Evaluasi hak akses Pro berdasarkan waktu server (`expiresAt > now()`).
   - Penanganan status `grace_period` dalam toleransi 3 hari.
   - Perhitungan sisa kuota AI bulanan.
2. **`CheckFeatureAccessUseCaseTest` (5 tests)**:
   - Penegakan batas maksimal 2 kendaraan aktif untuk paket Free.
   - Izin penambahan kendaraan tanpa batas untuk pengguna Pro.
   - Pengecekan sisa kuota ekstraksi AI sebelum request ke cloud.
3. **`EntitlementDtoTest` (3 tests)**:
   - Deserialisasi respons JSON `UserEntitlementDto` dari Edge Function `get-my-entitlements`.
   - Parsing daftar fitur `features` dan meter kuota AI `ai_quota`.

### D. Dashboard, Analitik, CSV & Pengingat (Tahap 5)
1. **`DashboardSummaryCalculationTest` (4 tests)**:
   - Akurasi perhitungan total pengeluaran bulanan, total volume, dan rata-rata tertimbang harga per liter.
   - Penanganan volume tidak lengkap tanpa menganggapnya nol.
   - Penanganan bulan lalu tanpa data (mencegah persentase menyesatkan).
   - Identifikasi kendaraan dengan pengeluaran tertinggi.
2. **`CalculateFuelEfficiencyTest` (3 tests)**:
   - Kalkulasi efisiensi km/liter dari odometer berurutan per kendaraan.
   - Penanganan anomali odometer menurun tanpa merusak leg valid lainnya.
   - Penolakan perhitungan saat entri valid kurang dari 2.
3. **`ExportTransactionsCsvTest` (4 tests)**:
   - Standar RFC 4180 dengan UTF-8 Byte Order Mark (`\uFEFF`).
   - Mitigasi formula injection dan preservasi integer rupiah.
4. **`MaintenanceReminderValidationTest` (7 tests)**:
   - Validasi kelengkapan form pengingat servis.
   - Evaluasi status jatuh tempo otomatis (`OVERDUE`, `DUE_SOON`, `UPCOMING`, `COMPLETED`).

### E. Fitur Hybrid OCR & AI Extraction (Tahap 4)
1. **`ReceiptTextParserTest` (5 tests)**: Ekstraksi teks struk Pertamina, Shell, diferensiasi nominal tunai vs total, toleransi pembulatan.
2. **`AiExtractionDtoTest` (3 tests)**: Deserialisasi respons Edge Function AI dan penggabungan (*merge*) kandidat lokal & cloud.

### F. Transaksi BBM & Kendaraan (Tahap 3)
1. **`FuelTransactionValidationTest` (7 tests)**: Validasi total > 0, volume > 0, dan toleransi selisih harga.
2. **`VehicleValidationTest` (7 tests)**: Validasi nama kendaraan, jenis, dan tahun pembuatan (1900..2100).
3. **`FuelTransactionModelTest` (5 tests)**: Perhitungan harga per liter otomatis dan formatting waktu.
4. **`VehicleModelTest` (4 tests)**: Pembuatan subtitle ringkasan kendaraan.

### G. Fondasi, Autentikasi & Utilitas (Tahap 1 & 2)
1. **`CurrencyFormatterTest` (8 tests)**: Pemformatan nominal Rupiah (`Rp 50.000`) dan parsing string ke Long.
2. **`DateUtilsTest` (6 tests)**: Konversi zona waktu UTC ke lokal Indonesia (WIB/WITA/WIT).
3. **`AuthValidationTest` (6 tests)**: Format regex email, batas minimum 6 karakter password, dan konfirmasi password.
4. **`AuthUseCaseTest` (9 tests)**: Registrasi, login, logout, reset password, dan session recovery.
5. **`NavigationTest` (5 tests)**: Tipe keamanan rute Compose Navigation.
6. **`UiStateTest` (5 tests)**: Integritas status sealed interface (`Idle`, `Loading`, `Success`, `Error`, `Empty`).
7. **`AppConfigTest` (4 tests)**: Injeksi variabel lingkungan BuildConfig.

---

## 🚀 3. Skenario Pengujian End-to-End (E2E Manual Walkthrough)

### 🚗 Alur A — Siklus Pengguna Free Tier
1. **Pendaftaran:** Pengguna mendaftar dengan email dan password di `RegisterScreen`. Akun otomatis terdaftar pada paket `Mari Free`.
2. **Tambah Kendaraan:** Tambahkan 2 kendaraan (misal: "Vario 160" dan "Avanza").
3. **Uji Batas Kendaraan:** Coba tambahkan kendaraan ke-3. Sistem menolak dengan dialog ramah: "Paket Free membatasi maksimal 2 kendaraan aktif. Tingkatkan ke Pro untuk kendaraan tanpa batas."
4. **Pencatatan Manual:** Catat transaksi BBM manual Rp 50.000 untuk Vario 160. Data berhasil tersimpan dan odometer kendaraan otomatis ter-update.
5. **Scan Struk Lokal:** Buka kamera struk, ambil foto struk Pertamina. Google ML Kit lokal mengekstrak data tanpa koneksi internet.
6. **Ekspor Riwayat:** Buka riwayat, ketuk ikon Download CSV. Berkas CSV standar RFC 4180 tersimpan rapi dengan sanitasi formula injection.
7. **Pengingat Servis:** Buat pengingat ganti oli mesin. Alarm berhasil dijadwalkan pada pukul 08:00 pagi.

### 💎 Alur B — Siklus Pengguna Pro Tier
1. **Akses Katalog:** Buka `ProPlansScreen` dari Beranda atau tab Profil. Meter kuota AI menampilkan sisa kuota paket.
2. **Pilih Paket:** Ketuk paket "Mari Pro Bulanan" (Rp 29.000) atau "Mari Pro Tahunan" (Rp 290.000).
3. **Verifikasi Entitlement:** Setelah aktivasi, lencana "PRO AKTIF" muncul di Beranda dan Profil.
4. **Multi-Kendaraan Bebas:** Tambahkan kendaraan ke-3, ke-4, dan ke-5 tanpa batas penolakan.
5. **Ekstraksi AI Prioritas:** Scan struk buram dan ketuk "Optimalkan dengan AI Cloud". Kuota AI berkurang 1 secara atomik dan terkonfirmasi.

### 💳 Alur C — Pembayaran & Idempotensi Webhook
1. **Inisiasi Checkout:** Pilih paket Pro Bulanan di `CheckoutScreen`.
2. **Jalur Play Store:** Diuji via License Testing Google Play. Token pembelian diterima dan diverifikasi ke server.
3. **Jalur QRISpy:** Diuji via mode Dynamic QRIS. QR code tampil dengan timer 15 menit.
4. **Simulasi Webhook Sukses:** Kirim payload webhook `PAID`. Pro aktif secara instan.
5. **Simulasi Webhook Duplikat:** Kirim kembali payload webhook yang sama. Server merespons `HTTP 200` dengan pesan `is_duplicate: true` tanpa memperpanjang langganan dua kali.
6. **Simulasi Token Replay:** Ajukan ulang purchase token yang sudah `paid`. Edge Function menolak dengan `HTTP 409 PURCHASE_TOKEN_ALREADY_REDEEMED`.

### 🛡️ Alur D — Uji Penetrasi Isolasi Dua Akun (Akun A vs Akun B)
1. **Setup:** Siapkan Akun A (`user_a`) dan Akun B (`user_b`).
2. **Akses Kendaraan:** Akun B menembak endpoint `vehicles` dengan query ID milik Akun A. Respons mengembalikan list kosong / 404 (RLS aktif).
3. **Injeksi Foreign Key Transaksi:** Akun B mengirim request `POST fuel_transactions` dengan `vehicle_id` milik Akun A. PostgreSQL menolak dengan foreign key constraint violation `(vehicle_id, user_id)`.
4. **Akses Foto Struk Privat:** Akun B mencoba mengunduh URL foto struk di path `receipts/user_a/file.jpg`. Storage policy menolak dengan 403 Forbidden.
5. **Klaim Pro Palsu:** Akun A mengirim mutasi `UPDATE subscriptions SET status = 'active'`. RLS PostgreSQL memblokir seluruh operasi UPDATE dari klien.
