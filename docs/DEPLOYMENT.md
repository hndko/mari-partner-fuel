# 🚀 Panduan Deployment & Operasional Produksi (DEPLOYMENT.md)

Panduan operasional deployment backend Supabase, migrasi database, dan rilis APK/AAB Android untuk **Mari Partner Fuel**.

Lihat juga panduan lengkap di: [04. Panduan Deployment, Rilis & Distribusi](04-panduan-deployment.md).

---

## 🗄️ 1. Urutan Migrasi Database Supabase (PostgreSQL)

Sebelum merilis aplikasi ke staging atau produksi, jalankan seluruh 8 berkas migrasi SQL secara berurutan:

```bash
# Melalui Supabase CLI:
supabase db push

# Atau eksekusi berurutan via Supabase Dashboard SQL Editor:
# 1. supabase/migrations/20261029000001_initial_schema.sql
# 2. supabase/migrations/20261029000002_rls_and_storage.sql
# 3. supabase/migrations/20261029000003_vehicles_and_transactions_tahap3.sql
# 4. supabase/migrations/20261029000004_receipt_scan_jobs_policy.sql
# 5. supabase/migrations/20261029000005_maintenance_reminders_enhancement.sql
# 6. supabase/migrations/20261029000006_subscription_entitlements_and_quota.sql
# 7. supabase/migrations/20261029000007_payment_integration_and_activation.sql
# 8. supabase/migrations/20261029000008_security_hardening_and_integrity.sql
```

---

## ⚡ 2. Deployment Supabase Edge Functions

Deploy seluruh 6 fungsi backend Edge Functions:

```bash
supabase functions deploy extract-fuel-receipt
supabase functions deploy get-my-entitlements
supabase functions deploy create-checkout
supabase functions deploy verify-play-purchase
supabase functions deploy get-payment-status
supabase functions deploy payment-webhook
supabase functions deploy sync-subscription
```

### Konfigurasi Supabase Secrets:
```bash
supabase secrets set GEMINI_API_KEY="AIzaSy..."
supabase secrets set GOOGLE_PLAY_SERVICE_ACCOUNT_JSON='{"type":"service_account",...}'
supabase secrets set QRISPY_API_KEY="qrispy_live_..."
supabase secrets set QRISPY_WEBHOOK_SECRET="whsec_..."
```

---

## 📱 3. Build & Signing Aplikasi Android

### A. Build Release AAB (Google Play Store):
```powershell
.\gradlew.bat bundleRelease
```
Artefak: `app/build/outputs/bundle/release/app-release.aab`

### B. Build Release APK (Direct/Web Distribution):
```powershell
.\gradlew.bat assembleRelease
```
Artefak: `app/build/outputs/apk/release/app-release.apk`

### C. Build Debug APK (Pengujian Internal):
```powershell
.\gradlew.bat assembleDebug
```
Artefak: `app/build/outputs/apk/debug/app-debug.apk` (~23.1 MB)
