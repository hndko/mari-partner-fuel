# AGENTS.md --- Mari Partner Fuel

## 1. Misi

Bangun Mari Partner Fuel sesuai `docs/PRD.md`: aplikasi Android Native
Kotlin untuk pencatatan pengeluaran BBM dengan OCR hybrid, database
Supabase online, autentikasi email/Google, dan langganan Pro yang
pembayaran default-nya QRISpy.

## 2. Sumber Kebenaran

-   `docs/PRD.md` adalah spesifikasi produk utama.
-   Dokumen arsitektur, database, keamanan, dan pembayaran tidak boleh
    bertentangan dengan PRD.
-   Jika persyaratan bertentangan atau ada informasi provider yang belum
    tersedia, jangan menebak. Dokumentasikan pertanyaan/risiko dan pilih
    implementasi paling aman.
-   Jangan mengklaim integrasi aktif sebelum kredensial dan pengujian
    nyata tersedia.

## 3. Keputusan Teknologi yang Dikunci

-   App: Mari Partner Fuel.
-   Android Native, Kotlin, Jetpack Compose, Material 3.
-   Minimum SDK: API 26.
-   Arsitektur: MVVM + Repository, domain/use case untuk aturan bisnis
    yang perlu diuji.
-   Backend: Supabase Auth, PostgreSQL, private Storage, RLS, Edge
    Functions.
-   Login: email/password + Google Sign-In.
-   OCR: Google ML Kit lokal dahulu, AI cloud fallback.
-   Monetisasi: Free + subscription Pro bulanan/tahunan.
-   Payment gateway default: QRISpy; adapter alternatif Midtrans dan
    Xendit.
-   Tidak memerlukan VPS terkelola sendiri untuk MVP.

## 4. Workflow Sebelum Coding

1.  Periksa semua file repository dan status Git sebelum mengubah apa
    pun.
2.  Baca PRD dan dokumentasi yang relevan.
3.  Buat checklist persyaratan dan petakan file/modul yang terpengaruh.
4.  Periksa versi Gradle/Kotlin/dependency dan kompatibilitas API 26.
5.  Cari implementasi yang sudah ada sebelum membuat duplikasi.
6.  Untuk perubahan database, buat migration baru; jangan mengedit
    migration yang sudah diterapkan di lingkungan bersama tanpa rencana
    migrasi yang aman.
7.  Gunakan hasil test/build untuk memandu perbaikan, bukan asumsi.

## 5. Aturan Kode Android

-   Ikuti Kotlin idioms dan struktur package konsisten.
-   UI Compose tidak boleh memuat logika bisnis berat atau akses
    jaringan langsung.
-   Gunakan ViewModel, immutable UI state, dan event yang jelas.
-   Operasi asinkron memakai Coroutines/Flow sesuai kebutuhan.
-   Pisahkan model domain, DTO API, dan model UI jika diperlukan.
-   Sediakan loading, empty, error, retry, dan success states.
-   Jangan melakukan blocking I/O pada main thread.
-   Hindari N+1 requests dan pengambilan semua transaksi tanpa
    pagination.
-   Tambahkan unit test untuk perhitungan dan validasi.
-   Hormati lifecycle, izin kamera/galeri/notifikasi, dan minimum API
    26.
-   Jangan memasukkan fitur, tombol, atau halaman dummy sebagai fitur
    selesai.

## 6. Aturan OCR dan AI

-   ML Kit berjalan terlebih dahulu untuk OCR lokal.
-   AI cloud hanya melalui Edge Function/backend; jangan memanggil
    provider AI langsung dari APK dengan secret.
-   Buat interface seperti `ReceiptAiProvider` untuk mengisolasi
    provider.
-   Validasi autentikasi, ukuran gambar, rate limit, dan kuota sebelum
    panggilan AI.
-   Kirim data minimum yang dibutuhkan dan dokumentasikan pemrosesan
    foto oleh pihak ketiga.
-   Parse output ke schema ketat; tolak output malformed.
-   Jangan langsung menyimpan hasil AI. Tampilkan review dan minta
    konfirmasi pengguna.
-   Jangan menimpa nilai sumber secara diam-diam saat perhitungan tidak
    cocok.
-   Jika AI gagal/kuota habis, berikan OCR lokal dan input manual
    sebagai fallback.
-   Jangan mengklaim akurasi 100% atau AI gratis tanpa batas.

## 7. Aturan Supabase dan Database

-   Semua schema changes harus berupa migration SQL yang tersimpan di
    repository.
-   RLS harus diaktifkan untuk seluruh tabel yang dapat diakses client.
-   Gunakan `auth.uid()` untuk kebijakan kepemilikan.
-   Validasi kendaraan dan file terkait benar-benar dimiliki user yang
    menyimpan transaksi.
-   Gunakan private Storage untuk struk dan signed URL berumur pendek.
-   Jangan pernah menaruh Supabase service-role key di aplikasi Android.
-   Jangan mempercayai `user_id`, role, harga plan, kuota AI, atau
    status pembayaran yang dikirim client tanpa validasi server.
-   Buat index untuk pola query aktual dan gunakan pagination.
-   Sertakan constraint, foreign key, dan unique constraint yang
    melindungi integritas.
-   Uji RLS dengan setidaknya dua akun berbeda.
-   Operasi privileged harus dilakukan melalui Edge Function atau
    mekanisme server-side yang dibatasi haknya.

## 8. Aturan Pembayaran dan Subscription

-   QRISpy adalah provider default; Midtrans dan Xendit harus mengikuti
    interface provider yang sama.
-   Semua merchant token/API secret hanya di backend secret store.
-   Harga paket yang ditagihkan diambil dari konfigurasi server, bukan
    nominal yang dipercaya dari client.
-   Return URL/redirect bukan bukti pembayaran.
-   Verifikasi webhook sesuai dokumentasi resmi provider; verifikasi
    status dari API provider bila diperlukan.
-   Cocokkan provider reference, payment reference, nominal, mata uang,
    dan status.
-   Implementasikan idempotency sehingga webhook replay tidak
    menggandakan transaksi atau aktivasi.
-   Status `pending`, `expired`, `cancelled`, `failed`, atau nominal
    mismatch tidak mengaktifkan Pro.
-   Simpan payment order dan subscription secara terpisah.
-   Pro hanya aktif setelah backend memverifikasi pembayaran yang sah.
-   Ketika Pro habis, jangan menghapus atau mengunci riwayat pengguna.
-   Jika dokumentasi webhook/signature provider belum tersedia, tandai
    integrasi belum siap produksi; jangan membuat skema verifikasi
    palsu.
-   Tinjau kebijakan pembayaran Google Play yang berlaku sebelum rilis.

## 9. Keamanan dan Privasi

-   Jangan commit `.env`, token, credential JSON, service-role key, atau
    merchant secret.
-   Sediakan `.env.example` hanya dengan placeholder.
-   Validasi semua input di client dan backend.
-   Terapkan rate limit pada endpoint AI dan pembayaran.
-   Redaksi data sensitif dari log.
-   Jangan menyimpan URL bertanda tangan atau token ke log permanen.
-   Jangan mengirim nomor polisi/profil ke AI jika tidak diperlukan.
-   Sediakan alur penghapusan akun, data, dan file terkait.
-   Jangan mengklaim data terenkripsi end-to-end kecuali benar-benar
    diimplementasikan.
-   Jangan mengklaim layanan cloud gratis tanpa batas.

## 10. Testing dan Quality Gate

Sebelum menyatakan tahap selesai: 1. Jalankan unit tests terkait. 2.
Jalankan lint/static checks yang tersedia. 3. Build debug. 4. Jalankan
build release pada milestone release. 5. Jalankan migration/database
checks jika schema berubah. 6. Jalankan security tests untuk RLS,
Storage, secret exposure, dan payment state. 7. Catat hasil test aktual;
jangan mengarang hasil. 8. Perbaiki kegagalan yang terkait perubahan
sebelum melanjutkan.

Kasus penting: - API 26 minimum. - OCR buram/ambigu. - AI provider
timeout/kuota habis. - transaksi duplikat. - akses lintas akun. -
private receipt URL. - payment pending/expired/replay/nominal
mismatch. - Pro berakhir dan data tetap bisa diakses sesuai kebijakan
Free.

## 11. Urutan Implementasi

Fase 1: audit repository, setup build, design system, navigation,
compatibility API 26.\
Fase 2: Supabase Auth email dan Google, migrations, RLS, private
Storage.\
Fase 3: kendaraan dan transaksi manual.\
Fase 4: OCR lokal, review screen, AI provider adapter, Edge Function.\
Fase 5: riwayat, dashboard, analitik, CSV, reminders.\
Fase 6: plans, subscriptions, QRISpy, webhook/status verification,
idempotency.\
Fase 7: adapter Midtrans/Xendit bila kredensial tersedia, QA, security,
release docs.

Jangan melewati tahap yang gagal. Jika ada dependency eksternal yang
belum dapat diuji, buat adapter dan test mock yang jelas, lalu catat
integrasi nyata sebagai blocker.

## 12. Dokumentasi Wajib

Pertahankan: - `README.md`: setup lokal, build, test, konfigurasi. -
`docs/PRD.md`: persyaratan produk. - `docs/ARCHITECTURE.md`: komponen
dan aliran data. - `docs/DATABASE.md`: tabel, relasi, index, RLS. -
`docs/SECURITY.md`: secrets, Storage, auth, threat model. -
`docs/PAYMENT_FLOWS.md`: order, webhook, status, idempotency. -
`supabase/migrations/`: seluruh migration. - `.env.example`: nama
konfigurasi tanpa secret asli.

## 13. Format Laporan Setiap Fase

Berikan laporan singkat berisi: - Yang diimplementasikan. - File penting
yang dibuat/diubah. - Migration atau konfigurasi yang diperlukan. -
Perintah test/build yang dijalankan dan hasil aktual. - Acceptance
criteria yang lulus/gagal. - Blocker atau kredensial eksternal yang
masih dibutuhkan. - Risiko dan langkah berikutnya.

## 14. Definisi Selesai

Fitur selesai hanya jika implementasi nyata, acceptance criteria terkait
lulus, dokumentasi diperbarui, dan tidak ada secret yang terekspos.
Jangan menyatakan proyek siap produksi sebelum review keamanan,
pembayaran, privasi, dan kebijakan Google Play selesai.
