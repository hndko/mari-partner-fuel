# 💳 PAYMENT_OPERATIONS.md — Prosedur Operasional Pembayaran & Siklus Langganan

Dokumen ini adalah panduan operasional penanganan transaksi pembayaran, rekonsiliasi finansial, pengelolaan status langganan (*subscription lifecycle*), dan investigasi kendala pembayaran pada sistem **Mari Partner Fuel**.

---

## 🏛️ 1. Arsitektur Jalur Pembayaran Ganda (Dual-Track Architecture)

Aplikasi menerapkan pemisahan jalur pembayaran secara ketat sesuai regulasi distribusi aplikasi:

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                   DUAL-TRACK PAYMENT SYSTEM — MARI PARTNER FUEL                        │
├───────────────────────────────────────────┬────────────────────────────────────────────┤
│ 1. Jalur Google Play Billing (Store Track)│ 2. Jalur QRISpy Dynamic (Direct Track)     │
├───────────────────────────────────────────┼────────────────────────────────────────────┤
│ • Distribusi: Resmi Google Play Store     │ • Distribusi: Direct APK / Web / Landing   │
│ • Pustaka: Play Billing Library v7.1.1    │ • Gateway: Dynamic QRIS API (qrispy.id)    │
│ • Tipe Produk: Auto-Renewing Subscription │ • Tipe Produk: One-Time Period Access      │
│ • Masa Berlaku: Bulanan / Tahunan         │ • Masa Berlaku: 30 Hari / 365 Hari         │
│ • Verifikasi: Edge Function Server-Side   │ • Verifikasi: Webhook Signature HMAC-SHA256│
│ • Anti-Replay: Token Hash Dedup in DB     │ • Idempotensi: Dedup di webhook table      │
└───────────────────────────────────────────┴────────────────────────────────────────────┘
```

---

## 🔄 2. Siklus Hidup Langganan (Subscription Lifecycle & State Machine)

Setiap entri langganan pengguna dalam tabel `subscriptions` berpindah status mengikuti aturan state machine otoritatif:

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

## 🛡️ 3. Standar Keamanan & Idempotensi Mutlak

1. **Harga Otoritatif Sisi Server (Server-Authoritative Pricing)**:
   - Klien Android dilarang mengirimkan nominal harga. Edge Function `create-checkout` mengambil harga resmi langsung dari tabel `subscription_plans`:
     - **Bulanan**: `Rp 29.000` (Nett)
     - **Tahunan**: `Rp 290.000` (Nett, Hemat 17%)
2. **Idempotensi Webhook Berulang**:
   - Gateway pembayaran sering mengirimkan webhook berkali-kali untuk transaksi yang sama.
   - Tabel `payment_webhook_events` memiliki *unique constraint* pada `(payment_reference, event_type)`.
   - RPC `record_payment_webhook_event` memverifikasi apakah event telah diproses sebelumnya. Event duplikat diabaikan dengan aman tanpa memperpanjang masa aktif Pro ganda.
3. **Pencegahan Replay Token Pembelian (Anti-Replay Protection)**:
   - Token pembelian Google Play yang berhasil diklaim dicatat dalam tabel `payment_transactions` berstatus `paid`.
   - Jika token yang sama dikirimkan ulang (baik oleh akun yang sama maupun akun penyerang), Edge Function `verify-play-purchase` menolak dengan error `HTTP 409 (PURCHASE_TOKEN_ALREADY_REDEEMED)`.

---

## 🔍 4. Prosedur Investigasi Masalah Transaksi (SOP)

### Skenario A: Pengguna Mengaku Sudah Membayar via QRIS, Status Masih Pending
1. Dapatkan **Nomor Pesanan (Order ID)** dari pengguna (misal: `MPF-20261029-ABC123XYZ`).
2. Masuk ke **Supabase SQL Editor** dan jalankan query audit:
   ```sql
   SELECT pt.id, pt.user_id, pt.amount, pt.status, pt.payment_reference,
          pwe.event_type, pwe.processed_at
   FROM payment_transactions pt
   LEFT JOIN payment_webhook_events pwe ON pt.payment_reference = pwe.payment_reference
   WHERE pt.order_id = 'MPF-20261029-ABC123XYZ';
   ```
3. Buka **Dashboard QRISpy** (`https://dashboard.qrispy.id`) -> Cari *Reference Number*.
4. Jika di QRISpy tercatat **PAID / SUCCESS**, tetapi di Supabase belum:
   - Kemungkinan webhook gagal terkirim karena timeout jaringan.
   - Jalankan fungsi rekonsiliasi manual via Edge Function `sync-subscription`:
     ```bash
     curl -X POST https://<PROJECT_REF>.supabase.co/functions/v1/sync-subscription \
          -H "Authorization: Bearer <SERVICE_ROLE_KEY>" \
          -H "Content-Type: application/json" \
          -d '{"payment_reference": "<REF_NUMBER>"}'
     ```
5. Periksa apakah status langganan di `subscriptions` telah berubah menjadi `active`.

### Skenario B: Permintaan Pengembalian Dana (Refund)
1. **Google Play Store**:
   - Pengguna mengajukan refund via Google Play Order History.
   - Google mengirimkan developer notification `SUBSCRIPTION_REVOKED`.
   - Edge Function menandai transaksi sebagai `refunded` dan menghentikan status Pro.
2. **QRISpy Direct Gateway**:
   - Transaksi QRIS adalah final (*settled* secara instan ke rekening merchant).
   - Pengembalian dana dilakukan manual melalui transfer bank oleh bagian keuangan perusahaan setelah verifikasi identitas dan persetujuan Product Owner.
   - Ubah status transaksi di database:
     ```sql
     UPDATE payment_transactions SET status = 'refunded' WHERE order_id = '<ORDER_ID>';
     UPDATE subscriptions SET status = 'cancelled' WHERE user_id = '<USER_ID>';
     ```

---

## 📈 5. Rekonsiliasi Finansial Harian (Daily Reconciliation)

Tim operasional keuangan wajib menjalankan kueri rekonsiliasi setiap akhir hari kerja:

```sql
-- Rekap transaksi sukses hari ini
SELECT 
    payment_provider,
    COUNT(*) AS total_transaksi,
    SUM(amount) AS total_penerimaan_bruto
FROM payment_transactions
WHERE status = 'paid'
  AND updated_at >= CURRENT_DATE
GROUP BY payment_provider;
```
Cocokkan angka `total_penerimaan_bruto` dengan saldo mutasi rekening bank merchant QRISpy dan laporan Google Play Console Financial Reports.
