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
- **Data riwayat pengeluaran, kendaraan, dan transaksi pengguna TETAP AMAN dan dapat dibaca penuh.**
- Aplikasi beralih ke batas fitur tier Free (maksimal 2 kendaraan aktif untuk penambahan baru, kuota AI bulanan tier Free). Tidak ada penguncian data secara sepihak.
