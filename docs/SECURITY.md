# 🛡️ SECURITY.md — Kebijakan Keamanan, Threat Model & Hasil Audit Hardening

Dokumen ini memuat standar keamanan informasi, model ancaman (*threat modeling*), kebijakan *zero-secret*, mitigasi kerentanan OWASP Mobile Top 10, serta laporan audit pengerasan keamanan (*security hardening*) pada sistem **Mari Partner Fuel**.

---

## 🔒 1. Prinsip Utama Keamanan Sistem (Security Core Principles)

1. **Zero Secret in Client**: Dilarang keras menaruh kunci rahasia (*secret keys*), token administratif, service-role key, ataupun password di dalam kode sumber Android, manifest, APK, AAB, maupun Git repositori.
2. **PostgreSQL Row Level Security (RLS)**: Setiap tabel publik di Supabase wajib memiliki RLS aktif dengan validasi kepemilikan data `auth.uid() = user_id`.
3. **Composite Foreign Keys**: Hubungan kepemilikan data wajib melibatkan `user_id` bersama `vehicle_id` untuk mencegah serangan manipulasi identitas (*BOLA / IDOR*).
4. **Pencabutan Hak Eksekusi Otoritatif (Least Privilege)**: Fungsi finansial dan kuota AI hanya dapat dipanggil oleh peran `service_role` melalui Supabase Edge Functions.

---

## 🎯 2. Model Ancaman & Mitigasi OWASP Mobile Top 10

| Vektor Ancaman | Standar OWASP | Implementasi & Mitigasi Mari Partner Fuel | Status |
|---|---|---|:---:|
| **Hardcoded Secrets** | M9: Reverse Engineering | Zero-secret di APK/AAB; kredensial backend di Supabase Secrets Vault, sensor `AppConfig.redactedKey`. | ✅ SECURE |
| **Insecure Data Storage** | M2: Insecure Storage | Foto struk di private bucket storage Supabase; akses hanya via Signed URL (15 menit). | ✅ SECURE |
| **Broken Access Control (BOLA/IDOR)** | M1: Improper Platform Usage | PostgreSQL Row Level Security (RLS) aktif pada 13 tabel; relasi composite foreign key `(vehicle_id, user_id)`. | ✅ SECURE |
| **Privilege Escalation** | M6: Insecure Authorization | Seluruh fungsi RPC pembayaran & kuota di-revoke dari publik (`REVOKE EXECUTE ... FROM public, authenticated`). | ✅ SECURE |
| **Spreadsheet Injection** | CWE-1236: CSV Injection | Netralisasi karakter formula (`=`, `+`, `-`, `@`, `\t`, `\r`) dengan tanda petik tunggal (`'`). | ✅ SECURE |
| **Replay Attack** | CWE-294: Authentication Bypass | Edge Function `verify-play-purchase` memverifikasi hash token unik dan menolak token `paid` berulang (`HTTP 409`). | ✅ SECURE |
| **Prompt Injection** | LLM01: Prompt Injection | Pemisahan ketat tag `<untrusted_receipt_text>` dan pembatasan panjang teks maks 8.000 karakter. | ✅ SECURE |

---

## 🗄️ 3. Audit Hardening Basis Data (Migrasi 008)

Pada file migrasi `20261029000008_security_hardening_and_integrity.sql`, diterapkan pengerasan basis data secara permanen:

```sql
-- 1. Cabut hak eksekusi default dari publik dan authenticated users:
REVOKE EXECUTE ON FUNCTION activate_subscription_for_payment FROM public, anon, authenticated;
REVOKE EXECUTE ON FUNCTION record_payment_webhook_event FROM public, anon, authenticated;
REVOKE EXECUTE ON FUNCTION handle_subscription_lifecycle_event FROM public, anon, authenticated;
REVOKE EXECUTE ON FUNCTION reserve_ai_quota FROM public, anon, authenticated;
REVOKE EXECUTE ON FUNCTION finalize_ai_quota FROM public, anon, authenticated;

-- 2. Berikan izin eksklusif HANYA untuk peran service_role:
GRANT EXECUTE ON FUNCTION activate_subscription_for_payment TO service_role;
GRANT EXECUTE ON FUNCTION record_payment_webhook_event TO service_role;
GRANT EXECUTE ON FUNCTION handle_subscription_lifecycle_event TO service_role;
GRANT EXECUTE ON FUNCTION reserve_ai_quota TO service_role;
GRANT EXECUTE ON FUNCTION finalize_ai_quota TO service_role;
```

---

## 📁 4. Aturan Isolasi Private Storage Bucket (`receipts`)

- Bucket foto struk `receipts` berstatus privat (`public = false`).
- Struktur folder berjenjang: `receipts/{user_id}/{filename}`.
- Kebijakan penyimpanan storage RLS:
  ```sql
  CREATE POLICY "Users can only read own receipt files"
  ON storage.objects FOR SELECT TO authenticated
  USING (bucket_id = 'receipts' AND (storage.foldername(name))[1] = auth.uid()::text);
  ```
- Klien Android hanya menerima *Signed URL* berumur pendek (15 menit) yang digenerate oleh backend saat menampilkan gambar di layar review atau riwayat.

---

## 🔍 5. Sanitasi Spreadsheet Formula Injection (CSV Security)

Saat mengekspor riwayat transaksi ke CSV, teks yang berawalan karakter berbahaya (`=`, `+`, `-`, `@`, `\t`, `\r`) otomatis dinetralkan dengan awalan petik tunggal (`'`). Angka numerik bertanda positif/negatif yang sah tetap dipertahankan format angkanya tanpa rusak.
