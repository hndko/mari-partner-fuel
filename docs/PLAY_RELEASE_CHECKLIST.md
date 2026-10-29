# 📋 PLAY_RELEASE_CHECKLIST.md — Master Checklist Rilis Google Play Console

Dokumen ini adalah daftar periksa induk (*Master Release Checklist*) khusus untuk tim teknis dan pemilik proyek sebelum mengirimkan aplikasi **Mari Partner Fuel** ke proses review Google Play Console.

---

## 🛠️ Bagian 1: Kesiapan Teknis & Artefak Rilis

- [x] **Format Android App Bundle**: Berkas paket produksi berformat `.aab` (bukan `.apk`).
- [x] **Ukuran Paket Teroptimasi**: Berkas `app-release.aab` berukuran **`7.57 MB`** (`7,573,319 bytes`) berkat optimasi R8 minification dan resource shrinking.
- [x] **Target SDK 35 (Android 15)**: `compileSdk = 35`, `targetSdk = 35` (memenuhi standar Play Store 2024/2025).
- [x] **Minimum SDK 26 (Android 8.0)**: Desugaring Java 8+ (`desugar_jdk_libs:2.1.2`) aktif dan terverifikasi bebas crash.
- [x] **Kesesuaian Identitas Rilis**: `applicationId = "id.maripartner.fuel"`, `versionCode = 8`, `versionName = "1.7.0"`.
- [x] **Kelulusan Unit & Performance Test**: 137 unit test di 29 test suites lulus 100% (`.\gradlew.bat testDebugUnitTest`).
- [x] **Lint Vital Release Check**: Bebas dari kesalahan fatal (*0 fatal errors*) pada task `lintVitalRelease`.
- [x] **Zero-Secret di Kode Sumber**: Tidak ada API key backend, secret payment gateway, atau private key yang terekspos di APK/AAB.
- [x] **Keamanan Jaringan**: Komunikasi wajib TLS 1.3/HTTPS, tidak ada `usesCleartextTraffic="true"`.
- [x] **Izin Minimal (Least Privilege)**: Hanya meminta izin `INTERNET`, `CAMERA`, `POST_NOTIFICATIONS`, dan `READ_MEDIA_IMAGES`.

---

## 🏪 Bagian 2: Kelengkapan Daftar Toko (Store Listing)

- [x] **Nama Aplikasi**: "Mari Partner Fuel" (17 karakter, batas maks 30 karakter).
- [x] **Deskripsi Singkat**: Telah disusun dalam bahasa Indonesia (79 karakter, batas maks 80 karakter).
- [x] **Deskripsi Lengkap**: Telah disusun dalam bahasa Indonesia tanpa klaim berlebihan atau testimoni palsu.
- [x] **Kategori Aplikasi**: Ditetapkan sebagai *Otomotif & Kendaraan* (*Auto & Vehicles*) dengan tag sekunder *Produktivitas*.
- [ ] **Ikon Aplikasi (App Icon)**: Draf siap, perlu diunggah dalam format PNG 512 x 512 px (32-bit).
- [ ] **Gambar Fitur (Feature Graphic)**: Draf siap, perlu diunggah dalam format JPEG/PNG 1024 x 500 px.
- [ ] **Tangkapan Layar Ponsel (Phone Screenshots)**: Minimal 4 tangkapan layar antarmuka riil (rasio 9:16).
- [x] **Email Kontak Dukungan**: `support@maripartner.id`.

---

## 🔒 Bagian 3: Kebijakan, Privasi & Keamanan Data (Policy & Data Safety)

- [ ] **URL Kebijakan Privasi Publik**: Teks kebijakan privasi di [`docs/PLAY_DATA_SAFETY_AND_PRIVACY.md`](PLAY_DATA_SAFETY_AND_PRIVACY.md) siap di-host di domain publik `https://maripartner.id/privacy-policy`.
- [ ] **Kuesioner Keamanan Data (Data Safety Form)**: Diisi sesuai panduan Bagian 2 di [`docs/PLAY_DATA_SAFETY_AND_PRIVACY.md`](PLAY_DATA_SAFETY_AND_PRIVACY.md).
- [ ] **Petunjuk Akses Aplikasi (App Access)**: Kredensial akun peninjauan reviewer telah disiapkan pada formulir App Access di konsol.
- [ ] **Kuesioner Rating Konten (IARC)**: Kuesioner rating konten telah dilengkapi di konsol (ekspektasi rating: Semua Umur / PEGI 3 / 3+).
- [ ] **Deklarasi Audiens Target**: Kategori usia target ditetapkan 18 tahun ke atas (pengemudi kendaraan bermotor).
- [ ] **Deklarasi Iklan**: Dinyatakan **"Tidak, aplikasi saya tidak berisi iklan"**.
- [ ] **Mekanisme Penghapusan Akun**: Tombol hapus akun in-app tersedia, formulir web `https://maripartner.id/delete-account` siap di-hosting.

---

## 💳 Bagian 4: Kepatuhan Pembayaran (Monetization & Play Billing)

- [x] **Pustaka Google Play Billing**: Menggunakan versi resmi terbaru `billing-ktx:7.1.1`.
- [x] **Verifikasi Sisi Server (Server-Authoritative)**: Edge Function `verify-play-purchase` memvalidasi token dan mencegah token reuse (*anti-replay*).
- [ ] **Produk Langganan di Play Console**: SKU `mari_pro_monthly` (Rp 29.000) dan `mari_pro_yearly` (Rp 290.000) terdaftar aktif.
- [ ] **Pengaturan License Testing**: Akun Gmail tester terdaftar di License Testing agar tidak dikenakan tagihan uang riil.
- [ ] **Service Account Google Play**: Service Account ditautkan ke Supabase Secrets Vault `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`.

---

## 👥 Bagian 5: Pengujian Tertutup (Closed Testing Track)

- [ ] Jalur Closed Testing dibuat di Play Console dengan rilis `1.7.0 (8) - Closed Beta`.
- [ ] Minimal 20 penguji terdaftar via email Google.
- [ ] Penguji telah menerima tautan opt-in dan mengunduh aplikasi via Play Store.
- [ ] Periode pengujian 14 hari penuh berjalan terus-menerus (wajib untuk akun pengembang personal).
- [ ] Formulir umpan balik penguji terkumpul dan dievaluasi.
- [ ] Tidak ada temuan bug P0 / crash fatal selama masa pengujian.

---

## 🛑 Bagian 6: Persetujuan Rilis Produksi (Production Sign-Off Gate)

- [ ] **Persetujuan Tertulis Pemilik Proyek**: Pemilik proyek memberikan izin eksplisit untuk mengajukan permohonan produksi (*Apply for production*).
- [ ] **Akses Produksi Disetujui Google Play**: Tim Google Play telah menyetujui permohonan akses produksi.
- [ ] **Strategi Staged Rollout Diterapkan**: Peluncuran awal dimulai dari persentase **20%** dengan pemantauan ketat di Android Vitals.
