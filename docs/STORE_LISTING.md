# 🏪 STORE_LISTING.md — Metadata Toko, Keamanan Data & Kebijakan Privasi Google Play

Dokumen ini adalah acuan terpadu untuk pengaturan **Google Play Store Listing**, pengisian formulir **Keamanan Data (Data Safety)**, draf resmi **Kebijakan Privasi (Privacy Policy)**, dan petunjuk akses reviewer (*App Access*) untuk aplikasi **Mari Partner Fuel**.

---

## 📱 1. Metadata Utama Aplikasi (Store Listing)

| Elemen Listing | Batas Karakter | Draf Konten Resmi Mari Partner Fuel |
|---|:---:|---|
| **App Name** | Maks 30 Karakter | **Mari Partner Fuel** *(17 Karakter)* |
| **Short Description** | Maks 80 Karakter | **Catat pengeluaran BBM & servis kendaraan dengan scan struk OCR cepat & akurat.** *(79 Karakter)* |
| **Kategori Utama** | Pilihan Konsol | **Otomotif & Kendaraan** (*Auto & Vehicles*) |
| **Kategori Sekunder** | Pilihan Konsol | **Produktivitas** (*Productivity*) |
| **Tags / Label** | Maks 5 Tag | `Pengeluaran BBM`, `Otomotif`, `Servis Kendaraan`, `Efisiensi BBM`, `Catatan Finansial` |
| **Kontak Support Email**| Wajib | `support@maripartner.id` |
| **Website Resmi** | Opsional | `https://maripartner.id` |

---

## 📝 2. Deskripsi Lengkap (Full Description — Bahasa Indonesia)

```text
Kelola dan pantau seluruh pengeluaran bahan bakar kendaraan Anda dengan cerdas, cepat, dan transparan bersama Mari Partner Fuel!

Mari Partner Fuel dirancang khusus untuk pengendara motor pribadi, mobil keluarga, pengemudi transportasi online, hingga staf operasional armada UMKM yang menginginkan pencatatan pengeluaran BBM yang praktis tanpa repot menghitung manual.

Dengan teknologi Hybrid OCR (Google ML Kit on-device), Anda dapat memindai struk fisik SPBU secara instan langsung di ponsel tanpa memerlukan kuota internet. Seluruh data disinkronkan secara aman ke cloud dengan isolasi data terenkripsi.

✨ FITUR-FITUR UTAMA:

⚡ Scan Struk Cepat (Local-First OCR)
Pindai struk pengisian bensin di SPBU (Pertamina, Shell, BP, AKR, dll.) dalam hitungan detik menggunakan kamera. Sistem otomatis mendeteksi nama SPBU, tanggal pengisian, volume liter, jenis BBM, dan total biaya. 100% diproses langsung di perangkat Anda tanpa memotong kuota internet.

👁️ Peninjauan Transparan (Human-in-the-Loop)
Data hasil scan struk tidak pernah langsung disimpan otomatis tanpa persetujuan Anda. Anda selalu dapat memeriksa, menyesuaikan, dan mengoreksi angka nominal sebelum transaksi disimpan ke buku catatan.

✍️ Input Transaksi Manual Praktis
Tidak punya struk fisik? Catat pengeluaran BBM Anda kapan saja dengan formulir manual yang dilengkapi pemformat Rupiah otomatis, pencatat odometer (kilometer), dan kalkulasi harga per liter instan.

🚗 Manajemen Multi-Kendaraan
Kelola sepeda motor harian, mobil keluarga, maupun armada kantor dalam satu akun. Pantau riwayat konsumsi bahan bakar untuk masing-masing kendaraan secara terpisah dan rapi.

📊 Dashboard & Analitik Finansial
- Pantau total pengeluaran BBM bulanan dan total volume liter.
- Analisis rata-rata tertimbang biaya per liter secara akurat.
- Grafik efisiensi konsumsi bahan bakar (km/liter) berdasarkan catatan odometer berurutan.
- Identifikasi kendaraan dengan konsumsi pengeluaran tertinggi bulan ini.

📜 Riwayat Lengkap & Ekspor CSV
- Cari dan filter transaksi berdasarkan rentang tanggal, jenis kendaraan, atau nama SPBU secara real-time.
- Ekspor seluruh laporan riwayat transaksi ke format CSV standar (kompatibel penuh dengan Microsoft Excel dan Google Sheets) dilengkapi pengaman data spreadsheet injection.

🔔 Pengingat Servis & Perawatan Berkala
Catat jadwal servis rutin, ganti oli mesin, dan pengecekan rem. Dapatkan notifikasi pengingat tepat waktu saat tanggal target atau batas kilometer odometer kendaraan Anda tercapai.

💎 Pilihan Paket Sesuai Kebutuhan:
- Paket Mari Free: Kelola hingga 2 kendaraan aktif dengan fitur analitik lengkap dan 5 kuota pemindaian struk bantuan AI cloud setiap bulan.
- Paket Mari Pro: Nikmati kebebasan mengelola kendaraan tanpa batas dan 100 kuota bantuan AI cloud per bulan untuk struk buram/berkerut. Pembelian langganan resmi dan aman melalui Google Play Store.

🔒 Keamanan & Privasi Data Terjamin:
Data transaksi dan foto struk Anda tersimpan di private cloud storage dengan enkripsi tingkat tinggi. Foto struk hanya dapat diakses oleh Anda dan tidak pernah dibagikan kepada pihak ketiga.

Unduh Mari Partner Fuel sekarang dan kendalikan pengeluaran bahan bakar kendaraan Anda dengan lebih cerdas!
```

---

## 🎨 3. Daftar Periksa Aset Grafis (Graphic Assets)

- [ ] **Ikon Aplikasi (App Icon)**: 512 x 512 px, 32-bit PNG (maks 1 MB). Logo dispenser BBM *Petrol Teal* (`#006D77`) dengan aksen *Fuel Amber* (`#FFB703`).
- [ ] **Gambar Fitur (Feature Graphic)**: 1024 x 500 px, JPEG / PNG (maks 15 MB). Banner horizontal bertuliskan "Mari Partner Fuel".
- [ ] **Tangkapan Layar Ponsel (Phone Screenshots)**: Minimal 4 screenshot rasio 9:16 (1080 x 1920 px atau 1080 x 2400 px) dari antarmuka riil (Dashboard, Kamera OCR, Analitik km/liter, Kendaraan & Servis).

---

## 🔒 4. Panduan Isian Keamanan Data (Data Safety Form)

- **Apakah aplikasi mengumpulkan data?**: Ya.
- **Apakah data dienkripsi saat transit?**: Ya (HTTPS / TLS 1.3).
- **Apakah pengguna dapat meminta data dihapus?**: Ya (Tersedia tombol hapus akun di profil dan form web).
- **Apakah data dibagikan ke pihak ketiga?**: **TIDAK (0%)**.
- **Jenis data yang dikumpulkan**:
  1. *Info Pribadi*: Alamat Email (untuk autentikasi akun & fungsionalitas aplikasi).
  2. *Info Finansial*: Riwayat Pengeluaran BBM & Transaksi (untuk fitur inti aplikasi).
  3. *Foto/Video*: Foto Struk SPBU (untuk pemindaian OCR & arsip digital).
  4. *Info Performa*: Log Crash & Diagnostik (Google Play Android Vitals).

---

## 📄 5. Draf Kebijakan Privasi Resmi (Privacy Policy)

```text
KEBIJAKAN PRIVASI MARI PARTNER FUEL
Terakhir Diperbarui: 29 Oktober 2026

Mari Partner ("kami") berkomitmen untuk melindungi privasi pengguna aplikasi Mari Partner Fuel:
1. INFORMASI YANG DIKUMPULKAN: Alamat email akun, data transaksi BBM (tanggal, nominal, liter, jenis bahan bakar, SPBU), data kendaraan, dan foto struk yang diambil via kamera/galeri.
2. PENGGUNAAN INFORMASI: Informasi digunakan secara eksklusif untuk operasional fitur aplikasi (analitik pengeluaran, konsumsi km/liter, pengingat servis) dan ekstraksi OCR struk. Kami TIDAK MENJUAL atau membagikan data Anda kepada pihak ketiga.
3. PEMROSESAN STRUK: Pemrosesan teks struk diutamakan secara lokal di perangkat Anda (on-device) melalui Google ML Kit tanpa kuota internet. Pemrosesan cloud AI hanya berjalan atas persetujuan Anda.
4. KEAMANAN DATA: Komunikasi data dienkripsi penuh dengan TLS 1.3/HTTPS. Data tersimpan di PostgreSQL dengan Row Level Security (RLS) dan private storage signed URL (15 menit).
5. PENGHAPUSAN AKUN & DATA: Pengguna dapat menghapus akun dan seluruh datanya kapan saja secara mandiri melalui menu Profil -> Hapus Akun, atau melalui formulir web di https://maripartner.id/delete-account. Seluruh data transaksi, kendaraan, dan foto struk dihapus permanen dalam 1x24 jam.
6. KONTAK: support@maripartner.id | https://maripartner.id
```

---

## 🔑 6. Petunjuk Akses Aplikasi untuk Reviewer (App Access)

- **Status Akses**: Dibatasi (*Memerlukan akun login*).
- **Kredensial Akun Uji**:
  - Email: `reviewer@maripartner.id`
  - Password: `[PasswordUjiYangDikonfigurasiPemilik]`
- **Keterangan**: Reviewer juga dapat membuat akun baru secara instan via "Daftar Akun Baru". Akun uji telah dilengkapi data contoh kendaraan dan transaksi untuk memeriksa dashboard dan grafik analitik.
