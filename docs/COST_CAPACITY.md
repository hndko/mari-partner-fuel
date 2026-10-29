# 💰 COST_CAPACITY.md — Analisis Biaya Operasional & Proyeksi Kapasitas Sistem

Dokumen ini memuat audit rincian struktur biaya operasional, batas kuota infrastruktur (*capacity planning*), estimasi biaya per skala pengguna (*unit economics*), dan strategi mitigasi risiko lonjakan biaya (*cost control*) untuk sistem **Mari Partner Fuel**.

---

## 🏛️ 1. Komponen Biaya Infrastruktur Aktual

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        KOMPONEN BIAYA INFRASTRUKTUR SISTEM                             │
├─────────────────────┬─────────────────────┬─────────────────────┬──────────────────────┤
│ 1. Supabase Backend │ 2. Google Gemini AI │ 3. Payment Fees     │ 4. Platform Store    │
│    (Database & Auth)│    (Cloud Fallback) │    (QRISpy & Google)│    (Play Console)    │
└─────────────────────┴─────────────────────┴─────────────────────┴──────────────────────┘
```

| Komponen Layanan | Skema Harga / Tarif | Batas Kuota Termasuk | Biaya Tambahan (Overage) |
|---|---|---|---|
| **Supabase Free Tier** | **$0 / Bulan** | 500 MB DB, 1 GB Storage, 50k MAU, 500k Edge Invocations | Proyek di-pause jika tidak aktif 7 hari |
| **Supabase Pro Tier** *(Disarankan)* | **$25 / Bulan** (~Rp 400.000) | 8 GB DB, 100 GB Storage, 100k MAU, 2M Edge Invocations, PITR 7 Hari | $0.125 / GB DB, $0.021 / GB Storage |
| **Google Cloud Gemini 1.5 Flash** | Pay-As-You-Go | Free Tier: 15 RPM / 1M TPM gratis; Berbayar: $0.075 / 1M Input Tokens | ~$0.00008 (~Rp 1,20) per ekstraksi struk |
| **Google ML Kit OCR (On-Device)** | **100% GRATIS** | Tanpa batas kuota, berjalan di hardware ponsel pengguna | Rp 0 (Bebas Biaya) |
| **Google Play Developer Account** | **$25 Sekali Bayar** (~Rp 400.000) | Akses seumur hidup untuk distribusi aplikasi di Play Store | Tidak ada biaya bulanan |
| **Google Play Billing Fee** | **15% dari Penjualan** | Potongan resmi Google Play untuk langganan auto-renewing | Mengurangi penerimaan kotor langganan Pro |
| **QRISpy Gateway Fee** | **0.7% MDR** (Standar BI) | Potongan per transaksi QRIS perbankan sukses | Biaya terpotong otomatis per transaksi sukses |

---

## 📈 2. Proyeksi Kapasitas & Estimasi Biaya Berdasarkan Skala Pengguna

Asumsi model:
- 80% pengguna aktif menggunakan **Free Tier** (maks 5 scan AI/bln, rata-rata 15 transaksi/bln).
- 20% pengguna aktif berlangganan **Pro Tier** (maks 100 scan AI/bln, rata-rata 30 transaksi/bln).
- Rata-rata ukuran foto struk terkompresi lokal: **150 KB**.
- 80% pemindaian struk diproses oleh **Google ML Kit lokal** (Rp 0); hanya 20% membutuhkan **Gemini Cloud AI fallback**.

| Metrik / Skala Pengguna | 100 Pengguna Aktif (Tahap Beta) | 1.000 Pengguna Aktif (Peluncuran Awal) | 10.000 Pengguna Aktif (Pertumbuhan) |
|---|:---:|:---:|:---:|
| **Rincian Akun** | 80 Free / 20 Pro | 800 Free / 200 Pro | 8.000 Free / 2.000 Pro |
| **Total Transaksi / Bulan** | 1.800 transaksi | 18.000 transaksi | 180.000 transaksi |
| **Volume Foto Struk / Bulan** | ~270 MB | ~2.7 GB | ~27 GB |
| **Panggilan Cloud AI / Bulan** | ~360 request | ~3.600 request | ~36.000 request |
| **Paket Supabase yang Dibutuhkan** | **Free Tier** ($0) | **Pro Tier** ($25) | **Pro Tier + Add-ons** ($50) |
| **Biaya Gemini Cloud AI** | Rp 0 (Free Tier) | ~$0.30 (~Rp 4.800) | ~$3.00 (~Rp 48.000) |
| **Estimasi Pendapatan Pro Kotor** | Rp 580.000 / bln | Rp 5.800.000 / bln | Rp 58.000.000 / bln |
| **Estimasi Biaya Operasional Total** | **~Rp 0 / bln** | **~Rp 405.000 / bln** | **~Rp 850.000 / bln** |
| **Margin Keuntungan Operasional** | **100% (Sandbox)** | **~93.0%** | **~98.5%** |

---

## 🛡️ 3. Mekanisme Mitigasi Lonjakan Biaya (Cost Control & Guardrails)

1. **Strategi Local-First OCR (Prioritas Utama)**:
   - 100% ekstraksi awal dijalankan langsung di prosesor smartphone pengguna via Google ML Kit tanpa konsumsi internet maupun kuota cloud API.
2. **Penegakan Kuota Ketat di Sisi Server (PostgreSQL RPC)**:
   - Pengguna Free dibatasi maksimal 5 permintaan AI cloud per bulan.
   - Panggilan ke Edge Function `extract-fuel-receipt` langsung ditolak dengan kode `HTTP 403 (AI_QUOTA_EXHAUSTED)` sebelum memanggil API Gemini jika kuota habis.
3. **Kompresi Gambar Sebelum Upload**:
   - Foto kamera struk dikompresi di memori perangkat Android (resolusi maksimal 1280px dan format WebP/JPEG kualitas 80%) sehingga rata-rata berkas hanya ~150 KB (bukan 5-8 MB foto kamera mentah). Ini mereduksi biaya Supabase Storage hingga **97%**.
4. **Circuit Breaker Serverless**:
   - Jika pengeluaran API Gemini mendekati batas anggaran yang ditentukan tim produk, admin dapat mengubah flag konfigurasi di Supabase Vault untuk mematikan sementara tombol cloud AI tanpa merusak fungsi pencatatan lokal aplikasi.
