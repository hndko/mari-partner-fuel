# 💳 PLAY_BILLING_COMPLIANCE.md — Kepatuhan Kebijakan Pembayaran Google Play & Langganan Digital

Dokumen ini memuat audit kepatuhan kebijakan pembayaran Google Play (*Google Play Payments Policy Compliance*), penegakan penggunaan **Google Play Billing Library v7.1.1**, pemisahan arsitektur jalur ganda (*Dual-Track*), dan mitigasi risiko penolakan (*rejection*) toko aplikasi pada **Mari Partner Fuel**.

---

## 🏛️ 1. Kebijakan Resmi Google Play Payments & Analisis Kepatuhan

Berdasarkan kebijakan resmi Google Play (*Payments Policy - Digital Goods & Services*):
> *"Pengembang yang menawarkan pembelian dalam aplikasi (in-app purchases) untuk konten digital, layanan, atau langganan digital yang dapat diakses di dalam aplikasi Android yang didistribusikan melalui Google Play WAJIB menggunakan sistem penagihan Google Play (Google Play Billing System)."*

### Penilaian Model Monetisasi Mari Partner Fuel:
| Fitur / Entitlement | Kategori Barang | Wajib Google Play Billing? | Status Implementasi |
|---|:---:|:---:|:---:|
| **Paket Mari Pro Bulanan** | Layanan Langganan Digital | **YA, WAJIB** | ✅ Menggunakan Play Billing v7.1.1 |
| **Paket Mari Pro Tahunan** | Layanan Langganan Digital | **YA, WAJIB** | ✅ Menggunakan Play Billing v7.1.1 |
| **Batas Kendaraan Tidak Terbatas** | Fitur Digital In-App | **YA, WAJIB** | ✅ Diatur oleh server entitlement |
| **Kuota Ekstraksi AI 100/bln** | Fitur Layanan Digital | **YA, WAJIB** | ✅ Diatur oleh server quota meter |

---

## 🛡️ 2. Arsitektur Dual-Track & Pemisahan Distribusi yang Patuh

Aplikasi mengimplementasikan pemisahan jalur pembayaran secara modular (*Dual-Track Architecture*) untuk memastikan build Google Play Store bebas dari pelanggaran kebijakan:

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                   ARSITEKTUR DISTRIBUSI & KEPATUHAN BILLING                            │
├───────────────────────────────────────────┬────────────────────────────────────────────┤
│ JALUR A: DISTRIBUSI RESMI GOOGLE PLAY     │ JALUR B: DISTRIBUSI DIRECT / WEB PORTAL    │
├───────────────────────────────────────────┼────────────────────────────────────────────┤
│ • Build Variant: Google Play AAB          │ • Build Variant: Standalone Direct APK     │
│ • Sistem Pembayaran: Google Play Billing  │ • Sistem Pembayaran: Dynamic QRISpy        │
│ • Pustaka: BillingClient v7.1.1           │ • Gateway: API Gateway qrispy.id           │
│ • Verifikasi: Edge Function Server        │ • Verifikasi: Webhook HMAC-SHA256          │
│ • Kepatuhan Play Store: 100% COMPLIANT    │ • Tidak melibatkan Google Play Store       │
└───────────────────────────────────────────┴────────────────────────────────────────────┘
```

> ⚠️ **ATURAN WAJIB UNTUK BUILD PLAY STORE**:
> Pada paket AAB yang diunggah ke Google Play Console, tombol pembelian di layar katalog Pro (`ProPlansScreen`) dan Checkout (`CheckoutScreen`) mengarahkan pengguna secara eksklusif ke antarmuka resmi bottom-sheet **Google Play Billing**. Metode QRIS eksternal dinonaktifkan pada build varian Google Play Store untuk mencegah penolakan review Google Play (*Policy Rejection: Alternative In-App Billing*).

---

## ⚙️ 3. Konfigurasi Produk Langganan di Google Play Console

Pemilik proyek wajib mendaftarkan 2 ID Produk Langganan (*Subscription Products*) pada menu Google Play Console -> **Monetize** -> **Subscriptions**:

| ID Produk (SKU) | Nama Produk di Konsol | Periode Penagihan | Harga Resmi (IDR) | Masa Tenggang (Grace Period) |
|---|---|:---:|:---:|:---:|
| **`mari_pro_monthly`** | Mari Pro — Bulanan | 1 Bulan (Auto-Renewing) | Rp 29.000 / bln | 3 Hari |
| **`mari_pro_yearly`** | Mari Pro — Tahunan | 1 Tahun (Auto-Renewing) | Rp 290.000 / thn | 7 Hari |

### Konfigurasi Base Plan:
- Tipe Base Plan: **Auto-renewing**.
- Pajak: Sudah termasuk PPN (Nett).
- Perpanjangan otomatis dikelola langsung oleh Google Play Store.

---

## 🔒 4. Verifikasi Pembelian Sisi Server & Anti-Replay

Aplikasi **tidak pernah** mengaktifkan status Pro hanya berdasarkan respons callback lokal dari perangkat Android. Alur validasi dijalankan secara aman:

1. Klien menerima `PurchaseToken` dan `OrderId` dari Google Play Billing Library.
2. Klien mengirimkan token ke Edge Function Supabase `verify-play-purchase`.
3. Edge Function memverifikasi keaslian token melalui Google Play Developer API menggunakan Service Account terenkripsi.
4. **Pencegahan Replay Token (Anti-Replay Protection)**:
   - Hash token diperiksa terhadap tabel `payment_transactions`.
   - Token yang sudah pernah diklaim (`status = 'paid'`) akan ditolak dengan error `HTTP 409 (PURCHASE_TOKEN_ALREADY_REDEEMED)`.
5. Edge Function memanggil RPC PostgreSQL `activate_subscription_for_payment` dengan peran istimewa `service_role`.
6. Entitlement Pro aktif dan kuota AI (100 scans) diperbarui secara atomik.

---

## 🧪 5. Pengujian Tanpa Biaya Riil (License Testing Configuration)

Untuk mencegah tim penguji (*testers*) atau tim peninjau Google Play dikenakan biaya uang riil saat menguji alur langganan:

1. Masuk ke **Google Play Console** -> **Setup** -> **License Testing**.
2. Masukkan alamat email akun Gmail tester (misal: `reviewer@maripartner.id`, email tim internal).
3. Atur **License test response** ke: **`RESPOND_NORMALLY`**.
4. Tester yang terdaftar di License Testing akan melihat dialog pembelian Google Play dengan label *"Test Card, always approves"* dan penagihan berlangsung dalam interval pengujian (1 bulan dipercepat menjadi 5 menit).

---

## 🛑 6. Keputusan yang Membutuhkan Konfirmasi Pemilik Proyek

1. **Pendaftaran Entitas Pajak & Akun Pembayaran Google Play**:
   - Pemilik proyek wajib melengkapi profil pembayaran Google Payments Merchant di Play Console untuk menerima pencairan dana langganan Pro.
2. **Penyediaan Service Account Google Cloud**:
   - Pemilik proyek perlu mengaktifkan Google Play Android Developer API di Google Cloud Console dan menautkan Service Account JSON ke Supabase Vault `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`.
