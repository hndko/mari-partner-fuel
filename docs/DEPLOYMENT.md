# 🚀 DEPLOYMENT.md — Runbook Deployment Produksi & Prosedur Pelepasan Rilis

Dokumen ini adalah runbook resmi 15 langkah (*Deployment Runbook*) untuk merilis pembaruan sistem **Mari Partner Fuel** (*Klien Android, PostgreSQL Supabase, Storage, Edge Functions, dan Integrasi Pembayaran*).

> ⚠️ **ATURAN KESELAMATAN WAJIB**:
> 1. Dilarang melakukan deployment ke lingkungan produksi tanpa persetujuan tertulis (*explicit sign-off*) dari Product Owner dan Lead Architect.
> 2. Dilarang menjalankan migrasi destruktif (`DROP TABLE`, `TRUNCATE`) pada basis data produksi yang sedang aktif.
> 3. Seluruh langkah di bawah ini wajib diuji terlebih dahulu pada lingkungan Staging sebelum diterapkan ke Produksi.

---

## 📋 15 Langkah Standar Operasional Deployment (Deployment Runbook)

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        15 LANGKAH STANDAR OPERASIONAL DEPLOYMENT                       │
├─────────────────┬─────────────────┬─────────────────┬─────────────────┬────────────────┤
│ 1. Git Freeze   │ 2. QA & Test    │ 3. Secrets Check│ 4. Pre-DB Backup│ 5. DB Migration│
│ 6. Edge Funcs   │ 7. Storage RLS  │ 8. AI & Payment │ 9. Build AAB/APK│ 10. Smoke Test │
│ 11. Monitoring  │ 12. Abort Check │ 13. App Rollback│ 14. DB Mitigasi │ 15. Sign-Off   │
└─────────────────┴─────────────────┴─────────────────┴─────────────────┴────────────────┘
```

---

### Langkah 1: Pemeriksaan Branch dan Commit yang Akan Dirilis
- Pastikan branch adalah `main` atau `release/v1.7.0`.
- Periksa status Git bersih tanpa *uncommitted changes*:
  ```bash
  git status
  git log -1 --format="Commit: %h - %s (%ci)"
  ```

### Langkah 2: Pemeriksaan Kelulusan Test & Status Blocker
- Jalankan seluruh 137 unit test dan benchmark performa:
  ```powershell
  .\gradlew.bat testDebugUnitTest
  ```
- Pastikan 100% test lulus (0 gagal, 0 error).

### Langkah 3: Verifikasi Environment & Secrets Vault
- Pastikan variabel lingkungan produksi pada Supabase Vault telah terkonfigurasi dengan benar tanpa mengekspos nilainya ke log:
  - `GEMINI_API_KEY`
  - `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`
  - `QRISPY_API_KEY`
  - `QRISPY_WEBHOOK_SECRET`

### Langkah 4: Pencadangan Basis Data Pra-Deployment (Pre-Deployment Backup)
- Lakukan snapshot manual basis data sebelum menerapkan migrasi:
  ```bash
  # Dump manual via Supabase CLI
  supabase db dump --project-ref <PROD_PROJECT_REF> -f pre_deploy_backup_$(date +%Y%m%d).sql
  ```

### Langkah 5: Penerapan Migrasi Basis Data (Database Migrations)
- Terapkan seluruh migrasi SQL yang belum terpasang secara sekuensial:
  ```bash
  supabase db push --project-ref <PROD_PROJECT_REF>
  ```
- Verifikasi tabel publik dan kebijakan RLS:
  ```sql
  SELECT tablename, rowsecurity FROM pg_tables WHERE schemaname = 'public';
  ```

### Langkah 6: Deployment Supabase Edge Functions
- Deploy seluruh 7 Edge Functions backend:
  ```bash
  supabase functions deploy extract-fuel-receipt --project-ref <PROD_PROJECT_REF>
  supabase functions deploy get-my-entitlements --project-ref <PROD_PROJECT_REF>
  supabase functions deploy create-checkout --project-ref <PROD_PROJECT_REF>
  supabase functions deploy verify-play-purchase --project-ref <PROD_PROJECT_REF>
  supabase functions deploy get-payment-status --project-ref <PROD_PROJECT_REF>
  supabase functions deploy payment-webhook --project-ref <PROD_PROJECT_REF>
  supabase functions deploy sync-subscription --project-ref <PROD_PROJECT_REF>
  ```

### Langkah 7: Verifikasi Konfigurasi Storage & Kebijakan RLS
- Pastikan bucket `receipts` berstatus privat (*Public: false*).
- Pastikan policy membatasi pembacaan foto hanya untuk pemilik via token folder path `auth.uid() = (storage.foldername(name))[1]`.

### Langkah 8: Verifikasi Endpoint Provider AI & Pembayaran
- Lakukan health-check ringan pada endpoint Edge Functions:
  - Validasi penolakan tanpa token JWT (`HTTP 401 Unauthorized`).
  - Validasi penolakan webhook tanpa signature HMAC sah (`HTTP 401 Invalid Signature`).

### Langkah 9: Pembuatan Artefak Build Android Release
- Kompilasi paket rilis produksi dengan optimasi R8 / ProGuard:
  ```powershell
  # A. Buat Android App Bundle untuk Google Play Store:
  .\gradlew.bat bundleRelease

  # B. Buat Universal Signed APK untuk Direct/Web Distribution:
  .\gradlew.bat assembleRelease
  ```
- Verifikasi artefak:
  - `app/build/outputs/bundle/release/app-release.aab` (~7.57 MB)
  - `app/build/outputs/apk/release/app-release.apk` (~3.39 MB)

### Langkah 10: Smoke Test Fungsional Aplikasi
- Pasang artefak release APK pada minimal 2 perangkat uji (API 26 dan API 34+).
- Uji alur inti:
  1. Login dengan akun uji staging.
  2. Tambah 1 transaksi BBM manual dengan format Rupiah.
  3. Scan 1 struk fisik menggunakan Google ML Kit lokal on-device.
  4. Periksa ringkasan kartu finansial di Dashboard.
  5. Ekspor riwayat ke CSV dan periksa integritas tanda tangan UTF-8 BOM.

### Langkah 11: Pemeriksaan Monitoring & Observability
- Buka Supabase Dashboard -> Reports.
- Pastikan tidak ada lonjakan error 5xx atau kebuntuan *connection pool*.

### Langkah 12: Kriteria Pembatalan Deployment (Deployment Abort Criteria)
Deployment WAJIB dibatalkan seketika jika:
- Terjadi error SQL syntax atau kegagalan foreign key saat `supabase db push`.
- Error rate Edge Function > 2% pada smoke test.
- Terjadi crash aplikasi (*Fatal Exception*) pada perangkat Android API 26.
- Terdeteksi kebocoran kredensial rahasia di log atau repositori.

---

## 🔄 Prosedur Rollback Darurat

### 13. Prosedur Rollback Aplikasi Android (App Rollback)
1. **Google Play Console (Closed / Production Track)**:
   - Masuk ke Google Play Console -> Pilih rilis aktif -> Klik **Halt rollout**.
   - Naikkan `versionCode` pada branch perbaikan: `versionCode = 9`, `versionName = "1.7.1"`.
   - Build dan unggah AAB perbaikan darurat (*hotfix*) dengan target rollout 100%.
2. **Direct APK**:
   - Ganti tautan unduhan APK di portal resmi dengan versi stabil sebelumnya (`v1.6.0`).

### 14. Prosedur Rollback & Mitigasi Migrasi Basis Data (Database Rollback)
> ⚠️ **PERINGATAN**: Dilarang me-rollback database secara membabi-buta dengan `DROP TABLE` jika data transaksi pelanggan baru sudah mulai masuk ke produksi!
- **Jika data belum ada perubahan**: Pulihkan database dari snapshot dump pra-deployment (`pre_deploy_backup.sql`).
- **Jika data pelanggan sudah tercatat**: Terapkan *compensating migration* (expand-and-contract) untuk menonaktifkan kolom atau fungsi baru tanpa menghapus data transaksi yang sudah tercipta.

---

### Langkah 15: Verifikasi Akhir & Persetujuan Rilis (Sign-Off)
- Lead Architect, QA Lead, dan DevOps menandatangani checklist kesiapan rilis.
- Dokumentasikan status rilis pada `README.md` dan arsipkan catatan deployment.
