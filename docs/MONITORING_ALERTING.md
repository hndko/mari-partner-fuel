# 📈 MONITORING_ALERTING.md — Panduan Pemantauan, Pencatatan & Notifikasi Insiden

Dokumen ini adalah acuan operasional untuk memantau kesehatan sistem (*observability*), mendeteksi anomali operasional, dan merespons kegagalan pada aplikasi **Mari Partner Fuel** (*Klien Android, PostgreSQL Supabase, Edge Functions, Google ML Kit, dan Layanan Pembayaran*).

---

## 🎯 1. Filosofi & Pendekatan Pemantauan

1. **Native-First & Zero-Cost Baseline**: Memanfaatkan kapabilitas monitoring bawaan platform (Google Play Console Android Vitals, Supabase Dashboard Metrics, Cloudflare/Deno Deploy logs) sebelum mengadopsi layanan pemantauan berbayar pihak ketiga.
2. **Zero-Secret Logging**: Dilarang keras mencatat kata sandi, token otentikasi JWT mentah, payload kartu kredit, atau teks struk mentah ke dalam stream log mana pun.
3. **Id Korelasi Terenkripsi**: Setiap request penting menyertakan `x-request-id` unik untuk penelusuran masalah lintas layer tanpa mengekspos identitas pengguna (*PII*).

---

## 📊 2. Sumber Log, Metrik, dan Dashboard Operasional

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                         PETA DASHBOARD & SUMBER LOG SISTEM                             │
├─────────────────────┬─────────────────────┬─────────────────────┬──────────────────────┤
│ 1. Android Vitals   │ 2. Supabase DB      │ 3. Edge Functions   │ 4. Payment Gateways  │
│    Google Play Dev  │    PostgreSQL Log   │    Deno Serverless  │    QRISpy & Google   │
│    (Crash/ANR/Start)│    (Slow Query/Pool)│    (Timeout/Errors) │    (Webhook/Pending) │
└─────────────────────┴─────────────────────┴─────────────────────┴──────────────────────┘
```

| Sumber Data | Metrik / Informasi Kunci | Lokasi Dashboard | Frekuensi Tinjauan |
|---|---|---|:---:|
| **Google Play Android Vitals** | Crash rate, ANR rate, slow start, background wakeups | Google Play Console -> *Quality* -> *Android Vitals* | Harian |
| **Supabase Database Metrics** | CPU usage, RAM, Disk IOPS, Connection pool usage, Slow queries | Supabase Dashboard -> *Reports* -> *Database* | Harian |
| **Edge Functions Log** | HTTP status 5xx, invocation count, execution duration, CPU time | Supabase Dashboard -> *Edge Functions* -> *Logs* | Real-time saat insiden |
| **Supabase Storage Metrics** | Request bandwidth, storage volume, upload error count | Supabase Dashboard -> *Storage* -> *Usage* | Mingguan |
| **QRISpy Dashboard** | Transaksi pending, webhook status delivery, volume harian | Portal Resmi `https://dashboard.qrispy.id` | Harian / Real-time |
| **Google Cloud Console** | Gemini AI token usage, quota limit, request latency | Google Cloud Console -> *APIs & Services* -> *Metrics* | Mingguan |

---

## 🚨 3. Ambang Batas Peringatan (Alert Thresholds) & Matriks Insiden

| Level Insiden | Metrik Pemicu (Alert Trigger) | Ambang Batas Waspada | Dampak Bisnis | Tindakan Awal |
|:---:|---|---|---|---|
| **P0 (Kritis)** | **Android Crash Rate** | **> 1.0%** sesi terdampak crash | Pengguna tidak dapat membuka atau menggunakan aplikasi | Hentikan rollout Play Store, identifikasi stack trace, siapkan hotfix |
| **P0 (Kritis)** | **Database Unavailable** | Status 503 / koneksi terputus > 2 menit | Seluruh sinkronisasi data terhenti | Periksa status cloud Supabase, failover ke replica |
| **P0 (Kritis)** | **Webhook Signature Bypass** | Ditemukan event sukses tanpa signature sah | Risiko transaksi palsu dan bypass Pro | Nonaktifkan Edge Function webhook sementara, audit database |
| **P1 (Tinggi)** | **Edge Function Error Rate** | **> 5.0%** error dalam jendela 10 menit | Fitur OCR AI / Checkout gagal bekerja | Periksa ketersediaan kuota Gemini atau API QRISpy |
| **P1 (Tinggi)** | **Payment Webhook Delay** | Status `pending` > 30 menit setelah bayar | Pengguna Pro komplain hak belum aktif | Jalankan rekonsiliasi manual via `sync-subscription` |
| **P2 (Sedang)** | **AI Quota Burn Spike** | Pemakaian > 80% kuota bulanan provider | Risiko tagihan membengkak | Evaluasi batas kuota per akun, nyalakan circuit breaker |
| **P2 (Sedang)** | **Slow Queries** | Query transaksi > 500 ms secara konsisten | Aplikasi terasa lambat saat membuka riwayat | Periksa indeks `(user_id, transaction_at DESC)` |

---

## 🔍 4. Prosedur Investigasi Log

### A. Melacak Kegagalan Ekstraksi AI Cloud:
1. Buka Supabase Dashboard -> **Edge Functions** -> `extract-fuel-receipt` -> **Logs**.
2. Filter berdasarkan status code `500` atau string `[AI_EXTRACTION_ERROR]`.
3. Periksa penyebab: apakah *Rate Limit (HTTP 429)*, *Model Timeout*, atau *Invalid API Key*.
4. Pastikan token kuota pengguna di-refund otomatis oleh RPC `finalize_ai_quota`.

### B. Melacak Transaksi Pembayaran Menggantung (Pending):
1. Buka Supabase SQL Editor.
2. Jalankan kueri pelacakan transaksi menggantung:
   ```sql
   SELECT id, user_id, order_id, payment_reference, amount, status, created_at
   FROM payment_transactions
   WHERE status = 'pending' AND created_at < NOW() - INTERVAL '30 minutes'
   ORDER BY created_at DESC;
   ```
3. Cek log webhook pada tabel `payment_webhook_events` untuk mencocokkan apakah ada webhook yang tertunda atau salah signature.

---

## 🔒 5. Standar Kebersihan Privasi Pencatatan (Privacy Logging Policy)

Aplikasi dan backend wajib menerapkan aturan sensor ketat:

1. **Sensor Kredensial**:
   ```kotlin
   // Digunakan pada klien Android (AppConfig.kt)
   fun redactedKey(key: String): String {
       if (key.length <= 8) return "***"
       return "${key.take(4)}...${key.takeLast(4)}"
   }
   ```
2. **Tidak Ada Rekaman Foto Struk Mentah di Log**: URL foto hanya dicatat berupa ID berkas (`file_id`), bukan signed URL lengkap yang dapat diakses publik.
3. **Penyimpanan Log**: Log Edge Functions dan Supabase otomatis dirotasi sesuai kebijakan retensi platform (standar: 7 hari untuk Free/Pro, 30 hari untuk Enterprise).
