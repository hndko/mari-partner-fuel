# SECURITY.md --- Mari Partner Fuel

**Kebijakan Keamanan, Manajemen Secret, Proteksi Data & Model Ancaman**\
**Aplikasi:** Mari Partner Fuel

---

## 1. Prinsip Utama Keamanan

1. **Zero Secret Exposure:** Tidak ada kredensial backend, Supabase service-role key, ataupun API secret payment gateway (QRISpy / Midtrans / Xendit) di dalam source code, Git history, file assets, maupun APK Android.
2. **Prinsip Hak Akses Terkecil (*Least Privilege*):** Aplikasi Android hanya memegang `SUPABASE_ANON_KEY` dengan hak yang dibatasi secara ketat oleh Row Level Security (RLS).
3. **Validasi Dua Sisi (*Client & Server Validation*):** Setiap input divalidasi di Android untuk pengalaman pengguna (*fail fast*) dan divalidasi ulang di backend/PostgreSQL sebelum penyimpanan.
4. **Data Minimization untuk AI:** Ketika mengirim gambar struk ke backend Edge Function untuk pemrosesan AI, nomor polisi, profil pribadi, dan identitas pengguna tidak pernah diteruskan ke provider AI pihak ketiga.

---

## 2. Manajemen Secret & Konfigurasi Lingkungan

### Konfigurasi Klien Android
- Variabel publik (`SUPABASE_URL` dan `SUPABASE_ANON_KEY`) diinjeksikan melalui build system (`BuildConfig`).
- Pengembang lokal menggunakan `local.properties` atau file `.env` lokal yang **wajib di-ignore oleh Git**.
- File `.env.example` disediakan sebagai acuan tanpa nilai rahasia nyata.

```properties
# Contoh local.properties (tidak di-commit ke Git)
supabase.url=https://your-project.supabase.co
supabase.anon.key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Konfigurasi Backend (Supabase Edge Functions)
- Kredensial rahasia (`SUPABASE_SERVICE_ROLE_KEY`, `QRISPY_API_TOKEN`, `MIDTRANS_SERVER_KEY`, `GEMINI_API_KEY`) disimpan di Supabase Secret Manager.
- Edge Functions memvalidasi token JWT pengguna (`Authorization: Bearer <jwt>`) sebelum mengeksekusi operasi privileged (seperti membuat order pembayaran atau memproses struk via AI).

---

## 3. Penyimpanan Struk & Akses Gambar Privat

1. **Private Storage Bucket:** File struk disimpan di bucket `receipts` berstatus privat (*not publicly readable*).
2. **Pemisahan Jalur Penyimpanan:**
   Path file mengikuti format: `receipts/{user_id}/{uuid}.jpg`.
3. **Signed URLs Berumur Pendek:**
   Aplikasi menampilkan struk hanya melalui URL bertanda tangan (*signed URL*) berumur pendek (maksimal 15-30 menit). URL ini tidak disimpan permanen di database maupun log.

---

## 4. Model Ancaman & Mitigasi

| Ancaman (*Threat*) | Vektor Serangan | Mitigasi yang Diterapkan |
| :--- | :--- | :--- |
| **Akses Lintas Akun (*BOLA/IDOR*)** | Memanipulasi `vehicle_id` atau `receipt_file_id` milik akun lain | PostgreSQL RLS + Composite Foreign Key `(id, user_id)` memastikan relasi hanya sah jika dimiliki `auth.uid()`. |
| **Aktivasi Pro Palsu** | Memanipulasi URL redirect dari WebView pembayaran | Return URL hanya pemicu refresh. Aktivasi Pro hanya dilakukan server setelah verifikasi status/webhook sah dari gateway. |
| **Penyalahgunaan Kuota AI** | Spam request ke endpoint OCR AI | Validasi autentikasi, rate-limiting di Edge Function, dan pengecekan sisa kuota paket akun sebelum memanggil provider AI. |
| **Kebocoran Struk Transaksi** | Mengakses link foto struk tanpa otentikasi | Bucket private storage; akses hanya melalui signed URL berumur pendek yang dihasilkan secara dinamis. |
| **Replay Webhook Pembayaran** | Mengirim payload webhook yang sama berulang kali | Tabel `payment_webhook_events` dengan *unique constraint* pada event/payment reference untuk idempotensi. |

---

## 5. Penghapusan Akun & Hak Privasi Pengguna

Sesuai regulasi privasi data:
1. Pengguna dapat mengajukan penghapusan akun melalui menu Pengaturan/Profil.
2. Proses penghapusan akun memicu cascade delete pada data `profiles`, `vehicles`, `fuel_transactions`, dan penghapusan seluruh file terkait di Supabase Storage bucket.
