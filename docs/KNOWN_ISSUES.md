# ⚠️ Batasan Teknis & Isu yang Diketahui (KNOWN_ISSUES.md)

Dokumen ini memuat catatan teknis, batasan arsitektur, dan dependensi lingkungan eksternal yang perlu diperhatikan oleh pengembang, tim QA, dan tim operasional **Mari Partner Fuel** (*Versi 1.7.0, Release Candidate 1*).

---

## 📌 1. Kredensial & Lingkungan Eksternal (Production Blockers)

Semua komponen kode telah siap dan teruji menggunakan sandbox/fixtures. Transaksi dan integrasi produksi riil memerlukan penyediaan kredensial di sisi server (Supabase Secrets Vault):

| Provider / Layanan | Variabel / Kredensial | Dampak Jika Belum Terpasang |
|---|---|---|
| **Google Cloud / Play Developer** | `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Edge Function `verify-play-purchase` menggunakan validator internal sandboxing. Untuk transaksi Play Store uang riil, Service Account Google Play Console wajib di-setup. |
| **Google Cloud OAuth** | `GOOGLE_WEB_CLIENT_ID` (di `local.properties`) | Google Sign-In Credential Manager dinonaktifkan secara anggun (*graceful fallback*) ke Email/Password jika Web Client ID belum disetel. |
| **QRISpy Gateway** | `QRISPY_API_KEY` & `QRISPY_WEBHOOK_SECRET` | Checkout QRISpy menggunakan simulasi QR dinamis internal. Transaksi QRIS perbankan riil membutuhkan registrasi akun bisnis dan KYC di `qrispy.id`. |
| **Google Gemini Cloud AI** | `GEMINI_API_KEY` (di Supabase Secrets) | Ekstraksi teks struk mengandalkan Google ML Kit lokal (100% on-device). Tombol "Optimalkan dengan AI Cloud" menampilkan notifikasi ramah bahwa AI Cloud sedang dalam konfigurasi administrator. |

---

## 📱 2. Batasan Latar Belakang Android (Doze Mode & Reboot)

1. **Penjadwalan Alarm Setelah Reboot:**
   - Android secara default membatalkan seluruh alarm `AlarmManager` ketika perangkat di-reboot.
   - **Mitigasi Saat Ini:** Seluruh pengingat aktif dijadwalkan ulang secara otomatis saat pengguna membuka layar Pengingat (`/reminders`).
   - **Status Tahap 9 & 10:** BroadcastReceiver `BootCompletedReceiver` disiapkan untuk mendaftarkan ulang jadwal pengingat servis begitu sistem selesai boot tanpa mewajibkan pengguna membuka UI.
2. **Manajemen Daya Agresif (OEM Battery Savers):**
   - Pabrikan seperti Xiaomi (MIUI/HyperOS), Huawei, dan Samsung dapat menunda alarm `RTC_WAKEUP` pada mode hemat daya ekstrem.
   - **Mitigasi Saat Ini:** Menggunakan `setAndAllowWhileIdle()` pada Android 6.0+ untuk meminimalkan penundaan alarm.
3. **Android 12+ (API 31+) Exact Alarm Permission:**
   - Android 12 memperkenalkan pembatasan `SCHEDULE_EXACT_ALARM`. Jika pengguna mencabut izin ini di Pengaturan Sistem, aplikasi secara anggun beralih menggunakan alarm inexact (`setWindow` / `setAndAllowWhileIdle`) dengan toleransi beberapa menit agar tidak memicu crash `SecurityException`.

---

## 🗄️ 3. Skema Basis Data & Kunci Asinkron

1. **Composite Foreign Keys:**
   - Seluruh relasi kepemilikan data (`fuel_transactions`, `maintenance_reminders`) mewajibkan `user_id` bersama `vehicle_id` untuk mencegah BOLA/IDOR. Penghapusan akun secara permanen memicu `ON DELETE CASCADE` pada data turunan yang sah.
2. **Penghapusan Kendaraan Ber-Riwayat:**
   - Kendaraan yang sudah memiliki riwayat transaksi BBM tidak dapat dihapus (`ON DELETE RESTRICT`) demi menjaga integritas laporan finansial. Pengguna diarahkan untuk mengarsipkan kendaraan (`is_archived = true`).
3. **Revokasi Hak Eksekusi Fungsi Otoritatif (Migrasi 008):**
   - Fungsi basis data `activate_subscription_for_payment`, `handle_subscription_lifecycle_event`, dan `record_payment_webhook_event` telah dicabut dari `public, anon, authenticated` dan hanya dapat dipanggil oleh `service_role` dari Supabase Edge Functions yang terverifikasi.

---

## 🔒 4. Evaluasi Keamanan OWASP

1. **Spreadsheet Formula Injection:**
   - Seluruh teks input yang diekspor ke CSV dinetralkan jika diawali karakter `=`, `+`, `-`, `@`, `\t`, `\r` dengan menambahkan petik tunggal (`'`).
   - Angka bertanda sah (seperti `+50000` atau `-1500`) dideteksi secara numerik dan tidak dirusak formatnya.
2. **Signed URL Lifetime:**
   - Akses foto struk dari private bucket storage dibatasi dengan URL berumur 15 menit. Tampilan gambar di cache klien Android diperbarui secara transparan jika URL kedaluwarsa.
3. **Anti-Replay Token Purchase:**
   - Token pembelian Google Play yang sudah digunakan dilarang untuk dipakai ulang pada akun yang sama maupun akun lain via pengecekan status `isPaid` dan hash token di database.

---

## 📋 5. Batasan Verifikasi Lingkungan Produksi (Audit Tahap 10)

1. **Akses Dashboard Produksi Supabase**: Audit statis dan verifikasi skema migrasi telah tuntas. Namun, pengujian beban riil pada compute cluster produksi Supabase hanya dapat diverifikasi setelah akun organisasi produksi aktif.
2. **Merchant Live Transaksi Bank**: Penagihan QRIS dengan uang riil via QRISpy dan auto-renewing Google Play Billing memerlukan penyelesaian proses KYC entitas bisnis perusahaan pada masing-masing provider.
3. **Tautan Dokumen Legal Publik**: URL `https://maripartner.id/privacy-policy` dan `https://maripartner.id/delete-account` wajib di-hosting di server landing page publik sebelum pengajuan rilis ke Google Play Store.
