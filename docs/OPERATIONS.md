# 🛡️ OPERATIONS.md — Manual Kesiapan Operasional, Pemulihan Bencana, Monitoring & Biaya

Buku manual ini memuat panduan terpadu operasional produksi (*Operations & Site Reliability Manual*) untuk sistem **Mari Partner Fuel** (*Klien Android, PostgreSQL Supabase, Storage, Edge Functions, serta Layanan Pembayaran*). Manual ini memadukan **Audit Kesiapan Produksi**, **Pencadangan & Pemulihan Bencana (Backup & DR)**, **Standar Pemantauan (Monitoring & Alerting)**, **Rencana Tanggap Darurat (Incident Response)**, dan **Analisis Biaya & Kapasitas**.

---

## 📌 Bagian 1: Status Kesiapan Produksi (Production Readiness)

- **Status Rilis**: **READY FOR CLOSED TESTING**
- **Artefak Produksi**:
  - `app/build/outputs/bundle/release/app-release.aab` (**`7.57 MB`** — R8 Minified)
  - `app/build/outputs/apk/release/app-release.apk` (**`3.39 MB`** — R8 Minified)
- **Kepatuhan Regulasi**: Target SDK API 35 (Android 15), Min SDK API 26 (Android 8.0 Oreo), 137 unit tests 100% lulus.

---

## 💾 Bagian 2: Pencadangan & Pemulihan Bencana (Backup & Disaster Recovery)

### 2.1 Target RPO & RTO
- **Recovery Point Objective (RPO)**: **Maksimal 1 Jam**
- **Recovery Time Objective (RTO)**: **Maksimal 2 Jam**

### 2.2 Prosedur Pencadangan Manual PostgreSQL
```bash
pg_dump -h db.<PROJECT_REF>.supabase.co \
        -U postgres -d postgres -F c -b -v \
        -f "backup_mpf_db_$(date +%Y%m%d_%H%M%S).dump"
```

### 2.3 Prosedur Pemulihan (Restorasi)
- **Kerusakan Data Ringan**: Gunakan fitur *Point-in-Time Recovery (PITR)* di Supabase Dashboard untuk memutar kembali data ke 1 menit sebelum insiden.
- **Restorasi Penuh ke Server Baru**:
  ```bash
  pg_restore -h db.<NEW_PROJECT_REF>.supabase.co \
             -U postgres -d postgres -v --clean --if-exists \
             "backup_mpf_db_<TIMESTAMP>.dump"
  ```
- **Sinkronisasi Objek Foto Struk**: Gunakan `rclone sync` untuk menyalin folder bucket privat `receipts` ke penyimpanan sekunder offsite.

---

## 📊 Bagian 3: Pemantauan, Logging & Ambang Batas Alert (Monitoring SLAs)

| Level Insiden | Metrik Pemicu (Alert Trigger) | Ambang Batas Waspada | Tindakan Awal |
|:---:|---|---|---|
| **P0 (Kritis)** | **Android Crash Rate** | **> 1.0%** sesi terdampak crash | Hentikan rollout Play Store, siapkan hotfix |
| **P0 (Kritis)** | **Database Down** | Status 503 / terputus > 2 menit | Switchover standby replica, hubungi support cloud |
| **P0 (Kritis)** | **Webhook Signature Bypass** | Event sukses tanpa signature sah | Nonaktifkan Edge Function webhook sementara |
| **P1 (Tinggi)** | **Edge Function Error Rate** | **> 5.0%** error dalam 10 menit | Cek ketersediaan kuota Gemini / QRISpy |
| **P1 (Tinggi)** | **Payment Webhook Delay** | Status `pending` > 30 menit | Jalankan rekonsiliasi manual via `sync-subscription` |
| **P2 (Sedang)** | **AI Quota Burn Spike** | Pemakaian > 80% kuota provider | Pasang rate limit, evaluasi pembatasan akun |
| **P2 (Sedang)** | **Slow Queries** | Query transaksi > 500 ms | Audit indeks `(user_id, transaction_at DESC)` |

> 🔒 **Kebijakan Zero-PII di Log**: Dilarang mencatat kata sandi, token JWT mentah, nomor kartu, atau teks struk lengkap pengguna ke dalam log server maupun klien.

---

## 🚨 Bagian 4: Rencana Tanggap Darurat (10 Skenario Insiden Kritis)

1. **Kebocoran Kunci Rahasia**: Segera cabut (*revoke*) kunci di console provider, generate kunci baru, dan perbarui di Supabase Secrets Vault.
2. **Akses Data Lintas Akun (BOLA/IDOR)**: Terapkan ulang penguncian foreign key komposit `(vehicle_id, user_id)` dan audit log akses RLS.
3. **Layanan Cloud AI Outage**: Aplikasi Android otomatis beralih ke hasil pemindaian Google ML Kit lokal (100% on-device).
4. **Lonjakan Pemakaian Kuota AI**: Aktifkan circuit breaker di Edge Function `extract-fuel-receipt` untuk membatasi kuota harian.
5. **Pembayaran Sukses tetapi Pro Belum Aktif**: Jalankan rekonsiliasi manual berotoritas server via Edge Function `sync-subscription`.
6. **Aktivasi Pro Ilegal Tanpa Bayar**: Ubah status `subscriptions` menjadi `expired`, pastikan izin eksekusi RPC hanya untuk `service_role`.
7. **Database Supabase Down**: Tampilkan pengumuman pemeliharaan di aplikasi klien; jika rusak, restore dari snapshot PITR.
8. **Kegagalan Migrasi Database**: Hentikan deployment seketika; terapkan *compensating migration* tanpa merusak data pelanggan.
9. **Lonjakan Crash Aplikasi Pasca Rilis (> 1%)**: Buka Play Console -> Klik **Halt rollout** seketika; rilis AAB hotfix `1.7.1` dengan rollout 100%.
10. **Berkas Backup Rusak**: Lakukan uji coba integritas restorasi mingguan di database isolasi sementara.

---

## 💰 Bagian 5: Analisis Biaya Operasional & Proyeksi Kapasitas

| Skala Pengguna Aktif | Paket Supabase | Estimasi Biaya Cloud AI | Estimasi Pendapatan Pro | Margin Keuntungan |
|:---:|:---:|:---:|:---:|:---:|
| **100 Pengguna (Beta)** | **Free Tier** ($0) | Rp 0 (Free Tier) | Rp 580.000 / bln | **100% (Sandbox)** |
| **1.000 Pengguna (Peluncuran)** | **Pro Tier** ($25) | ~$0.30 (~Rp 4.800) | Rp 5.800.000 / bln | **~93.0%** |
| **10.000 Pengguna (Skala Penuh)**| **Pro Tier + Add-ons** ($50) | ~$3.00 (~Rp 48.000) | Rp 58.000.000 / bln | **~98.5%** |

### Strategi Efisiensi Biaya:
- **Local-First ML Kit**: 80% scan struk diproses di hardware pengguna tanpa kuota internet (Rp 0).
- **Kompresi Gambar Lokal**: Foto struk dikompresi menjadi ~150 KB sebelum upload, menghemat 97% storage cloud.
- **Server Quota Enforcement**: Pengguna Free dibatasi maksimal 5 AI scans/bulan via PostgreSQL RPC.
