# 💳 PAYMENTS.md — Sistem Pembayaran Dual-Track, Play Billing & QRISpy Gateway

Dokumen ini adalah spesifikasi arsitektur teknis dan panduan operasional terpadu sistem pembayaran **Mari Partner Fuel** (*Dual-Track Architecture*). Dokumen ini mencakup integrasi **Google Play Billing Library v7.1.1** untuk distribusi resmi Play Store, **QRISpy Dynamic Payment Gateway** untuk distribusi web/direct, verifikasi otoritatif sisi server (*server-authoritative*), idempotensi webhook, serta prosedur rekonsiliasi dan pengembalian dana (*refund*).

---

## 🏛️ 1. Arsitektur Jalur Pembayaran Ganda (Dual-Track Architecture)

Aplikasi menerapkan pemisahan jalur pembayaran secara ketat sesuai regulasi distribusi aplikasi:

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                   DUAL-TRACK PAYMENT SYSTEM — MARI PARTNER FUEL                        │
├───────────────────────────────────────────┬────────────────────────────────────────────┤
│ JALUR A: GOOGLE PLAY BILLING (STORE TRACK)│ JALUR B: QRISPY DYNAMIC (DIRECT TRACK)     │
├───────────────────────────────────────────┼────────────────────────────────────────────┤
│ • Distribusi: Resmi Google Play Store     │ • Distribusi: Direct APK / Web / Landing   │
│ • Pustaka: Play Billing Library v7.1.1    │ • Gateway: Dynamic QRIS API (qrispy.id)    │
│ • Tipe Produk: Auto-Renewing Subscription │ • Tipe Produk: One-Time Period Access      │
│ • Masa Berlaku: Bulanan / Tahunan         │ • Masa Berlaku: 30 Hari / 365 Hari         │
│ • Verifikasi: Edge Function Server-Side   │ • Verifikasi: Webhook Signature HMAC-SHA256│
│ • Anti-Replay: Token Hash Dedup in DB     │ • Idempotensi: Dedup di webhook table      │
│ • Kepatuhan Play Store: 100% COMPLIANT    │ • Bebas biaya Google Play Store (15%)      │
└───────────────────────────────────────────┴────────────────────────────────────────────┘
```

---

## ⚙️ 2. Konfigurasi Paket & Harga Otoritatif (Server-Authoritative Pricing)

Nominal harga langganan dikendalikan 100% oleh tabel basis data `subscription_plans`. Klien Android **dilarang mengirimkan nominal uang buatan sendiri**:

| ID Paket (Plan ID) | SKU Play Store | Durasi | Harga Resmi (IDR) | Kuota Ekstraksi AI | Batas Kendaraan |
|---|---|:---:|:---:|:---:|:---:|
| **`free`** | — | Selamanya | **Rp 0** | 5 scan / bulan | Maksimal 2 Kendaraan |
| **`pro_monthly`** | `mari_pro_monthly` | 1 Bulan | **Rp 29.000** | 100 scan / bulan | **Tanpa Batas** |
| **`pro_yearly`** | `mari_pro_yearly` | 1 Tahun | **Rp 290.000** *(Hemat 17%)* | 100 scan / bulan | **Tanpa Batas** |

---

## 🔄 3. Siklus Hidup Langganan & Mesin Status (Subscription State Machine)

```text
[PENDING] ──(Bayar Valid)──> [ACTIVE] ──(Habis Masa Aktif)──> [GRACE_PERIOD (3 Hari)]
                                 │                                    │
                         (Refund/Batal)                        (Lewat 3 Hari)
                                 ▼                                    ▼
                            [CANCELLED]                           [EXPIRED]
```

1. **`pending`**: Order pembayaran telah dibuat, menunggu konfirmasi perbankan / verifikasi Play Billing.
2. **`active`**: Transaksi diverifikasi berhasil oleh backend. Pengguna memiliki akses penuh fitur Pro dan kuota AI (100 scans/bln).
3. **`grace_period`**: Masa aktif telah berakhir namun sistem memberikan masa tenggang toleransi **3 hari** untuk memberi kesempatan pengguna memperpanjang tanpa kehilangan akses instan.
4. **`expired`**: Masa tenggang berakhir. Akun secara anggun kembali ke **Free Tier** (maksimal 2 kendaraan aktif dan 5 AI scans/bln). Seluruh riwayat transaksi BBM tetap tersimpan aman.
5. **`cancelled`**: Langganan dibatalkan oleh pengguna atau dana dikembalikan (*refund*).

---

## 🛡️ 4. Standar Keamanan, Idempotensi & Anti-Replay Token

### 4.1 Verifikasi Sisi Server & Anti-Replay Google Play
- Klien menerima `purchaseToken` dari Google Play Billing Library.
- Token dikirim ke Supabase Edge Function `verify-play-purchase`.
- Edge Function memeriksa apakah token sudah pernah berstatus `paid` di tabel `payment_transactions`.
- Jika token dikirimkan ulang, server menolak dengan error `HTTP 409 (PURCHASE_TOKEN_ALREADY_REDEEMED)`.

### 4.2 Idempotensi Webhook QRISpy (HMAC-SHA256)
- Gateway QRISpy mengirim notifikasi ke Edge Function `payment-webhook`.
- Header `x-qrispy-signature` diverifikasi menggunakan rahasia `QRISPY_WEBHOOK_SECRET`.
- Tabel `payment_webhook_events` memiliki *unique constraint* `(payment_reference, event_type)`. Notifikasi duplikat diabaikan dengan aman tanpa memicu perpanjangan ganda masa aktif Pro.

---

## 🔍 5. Prosedur Investigasi & Rekonsiliasi Transaksi

### 5.1 Melacak Transaksi Menggantung (Pending > 15 Menit)
Jalankan kueri audit di Supabase SQL Editor:
```sql
SELECT pt.id, pt.user_id, pt.order_id, pt.payment_reference, pt.amount, pt.status, pt.created_at
FROM payment_transactions pt
WHERE pt.status = 'pending' AND pt.created_at < NOW() - INTERVAL '15 minutes'
ORDER BY pt.created_at DESC;
```

### 5.2 Menjalankan Sinkronisasi Manual (Edge Function `sync-subscription`)
Jika ada pengguna yang telah membayar via QRIS tetapi hak Pro belum aktif akibat gangguan jaringan webhook:
```bash
curl -X POST https://<PROJECT_REF>.supabase.co/functions/v1/sync-subscription \
     -H "Authorization: Bearer <SERVICE_ROLE_KEY>" \
     -H "Content-Type: application/json" \
     -d '{"payment_reference": "<REF_NUMBER>"}'
```

### 5.3 Prosedur Pengembalian Dana (Refund SOP)
- **Google Play**: Pengguna meminta refund melalui Google Play Order History. Notifikasi developer `SUBSCRIPTION_REVOKED` otomatis menandai transaksi `refunded` dan mengembalikan akun ke status Free.
- **QRISpy Direct**: Pembatalan manual diproses oleh bagian keuangan perusahaan setelah verifikasi identitas pengguna, dan status di database diubah:
  ```sql
  UPDATE payment_transactions SET status = 'refunded' WHERE order_id = '<ORDER_ID>';
  UPDATE subscriptions SET status = 'cancelled' WHERE user_id = '<USER_ID>';
  ```

---

## 📈 6. Rekonsiliasi Finansial Harian (Daily Reconciliation)

```sql
SELECT 
    payment_provider,
    COUNT(*) AS total_transaksi,
    SUM(amount) AS total_penerimaan_bruto
FROM payment_transactions
WHERE status = 'paid'
  AND updated_at >= CURRENT_DATE
GROUP BY payment_provider;
```
Cocokkan angka bruto dengan saldo mutasi rekening bank merchant QRISpy dan laporan Google Play Console Financial Reports.
