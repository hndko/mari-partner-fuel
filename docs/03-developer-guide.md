# 03. Panduan Pengembang & Arsitektur (Developer Guide)

Dokumen ini ditujukan bagi Software Engineer, Code Reviewer, dan Kontributor teknis yang mengembangkan dan merawat codebase **Mari Partner Fuel**.

---

## 🏛️ 1. Prinsip Arsitektur Inti

Mari Partner Fuel mengadopsi **Clean Architecture** yang dipadukan dengan pola **MVVM (Model-View-ViewModel)** dan **Unidirectional Data Flow (UDF)**.

```
┌─────────────────────────────────────────────────────────────┐
│                         UI LAYER                            │
│  - Jetpack Compose + Material 3 Screens & Components        │
│  - ViewModels mengelola UiState (Sealed/Data class)         │
│  - UI Event (Intent pemanggilan aksi dari pengguna)         │
└──────────────────────────────▲──────────────────────────────┘
                               │ StateFlow<UiState<T>>
                               │ Dispatches onEvent(...)
┌──────────────────────────────┴──────────────────────────────┐
│                        DOMAIN LAYER                         │
│  - Pure Kotlin Use Cases (bebas Android framework)          │
│  - Business Invariants: Kalkulasi biaya BBM, validasi scan  │
│  - Domain Models & Repository Interfaces                    │
└──────────────────────────────▲──────────────────────────────┘
                               │ Implements Interfaces
┌──────────────────────────────┴──────────────────────────────┐
│                         DATA LAYER                          │
│  - Repository Implementations                               │
│  - Remote: Supabase (Auth-kt, Postgrest-kt, Storage-kt)     │
│  - Engine Hardware: Google ML Kit Text Recognition          │
│  - Payment Adapters: QRISpy, Midtrans, Xendit               │
│  - Local: DataStore Preferences & SQLite/Room jika ada      │
└─────────────────────────────────────────────────────────────┘
```

### Standar State Management (UDF)
Setiap layar mengonsumsi `UiState<T>` terstandar:
```kotlin
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>
    data class Empty(val message: String) : UiState<Nothing>
}
```

---

## ⚙️ 2. Bootstrap & Konfigurasi Lingkungan Tanpa Secret

Sesuai prinsip *Twelve-Factor App* dan *Zero-Secret Policy*:
1. **Dilarang Hardcode Token**: String koneksi dan API key rahasia dilarang ditulis langsung dalam berkas Kotlin atau XML.
2. **Injeksi BuildConfig**:
   Variabel publik dibaca melalui `app/build.gradle.kts`:
   ```kotlin
   val supabaseUrl = (project.findProperty("SUPABASE_URL") as? String)
       ?: System.getenv("SUPABASE_URL")
       ?: "https://placeholder-project.supabase.co"

   buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
   ```
3. **Penggunaan di Kode**:
   Akses nilai melalui singleton aman [AppConfig.kt](file:///d:/Kyoo/OneDrive/Documents/Project/Android%20Native/mari-partner-fuel/app/src/main/java/id/maripartner/fuel/core/common/config/AppConfig.kt):
   ```kotlin
   val url = AppConfig.supabaseUrl
   val masked = AppConfig.redactedKey(AppConfig.supabaseAnonKey)
   ```
4. **Isolasi Git**: Pastikan `local.properties` dan `.env` terdaftar di `.gitignore`.

---

## ☕ 3. Kompatibilitas Android API 26 (Desugaring Core Library)

Mari Partner Fuel mendukung **Android 8.0 Oreo (API 26)** hingga **Android 15 (API 35)**.
Untuk menjamin penggunaan API modern tanpa crash:
- **Core Library Desugaring** aktif di `app/build.gradle.kts`:
  ```kotlin
  compileOptions {
      isCoreLibraryDesugaringEnabled = true
      sourceCompatibility = JavaVersion.VERSION_21
      targetCompatibility = JavaVersion.VERSION_21
  }
  dependencies {
      coreLibraryDesugaring(libs.desugar.jdk.libs)
  }
  ```
- **Java Time**: Gunakan `java.time.Instant`, `OffsetDateTime`, dan `LocalDate` secara bebas melalui [DateUtils.kt](file:///d:/Kyoo/OneDrive/Documents/Project/Android%20Native/mari-partner-fuel/app/src/main/java/id/maripartner/fuel/core/common/util/DateUtils.kt).
- **Notification Channels**: Wajib didaftarkan pada [MariPartnerFuelApp.kt](file:///d:/Kyoo/OneDrive/Documents/Project/Android%20Native/mari-partner-fuel/app/src/main/java/id/maripartner/fuel/MariPartnerFuelApp.kt) sebelum memunculkan notifikasi.

---

## 🎨 4. Design System & Aturan Penulisan UI

1. **Tema Baku**: Menggunakan Material 3 dengan warna primer *Petrol Teal* (`#006D77` Light, `#83C5BE` Dark) dan sekunder *Fuel Amber* (`#E29578`).
2. **Anti-Double-Submit**:
   Gunakan [FuelPrimaryButton.kt](file:///d:/Kyoo/OneDrive/Documents/Project/Android%20Native/mari-partner-fuel/app/src/main/java/id/maripartner/fuel/core/designsystem/component/FuelButton.kt) yang otomatis menampilkan circular spinner dan mendisable klik ganda saat `isLoading = true`.
3. **Penanganan Status Reusable**:
   Gunakan [FuelStateView.kt](file:///d:/Kyoo/OneDrive/Documents/Project/Android%20Native/mari-partner-fuel/app/src/main/java/id/maripartner/fuel/core/designsystem/component/FuelStateView.kt) untuk menampilkan `FuelLoadingState`, `FuelEmptyState`, dan `FuelErrorState` agar UX konsisten di seluruh layar.
4. **Format Rupiah Real-Time**:
   Gunakan [CurrencyFormatter.kt](file:///d:/Kyoo/OneDrive/Documents/Project/Android%20Native/mari-partner-fuel/app/src/main/java/id/maripartner/fuel/core/common/util/CurrencyFormatter.kt) untuk mengonversi angka mentah ke format Indonesia (`Rp 50.000`) dan mem-parse teks input pengguna atau hasil OCR.

---

## 🔒 5. Keamanan Database & Supabase RLS

1. **Prinsip Least Privilege**: Klien Android hanya memegang `anon key`.
2. **PostgreSQL Row Level Security (RLS)**:
   Setiap tabel yang diakses client wajib memiliki RLS:
   ```sql
   alter table public.vehicles enable row level security;
   create policy "Users can only view their own vehicles"
     on public.vehicles for select
     using (auth.uid() = user_id);
   ```
3. **Composite Foreign Keys**:
   Untuk mencegah IDOR (manipulasi vehicle_id akun lain pada transaksi), foreign key wajib menyertakan `user_id`:
   ```sql
   foreign key (vehicle_id, user_id) references public.vehicles(id, user_id)
   ```
4. **Private Storage Signed URL**:
   Foto struk disimpan di private bucket `receipts/{user_id}/{filename}`. Client hanya mengakses gambar melalui signed URL berumur 15-30 menit yang dihasilkan server.

---

## 🧪 6. Siklus Penambahan Fitur Baru (8 Langkah)

Setiap penambahan fitur baru wajib mengikuti urutan langkah ini:

```
1. Domain Model ──► 2. Repository Interface ──► 3. Pure Use Case ──► 4. Unit Test
                                                                            │
8. Git Commit & Push ◄── 7. SemVer Bump ◄── 6. Composable Screen ◄── 5. ViewModel
```

1. **Definisikan Domain Model**: Buat data class di package `id.maripartner.fuel.domain.model`.
2. **Definisikan Repository Interface**: Buat kontrak di `domain.repository`.
3. **Buat Use Case**: Logika kalkulasi bisnis murni di `domain.usecase`.
4. **Tulis Unit Test**: Buat test di `app/src/test/java/...` untuk memverifikasi logika domain.
5. **Implementasikan ViewModel**: Kelola event dan pancarkan `StateFlow<UiState>`.
6. **Rakit Composable UI**: Bangun antarmuka Jetpack Compose Material 3.
7. **Daftarkan Rute**: Tambahkan rute di `NavRoute.kt` dan tautkan di `AppNavHost.kt`.
8. **Verifikasi & Commit**: Jalankan `./gradlew.bat testDebugUnitTest` sebelum commit.

---

## 🧼 7. Kebersihan Kode & Anti-Slop Guidelines

1. **Zero Dead Code**: Dilarang meninggalkan kode yang dikomentari (*commented-out blocks*), sisa print debug (`println`, `Log.d` tanpa guard), `TODO`, atau `FIXME`.
2. **Komentar Esensial**: Komentar hanya ditulis untuk menjelaskan *alasan* suatu keputusan arsitektural diambil (*why*), bukan mengulangi apa yang terbaca dari kode (*what*).
3. **Immutable Collections**: Selalu gunakan `List<T>` daripada `ArrayList<T>` pada model UI state.
