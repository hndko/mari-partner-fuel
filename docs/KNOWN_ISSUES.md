# ⚠️ Batasan Teknis & Isu yang Diketahui (KNOWN_ISSUES.md)

Dokumen ini memuat catatan teknis, batasan arsitektur, dan dependensi lingkungan eksternal yang perlu diperhatikan oleh pengembang, tim QA, dan tim operasional **Mari Partner Fuel**.

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
   - **Solusi Masa Depan (Tahap 9):** Menambahkan `BOOT_COMPLETED` BroadcastReceiver dan WorkManager periodic sync jika diperlukan peringatan tanpa interaksi buka aplikasi.
2. **Manajemen Daya Agresif (OEM Battery Savers):**
   - Pabrikan seperti Xiaomi (MIUI/HyperOS), Huawei, dan Samsung dapat menunda alarm `RTC_WAKEUP` pada mode hemat daya ekstrem.
   - **Mitigasi Saat Ini:** Menggunakan `setAndAllowWhileIdle()` pada Android 6.0+ untuk meminimalkan penundaan alarm.

---

## 🗄️ 3. Skema Basis Data & Kunci Asinkron

1. **Composite Foreign Keys:**
   - Seluruh relasi kepemilikan data (`fuel_transactions`, `maintenance_reminders`) mewajibkan `user_id` bersama `vehicle_id` untuk mencegah BOLA/IDOR. Penghapusan akun secara permanen memicu `ON DELETE CASCADE` pada data turunan yang sah.
2. **Penghapusan Kendaraan Ber-Riwayat:**
   - Kendaraan yang sudah memiliki riwayat transaksi BBM tidak dapat dihapus (`ON DELETE RESTRICT`) demi menjaga integritas laporan finansial. Pengguna diarahkan untuk mengarsipkan kendaraan (`is_archived = true`).

---

## 🔒 4. Evaluasi Keamanan OWASP

1. **Spreadsheet Formula Injection:**
   - Seluruh teks input yang diekspor ke CSV dinetralkan jika diawali karakter `=`, `+`, `-`, `@`, `\t`, `\r` dengan menambahkan petik tunggal (`'`).
   - Angka bertanda sah (seperti `+50000` atau `-1500`) dideteksi secara numerik dan tidak dirusak formatnya.
2. **Signed URL Lifetime:**
   - Akses foto struk dari private bucket storage dibatasi dengan URL berumur 15 menit. Tampilan gambar di cache klien Android diperbarui secara transparan jika URL kedaluwarsa.
