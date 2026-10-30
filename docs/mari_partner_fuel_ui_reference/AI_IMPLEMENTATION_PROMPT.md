# Prompt untuk AI Coding Agent — Implementasi UI Mari Partner Fuel

Gunakan seluruh isi folder `mari_partner_fuel_ui_reference` sebagai referensi visual resmi Mari Partner Fuel, terutama `DESIGN_SYSTEM.md`, `index.html`, `styles.css`, dan logo di `assets/`.

## Instruksi wajib
1. Audit UI yang sudah ada sebelum mengubah file. Pertahankan business logic, navigation, state, repository, Supabase integration, dan validasi yang sudah benar.
2. Implementasikan ulang UI menggunakan Kotlin + Jetpack Compose + Material 3 sesuai arsitektur proyek. Jangan menyalin HTML/CSS ke aplikasi Android.
3. Ikuti identitas logo: navy + blue + cyan, surface putih/abu sangat muda, tipografi modern, card sederhana, whitespace cukup, dan hierarchy yang jelas.
4. Buat reusable theme tokens, typography, shape, button, text field, card, status chip, empty state, loading state, error state, dan dialog.
5. Implementasikan setiap halaman yang benar-benar ada dalam PRD: authentication, dashboard, kendaraan (list/form/detail), transaksi (list/form/detail), OCR receipt (capture/preview/processing/review/error), analytics, reminders, subscription, profile/settings.
6. Mobile-first. Gunakan bottom navigation untuk destinasi utama pada ponsel dan navigation rail/drawer untuk layar besar. Jangan menjejalkan semua menu ke bottom bar.
7. Semua layar harus memiliki loading, empty, error, offline, dan permission state yang relevan. Form harus punya label permanen, contoh input, validasi inline, keyboard type yang sesuai, dan tombol submit dengan loading state.
8. Hasil OCR/AI harus dapat diedit dan tidak boleh otomatis disimpan tanpa konfirmasi eksplisit.
9. Jangan gunakan angka, transaksi, chart, kendaraan, harga, atau insight contoh dari mockup ini sebagai data production. Gunakan data dari repository/backend; data dummy hanya untuk preview/test.
10. Harga paket dan kuota harus bersumber dari backend. Jangan hardcode harga komersial.
11. Pastikan dark theme, accessibility, font scaling, content descriptions, touch target minimal 48dp, dan kontras memadai.
12. Gunakan ikon Material yang konsisten; emoji pada HTML hanya placeholder ilustratif.
13. Jangan mengubah skema database, kebijakan RLS, autentikasi, billing, atau alur bisnis sebagai bagian dari pekerjaan UI tanpa persetujuan.
14. Jalankan build, lint, dan UI tests yang tersedia. Laporkan file yang berubah, test yang dijalankan, screenshot hasil aktual, dan blocker. Jangan mengklaim test yang tidak dijalankan.

## Acceptance criteria
- Tampilan konsisten dengan brand Mari Partner.
- Setiap halaman mudah dipahami oleh pengguna non-teknis.
- CTA utama jelas dan konsisten.
- Navigasi antarhalaman tidak membingungkan.
- Tidak ada overflow pada layar kecil, keyboard menutupi CTA, atau teks terpotong pada font scale yang wajar.
- Semua state loading/empty/error/offline relevan tersedia.
- UI terhubung ke data dan aksi aktual; tidak ada tombol palsu pada production.
- Fitur manual tetap berfungsi saat AI tidak tersedia.
- Seluruh alur utama dapat diuji end-to-end.

Mulai dengan membuat audit singkat dan rencana implementasi per halaman. Setelah itu implementasikan secara bertahap, jangan mengganti seluruh aplikasi sekaligus tanpa checkpoint.
