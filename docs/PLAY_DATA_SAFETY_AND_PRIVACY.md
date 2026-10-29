# 🔒 PLAY_DATA_SAFETY_AND_PRIVACY.md — Deklarasi Data Safety, Kebijakan Privasi & Petunjuk Reviewer

Dokumen ini memuat audit aliran data pengguna (*data flow audit*), draf deklarasi formulir **Keamanan Data (Data Safety)** di Google Play Console, draf resmi **Kebijakan Privasi (Privacy Policy)**, serta petunjuk akses aplikasi (*App Access Instructions*) untuk tim peninjau Google Play (*App Review Team*).

---

## 🧭 1. Audit Aliran Data Aktual (Actual Data Flow Audit)

| Kategori Data | Jenis Data Spesifik | Tujuan Pengumpulan | Dibagikan ke Pihak Ketiga? | Diambil Secara Otomatis? | Disimpan / Dihapus |
|---|---|---|:---:|:---:|---|
| **Informasi Pribadi** | Alamat Email | Autentikasi akun & pemulihan sesi | **TIDAK** | Input sadar pengguna | Disimpan di Supabase Auth; dihapus permanen saat akun dihapus |
| **Informasi Keuangan** | Riwayat Transaksi BBM, Nominal Rupiah, Volume Liter | Fungsionalitas inti aplikasi (pelacakan pengeluaran & analitik) | **TIDAK** | Input manual atau scan struk terkonfirmasi | Disimpan di PostgreSQL terenkripsi; dihapus saat akun dihapus |
| **Foto & Video** | Foto Struk SPBU | Pembacaan teks struk (OCR) & arsip digital pengguna | **TIDAK** | Pengguna mengambil foto via kamera atau galeri | Disimpan di Private Storage Supabase; dihapus saat transaksi/akun dihapus |
| **Info Kendaraan** | Nama, jenis, odometer kilometer, pelat nomor | Analisis konsumsi bbm (km/liter) & pengingat servis | **TIDAK** | Input sadar pengguna | Disimpan di PostgreSQL; dihapus saat akun dihapus |
| **Performa & Diagnostik** | Jejak crash, ANR, waktu muat aplikasi | Peningkatan stabilitas dan perbaikan bug | **TIDAK** | Bawaan Google Play Vitals | Dikelola oleh Google Play Console (agregat anonim) |

---

## 📋 2. Panduan Pengisian Formulir Keamanan Data (Play Console Data Safety Form)

Saat mengisi kuesioner **Keamanan Data (Data Safety)** di Google Play Console, pilih opsi berikut secara akurat:

### A. Gambaran Umum Keamanan (Security Overview)
- Apakah aplikasi Anda mengumpulkan atau membagikan salah satu jenis data pengguna yang diwajibkan? -> **Ya**.
- Apakah semua data pengguna yang dikumpulkan oleh aplikasi Anda dienkripsi saat transit? -> **Ya** *(Seluruh komunikasi menggunakan HTTPS / TLS 1.3)*.
- Apakah Anda menyediakan cara bagi pengguna untuk meminta agar data mereka dihapus? -> **Ya** *(Tersedia tombol hapus akun di dalam aplikasi dan formulir permintaan web)*.

### B. Pembagian Data (Data Sharing)
- Apakah data pengguna dibagikan kepada perusahaan atau organisasi lain? -> **Tidak** *(Data tidak pernah dibagikan atau dijual kepada pihak ketiga)*.

### C. Pengumpulan Data (Data Collection)
Centang jenis data berikut:
1. **Info Pribadi (Personal Info)**:
   - *Alamat email*: Dikumpulkan untuk **Fungsionalitas Aplikasi** dan **Pengelolaan Akun**. Bersifat wajib untuk login.
2. **Info Finansial (Financial Info)**:
   - *Riwayat pembelian / Pengeluaran BBM*: Dikumpulkan untuk **Fungsionalitas Aplikasi**.
3. **Foto dan Video (Photos and Videos)**:
   - *Foto (Struk)*: Dikumpulkan untuk **Fungsionalitas Aplikasi**.
4. **Info dan Performa Aplikasi (App Info and Performance)**:
   - *Log error / Crash logs*: Dikumpulkan untuk **Analisis / Diagnostik**.

---

## 📄 3. Draf Kebijakan Privasi (Privacy Policy — Bahasa Indonesia)

```text
KEBIJAKAN PRIVASI MARI PARTNER FUEL
Terakhir Diperbarui: 29 Oktober 2026

Mari Partner ("kami") berkomitmen untuk melindungi dan menghormati privasi pengguna ("Anda") aplikasi Mari Partner Fuel. Kebijakan Privasi ini menjelaskan bagaimana kami mengumpulkan, menggunakan, menyimpan, dan melindungi informasi pribadi Anda saat menggunakan aplikasi Mari Partner Fuel.

1. INFORMASI YANG KAMI KUMPULKAN
Kami hanya mengumpulkan informasi yang diperlukan untuk menyediakan layanan pelacakan pengeluaran BBM dan perawatan kendaraan:
a. Informasi Akun: Alamat email dan kredensial login terenkripsi untuk mengidentifikasi akun Anda secara unik.
b. Data Kendaraan & Pengeluaran: Nama kendaraan, tipe, nomor odometer, tanggal pengisian, volume liter, jenis bahan bakar, nama SPBU, dan nominal biaya pengisian BBM.
c. Foto Struk: Gambar struk fisik yang Anda ambil melalui kamera atau unggah dari galeri untuk diproses oleh teknologi pembaca teks (OCR).
d. Data Teknis: Log diagnostik kerusakan aplikasi (crash reports) yang disediakan secara anonim oleh sistem Google Play untuk pemeliharaan mutu aplikasi.

2. PENGGUNAAN INFORMASI
Informasi yang kami kumpulkan digunakan secara eksklusif untuk:
- Mengoperasikan fitur utama aplikasi (menghitung total pengeluaran bulanan, konsumsi km/liter, dan menjadwalkan pengingat servis).
- Mengekstrak data transaksi dari struk fisik secara lokal di perangkat Anda (on-device) melalui Google ML Kit, dengan opsi bantuan cloud AI hanya atas persetujuan Anda.
- Memproses dan memvalidasi hak langganan paket Pro melalui Google Play Billing.

3. PEMROSESAN STRUK & TEKNOLOGI AI
Aplikasi mengutamakan pemrosesan teks struk langsung di dalam ponsel cerdas Anda (on-device processing) menggunakan pustaka Google ML Kit tanpa memerlukan transfer data foto keluar perangkat. Jika Anda memilih fitur "Optimalkan dengan AI Cloud", hanya teks struk yang diekstrak yang diproses oleh serverless backend kami untuk meningkatkan akurasi. Kami TIDAK mengirimkan identitas pribadi atau foto berwajah ke pihak ketiga.

4. KEAMANAN & PENYIMPANAN DATA
- Enkripsi Data Transit: Seluruh pertukaran data antara aplikasi dan basis data backend dienkripsi menggunakan protokol aman TLS 1.3 / HTTPS.
- Penyimpanan Aman: Data transaksi disimpan dalam basis data PostgreSQL berstandar Row Level Security (RLS) di mana hanya akun Anda yang memiliki akses ke data Anda sendiri. Foto struk disimpan dalam penyimpanan privat dengan tautan bertanda tangan digital berbatas waktu (signed URL).
- Kami TIDAK MENJUAL atau membagikan data pribadi maupun data finansial Anda kepada pengiklan atau pihak ketiga mana pun.

5. HAK PENGGUNA & PENGHAPUSAN AKUN
Anda memiliki kontrol penuh atas data Anda:
- Mengubah & Menghapus Transaksi: Anda dapat menyunting atau menghapus catatan kendaraan dan transaksi kapan saja langsung dari aplikasi.
- Penghapusan Akun Menyeluruh: Anda dapat menghapus akun dan seluruh data terkait secara permanen melalui menu Profil -> "Hapus Akun", atau dengan mengajukan permohonan melalui formulir web di https://maripartner.id/delete-account. Saat akun dihapus, seluruh riwayat transaksi, data kendaraan, dan foto struk akan dihapus permanen dari server kami dalam waktu 1x24 jam.

6. PERUBAHAN KEBIJAKAN PRIVASI
Kami dapat memperbarui Kebijakan Privasi ini dari waktu ke waktu. Pembaruan akan diberitahukan melalui aplikasi atau halaman situs resmi kami.

7. KONTAK KAMI
Jika Anda memiliki pertanyaan tentang Kebijakan Privasi ini atau pengelolaan data Anda, silakan hubungi tim kami di:
Email: support@maripartner.id
Website: https://maripartner.id
```

---

## 🔑 4. Petunjuk Akses Aplikasi untuk Tim Reviewer Google Play (App Access Instructions)

Jika tim peninjau Google Play meminta kredensial akun uji untuk memverifikasi fitur di balik layar login (*App Access*):

- **Status Akses**: **Beberapa atau semua bagian aplikasi dibatasi** *(Aplikasi memerlukan login untuk mengakses Beranda, Riwayat, dan Scan Struk)*.
- **Petunjuk untuk Reviewer (App Access Instructions)**:
  ```text
  Aplikasi Mari Partner Fuel menyediakan alur pencatatan pengeluaran bahan bakar kendaraan. Untuk menguji seluruh fungsionalitas aplikasi:

  1. Buka aplikasi, Anda akan berada di layar Masuk (Login).
  2. Masukkan kredensial akun peninjauan resmi:
     - Email: reviewer@maripartner.id
     - Password: [Disediakan oleh pemilik proyek di Play Console Credentials]
  3. Atau, reviewer dapat membuat akun baru secara instan dengan mengetuk "Daftar Akun Baru" menggunakan email uji apa saja tanpa memerlukan verifikasi SMS.
  4. Akun uji telah dilengkapi dengan 1 kendaraan contoh ("Toyota Avanza") dan 2 transaksi BBM contoh untuk memeriksa Dashboard Finansial dan grafik efisiensi BBM.
  5. Untuk menguji fitur kamera OCR: izinkan akses Kamera dan arahkan ke struk SPBU apa saja atau ketuk tombol "Input Manual" untuk mencoba pencatatan manual.
  6. Untuk menguji fitur langganan Pro: gunakan Google Play License Tester Account untuk memicu alur Play Billing Sandbox tanpa biaya riil.
  ```

> ⚠️ **Catatan Penting**: Kredensial akun uji di atas hanya diinput secara aman pada kolom *App Access* di Google Play Console dan tidak pernah di-commit ke Git repositori publik.
