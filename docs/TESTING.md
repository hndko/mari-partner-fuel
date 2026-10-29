# TESTING.md — Panduan & Rekap Pengujian Mutu (QA) Mari Partner Fuel

Dokumen ini memuat seluruh metodologi pengujian, laporan eksekusi pengujian otomatis, cakupan pengujian, dan panduan verifikasi manual untuk aplikasi **Mari Partner Fuel**.

---

## 📌 1. Ringkasan Eksekusi Pengujian Mutu

- **Status Build**: ✅ **BUILD SUCCESSFUL**
- **Total Unit Test**: **80 Tests** (17 Test Suites)
- **Tingkat Kelulusan (Pass Rate)**: **100% (0 Gagal, 0 Error, 0 Diabaikan)**
- **Waktu Eksekusi**: ~26 detik
- **Artefak Build Terverifikasi**: `app/build/outputs/apk/debug/app-debug.apk` (Ukuran: ~23.1 MB)
- **Kompatibilitas Runtime**: Min SDK API 26 (Android 8.0 Oreo), Target SDK API 35 (Android 15), Desugaring JDK Java 8+ / `java.time` aktif.

---

## 🧪 2. Rincian Rangkaian Unit Test (17 Test Suites)

### A. Fitur Hybrid OCR & AI Extraction (Tahap 4)
1. **`ReceiptTextParserTest` (5 tests)**:
   - Verifikasi ekstraksi struk Pertamina (Pertamax, Rp 35.000, 2.703 L, SPBU 34-12345, tanggal/waktu, dan nomor struk).
   - Verifikasi ekstraksi struk Shell (Shell V-Power, Rp 50.000, 3.425 L, SPBU Shell Gatot Subroto).
   - Diferensiasi ketat antara nominal tunai/kembalian (*cash/change*) dengan total biaya pengisian BBM sesungguhnya.
   - Deteksi selisih matematis bila `volume * harga != total` dengan toleransi pembulatan wajar.
   - Pengujian ketahanan terhadap teks rusak/buram (*garbled text*) yang menghasilkan nilai aman (null) tanpa memicu crash.
2. **`AiExtractionDtoTest` (3 tests)**:
   - Serialisasi dan deserialisasi JSON DTO respon Edge Function AI (dukungan field camelCase & snake_case).
   - Logika penggabungan (*merge*) kandidat lokal ML Kit dan kandidat AI (`mergeWithAiCandidates`), memprioritaskan field AI jika kandidat lokal kosong atau AI memiliki confidence tinggi.
   - Penanganan ramah ketika backend Edge Function belum memiliki `GEMINI_API_KEY` (`configured = false`).

### B. Fitur Kendaraan & Transaksi BBM Manual (Tahap 3)
3. **`ValidateVehicleInputUseCaseTest` (7 tests)**:
   - Verifikasi nama kendaraan wajib diisi (menolak string kosong/spasi).
   - Validasi rentang tahun kendaraan yang masuk akal (1900 hingga tahun depan).
   - Penolakan kapasitas tangki bernilai negatif atau nol.
   - Penolakan angka odometer negatif.
   - Validasi kendaraan dengan isian bidang opsional bernilai null.
4. **`ValidateTransactionInputUseCaseTest` (6 tests)**:
   - Verifikasi pemilihan kendaraan wajib (`vehicleId` tidak boleh kosong).
   - Penolakan total pembayaran bernilai Rp 0 atau negatif.
   - Penolakan volume liter bernilai negatif atau nol jika diisi.
   - Pengujian kelulusan input cepat tanpa volume (`volumeLiters = null`).
   - Penolakan angka odometer transaksi bernilai negatif.
5. **`EstimateFuelCalculationUseCaseTest` (6 tests)**:
   - Akurasi perhitungan harga per liter dari total pembayaran dan volume.
   - Penanganan pembagian dengan nol atau parameter invalid menghasilkan null aman.
   - Akurasi perhitungan volume liter dari total pembayaran dan harga per liter.
   - Verifikasi kecocokan nominal tanpa selisih (*exact match*).
   - Deteksi selisih signifikan (> 5% toleransi atau selisih > Rp 1.000) dan pembentukan pesan peringatan yang ramah.
   - Toleransi selisih pembulatan wajar (< Rp 1.000 atau < 5%) tidak memicu false alarm.
6. **`VehicleDtoMappingTest` (3 tests)**:
   - Pemetaan seluruh kolom Postgrest Supabase DTO ke domain model `Vehicle`.
   - Pemetaan domain model ke DTO untuk simpan/update, termasuk normalisasi plat nomor menjadi huruf kapital (*uppercase*).
   - Penanganan ID kosong menjadi null untuk operasi auto-generate UUID di server.
7. **`FuelTransactionDtoMappingTest` (2 tests)**:
   - Pemetaan relasi Postgrest embedded (`vehicles` dan `receipt_files`) ke domain model `FuelTransaction`.
   - Pembentukan Signed URL pada model domain transaksi.
   - Pemetaan data dari domain ke DTO untuk persistensi data baru.
8. **`VehicleUseCaseTest` (4 tests)**:
   - Pengujian alur pembuatan kendaraan baru dan penugasan ID otomatis.
   - Pengujian penolakan penyimpanan jika validasi nama kendaraan gagal.
   - Pengujian mekanisme arsip dan aktifkan kembali kendaraan (kendaraan diarsipkan tidak muncul di filter default).
   - Pengujian penghapusan kendaraan.
9. **`TransactionUseCaseTest` (4 tests)**:
   - Pembuatan transaksi baru beserta pengaitan metadata struk foto.
   - Penolakan penyimpanan transaksi jika total pembayaran invalid.
   - Pengujian filter komprehensif (filter kendaraan, filter jenis BBM, pencarian nama SPBU) dan server-side pagination.
   - Pengujian penghapusan transaksi.

### C. Autentikasi, Database, & Storage (Tahap 1 & 2)
10. **`AuthValidationTest` (6 tests)**: Format regex email, batas minimum 8 karakter password, dan validasi konfirmasi password.
11. **`AuthUseCaseTest` (9 tests)**: Registrasi, login, logout, reset password, dan session recovery.
12. **`ReceiptStorageManagerTest` (6 tests)**: Batas maksimal 5 MB, filter MIME type (JPEG/PNG/WebP), generator path anti-tabrakan `{userId}/{UUID}.{ext}`, dan signed URL TTL.
13. **`MigrationSqlTest` (8 tests)**: Integritas skema DDL 13 tabel, Composite Foreign Key `(vehicle_id, user_id)`, RLS policies, konfigurasi bucket private, verifikasi migration `20261029000003_vehicles_and_transactions_tahap3.sql`, dan verifikasi migration `20261029000004_receipt_scan_jobs_policy.sql` (kebijakan update own job & validasi status constraint).
14. **`CurrencyFormatterTest` (5 tests)**: Format Rupiah `Rp 50.000`, pemformatan desimal liter `25,450 L`, dan pembersihan string OCR/input kotor menjadi `Long`.
15. **`NavRouteTest` (3 tests)**: Rute statis, 5 tab bottom navigation, dan pembuatan rute dinamis bertipe aman (`VehicleForm`, `TransactionForm`, `TransactionDetail`).
16. **`UiStateTest` (2 tests)**: Verifikasi status `UiState` (Idle, Loading, Success, Empty, Error).
17. **`AppConfigTest` (2 tests)**: Redaksi token Supabase/Google Client ID untuk proteksi log debug (*zero secret leakage*).

---

## 💻 3. Cara Mereproduksi Hasil Pengujian

Jalankan perintah berikut di terminal (Windows PowerShell atau Linux Bash):

```powershell
# 1. Jalankan seluruh Automated Unit Tests
.\gradlew.bat testDebugUnitTest

# 2. Bangun APK Debug Mandiri
.\gradlew.bat assembleDebug
```

Laporan HTML hasil pengujian otomatis tersedia di:
`app/build/reports/tests/testDebugUnitTest/index.html`

---

## 🛡️ 4. Prosedur Verifikasi Keamanan Multi-Akun (Security & RLS)

Untuk memverifikasi isolasi data antarakun pada instance Supabase sungguhan:

1. **Uji Isolasi Kendaraan**:
   - Daftarkan Akun A dan tambahkan kendaraan "Vario Akun A".
   - Daftarkan Akun B dan tambahkan kendaraan "Mobil Akun B".
   - Buka layar kendaraan pada Akun A: Pastikan hanya "Vario Akun A" yang tampil.
   - Upaya query langsung melalui Postgrest klien dengan ID kendaraan Akun B harus menghasilkan baris kosong atau error 404 (ditegakkan oleh RLS policy `auth.uid() = user_id`).
2. **Uji Pencegahan BOLA/IDOR pada Transaksi BBM**:
   - Akun A mencoba membuat transaksi baru dengan menyisipkan `vehicle_id` milik Akun B.
   - **Hasil yang Diharapkan**: PostgreSQL menolak transaksi dengan `foreign key violation` pada constraint composite foreign key `(vehicle_id, user_id) REFERENCES vehicles(id, user_id)`.
3. **Uji Isolasi Private Storage Struk**:
   - Akun A mengunggah foto struk ke path `receipts/{user_id_A}/{uuid}.jpg`.
   - Akun B mencoba mengunduh atau membuat signed URL untuk path Akun A.
   - **Hasil yang Diharapkan**: Storage policy `receipts_storage_select_own` menolak akses karena `(storage.foldername(name))[1] != auth.uid()`.
