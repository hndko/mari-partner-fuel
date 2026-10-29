# 02. Buku Panduan Pengguna & Manual Book Lengkap

Selamat datang di Buku Panduan Pengguna **Mari Partner Fuel** (*AI-Powered Fuel Expense Tracker for Android*). Dokumen ini disusun untuk memudahkan pengguna, pengemudi, staf operasional armada, dan penguji fitur dalam memanfaatkan seluruh kemampuan aplikasi secara maksimal.

---

## 🌟 1. Profil Singkat & Manfaat Aplikasi

**Mari Partner Fuel** adalah asisten cerdas pencatatan pengeluaran bahan bakar kendaraan Anda. Aplikasi ini memadukan teknologi pembaca struk otomatis (OCR on-device Google ML Kit) dengan basis data cloud Supabase yang aman dan privat.

### Manfaat Utama:
- ⚡ **Bebas Ketik Manual**: Cukup foto struk SPBU (Pertamina, Shell, BP-AKR, Vivo), data otomatis terekstraksi.
- 🔍 **Koreksi Transparan**: Pengguna selalu memegang kendali penuh untuk memeriksa hasil sebelum data disimpan.
- 🚗 **Multi-Kendaraan**: Catat pengeluaran untuk sepeda motor, mobil pribadi, hingga kendaraan operasional kantor.
- 📈 **Analitik Akurat**: Mengetahui total biaya per bulan dan rata-rata tertimbang harga per liter.
- 🔒 **Data Aman & Privat**: Struk tersimpan di private cloud, bebas akses dari akun lain.

---

## 👥 2. Matriks Fitur: Free Tier vs Langganan Pro

| Fitur | Paket Free (Gratis) | Paket Pro (Berlangganan) |
| :--- | :---: | :---: |
| **Pencatatan Transaksi Manual** | ✅ Tersedia | ✅ Tersedia |
| **Scan Struk OCR Lokal (Google ML Kit)** | ✅ Tanpa Batas | ✅ Tanpa Batas |
| **Bantuan Ekstraksi Cloud AI (Struk Pudar)** | ⚡ Kuota Dasar Bulanan | ⚡ Kuota Prioritas Besar |
| **Maksimal Kendaraan Aktif** | Hingga 2 Kendaraan | Tanpa Batas |
| **Riwayat Pengeluaran & Pencarian** | ✅ Lengkap | ✅ Lengkap |
| **Analitik Konsumsi km/Liter** | ✅ Dasar | ✅ Lanjutan + Tren Antarbulan |
| **Ekspor Laporan Transaksi CSV** | ✅ Standar | ✅ Lengkap (Semua Periode) |
| **Penyimpanan Struk Cloud Privat** | ✅ Tersedia | ✅ Tersedia |

---

## 📱 3. Panduan Penggunaan Modul Langkah Demi Langkah

### 1. Masuk & Pendaftaran Akun
1. Buka aplikasi **Mari Partner Fuel**.
2. Masukkan alamat email dan kata sandi Anda, lalu tekan **Masuk**.
3. Atau tekan **Masuk dengan Google** untuk masuk secara instan dan aman menggunakan akun Google di perangkat Anda.
4. Jika belum memiliki akun, tekan tautan **Daftar Sekarang**.

---

### 2. Beranda (Dashboard Utama)
Setelah login, Anda langsung diarahkan ke layar Beranda:
- **Kartu Ringkasan Bulanan**: Menampilkan total nominal Rupiah yang dihabiskan pada bulan berjalan.
- **Indikator KPI**: Menampilkan total liter yang dibeli, rata-rata biaya per liter (rata-rata tertimbang), dan frekuensi pengisian.
- **Tombol Cepat**:
  - **Scan Struk** (Tombol Utama Teal): Membuka alur pemindaian foto struk.
  - **Manual** (Tombol Sekunder): Membuka formulir pengisian manual.
- **Transaksi Terbaru**: Menampilkan 3-5 catatan pengisian bensin terkini.

---

### 3. Alur Pemindaian Struk BBM (OCR Hybrid)
1. Pada Beranda atau Riwayat, tekan tombol **Scan Struk**.
2. Pilih sumber gambar:
   - **Kamera**: Arahkan lensa tegak lurus ke struk SPBU di tempat berpenerangan cukup.
   - **Galeri**: Pilih foto struk yang sebelumnya telah Anda foto.
3. Sistem secara otomatis menjalankan **Google ML Kit OCR** pada perangkat:
   - Mengekstrak tanggal/jam transaksi.
   - Mengekstrak nama SPBU (misal: SPBU Pertamina 31.xxx).
   - Mengekstrak jenis bahan bakar (Pertalite, Pertamax, Solar, Shell Super, dll.).
   - Mengekstrak volume pembelian (Liter).
   - Mengekstrak total pembayaran (Rupiah).
4. **Layar Review Hasil Scan (Wajib)**:
   - Data hasil pembacaan disajikan pada form formulir yang dapat diedit.
   - Field yang meragukan/ambigu akan diberi tanda perhatian.
   - Pengguna dapat memperbaiki angka bila struk terlipat atau pudar.
5. Pilih kendaraan yang diisi.
6. Masukkan odometer speedometer kendaraan saat ini (opsional untuk menghitung km/liter).
7. Tekan **Konfirmasi & Simpan Transaksi**. Data langsung tersimpan di cloud Supabase.

---

### 4. Input Transaksi Manual (Tanpa Struk)
Gunakan fitur ini jika SPBU kehabisan kertas struk atau Anda membeli bensin eceran:
1. Tekan tombol **Manual** pada Beranda.
2. Isi formulir:
   - Pilih Kendaraan.
   - Tanggal & Waktu Pengisian.
   - Jenis Bahan Bakar.
   - Volume (Liter) atau Total Biaya (Rupiah) — sistem akan menghitung harga per liter otomatis.
   - Odometer saat ini.
3. Tekan **Simpan Transaksi**.

---

### 5. Riwayat & Pencarian Transaksi
1. Akses tab **Riwayat** dari bilah navigasi bawah.
2. Anda dapat melihat lini masa pengeluaran BBM yang tersusun rapi dari yang terbaru.
3. Gunakan filter:
   - Filter berdasarkan rentang tanggal (Bulan ini, 3 Bulan Terakhir, Tahun Ini).
   - Filter berdasarkan kendaraan tertentu (Motor A vs Mobil B).
   - Pencarian real-time berdasarkan nama SPBU.
4. Tekan salah satu kartu transaksi untuk melihat detail lengkap dan membuka foto struk asli resolusi tinggi melalui tautan privat.

---

### 6. Analitik & Efisiensi Bahan Bakar
1. Akses tab **Analitik** dari bilah navigasi bawah.
2. Layar ini menampilkan:
   - **Grafik Pengeluaran Mingguan / Bulanan**.
   - **Proporsi Pengeluaran per Jenis BBM** (misal: 70% Pertamax, 30% Pertalite).
   - **Efisiensi Konsumsi (km/Liter)**: Dihitung secara matematis apabila Anda mengisi angka odometer sekurangnya pada dua pengisian berurutan.

---

### 7. Pengelolaan Kendaraan Saya
1. Akses tab **Kendaraan** dari bilah navigasi bawah.
2. Tekan tombol **(+)** untuk menambah kendaraan baru:
   - Nama Kendaraan (contoh: *Vario 160 Harian*, *Avanza Operasional*).
   - Tipe Kendaraan: Sepeda Motor, Mobil, atau Lainnya.
   - Nomor Polisi (disimpan aman di database privat Anda).
   - Jenis BBM Bawaan (Default Fuel Type).
   - Kapasitas Tangki (Liter).
3. Anda dapat mengarsipkan kendaraan lama yang sudah dijual. Transaksi historis kendaraan tersebut tetap tersimpan di riwayat Anda.

---

### 8. Upgrade ke Pro via QRISpy
1. Tekan chip **Pro** pada kanan atas Beranda atau buka menu **Profil > Tingkatkan ke Pro**.
2. Pilih paket langganan yang Anda inginkan:
   - **Paket Bulanan**: Rp15.000 / bulan.
   - **Paket Tahunan**: Rp120.000 / tahun (lebih hemat).
3. Tekan tombol **Lanjutkan ke Pembayaran**.
4. Sistem akan menampilkan kode **QRIS Dinamis resmi dari QRISpy**:
   - Buka aplikasi Mobile Banking (BCA, Mandiri, BRI, BNI) atau E-Wallet (GoPay, OVO, Dana, ShopeePay).
   - Scan kode QRIS yang tampil di layar.
   - Periksa nominal persis dan selesaikan transfer.
5. Setelah pembayaran berhasil, aplikasi secara otomatis mendeteksi konfirmasi dan mengaktifkan status **Pro** pada akun Anda.

---

## ❓ 4. Tanya Jawab & Penanganan Masalah (FAQ)

### Q: Apa yang terjadi jika foto struk saya pudar atau terpotong?
> **Jawab:** Sistem OCR lokal akan mengekstrak teks sebanyak mungkin. Layar Review akan meminta Anda melengkapi field yang hilang (misal: mengetik nominal yang buram). Anda tidak perlu mengulang foto jika hanya satu angka yang tidak terbaca.

### Q: Apakah struk foto saya bisa dilihat oleh pengguna lain?
> **Jawab:** **Sama sekali tidak bisa.** Foto struk tersimpan dalam storage privat Supabase dengan Row Level Security. Tautan gambar hanya dibuat sementara (signed URL) khusus untuk sesi login Anda.

### Q: Apa yang terjadi jika langganan Pro saya berakhir?
> **Jawab:** Seluruh riwayat pengeluaran, foto struk, dan data kendaraan Anda **TIDAK AKAN HILANG ATAU TERKUNCI**. Akun Anda hanya kembali ke batas kuota Free Tier untuk transaksi baru berikutnya.
