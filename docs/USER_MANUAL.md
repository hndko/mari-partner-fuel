# 📖 USER_MANUAL.md — Buku Panduan Pengguna Lengkap (End-User Manual)

Selamat datang di Buku Panduan Pengguna **Mari Partner Fuel** (*AI-Powered Fuel Expense Tracker for Android*). Dokumen ini disusun untuk memudahkan pengguna, pengemudi transportasi online, staf operasional armada, dan penguji fitur dalam memanfaatkan seluruh kemampuan aplikasi secara maksimal.

---

## 🌟 1. Profil Singkat & Manfaat Aplikasi

**Mari Partner Fuel** adalah asisten cerdas pencatatan pengeluaran bahan bakar kendaraan Anda. Aplikasi ini memadukan teknologi pembaca struk otomatis (OCR on-device Google ML Kit) dengan basis data cloud Supabase yang aman dan privat.

### Manfaat Utama:
- ⚡ **Bebas Ketik Manual**: Cukup foto struk SPBU (Pertamina, Shell, BP-AKR, Vivo), data otomatis terekstraksi.
- 🔍 **Koreksi Transparan (Human-in-the-Loop)**: Pengguna selalu memegang kendali penuh untuk memeriksa dan mengoreksi hasil sebelum data disimpan.
- 🚗 **Multi-Kendaraan**: Catat pengeluaran untuk sepeda motor, mobil pribadi, hingga kendaraan operasional armada kantor.
- 📈 **Analitik Akurat**: Mengetahui total biaya per bulan, konsumsi km/liter, dan rata-rata tertimbang harga per liter.
- 🔒 **Data Aman & Privat**: Struk tersimpan di private cloud storage, bebas akses dari akun lain.

---

## 👥 2. Matriks Fitur: Free Tier vs Langganan Pro

| Fitur | Paket Mari Free (Gratis) | Paket Mari Pro (Berlangganan) |
| :--- | :---: | :---: |
| **Pencatatan Transaksi Manual** | ✅ Tersedia | ✅ Tersedia |
| **Scan Struk OCR Lokal (Google ML Kit)** | ✅ Tanpa Batas (Offline) | ✅ Tanpa Batas (Offline) |
| **Bantuan Ekstraksi Cloud AI (Struk Pudar)** | ⚡ 5 Scan / Bulan | ⚡ 100 Scan / Bulan |
| **Maksimal Kendaraan Aktif** | Maksimal 2 Kendaraan | **Tanpa Batas** |
| **Riwayat Pengeluaran & Pencarian** | ✅ Lengkap | ✅ Lengkap |
| **Analitik Konsumsi km/Liter** | ✅ Dasar | ✅ Lanjutan + Tren Antarbulan |
| **Ekspor Laporan Transaksi CSV** | ✅ Standar RFC 4180 | ✅ Lengkap (Semua Periode) |
| **Penyimpanan Struk Cloud Privat** | ✅ Tersedia | ✅ Tersedia |

---

## 📱 3. Panduan Penggunaan Modul Langkah Demi Langkah

### 1. Masuk & Pendaftaran Akun
1. Buka aplikasi **Mari Partner Fuel**.
2. Masukkan alamat email dan kata sandi Anda, lalu tekan **Masuk**.
3. Jika belum memiliki akun, tekan tautan **Daftar Akun Baru**.

### 2. Beranda & Dashboard Finansial
- **Kartu Ringkasan Bulanan**: Menampilkan total nominal Rupiah yang dihabiskan pada bulan berjalan.
- **Indikator KPI**: Menampilkan total volume liter yang dibeli, rata-rata tertimbang harga per liter, dan frekuensi pengisian.
- **Daftar Transaksi Terkini**: Menampilkan 5 transaksi terakhir untuk pengecekan cepat.

### 3. Pemindaian Struk SPBU (Scan Struk OCR)
1. Tekan tombol **Scan Struk** di Beranda atau tombol kamera.
2. Arahkan kamera ke struk fisik SPBU Anda, lalu tekan tombol ambil foto (atau pilih dari galeri).
3. Google ML Kit di perangkat Anda akan membaca teks struk secara lokal tanpa internet.
4. **Layar Peninjauan (Review)**: Periksa angka yang terdeteksi (nama SPBU, tanggal, volume liter, nominal total).
5. Jika ada perbedaan, Anda dapat mengoreksi angka secara manual di layar.
6. Tekan **Konfirmasi & Simpan Transaksi** untuk mencatat transaksi.

### 4. Input Transaksi BBM Manual
1. Jika tidak memiliki struk fisik, tekan tombol **Input Manual**.
2. Pilih kendaraan yang diisi bahan bakar.
3. Masukkan total nominal Rupiah (otomatis diformat pemisah ribuan).
4. Masukkan volume liter dan angka odometer kilometer saat pengisian.
5. Tekan **Simpan Transaksi**.

### 5. Manajemen Multi-Kendaraan
1. Masuk ke tab **Kendaraan**.
2. Tekan tombol **+ Tambah Kendaraan**.
3. Masukkan nama kendaraan, jenis (Motor / Mobil / Lainnya), merek, model, tahun pembuatan, plat nomor, dan odometer awal.
4. Tekan **Simpan Kendaraan**.

### 6. Analitik Finansial & Konsumsi BBM (km/liter)
1. Buka tab **Analitik**.
2. Pilih filter rentang waktu: Bulan Ini, Bulan Lalu, 3 Bulan, 6 Bulan, atau Tahun Ini.
3. Lihat grafik tren pengeluaran time-series dan distribusi biaya per kendaraan.
4. Periksa estimasi efisiensi konsumsi bahan bakar (km/liter) yang dihitung dari selisih odometer pengisian berurutan.

### 7. Ekspor Riwayat Transaksi ke CSV
1. Masuk ke tab **Riwayat Transaksi**.
2. Tekan ikon **Download / Ekspor CSV** di bilah atas.
3. Pilih folder penyimpanan di perangkat Anda.
4. File CSV standar RFC 4180 (dilengkapi UTF-8 BOM) dapat langsung dibuka dengan rapi di Microsoft Excel atau Google Sheets.

### 8. Pengingat Servis & Ganti Oli Mesin
1. Buka menu **Pengingat**.
2. Tekan **+ Tambah Pengingat**.
3. Pilih jenis servis (Ganti Oli Mesin, Servis Berkala, Penggantian Rem, dll.).
4. Masukkan tanggal target dan/atau kilometer target odometer.
5. Sistem akan menjadwalkan notifikasi lokal otomatis saat jadwal tiba.

### 9. Pembelian Langganan Mari Pro
1. Buka menu **Profil** -> Tekan banner **Upgrade ke Mari Pro**.
2. Pilih paket: **Bulanan (Rp 29.000)** atau **Tahunan (Rp 290.000, hemat 17%)**.
3. Pada build Google Play Store, selesaikan pembayaran via **Google Play Billing** (metode pembayaran yang tersimpan di akun Google Anda).
4. Fitur Pro dan kuota 100 scan AI cloud akan langsung aktif seketika.
