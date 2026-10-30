# Mari Partner Fuel — UI/UX Design Reference

## Arah visual
- Modern, clean, trustworthy, ringan, dan mudah dipahami.
- Identitas mengikuti logo Mari Partner: biru navy sebagai fondasi, biru elektrik/cyan sebagai aksen, putih sebagai ruang utama.
- Hindari tampilan ramai, gradient berlebihan, glassmorphism berat, dan dekorasi yang mengganggu pembacaan data.
- Prioritaskan informasi penting, label yang jelas, dan tindakan utama yang mudah ditemukan.

## Design tokens
| Token | Nilai | Penggunaan |
|---|---|---|
| Brand navy | `#0C2456` | Sidebar/hero/teks kuat |
| Primary blue | `#087FE5` | CTA, link, selected state |
| Accent cyan | `#11A7EE` | Highlight dan aksen |
| Background | `#F5F7FB` | Latar halaman |
| Surface | `#FFFFFF` | Card/form |
| Text | `#182338` | Teks utama |
| Muted | `#788397` | Keterangan |
| Border | `#E9EDF4` | Garis dan pemisah |
| Success | `#169B70` | Status berhasil |
| Warning | `#C9871C` | Perlu perhatian |
| Danger | `#D85B55` | Aksi destruktif |

## Tipografi
Gunakan Plus Jakarta Sans atau DM Sans. Judul tegas dengan ukuran proporsional, body 14–16sp, label 12–13sp, caption minimal 11sp. Di Android gunakan skala typography Material 3 yang setara; jangan mengecilkan teks demi memuat banyak data.

## Komponen
- Primary button: biru, label berupa kata kerja spesifik.
- Secondary button: putih dengan border netral.
- Cards: radius 12–16dp, border halus, shadow sangat ringan.
- Inputs: label di atas field, placeholder berupa contoh, validasi inline.
- Status: chip warna lembut dengan teks, jangan hanya mengandalkan warna.
- Empty state: ikon sederhana, penjelasan singkat, satu CTA.
- Charts: judul dan periode jelas, label terbaca, jangan menampilkan metrik tanpa data memadai.
- Navigation: bottom navigation pada ponsel; navigation rail/drawer untuk layar lebar. Prioritaskan 4–5 destinasi utama.

## Prinsip UX
1. Satu layar memiliki satu tujuan utama dan satu CTA primer.
2. Form transaksi dibuat bertahap secara visual, tetapi jangan memaksa wizard bila form sederhana lebih cepat.
3. Pengguna selalu dapat input manual jika OCR/AI gagal atau kuota habis.
4. Hasil OCR/AI wajib ditinjau dan dikonfirmasi pengguna sebelum disimpan.
5. Gunakan format Rupiah dan tanggal Indonesia secara konsisten.
6. Tampilkan loading, empty, error, offline, dan success state.
7. Hindari data dummy pada production. Semua grafik dan total harus berasal dari data aktual.
8. Aksesibilitas: touch target ideal minimal 48dp, kontras memadai, content description untuk ikon, dukungan font scaling.
9. Desain dark theme sebagai pasangan token, bukan sekadar membalik warna.
10. Jangan mengandalkan emoji sebagai ikon final; gunakan Material Symbols/Material Icons yang konsisten.

## Peta halaman
- Auth: Splash, onboarding, login, register, forgot/reset password.
- Dashboard: ringkasan biaya, volume, kendaraan, tren, transaksi terakhir.
- Kendaraan: daftar, tambah/edit, detail, arsip.
- Transaksi: daftar/filter, tambah manual, detail/edit, konfirmasi hapus.
- OCR: izin kamera/galeri, preview, processing, review hasil, error/fallback.
- Analitik: periode, ringkasan, tren, distribusi per kendaraan, insight, ekspor CSV.
- Pengingat: daftar, tambah/edit, detail, selesai, izin notifikasi.
- Subscription: Free/Pro, benefit, harga backend, status, restore/cancel sesuai billing.
- Profil: akun, tema, notifikasi, privasi, bantuan, versi, logout.
- Global states: offline, session expired, permission denied, quota exceeded, backend unavailable.

## Catatan implementasi Android
Gunakan Material 3 Compose. Buat reusable components dan theme tokens. Gunakan adaptive navigation: NavigationBar pada ponsel dan NavigationRail/Drawer pada tablet. Hormati edge-to-edge, insets, keyboard/IME, dark mode, font scale, dan Android API 26+. Mockup HTML ini adalah referensi arah visual dan hierarki, bukan spesifikasi ukuran piksel yang wajib ditiru.
