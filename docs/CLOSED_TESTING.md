# 👥 CLOSED_TESTING.md — Panduan Lengkap Pengujian Tertutup Google Play (Closed Testing)

Dokumen ini adalah panduan operasional terpadu pelaksanaan **Pengujian Tertutup (Closed Testing)** untuk aplikasi **Mari Partner Fuel** di Google Play Console. Dokumen ini memadukan **Rencana Pengujian (Roadmap 14 Hari)**, **Daftar Periksa Pengujian (Checklist 6 Fase)**, dan **Formulir Umpan Balik Penguji (Feedback Template)**.

---

## 🏛️ 1. Regulasi Google Play: Akun Personal vs Akun Organisasi

Google Play menerapkan kebijakan pengujian yang berbeda berdasarkan jenis akun pengembang:

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                   PERSYARATAN PENGUJIAN TERTUTUP GOOGLE PLAY                           │
├───────────────────────────────────────────┬────────────────────────────────────────────┤
│ TIPE A: AKUN PERSONAL (Setelah Nov 2023)  │ TIPE B: AKUN ORGANISASI / PERUSAHAAN       │
├───────────────────────────────────────────┼────────────────────────────────────────────┤
│ • Minimal Penguji: **20 Orang Penguji**   │ • Minimal Penguji: Tidak dibatasi kaku     │
│ • Durasi Wajib: **Minimal 14 Hari Penuh** │ • Durasi Wajib: Fleksibel sesuai kesiapan  │
│ • Status Opt-in: Aktif terus menerus      │ • Kuesioner Evaluasi: Tidak diwajibkan     │
│ • Kuesioner Evaluasi: Wajib dijawab untuk │ • Akses Produksi: Langsung dapat diajukan  │
│   mengajukan akses Produksi               │   setelah closed testing selesai           │
└───────────────────────────────────────────┴────────────────────────────────────────────┘
```

> ⚠️ **Prinsip Kejujuran Mutlak**:
> Dilarang menggunakan bot atau penguji palsu. Pemilik proyek bertanggung jawab merekrut penguji nyata (mitra pengemudi, staf armada, rekan/keluarga) yang aktif menguji aplikasi di ponsel mereka masing-masing.

---

## 📅 2. Jadwal Pengujian 14 Hari (14-Day Testing Roadmap)

```text
[H-1 s.d. H-3]   Persiapan Akun & Rilis AAB ke Closed Track
      │
[Hari 1 - 3]     Opt-in Tester, Instalasi, Onboarding, Pembuatan Akun & Input Kendaraan
      │
[Hari 4 - 7]     Pencatatan Transaksi BBM Manual & Scan Struk Fisik SPBU (ML Kit)
      │
[Hari 8 - 10]    Eksplorasi Dashboard, Grafik Efisiensi km/liter, dan Ekspor Riwayat CSV
      │
[Hari 11 - 12]   Uji Jadwal Pengingat Servis, Ganti Oli, dan Notifikasi Lokal
      │
[Hari 13 - 14]   Uji Langganan Pro (License Testing) & Pengisian Formulir Umpan Balik
      │
[Hari 15]        Evaluasi Akhir, Pengajuan Akses Produksi (Apply for Production)
```

---

## ⚙️ 3. Pengaturan Closed Testing di Play Console

1. Buka **Google Play Console** -> Aplikasi **Mari Partner Fuel** -> **Testing** -> **Closed testing**.
2. Masuk ke track *Closed testing (Alpha)* -> Tab **Testers**:
   - Daftarkan minimal **20 alamat email Google/Gmail** penguji.
   - Sediakan tautan formulir umpan balik atau email `support@maripartner.id`.
3. Klik **Releases** -> **Create new release**:
   - Unggah artefak rilis: `app/build/outputs/bundle/release/app-release.aab` (`7.57 MB`).
   - Masukkan Catatan Rilis (*Release Notes*) dalam bahasa Indonesia.
   - Simpan dan mulai peluncuran ke Closed testing.
4. Salin dan bagikan **Tautan Gabung Pengujian (Opt-in Links)**:
   - *Join on Android*: `https://play.google.com/store/apps/details?id=id.maripartner.fuel`
   - *Join on the web*: `https://play.google.com/apps/testing/id.maripartner.fuel`

---

## ✅ 4. Daftar Periksa Pengujian Tertutup (6 Fase)

### FASE 1: Persiapan Rilis (Release Team)
- [ ] Berkas `app-release.aab` (`7.57 MB`) telah lulus 137 unit test 100%.
- [ ] Minimal 20 email penguji terdaftar di Play Console.
- [ ] License Testing dikonfigurasi aktif agar tester tidak dikenakan biaya nyata.
- [ ] Tautan opt-in dibagikan ke seluruh penguji.

### FASE 2: Onboarding Penguji (Testers)
- [ ] Penguji membuka tautan opt-in dan menekan "Become a tester".
- [ ] Penguji mengunduh aplikasi dari Google Play Store dan membukanya tanpa crash.
- [ ] Penguji menjaga aplikasi tetap terpasang selama minimal 14 hari berturut-turut.

### FASE 3: Skenario Pengujian Fungsional
- [ ] **Pendaftaran & Login**: Registrasi akun baru dan pemulihan sesi berhasil.
- [ ] **Manajemen Kendaraan**: Tambah motor/mobil, pelat nomor, dan odometer awal.
- [ ] **Input Transaksi Manual**: Input nominal Rupiah otomatis dan kalkulasi harga/liter.
- [ ] **Scan Struk Kamera (ML Kit)**: Pemindaian struk SPBU fisik berjalan lancar di perangkat.
- [ ] **Peninjauan Struk (Review)**: Koreksi angka sebelum konfirmasi simpan transaksi.
- [ ] **Dashboard & Finansial**: Ringkasan pengeluaran bulanan dan rata-rata harga/liter akurat.
- [ ] **Grafik Efisiensi km/liter**: Konsumsi bahan bakar km/liter terhitung otomatis.
- [ ] **Ekspor CSV**: Mengunduh laporan CSV standar RFC 4180 dengan UTF-8 BOM.
- [ ] **Pengingat Servis**: Jadwal servis dan ganti oli memicu notifikasi lokal.
- [ ] **Uji Langganan Play Billing**: Checkout Pro via License Test card sukses mengaktifkan Pro.

### FASE 4: Pengujian Kasus Batas & Ketahanan
- [ ] **Offline Mode**: Buka aplikasi dalam mode pesawat; form manual tetap dapat diisi tanpa crash.
- [ ] **Rotasi Layar**: Putar layar ke landscape; data form tidak hilang (*state preserved*).
- [ ] **Dark & Light Mode**: Tampilan kontras dan terbaca jelas pada kedua tema.

### FASE 5: Bug Triage & Pembaruan Build
- [ ] Kumpulkan laporan kendala harian. Jika ada bug P0, rilis build update `versionCode = 9`.

### FASE 6: Kriteria Kelulusan (Exit Criteria)
- [ ] 20 penguji opt-in aktif selama 14 hari penuh.
- [ ] Crash-free sessions di Android Vitals **> 99.5%**.
- [ ] Tombol **Apply for production** di Play Console telah aktif.

---

## 📋 5. Formulir Umpan Balik Penguji (Tester Feedback Form)

Salin formulir di bawah ini ke Google Forms atau bagikan kepada penguji:

```text
FORMULIR EVALUASI PENGUJI TERTUTUP — MARI PARTNER FUEL

1. Informasi Penguji:
   - Nama / Inisial: ____________________________________
   - Email Akun Google: _________________________________
   - Tipe Ponsel & Versi Android: _______________________

2. Penilaian Kemudahan Fitur (Skala 1 - 5):
   [ ] Registrasi & Login
   [ ] Tambah Kendaraan
   [ ] Input Transaksi BBM Manual
   [ ] Scan Struk SPBU Kamera (OCR)
   [ ] Peninjauan Angka pada Layar Review
   [ ] Dashboard Finansial & Grafik km/liter
   [ ] Ekspor Laporan CSV (Excel)
   [ ] Pengingat Servis & Ganti Oli
   [ ] Kecepatan & Responsivitas Aplikasi

3. Laporan Kendala (Jika Ada Bug/Crash):
   - Menu tempat terjadinya kendala: ____________________
   - Langkah-langkah memunculkan masalah: _______________
   - Hasil yang terjadi vs seharusnya: __________________

4. Saran & Masukan Peningkatan:
   - Hal yang paling disukai: ___________________________
   - Fitur yang perlu disempurnakan: ____________________
```

---

## 📝 6. Panduan Menjawab Kuesioner "Apply for Production"

Setelah 14 hari closed testing selesai, pemilik proyek wajib mengisi 3 pertanyaan evaluasi di Play Console:

1. **Bagaimana Anda merekrut penguji tertutup Anda?**
   - *Jawaban*: Penguji direkrut dari komunitas mitra pengemudi lokal, staf armada UMKM, dan tim internal yang memiliki kendaraan aktif harian.
2. **Bagaimana para penguji berinteraksi dengan aplikasi Anda?**
   - *Jawaban*: Penguji menggunakan aplikasi secara konsisten selama 14 hari untuk mencatat transaksi BBM riil di SPBU, menguji pemindaian kamera OCR struk fisik, mengevaluasi grafik konsumsi km/liter, dan menjadwalkan servis rutin.
3. **Umpan balik apa yang Anda terima dan perbaikan apa yang telah dilakukan?**
   - *Jawaban*: Penguji memberikan masukan terkait penyesuaian kontras teks pada mode malam dan penanganan pembulatan harga bensin pada struk SPBU tertentu, yang seluruhnya telah diperbaiki pada rilis update pengujian.
