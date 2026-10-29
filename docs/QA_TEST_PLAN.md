# 📋 Rencana Pengujian Mutu Menyeluruh (QA_TEST_PLAN.md)
**Proyek:** Mari Partner Fuel (Android Native)\
**Fase:** Tahap 9 — QA, Performance, Android Compatibility & Release Candidate\
**Target Rilis:** Release Candidate 1 (`v1.7.0`, Build Code 8)\
**Tim Penanggung Jawab:** Android QA, Performance & Release Engineering

---

## 🎯 1. Tujuan & Strategi Pengujian (Test Strategy)

Dokumen ini mendefinisikan rencana pengujian mutu formal aplikasi **Mari Partner Fuel** sebelum memasuki fase produksi. Pengujian difokuskan pada:
1. **Stabilitas & Keandalan:** Memastikan nol crash pada skenario utama, kegagalan jaringan, dan transisi lifecycle aplikasi.
2. **Kompatibilitas Android:** Menjamin operabilitas penuh dari Android API 26 (Android 8.0 Oreo) hingga Android API 35 (Android 15).
3. **Performa & Responsivitas:** Menjaga antarmuka pengguna pada 60 FPS tanpa jank (< 16ms render time) dan memastikan operasi berat tidak memblokir main thread.
4. **Integritas Finansial & Data:** Memverifikasi kalkulasi rupiah, agregasi bulanan, konsumsi km/L, dan ekspor CSV akurat hingga desimal terkecil.
5. **Kepatuhan Kebijakan Toko Aplikasi:** Menjamin keselarasan dengan *Google Play Payments Policy* dan izin privasi minimum.

---

## 📱 2. Matriks Kompatibilitas Perangkat & Sistem Operasi

| Kategori | Target Pengujian | Catatan Teknis & Penanganan |
|---|---|---|
| **OS Minimum** | Android 8.0 Oreo (API 26) | Desugaring JDK `java.time` aktif, kompatibilitas Adaptive Icon, dan Notification Channel mandatory. |
| **OS Umum / Mainstream** | Android 11–13 (API 30–33) | Permission `POST_NOTIFICATIONS` dinamis pada Android 13+; integrasi PhotoPicker bawaan. |
| **OS Modern / Target** | Android 14–15 (API 34–35) | Predictive Back gesture, edge-to-edge layout, kepatuhan foreground service. |
| **Kepadatan Layar** | hdpi, xhdpi, xxhdpi, xxxhdpi | Uji responsivitas Compose pada layar 4.7 inci (720p) hingga 6.7 inci (1440p AMOLED). |
| **Tema Tampilan** | Light Mode & Dark Mode | Pengujian kontras warna WCAG AA (minimal 4.5:1 untuk teks normal). |
| **Kondisi Jaringan** | Online Cepat, 3G Lambat, Mode Pesawat | Pengujian timeout, retry graceful, dan ketersediaan fitur offline (OCR lokal ML Kit). |

---

## 🧪 3. Ruang Lingkup Skenario Pengujian Fungsional (Test Scope)

```text
┌─────────────────────────────────────────────────────────────────────────────────┐
│                    SKENARIO PENGUJIAN FUNGSIONAL MARI PARTNER FUEL              │
├────────────────────┬────────────────────┬───────────────────────────────────────┤
│ 1. Autentikasi     │ 2. Kendaraan & BBM │ 3. OCR & AI Receipt                   │
│ • Registrasi email │ • Tambah/Edit/Arsip│ • Google ML Kit lokal (on-device)     │
│ • Login/Logout     │ • Validasi tanggal │ • Deteksi SPBU, volume, harga, total  │
│ • Google Sign-In   │ • Upload foto      │ • Fallback cerdas Gemini Cloud AI     │
│ • Session recovery │ • Riwayat & Search │ • Konfirmasi Human-in-the-Loop        │
├────────────────────┼────────────────────┼───────────────────────────────────────┤
│ 4. Dashboard & CSV │ 5. Servis & Alarm  │ 6. Pembayaran & Pro                   │
│ • Total & rata-rata│ • Pemicu odometer  │ • Google Play Billing v7.1.1          │
│ • Efisiensi km/L   │ • Pemicu tanggal   │ • Dynamic QRISpy & countdown          │
│ • Ekspor RFC 4180  │ • AlarmManager 08:00│ • Anti-replay purchase token         │
│ • Anti-formula inj │ • Pembatalan alarm │ • Idempotensi webhook gateway         │
└────────────────────┴────────────────────┴───────────────────────────────────────┘
```

---

## ⚡ 4. Standar Layanan Performa (Performance SLAs)

| Indikator Performa | Target Maksimal (SLA) | Metrik Verifikasi |
|---|:---:|---|
| **Cold Start Aplikasi** | < 1.500 ms | Dari ikon diketuk hingga layar Splash/Beranda selesai me-render |
| **Warm Start / Resume** | < 400 ms | Memulihkan aktivitas dari latar belakang |
| **Respons Navigasi Antar Tab** | < 100 ms | Transisi Compose Navigation tanpa jank visual |
| **Pencarian Riwayat Debounce** | < 50 ms | Penyaringan in-memory untuk 1.000 transaksi |
| **Agregasi Finansial Dashboard** | < 50 ms | Perhitungan total belanja dan rata-rata tertimbang per liter |
| **Ekspor CSV 1.000 Transaksi** | < 500 ms | Pembuatan string CSV standar RFC 4180 dengan UTF-8 BOM |
| **Perhitungan Efisiensi BBM** | < 200 ms | Analisis sequential 500 leg odometer kendaraan |
| **Ekstraksi Teks ML Kit Lokal** | < 800 ms | Pemrosesan bitmap foto struk on-device tanpa jaringan |

---

## 🚪 5. Kriteria Masuk & Kriteria Keluar (Stopping Gates)

### Kriteria Memulai QA (Entry Criteria):
1. Seluruh kode fitur Tahap 1–7 terpasang dan lolos audit keamanan Tahap 8.
2. Build debug dan release berhasil dikompilasi dengan nol error fatal.
3. Seluruh unit test eksisting lulus 100%.

### Kriteria Kelulusan Rilis (Exit Criteria / Release Ready):
1. **Zero Blocker / Zero Critical Bug:** Tidak ada crash aplikasi pada alur kritis.
2. **Zero Financial Calculation Error:** Perhitungan rupiah akurat tanpa deviasi floating point.
3. **100% Pass Rate:** Seluruh automated unit, security, dan benchmark tests lulus 100%.
4. **Lint Vital Release:** Analisis R8/ProGuard dan Android Lint Vital lulus tanpa error.
5. **Artifacts Signed:** APK dan AAB Release Candidate berhasil di-generate dan terverifikasi utuh.
