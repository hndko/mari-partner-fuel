# 🚨 INCIDENT_RESPONSE.md — Rencana Tanggap Darurat & Penanganan Insiden Operasional

Dokumen ini adalah prosedur tanggap darurat (*Incident Response Plan / IRP*) untuk tim teknik, keamanan, dan operasional **Mari Partner Fuel**. Dokumen ini memuat langkah-langkah terstruktur untuk mendeteksi, mengisolasi (*containment*), menginvestigasi, dan memulihkan sistem dari 10 skenario insiden kritis.

---

## 👥 1. Peran & Tim Tanggap Darurat (Incident Response Team)

- **Incident Commander (Lead Engineer)**: Mengoordinasikan seluruh langkah pemulihan dan memutuskan tindakan darurat.
- **Security Auditor / DevSecOps**: Menangani insiden kebocoran kredensial, investigasi eksploitasi, dan audit basis data.
- **Backend & Database Engineer**: Menangani migrasi darurat, failover database, dan perbaikan Edge Functions.
- **Android Release Engineer**: Mengompilasi dan menerbitkan hotfix rilis ke Google Play Store.
- **Product Owner**: Berkomunikasi dengan pengguna eksternal, membuat keputusan komersial dan hukum.

---

## 📋 2. Prosedur 10 Skenario Insiden Kritis

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        10 SKENARIO INSIDEN OPERASIONAL KRITIS                          │
├────────────────────┬────────────────────┬────────────────────┬─────────────────────────┤
│ 1. Kebocoran Secret│ 2. Akses IDOR/BOLA │ 3. AI Cloud Outage │ 4. Lonjakan Kuota AI    │
│ 5. Pro Belum Aktif │ 6. Pro Ilegal      │ 7. DB/Storage Down │ 8. Gagal Migrasi DB     │
│ 9. Crash Spike App │ 10. Gagal Restore  │                    │                         │
└────────────────────┴────────────────────┴────────────────────┴─────────────────────────┘
```

---

### Skenario 1: Dugaan Kebocoran Kredensial / Secret Key
- **Deteksi**: Peringatan GitHub Secret Scanning, kemunculan token di forum publik, atau anomali lonjakan akses API tak dikenal.
- **Containment (Pembatasan)**:
  1. Segera cabut (*revoke*) kunci yang bocor melalui provider dashboard (Google Cloud Console / QRISpy Portal / Supabase Vault).
  2. Generate kunci baru (*rotation*) dan perbarui nilai di Supabase Secrets Vault:
     ```bash
     supabase secrets set GEMINI_API_KEY="NEW_KEY..."
     ```
- **Investigasi**: Periksa log akses untuk melihat apakah ada eksploitasi data selama kunci terpapar.
- **Pemulihan**: Uji panggilan Edge Functions dengan kredensial baru.
- **Pencegahan**: Audit `.gitignore`, jalankan git hook pre-commit secret detection, pastikan kunci tidak pernah di-commit ke Git.

---

### Skenario 2: Dugaan Akses Data Lintas Akun (BOLA / IDOR)
- **Deteksi**: Laporan bug pengguna melihat transaksi kendaraan akun lain, atau log PostgreSQL RLS policy rejection.
- **Containment**: Periksa apakah ada perubahan pada kebijakan RLS tabel `public.vehicles` atau `public.fuel_transactions`.
- **Investigasi**:
  ```sql
  -- Periksa kueri yang melibatkan user_id tidak sesuai
  SELECT * FROM fuel_transactions WHERE user_id != auth.uid();
  ```
- **Pemulihan**: Terapkan ulang migrasi penguncian foreign key komposit (`20261029000003_vehicles_and_transactions_tahap3.sql`) dan hardening 008.
- **Pencegahan**: Uji unit multi-akun otomatis wajib dijalankan pada setiap pipeline CI/CD (`SecurityHardeningAndIntegrityTest`).

---

### Skenario 3: Layanan Cloud AI (Google Gemini) Mati Total / Outage
- **Deteksi**: Lonjakan status 500 pada Edge Function `extract-fuel-receipt` dan error log `[AI_SERVICE_UNAVAILABLE]`.
- **Containment**: Sistem Android secara transparan beralih ke hasil pemindaian Google ML Kit lokal (100% on-device).
- **Investigasi**: Buka Google Cloud Status Dashboard untuk memeriksa ketersediaan region Gemini API.
- **Pemulihan**: Jika Gemini mati berkepanjangan, aktifkan fallback ke adapter AI sekunder di Edge Function tanpa perlu merilis ulang APK pengguna.
- **Pencegahan**: Selalu sediakan tombol pengisian struk manual bagi pengguna.

---

### Skenario 4: Lonjakan Pemakaian Kuota AI / Upaya Penyalahgunaan (Spike)
- **Deteksi**: Peringatan kuota Gemini mendekati 80% dari batas bulanan dalam tempo beberapa jam.
- **Containment**: Turunkan rate-limit di Supabase Edge Function atau nonaktifkan tombol "Optimalkan AI Cloud" sementara.
- **Investigasi**: Cek tabel `ai_usage_logs` untuk mengidentifikasi `user_id` dengan frekuensi request tidak wajar.
- **Pemulihan**: Blokir akun penyalahguna atau tetapkan kuota per hari.
- **Pencegahan**: Penegakan kuota atomik di PostgreSQL via RPC `reserve_ai_quota` dan `finalize_ai_quota`.

---

### Skenario 5: Pembayaran Pengguna Berhasil tetapi Pro Belum Aktif
- **Deteksi**: Laporan komplain pengguna bahwa saldo terpotong namun paket masih Free Tier.
- **Containment**: Customer support memeriksa status referensi transaksi pengguna di dashboard.
- **Investigasi**:
  1. Cari `order_id` di tabel `payment_transactions`.
  2. Jika berstatus `pending`, periksa webhook di tabel `payment_webhook_events`.
  3. Cek apakah ada kendala verifikasi tanda tangan HMAC.
- **Pemulihan**: Jalankan sinkronisasi manual berotoritas server:
  ```bash
  # Panggil Edge Function sync-subscription dengan service_role
  curl -X POST https://<PROJECT>.supabase.co/functions/v1/sync-subscription \
       -H "Authorization: Bearer <SERVICE_ROLE_KEY>" \
       -d '{"order_id": "MPF-20261029-XXXX"}'
  ```
- **Pencegahan**: Implementasikan cron job sinkronisasi otomatis harian untuk mengecek transaksi pending > 15 menit.

---

### Skenario 6: Aktivasi Pro Ilegal (Tanpa Pembayaran Valid)
- **Deteksi**: Entri `subscriptions` dengan status `paid` tanpa adanya pasangan record di `payment_transactions` yang sah.
- **Containment**: Segera nonaktifkan status Pro pada akun terkait:
  ```sql
  UPDATE subscriptions SET status = 'expired' WHERE id = '<SUBSCRIPTION_ID>';
  ```
- **Investigasi**: Audit log database untuk mengetahui peran yang memanggil `activate_subscription_for_payment`.
- **Pemulihan**: Pastikan Migrasi 008 aktif sehingga fungsi ini ter-revoke dari peran publik.
- **Pencegahan**: Guard `auth.role() = 'service_role'` wajib aktif di seluruh fungsi mutasi status langganan.

---

### Skenario 7: Gangguan Basis Data PostgreSQL atau Supabase Storage Down
- **Deteksi**: Status code 503 dari Supabase API, kegagalan login massal di klien Android.
- **Containment**: Pasang status bar pengumuman darurat di aplikasi klien: "Sinkronisasi cloud sedang dalam pemeliharaan".
- **Investigasi**: Periksa dashboard Supabase Status Page (`status.supabase.com`).
- **Pemulihan**: Jika database rusak, lakukan restorasi dari backup PITR terakhir sesuai [`docs/BACKUP_RESTORE.md`](file:///d:/Kyoo/OneDrive/Documents/Project/Android%20Native/mari-partner-fuel/docs/BACKUP_RESTORE.md).
- **Pencegahan**: Pertahankan paket compute yang memadai dan backup berkala.

---

### Skenario 8: Kegagalan Migrasi Skema Basis Data saat Deployment
- **Deteksi**: Error eksekusi SQL saat menjalankan `supabase db push` di staging atau produksi.
- **Containment**: JANGAN jalankan script migrasi lanjutan. Hentikan deployment seketika.
- **Investigasi**: Periksa baris SQL yang gagal, cek apakah ada benturan tipe data atau foreign key violation.
- **Pemulihan**: Terapkan script kompensasi migrasi atau pulihkan database ke snapshot pra-deployment.
- **Pencegahan**: Seluruh migrasi wajib lolos uji di staging database sebelum diterapkan ke produksi.

---

### Skenario 9: Lonjakan Crash Aplikasi Android Pasca Rilis (> 1%)
- **Deteksi**: Peringatan Google Play Android Vitals crash spike atau Crashlytics issue baru.
- **Containment**:
  1. Buka Google Play Console -> *Production* -> Klik **Halt Rollout** seketika untuk menghentikan distribusi.
- **Investigasi**: Periksa stack trace error (misal: `NullPointerException`, `ClassCastException`, atau `SecurityException`).
- **Pemulihan**:
  1. Buat branch hotfix darurat: `hotfix/v1.7.1`.
  2. Perbaiki bug, naikkan `versionCode = 9`, `versionName = "1.7.1"`.
  3. Bangun dan unggah AAB baru dengan rollout 100%.
- **Pencegahan**: Pengujian lint vital dan uji asap pada minimal 2 perangkat fisik sebelum rilis.

---

### Skenario 10: Berkas Cadangan (Backup) Rusak atau Gagal Dipulihkan
- **Deteksi**: Perintah `pg_restore` menghasilkan error korupsi file saat uji coba restorasi berkala.
- **Containment**: Jangan hapus dump cadangan lama.
- **Investigasi**: Cek log proses pembuatan dump; periksa apakah koneksi terputus saat proses transfer dump.
- **Pemulihan**: Buat ulang dump baru secara manual menggunakan `pg_dump` dengan verifikasi `-v`.
- **Pencegahan**: Otomasikan uji integritas restore mingguan di database isolasi sementara.
