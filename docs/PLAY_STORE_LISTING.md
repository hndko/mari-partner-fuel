# 🏪 PLAY_STORE_LISTING.md — Draft Metadata & Panduan Aset Google Play Store

Dokumen ini memuat draf resmi daftar toko (*Store Listing*) untuk aplikasi **Mari Partner Fuel** di Google Play Console. Seluruh teks disusun dalam bahasa Indonesia yang profesional, jelas, mematuhi panduan penulisan Google Play, dan **hanya mencantumkan fitur nyata** yang telah diimplementasikan pada versi `1.7.0`.

---

## 📱 1. Metadata Utama Aplikasi

| Elemen Listing | Batas Karakter | Draf Konten Resmi Mari Partner Fuel |
|---|:---:|---|
| **App Name** | Maks 30 Karakter | **Mari Partner Fuel** *(17 Karakter)* |
| **Short Description** | Maks 80 Karakter | **Catat pengeluaran BBM & servis kendaraan dengan scan struk OCR cepat & akurat.** *(79 Karakter)* |
| **Kategori Utama** | Pilihan Konsol | **Otomotif & Kendaraan** (*Auto & Vehicles*) |
| **Kategori Sekunder** | Pilihan Konsol | **Produktivitas** (*Productivity*) |
| **Tags / Label** | Maks 5 Tag | `Pengeluaran BBM`, `Otomotif`, `Servis Kendaraan`, `Efisiensi BBM`, `Catatan Finansial` |
| **Kontak Support Email**| Wajib | `support@maripartner.id` *(Dapat disesuaikan pemilik)* |
| **Website Resmi** | Opsional | `https://maripartner.id` *(Dapat disesuaikan pemilik)* |
| **Alamat Fisik** | Opsional/Badan | *Sesuai data legal entitas pengembang* |

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

## 🎨 3. Spesifikasi & Daftar Periksa Aset Grafis (Graphic Assets Checklist)

Seluruh aset grafis wajib disiapkan oleh desainer grafis/pemilik proyek sebelum rilis toko publik:

| No | Aset Grafis | Dimensi & Format Wajib | Keterangan & Panduan Desain | Status Ketersediaan |
|:--:|---|---|---|:---:|
| **1** | **Ikon Aplikasi (App Icon)** | 512 x 512 px, 32-bit PNG (maks 1 MB) | Ikon logo dispenser BBM *Petrol Teal* (`#006D77`) dengan aksen tetesan *Fuel Amber* (`#FFB703`). Latar belakang solid tanpa transparansi. | ⏳ PERLU DIBUAT DRAF |
| **2** | **Gambar Fitur (Feature Graphic)** | 1024 x 500 px, JPEG / 24-bit PNG (maks 15 MB) | Banner promosi horizontal di halaman Play Store. Menampilkan tipografi "Mari Partner Fuel" dan visual ponsel cerdas yang sedang memindai struk SPBU. | ⏳ PERLU DIBUAT DRAF |
| **3** | **Tangkapan Layar Ponsel (Phone Screenshots)** | Minimal 4 tangkapan layar, rasio 9:16 (Contoh: 1080 x 1920 px atau 1080 x 2400 px), PNG/JPEG | Menampilkan alur riil aplikasi yang telah diuji: <br>1. Dashboard Statistik & Ringkasan Pengeluaran<br>2. Layar Pemindaian Struk Kamera & Review OCR<br>3. Layar Analitik Grafik Konsumsi km/liter<br>4. Layar Manajemen Kendaraan & Pengingat Servis | ⏳ DIAMBIL DARI BUILD AKTUAL |
| **4** | **Tangkapan Layar Tablet 7" & 10"** | Rasio 16:10 / 16:9, min 1080 px | Opsional namun direkomendasikan agar aplikasi tampil prima di perangkat layar lebar/tablet. | ⏳ OPSIONAL |

> ⚠️ **Catatan Penting Google Play Policy**:
> Dilarang menempatkan teks promosi berlebihan pada screenshot (misal: "Aplikasi Terbaik No. 1", "Paling Murah", atau "Rating Bintang 5"). Screenshot wajib menggambarkan antarmuka pengguna riil dari aplikasi yang sedang berjalan.
