# ✅ CLOSED_TESTING_CHECKLIST.md — Daftar Periksa Pengujian Tertutup (Closed Testing Checklist)

Dokumen ini memuat daftar periksa (*checklist*) komprehensif bagi Tim Rilis (*Release Team*) dan Tim Penguji (*Testers*) selama pelaksanaan tahap Closed Testing aplikasi **Mari Partner Fuel**.

---

## 🚀 FASE 1: Persiapan Rilis Pengujian (Release Team)

- [ ] **Verifikasi Artefak Bundle**: Berkas `app-release.aab` (`7.57 MB`) telah dikompilasi dengan R8 minification dan berstatus 100% lulus 137 unit test.
- [ ] **Track Closed Testing Aktif**: Jalur pengujian tertutup telah dibuat di Google Play Console dengan nama rilis `1.7.0 (8) - Closed Beta`.
- [ ] **Daftar Email Penguji Terdaftar**: Minimal 20 email Google penguji telah dimasukkan ke dalam daftar penguji di Play Console.
- [ ] **License Testing Aktif**: Email penguji telah didaftarkan pada menu License Testing untuk pengujian pembelian Play Billing tanpa biaya riil.
- [ ] **Tautan Opt-in Dibagikan**: Tautan web opt-in (`https://play.google.com/apps/testing/id.maripartner.fuel`) telah dikirimkan ke seluruh penguji beserta panduan instalasi.
- [ ] **Kanal Umpan Balik Siap**: Google Form atau email dukungan `support@maripartner.id` telah siap menerima laporan kendala.

---

## 📱 FASE 2: Onboarding & Keikutsertaan Penguji (Testers)

- [ ] Penguji membuka tautan opt-in di browser ponsel dan menekan tombol **"Become a tester"**.
- [ ] Penguji membuka tautan Google Play Store dan berhasil mengunduh aplikasi Mari Partner Fuel.
- [ ] Penguji berhasil membuka aplikasi tanpa mengalami crash saat peluncuran awal (*cold start*).
- [ ] Penguji memastikan aplikasi tetap terpasang (*installed & opted-in*) selama **minimal 14 hari berturut-turut**.

---

## 🧪 FASE 3: Skenario Pengujian Fungsional Inti (Core Testing Scenarios)

| No | Modul / Skenario Pengujian | Hasil yang Diharapkan | Status |
|:--:|---|---|:---:|
| **1** | **Pendaftaran Akun Baru** | Pengguna mendaftar dengan email & password (min 6 karakter), langsung masuk ke aplikasi. | [ ] PASS / FAIL |
| **2** | **Masuk (Login) & Sesi** | Pengguna keluar dan masuk kembali; sesi login tersimpan tanpa harus login ulang saat buka aplikasi. | [ ] PASS / FAIL |
| **3** | **Tambah Kendaraan** | Input nama, merek, tipe, plat nomor, dan odometer awal berhasil tersimpan dan tampil di Beranda. | [ ] PASS / FAIL |
| **4** | **Input Transaksi Manual** | Input nominal BBM (pemformatan Rp otomatis), volume liter, dan harga per liter terhitung instan. | [ ] PASS / FAIL |
| **5** | **Scan Struk Kamera (ML Kit)** | Kamera membuka jendela bidik, memindai struk fisik SPBU, dan mendeteksi total harga serta volume. | [ ] PASS / FAIL |
| **6** | **Peninjauan Struk (Review)** | Pengguna dapat mengoreksi angka di layar review sebelum menekan tombol "Konfirmasi & Simpan". | [ ] PASS / FAIL |
| **7** | **Dashboard & Finansial** | Kartu total pengeluaran bulanan, total liter, dan rata-rata harga/liter terhitung akurat. | [ ] PASS / FAIL |
| **8** | **Grafik Efisiensi km/liter** | Konsumsi bahan bakar km/liter terhitung otomatis berdasarkan selisih odometer berurutan. | [ ] PASS / FAIL |
| **9** | **Ekspor Laporan CSV** | Mengunduh file CSV ke ponsel; berkas dapat dibuka dengan rapi di Microsoft Excel atau Google Sheets. | [ ] PASS / FAIL |
| **10**| **Pengingat Servis Rutin** | Mengatur tanggal/kilometer ganti oli; notifikasi pengingat muncul di status bar perangkat. | [ ] PASS / FAIL |
| **11**| **Uji Langganan Play Billing** | Membuka katalog Pro, memilih paket bulanan via License Test card, status Pro langsung aktif. | [ ] PASS / FAIL |
| **12**| **Hapus Akun Pengguna** | Menguji tombol "Hapus Akun" di profil; akun dan data berhasil terhapus sesuai regulasi privasi. | [ ] PASS / FAIL |

---

## ⚡ FASE 4: Pengujian Kasus Batas & Ketahanan (Edge Cases & Resilience)

- [ ] **Mode Jaringan Terputus (Offline Handling)**:
  - Buka aplikasi dalam mode pesawat (*Airplane mode*).
  - Pastikan aplikasi menampilkan pesan ramah tanpa crash dan tetap mengizinkan pengisian form manual.
- [ ] **Rotasi Layar (Screen Rotation)**:
  - Putar layar ke landscape saat mengisi form transaksi; pastikan teks input tidak hilang (*state preserved*).
- [ ] **Tema Gelap & Terang (Dark / Light Mode)**:
  - Beralih antara Dark Mode dan Light Mode di pengaturan sistem ponsel; pastikan seluruh teks terbaca jelas.
- [ ] **Manajemen Memori (Background Kill)**:
  - Minimalkan aplikasi, buka beberapa game/aplikasi berat, buka kembali Mari Partner Fuel; pastikan aplikasi melanjutkan sesi dengan mulus.

---

## 📋 FASE 5: Penanganan Umpan Balik & Perbaikan Bug (Bug Triage)

- [ ] Kumpulkan seluruh laporan kendala dari formulir umpan balik penguji secara harian.
- [ ] Klasifikasikan bug:
  - **P0 / Blocker**: Crash mendadak, transaksi tidak tersimpan, gagal bayar -> Segera rilis build update `versionCode = 9`.
  - **P1 / High**: Tampilan berantakan di layar kecil, keterlambatan notifikasi -> Jadwalkan perbaikan minor.
  - **P2 / Minor**: Penyesuaian kata/label, saran fitur baru -> Catat di backlog pasca rilis.
- [ ] Jika ada build update: unggah AAB baru ke track Closed Testing yang sama agar tester menerima pembaruan otomatis via Play Store.

---

## 🏁 FASE 6: Kriteria Selesai & Kelayakan Produksi (Exit Criteria)

Tahap Closed Testing dinyatakan **SELESAI & SUKSES** jika:
1. [ ] Minimal 20 penguji telah berpartisipasi dan tetap opt-in selama minimal 14 hari penuh.
2. [ ] Tingkat bebas crash (*Crash-free sessions*) di Android Vitals mencapai **> 99.5%**.
3. [ ] Seluruh bug P0 / Blocker telah diselesaikan dan diverifikasi oleh penguji.
4. [ ] Dokumen umpan balik penguji telah terisi lengkap.
5. [ ] Tombol **Apply for production** di Google Play Console telah aktif dan siap diajukan.
