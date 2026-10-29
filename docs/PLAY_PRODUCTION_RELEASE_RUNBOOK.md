# 🚀 PLAY_PRODUCTION_RELEASE_RUNBOOK.md — Panduan Publikasi Produksi & Staged Rollout (Production Release Runbook)

Dokumen ini adalah prosedur operasional standar (*Production Release Runbook*) untuk memublikasikan aplikasi **Mari Partner Fuel** dari jalur Closed Testing ke **Jalur Produksi Publik (Google Play Production Track)** dengan strategi peluncuran bertahap (*Staged Rollout*).

> ⚠️ **ATURAN KESELAMATAN WAJIB**:
> Tindakan pengajuan rilis produksi (*Submit for Production Review*) atau peningkatan persentase peluncuran publik HANYA boleh dieksekusi setelah mendapat persetujuan tertulis (*explicit approval*) dari Pemilik Proyek (*Product Owner*). Tidak ada jaminan persetujuan otomatis; keputusan akhir publikasi berada sepenuhnya pada tim peninjau Google Play.

---

## 📋 Ringkasan Alur Publikasi Produksi

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        ALUR PUBLIKASI PRODUKSI MARI PARTNER FUEL                       │
├─────────────────┬─────────────────┬─────────────────┬─────────────────┬────────────────┤
│ 1. Evaluasi     │ 2. Pengajuan    │ 3. Review       │ 4. Staged       │ 5. Full Rollout│
│    Closed Test  │    Akses Prod   │    Google Play  │    Rollout 20%  │    100% & Live │
│    (14 Hari/20) │    (Kuesioner)  │    (1 - 7 Hari) │    -> 50%       │    Monitoring  │
└─────────────────┴─────────────────┴─────────────────┴─────────────────┴────────────────┘
```

---

## 🛡️ 1. Prasyarat Wajib Sebelum Mengajukan Akses Produksi (Prerequisites Gate)

Sebelum tombol **Apply for production** ditekan di Google Play Console, seluruh kriteria di bawah ini WAJIB bernilai `[x]`:

- [ ] **Closed Testing Tuntas**: Pengujian tertutup telah berjalan minimal **14 hari penuh** dengan minimal **20 penguji nyata** yang berstatus opt-in aktif terus-menerus (wajib untuk akun personal).
- [ ] **Crash-Free Sessions Sehat**: Tingkat bebas crash di Google Play Android Vitals mencapai **> 99.5%** dan tingkat ANR **< 0.47%**.
- [ ] **Zero Blocker P0**: Tidak ada laporan crash fatal yang belum terselesaikan dari formulir umpan balik penguji.
- [ ] **Kepatuhan Privasi Publik**: URL Kebijakan Privasi (`https://maripartner.id/privacy-policy`) dan formulir Hapus Akun web (`https://maripartner.id/delete-account`) aktif di hosting publik.
- [ ] **Data Safety & Content Rating Disetujui**: Seluruh kuesioner Keamanan Data dan Rating Konten (IARC) telah berstatus *Approved* di Play Console.
- [ ] **Produk Langganan Aktif**: SKU `mari_pro_monthly` dan `mari_pro_yearly` berstatus *Active* di Play Console Subscriptions.
- [ ] **Kredensial Merchant Produksi Terpasang**: `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` telah dimasukkan ke Supabase Secrets Vault backend untuk validasi pembelian live.

---

## 📝 2. Prosedur Pengajuan Akses Produksi (Apply for Production)

1. Masuk ke **Google Play Console** -> Buka dasbor aplikasi **Mari Partner Fuel**.
2. Pada bagian atas dasbor, klik **Apply for production**.
3. Lengkapi 3 bagian pertanyaan evaluasi pengujian tertutup menggunakan data autentik dari [`docs/CLOSED_TESTING_PLAN.md`](file:///d:/Kyoo/OneDrive/Documents/Project/Android%20Native/mari-partner-fuel/docs/CLOSED_TESTING_PLAN.md):
   - *Pertanyaan 1 (Metode Rekrutmen)*: Jelaskan bahwa penguji direkrut dari komunitas mitra pengemudi dan pengguna kendaraan harian.
   - *Pertanyaan 2 (Aktivitas Penguji)*: Jelaskan frekuensi pengujian pemindaian struk, pencatatan manual, dan evaluasi grafik konsumsi km/liter.
   - *Pertanyaan 3 (Umpan Balik & Peningkatan)*: Jelaskan perbaikan antarmuka dan penanganan toleransi struk yang telah diterapkan berdasarkan masukan penguji.
4. Klik **Submit** untuk mengirim permohonan ke tim review Google Play.
5. Tunggu persetujuan akses produksi (estimasi waktu review: 1 hingga 7 hari kerja).

---

## 📈 3. Strategi Peluncuran Bertahap (Staged Rollout Strategy)

Setelah akses produksi disetujui, **JANGAN LANGSUNG MELUNCURKAN 100%**. Gunakan peluncuran bertahap untuk memitigasi risiko lonjakan crash pada variasi perangkat publik yang belum terduga:

```text
┌─────────────────┬────────────┬─────────────────────────────┬───────────────────────────┐
│ Tahap Rollout   │ Target %   │ Durasi Pemantauan           │ Kriteria Lanjut ke Tahap  │
├─────────────────┼────────────┼─────────────────────────────┼───────────────────────────┤
│ **Tahap 1**     │ **20%**    │ 48 Jam (Hari 1 – 2)         │ Crash-free sessions >99.5%│
│ **Tahap 2**     │ **50%**    │ 48 Jam (Hari 3 – 4)         │ Error server < 1%, no ANR │
│ **Tahap 3**     │ **100%**   │ Rilis Penuh Publik (Hari 5) │ Sistem stabil di kapasitas│
└─────────────────┴────────────┴─────────────────────────────┴───────────────────────────┘
```

### Langkah Eksekusi di Play Console:
1. Masuk ke **Release** -> **Production** -> **Create new release**.
2. Pilih berkas AAB yang telah disetujui dari library atau unggah versi final `app-release.aab`.
3. Masukkan Catatan Rilis resmi untuk pengguna:
   ```text
   Selamat datang di Mari Partner Fuel versi 1.7.0!
   - Pemindaian struk SPBU instan dengan teknologi OCR lokal on-device.
   - Pencatatan transaksi BBM manual dengan pemformatan Rupiah otomatis.
   - Analitik efisiensi bahan bakar (km/liter) dan dashboard finansial.
   - Pengingat servis berkala dan jadwal ganti oli kendaraan Anda.
   - Manajemen multi-kendaraan praktis dalam satu akun.
   ```
4. Pada opsi peluncuran, pilih **Staged rollout** -> Atur persentase ke **20%**.
5. Klik **Review release** -> **Start rollout to Production**.

---

## 🚨 4. Prosedur Tanggap Darurat & Penghentian Rollout (Halt Rollout Protocol)

Jika dalam jendela pemantauan 20% atau 50% terdeteksi anomali kritis:

### Kriteria Pemicu Penghentian Rilis (Halt Trigger):
- Tingkat crash melonjak hingga **> 1.0%** di Google Play Android Vitals.
- Terjadi kegagalan verifikasi pembelian langganan Pro secara massal.
- Terdeteksi kebocoran keamanan data pengguna.

### Langkah Penghentian Rilis:
1. Buka **Google Play Console** -> **Release** -> **Production**.
2. Klik tombol **Halt rollout**. Tindakan ini seketika menghentikan distribusi pembaruan kepada pengguna baru; pengguna yang belum mengunduh akan tetap berada di versi stabil sebelumnya.
3. Buat branch darurat: `hotfix/v1.7.1`.
4. Perbaiki penyebab bug, naikkan `versionCode = 9`, `versionName = "1.7.1"`.
5. Kompilasi AAB baru: `.\gradlew.bat bundleRelease`.
6. Unggah rilis hotfix baru untuk menimpa rilis bermasalah dengan rollout 100%.

---

## 📞 5. Komunikasi & Layanan Dukungan Pengguna (User Support Plan)

- **Kanal Dukungan Resmi**: `support@maripartner.id`
- **Waktu Respons SLA**: Maksimal 1x24 jam untuk komplain terkait transaksi langganan Pro.
- **Pembaruan Status Layanan**: Jika terjadi gangguan pada server backend Supabase, tim operasional menampilkan pemberitahuan pemeliharaan sistem di situs resmi `https://maripartner.id`.
