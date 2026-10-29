# 💾 BACKUP_RESTORE.md — Prosedur Pencadangan & Pemulihan Bencana (Disaster Recovery)

Dokumen ini adalah prosedur operasional standar (SOP) pencadangan (*backup*) dan pemulihan bencana (*disaster recovery*) untuk seluruh komponen infrastruktur **Mari Partner Fuel** (*PostgreSQL Supabase, Supabase Storage, Edge Functions, dan Konfigurasi Rahasia*).

---

## 🎯 1. Kebijakan RPO & RTO (Disaster Recovery Objectives)

Berdasarkan analisis dampak bisnis terhadap integritas pencatatan transaksi pengeluaran BBM dan langganan pengguna:

- **Recovery Point Objective (RPO)**: **Maksimal 1 Jam**
  *(Batas toleransi kehilangan data finansial maksimal 1 jam data transaksi terakhir sebelum insiden).*
- **Recovery Time Objective (RTO)**: **Maksimal 2 Jam**
  *(Batas waktu yang dibutuhkan tim teknis untuk memulihkan seluruh layanan sistem dari kegagalan total hingga beroperasi normal kembali).*

> ⚠️ **Catatan Pemilik Produk**: Kebijakan RPO < 1 jam memerlukan paket minimal **Supabase Pro Tier** dengan fitur *Point-in-Time Recovery (PITR)* aktif. Pada Supabase Free Tier, RPO aktual adalah 24 jam (pencadangan otomatis harian).

---

## 🗄️ 2. Komponen yang Wajib Dicadangkan

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        KOMPONEN PENCADANGAN MARI PARTNER FUEL                          │
├────────────────────┬────────────────────┬────────────────────┬─────────────────────────┤
│ 1. Basis Data      │ 2. Objek Struk     │ 3. Skema & Fungsi  │ 4. Secrets Vault        │
│    PostgreSQL 15+  │    Storage Bucket  │    Edge Functions  │    Kunci API & Merchant │
│    (Data Transaksi)│    (Foto Struk)    │    (Git Repo)      │    (KeePass / Vault)    │
└────────────────────┴────────────────────┴────────────────────┴─────────────────────────┘
```

1. **Basis Data PostgreSQL Relasional**:
   - Skema tabel (`user_profiles`, `vehicles`, `fuel_transactions`, `maintenance_reminders`).
   - Finansial & Langganan (`subscription_plans`, `subscriptions`, `payment_transactions`, `payment_webhook_events`, `ai_usage_logs`).
   - Trigger, fungsi basis data, dan aturan RLS.
2. **Supabase Private Storage (`receipts`)**:
   - Seluruh foto struk fisik pengguna yang tersimpan dalam folder `receipts/{user_id}/{filename}`.
3. **Kode Backend & Serverless Logic**:
   - Seluruh 8 file migrasi database di `supabase/migrations/`.
   - Seluruh 7 file fungsi backend di `supabase/functions/`.
4. **Konfigurasi & Rahasia Lingkungan**:
   - `GEMINI_API_KEY`, `QRISPY_API_KEY`, `QRISPY_WEBHOOK_SECRET`, `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`.

---

## ⏰ 3. Mekanisme & Frekuensi Pencadangan

| Komponen | Metode Pencadangan | Frekuensi | Masa Retensi | Tanggung Jawab |
|---|---|---|:---:|---|
| **PostgreSQL (Automated)** | Supabase Managed Backup (WAL Archive) | Harian (00:00 UTC) + Continuous WAL | 7 Hari (Pro) / 30 Hari (Enterprise) | Platform Supabase |
| **PostgreSQL (Manual Dump)** | `pg_dump` via Supabase CLI / Direct SSL | Mingguan & Pra-Deployment Rilis | 30 Hari | DevOps / Release Lead |
| **Foto Struk (Storage)** | S3 Sync / `rclone` ke Cold Storage Offsite | Harian (02:00 UTC) | 90 Hari | DevOps Engineer |
| **Kode & Migrasi** | Git Version Control (GitHub Private Repo) | Real-time saat Push | Permanen | Seluruh Tim Pengembang |
| **Secrets Vault** | Export Terenkripsi GPG / KeePassXC | Saat terjadi rotasi kredensial | Permanen (Versi Aktif) | Security Lead |

---

## 🛠️ 4. Prosedur Eksekusi Pencadangan Manual

### A. Pencadangan Basis Data PostgreSQL (Manual Dump)
Jalankan perintah ini sebelum melakukan migrasi besar atau rilis versi baru:

```bash
# 1. Pastikan koneksi aman ke Supabase Project
export PGPASSWORD="<SUPABASE_DB_PASSWORD>"

# 2. Lakukan pg_dump skema dan data (format custom kompresi)
pg_dump -h db.<PROJECT_REF>.supabase.co \
        -U postgres \
        -d postgres \
        -F c \
        -b \
        -v \
        -f "backup_mpf_db_$(date +%Y%m%d_%H%M%S).dump"

# 3. Verifikasi integritas berkas dump
pg_restore -l "backup_mpf_db_$(date +%Y%m%d_%H%M%S).dump" | head -n 20
```

### B. Pencadangan Objek Foto Struk (Storage Sync)
Gunakan tool `rclone` atau script Python S3 sync untuk mencadangkan bucket privat `receipts` ke Google Cloud Storage atau AWS S3 sekunder:

```bash
# Contoh sinkronisasi bucket Supabase Storage ke S3 Backup Bucket
rclone sync supabase-s3:receipts backup-s3:mpf-receipts-backup-cold \
    --fast-list \
    --transfers 8 \
    --checkers 16 \
    --log-file /var/log/receipts_backup.log
```

---

## 🔄 5. Prosedur Pemulihan Bencana (Restore Runbook)

### Skenario 1: Kerusakan Data atau Human Error (Point-In-Time Recovery / PITR)
*Jika terjadi kesalahan query SQL yang merusak data pengguna secara tidak sengaja:*

1. Buka **Supabase Dashboard** -> Proyek Anda -> **Database** -> **Backups**.
2. Pilih tab **Point in Time**.
3. Tentukan tanggal dan jam pemulihan tepat 1 menit sebelum insiden terjadi.
4. Klik **Restore database to this point in time**.
5. Tunggu proses provisi dan restorasi otomatis Supabase selesai (estimasi: 15–30 menit).
6. Verifikasi integritas relasional via SQL query verifikasi.

### Skenario 2: Pemulihan dari Berkas Dump Manual (Disaster Recovery ke Server Baru)
*Jika proyek Supabase rusak parah atau perlu dimigrasikan ke proyek cadangan:*

1. Buat proyek Supabase baru atau siapkan database PostgreSQL 15+ target.
2. Jalankan perintah restorasi:
   ```bash
   pg_restore -h db.<NEW_PROJECT_REF>.supabase.co \
              -U postgres \
              -d postgres \
              -v \
              --clean \
              --if-exists \
              "backup_mpf_db_<TIMESTAMP>.dump"
   ```
3. Terapkan hak eksekusi RLS dan fungsi otoritatif:
   ```bash
   # Jalankan hardening 008 jika restorasi parsial
   psql -h db.<NEW_PROJECT_REF>.supabase.co -U postgres -d postgres \
        -f supabase/migrations/20261029000008_security_hardening_and_integrity.sql
   ```
4. Sinkronkan kembali file foto struk:
   ```bash
   rclone sync backup-s3:mpf-receipts-backup-cold supabase-new-s3:receipts
   ```
5. Deploy ulang seluruh Edge Functions dengan secret yang sesuai:
   ```bash
   supabase functions deploy --project-ref <NEW_PROJECT_REF>
   ```

---

## ✅ 6. Checklist Verifikasi Pasca Pemulihan (Post-Restore Verification)

Setelah proses restore selesai, tim teknis WAJIB menjalankan pengujian fungsional berikut sebelum membuka akses ke pengguna:

- [ ] **Konektivitas Database**: Klien dapat terhubung dan mengeksekusi kueri sederhana.
- [ ] **Integritas Relasional Transaksi**:
  ```sql
  SELECT count(*) FROM fuel_transactions;
  SELECT count(*) FROM vehicles;
  SELECT count(*) FROM subscriptions WHERE status = 'paid';
  ```
- [ ] **Verifikasi Isolasi RLS**: Uji kueri dengan akun uji non-admin; pastikan data pengguna lain tidak bocor.
- [ ] **Akses Storage Foto Struk**: Generate signed URL untuk 3 foto struk acak dan pastikan gambar dapat dibuka di browser/aplikasi.
- [ ] **Verifikasi Edge Functions**: Panggil endpoint `get-my-entitlements` menggunakan token JWT valid dan periksa respons JSON.
- [ ] **Status Pembayaran**: Pastikan riwayat pembayaran terakhir konsisten dengan catatan merchant QRISpy & Google Play.

---

## ⚠️ 7. Risiko Kehilangan Data Residual (Residual Risks)

1. **Jendela Transaksi Selama Crash**: Transaksi BBM yang dicatat pada menit terjadinya crash (sebelum WAL disinkronkan) berpotensi hilang. Solusi: Aplikasi Android menyimpan antrean offline lokal dan mencoba sinkronisasi ulang saat koneksi pulih.
2. **Ketergantungan Supabase Cloud**: Jika terjadi gangguan regional pada region cloud Supabase, pemulihan bergantung pada status infrastruktur penyedia cloud.
