# 👥 CLOSED_TESTING_PLAN.md — Rencana Pengujian Tertutup Google Play (Closed Testing Plan)

Dokumen ini adalah rencana operasional pelaksanaan **Pengujian Tertutup (Closed Testing)** untuk aplikasi **Mari Partner Fuel** di Google Play Console. Rencana ini disusun sesuai dengan regulasi resmi Google Play bagi pengembang Android.

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
> Dilarang menggunakan akun bot, penguji palsu, atau memanipulasi keikutsertaan tester. Pemilik proyek bertanggung jawab merekrut penguji manusia nyata (misal: mitra pengemudi, staf internal, keluarga/kolega) yang aktif menguji aplikasi di smartphone mereka masing-masing.

---

## 🎯 2. Sasaran & Tujuan Pengujian Tertutup

1. **Stabilitas Lintas Perangkat**: Menguji aplikasi pada berbagai merek ponsel (Samsung, Xiaomi/Redmi, Oppo, Vivo, Infinix, Pixel) dengan variasi versi Android 8.0 s.d. Android 15.
2. **Kinerja Kamera & OCR**: Memverifikasi pemindaian struk SPBU fisik dengan berbagai kondisi pencahayaan dan cetakan struk.
3. **Ketahanan Fitur Offline**: Memastikan aplikasi tidak crash saat jaringan lambat atau terputus di SPBU.
4. **Verifikasi Pembelian License Testing**: Memastikan alur langganan Pro Google Play Billing berjalan mulus tanpa error penagihan.
5. **Kesesuaian Regulasi**: Mengumpulkan umpan balik (*feedback*) nyata untuk menjawab 3 bagian kuesioner kelayakan produksi Google Play.

---

## 📅 3. Jadwal Pengujian 14 Hari (14-Day Testing Roadmap)

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

### Rincian Tugas Harian Penguji:
- **Hari 1–3**: Penguji membuka tautan opt-in, mengunduh aplikasi dari Play Store, mendaftar akun baru, dan menambahkan minimal 1 kendaraan pribadi/kantor.
- **Hari 4–7**: Penguji memasukkan minimal 2 transaksi BBM (1 manual, 1 hasil scan struk kamera).
- **Hari 8–10**: Penguji membuka layar Analitik, memeriksa keakuratan rata-rata harga/liter, dan mengunduh laporan CSV.
- **Hari 11–12**: Penguji mengatur pengingat ganti oli dan memverifikasi notifikasi muncul sesuai jadwal.
- **Hari 13–14**: Penguji membuka katalog Pro, mencoba checkout Pro via Play Billing test card, dan mengirimkan umpan balik akhir.

---

## ⚙️ 4. Langkah Setup Closed Testing di Google Play Console

1. Buka **Google Play Console** -> Pilih aplikasi **Mari Partner Fuel**.
2. Masuk ke menu **Testing** -> **Closed testing**.
3. Klik **Manage track** pada jalur *Closed testing (Alpha)*.
4. Buka tab **Testers**:
   - Buat daftar email baru (*Email list*) atau hubungkan Google Group penguji (misal: `mpf-testers@googlegroups.com`).
   - Masukkan minimal **20 alamat email Google/Gmail** penguji aktif.
   - Sediakan tautan formulir umpan balik penguji (misal: Google Form atau email `support@maripartner.id`).
5. Klik **Releases** -> **Create new release**:
   - Unggah artefak bundle release resmi: `app-release.aab` (`7.57 MB`).
   - Berikan nama rilis: `1.7.0 (8) - Closed Beta`.
   - Masukkan Catatan Rilis (*Release Notes*) dalam bahasa Indonesia.
   - Simpan dan klik **Review release** -> **Start rollout to Closed testing**.
6. Salin **Tautan Gabung Pengujian (Opt-in Links)**:
   - *Join on the web*: `https://play.google.com/apps/testing/id.maripartner.fuel`
   - *Join on Android*: `https://play.google.com/store/apps/details?id=id.maripartner.fuel`
   - Bagikan tautan tersebut kepada seluruh penguji terdaftar.

---

## 📝 5. Persiapan Pengajuan Akses Produksi (Apply for Production Questions)

Setelah 14 hari closed testing selesai dengan minimal 20 penguji terverifikasi, Play Console akan membuka tombol **Apply for production**. Pemilik proyek wajib menjawab 3 kelompok pertanyaan berikut:

1. **Bagaimana Anda merekrut penguji tertutup Anda?**
   - *Jawaban*: Penguji direkrut dari komunitas mitra pengemudi lokal, staf operasional armada UMKM, dan tim internal yang memiliki kendaraan aktif sehari-hari.
2. **Bagaimana para penguji berinteraksi dengan aplikasi Anda?**
   - *Jawaban*: Penguji menggunakan aplikasi secara konsisten selama 14 hari untuk mencatat pengeluaran BBM riil di SPBU, menguji pemindaian struk kamera dengan berbagai jenis struk, mengecek analitik konsumsi km/liter, dan menjadwalkan servis rutin.
3. **Umpan balik apa yang Anda terima dan perbaikan apa yang telah Anda lakukan?**
   - *Jawaban*: Penguji memberikan masukan terkait penyesuaian kontras teks pada mode malam, penyempurnaan toleransi pembulatan harga bensin pada struk SPBU tertentu, dan optimasi pemformatan ribuan Rupiah, yang seluruhnya telah diperbaiki pada rilis update pengujian.
