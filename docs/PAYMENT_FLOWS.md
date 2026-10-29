# PAYMENT_FLOWS.md --- Mari Partner Fuel

**Arsitektur Pembayaran, Siklus Order, Webhook & Idempotensi Langganan Pro**\
**Provider Default:** QRISpy (`https://api.qrispy.id`)\
**Provider Alternatif:** Midtrans, Xendit

---

## 1. Prinsip Utama Pembayaran

1. **Server-Authoritative Pricing:** Harga paket langganan mutlak ditentukan oleh tabel `subscription_plans` di backend, bukan dari nominal yang dikirim oleh klien Android.
2. **Return URL Bukan Bukti Pembayaran:** Redirect atau kembali dari pembayaran hanya memicu pengecekan status ke backend; tidak pernah secara langsung mengaktifkan status Pro.
3. **Idempotensi Webhook:** Setiap event webhook dari payment gateway dicatat secara idempotent; duplikasi event tidak akan memperpanjang atau menggandakan aktivasi langganan.
4. **Pemisahan Order & Langganan:** Entitas pesanan pembayaran (`payment_transactions`) disimpan terpisah dari hak akses langganan (`subscriptions`).

---

## 2. Alur Pembayaran QRISpy (Default)

```
[Android Client]           [Supabase Edge Function]          [QRISpy Gateway]
       │                              │                              │
       ├──── 1. Pilih Paket (id) ────►│                              │
       │                              ├─ 2. Ambil harga dari DB      │
       │                              ├─ 3. Generate ref unik        │
       │                              ├─ 4. POST /payment/qris/gen ─►│
       │                              │◄─── 5. Return QR string ─────┤
       │◄─── 6. Tampilkan QR Code ────┤                              │
       │                              │                              │
       │  (Pengguna bayar via e-wallet)                              │
       │                              │                              │
       │                              │◄─── 7. Webhook / Polling ────┤
       │                              ├─ 8. Verifikasi status & IDR  │
       │                              ├─ 9. Aktifkan Subscriptions   │
       ├──── 10. Poll Status / Sync ─►│                              │
       │◄─── 11. Pro Status Aktif ────┤                              │
```

### Langkah Terperinci:
1. **Inisiasi Pembayaran:** Klien Android memanggil Edge Function `create-payment` dengan `plan_id`.
2. **Validasi & Pembuatan Order:** Server memverifikasi bahwa paket aktif, mengambil `price_idr`, membuat record di `payment_transactions` dengan status `created`, dan menghasilkan `payment_reference` unik (format: `MPF-{YYYYMMDD}-{UUID8}`).
3. **Panggilan ke QRISpy:** Server memanggil endpoint `POST /api/payment/qris/generate` menggunakan header rahasia `X-API-Token`.
4. **Penyajian QRIS:** Server mengembalikan data QRIS dan masa berlaku (dari respons provider) ke klien untuk ditampilkan.
5. **Verifikasi Webhook/Status:** Saat notifikasi pembayaran diterima, backend memverifikasi signature, status transaksi (`paid`), dan mencocokkan nominal persis (`amount_idr`).
6. **Aktivasi Subscription:** Backend mengaktifkan record di `subscriptions` (status: `active`, memperbarui `starts_at` dan `expires_at`).

---

## 3. Status State Machine Pembayaran

```
           ┌──────────┐
           │ Created  │
           └────┬─────┘
                │
                ▼
           ┌──────────┐
      ┌────┤ Pending  ├────┐
      │    └────┬─────┘    │
      ▼         ▼          ▼
┌──────────┐ ┌──────┐ ┌─────────┐
│ Expired  │ │ Paid │ │ Failed  │
└──────────┘ └──┬───┘ └─────────┘
                │
                ▼
         ┌─────────────┐
         │ Pro Active  │
         └─────────────┘
```

- Status `pending`, `expired`, `cancelled`, `failed`, atau nominal mismatch **TIDAK AKAN** mengaktifkan status Pro.
- Jika status `expired`, pengguna dapat membuat order pembayaran baru.

---

## 4. Idempotensi & Keamanan Webhook

```sql
create table public.payment_webhook_events (
  id uuid primary key default gen_random_uuid(),
  provider text not null,
  event_id text not null,
  payment_reference text not null,
  payload jsonb not null,
  processed_at timestamptz not null default now(),
  unique (provider, event_id)
);
```

Sebelum memproses aktivasi:
1. Backend memeriksa apakah `(provider, event_id)` sudah ada di `payment_webhook_events`.
2. Jika sudah ada, backend langsung mengembalikan respons `HTTP 200 OK` tanpa memproses ulang.
3. Jika belum ada, proses verifikasi dijalankan dalam transaksi database atomik.

---

## 5. Kebijakan Berakhirnya Langganan (*Graceful Expiry*)

Ketika periode langganan Pro berakhir:
- Status langganan berubah menjadi `expired`.
- Riwayat transaksi BBM dan data kendaraan pengguna **TIDAK DIHAPUS** atau dikunci.
- Pengguna bertransisi kembali ke batas Free Tier (maksimal 2 kendaraan aktif untuk penambahan baru dan 5 kuota AI per bulan).

---

## 6. Evaluasi Kepatuhan Google Play Payments Policy & Strategi Billing Tahap 7

> **Tanggal Pemeriksaan:** 29 Oktober 2026  
> **Auditor:** Senior Android Engineer & Security Compliance  
> **Referensi Resmi:**
> - [Google Play Payments Policy](https://support.google.com/googleplay/android-developer/answer/9858738)
> - [Google Play Billing — Integrasi Backend](https://developer.android.com/google/play/billing/backend)
> - [Google Play Billing — Perlindungan Pembelian](https://developer.android.com/google/play/billing/security)

### 6.1 Analisis Kewajiban Google Play Billing
1. **Definisi Produk:** Mari Partner Fuel menjual produk digital murni berupa langganan fitur cloud (*cloud AI receipt extraction*, kuota ekstraksi, dan analitik performa lanjutan).
2. **Kewajiban Distribusi Play Store:** Berdasarkan Bagian *Monetization and Ads — Payments Policy*, pengembang yang mendistribusikan aplikasi melalui Google Play Store yang menawarkan fitur digital atau langganan dalam aplikasi **wajib** menggunakan sistem penagihan Google Play (Google Play Billing System).
3. **Konsekuensi Gateway Alternatif (QRISpy / Midtrans / Xendit):**
   - Menampilkan formulir pembayaran atau QRIS pihak ketiga secara langsung di dalam aplikasi Play Store melanggar kebijakan Google Play dan berisiko ditolak (*rejection*) atau ditangguhkan (*suspension*).
   - Jalur QRISpy / Midtrans / Xendit diperbolehkan secara hukum hanya untuk:
     - **Distribusi di Luar Play Store (Direct APK / Web Portal / Sideloading Enterprise)**.
     - **User Choice Billing (UCB) / Alternative Billing Programs** jika telah mendaftar dan disetujui oleh Google untuk wilayah Indonesia.
4. **Keputusan Arsitektur Tahap 7:**
   - **Play Store Build Variant:** Menggunakan Google Play Billing Library di klien Android + verifikasi server di Supabase Edge Function via Google Play Developer API (`purchases.subscriptionsv2`).
   - **Direct / Web Variant:** Menggunakan adapter QRISpy untuk pembelian langsung di portal web Mari Partner.
   - **Zero-Mock Purchase di Tahap 6:** Pada Tahap 6, tombol langganan menampilkan dialog informatif "*Segera Hadir di Tahap 7*" tanpa simulasi transaksi palsu, menjamin integritas data dan ketaatan regulasi.

### 6.2 Desain Verifikasi Backend Google Play Billing (Tahap 7)
```
[Android Client]                  [Supabase Edge Function]          [Google Play API]
       │                                     │                              │
       ├─ 1. Beli via Play Billing ─────────►│                              │
       │  (Dapatkan purchaseToken)           │                              │
       ├─ 2. Kirim purchaseToken & plan_id ─►│                              │
       │                                     ├─ 3. Verifikasi token ───────►│
       │                                     │◄─── 4. Status valid & masa ──┤
       │                                     ├─ 5. Simpan token (anti-replay)
       │                                     ├─ 6. Aktifkan record Pro      │
       │◄─ 7. Response Entitlement Aktif ────┤                              │
```
- Server menyimpan `purchase_token` dengan indeks unik `(provider, provider_payment_id)` untuk mencegah serangan *purchase token replay*.
- Real-Time Developer Notifications (RTDN) melalui Google Cloud Pub/Sub akan memicu Supabase Edge Function webhook untuk perpanjangan otomatis (*renewal*), pembatalan (*cancellation*), atau kedaluwarsa (*expiry*).

- **Data riwayat pengeluaran, kendaraan, dan transaksi pengguna TETAP AMAN dan dapat dibaca penuh.**
- Aplikasi beralih ke batas fitur tier Free (maksimal 2 kendaraan aktif untuk penambahan baru, kuota AI bulanan tier Free). Tidak ada penguncian data secara sepihak.
