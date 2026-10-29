# 01. Daftar Perintah Wajib & Operasional (CLI Reference)

Dokumen ini memuat seluruh perintah CLI operasional yang digunakan sehari-hari dalam pengembangan, pengujian, audit, dan pembangunan artefak aplikasi **Mari Partner Fuel**.

---

## 📌 Ringkasan Parameter Lingkungan

| Parameter | Nilai Baku / Rekomendasi |
| :--- | :--- |
| **Sistem Operasi Host** | Windows 11 / Windows 10 (didukung juga di macOS & Linux) |
| **Java JDK** | JetBrains Runtime OpenJDK 21.0.11 (LTS) |
| **Android SDK Path** | `C:\Users\<Username>\AppData\Local\Android\Sdk` |
| **Target / Compile SDK** | Android API 35 (Android 15) |
| **Minimum SDK** | Android API 26 (Android 8.0 Oreo) |
| **Gradle Wrapper** | Versi 8.7 (`.\gradlew.bat` pada Windows, `./gradlew` pada Unix) |
| **Output APK Debug** | `app/build/outputs/apk/debug/app-debug.apk` |

---

## 🧪 Perintah Pengujian & Quality Assurance

### 1. Menjalankan Seluruh Unit Test (Local JVM Tests)
Menjalankan pengujian logika domain, formatting mata uang, parse liter, UiState, dan konfigurasi:
```powershell
.\gradlew.bat testDebugUnitTest
```
> [!TIP]
> Laporan pengujian interaktif berformat HTML akan otomatis dihasilkan di:
> `app/build/reports/tests/testDebugUnitTest/index.html`

### 2. Menjalankan Unit Test Spesifik Berdasarkan Kelas
```powershell
.\gradlew.bat testDebugUnitTest --tests "id.maripartner.fuel.core.common.util.CurrencyFormatterTest"
```

### 3. Menjalankan Unit Test Spesifik Berdasarkan Fungsi
```powershell
.\gradlew.bat testDebugUnitTest --tests "id.maripartner.fuel.core.common.util.CurrencyFormatterTest.parseIdr_handlesVariousRawFormats"
```

---

## 🏗️ Perintah Build & Kompilasi APK

### 1. Membangun APK Debug (Development Build)
```powershell
.\gradlew.bat assembleDebug
```
Lokasi file keluaran:
```text
app/build/outputs/apk/debug/app-debug.apk
```

### 2. Memeriksa L8 Dexing & Desugaring API 26
Memastikan desugaring core library berhasil tanpa error:
```powershell
.\gradlew.bat :app:l8DexDesugarLibDebug
```

### 3. Membangun APK Release (Production Milestone)
```powershell
.\gradlew.bat assembleRelease
```
Lokasi file keluaran:
```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

---

## 📱 Perintah Android Debug Bridge (ADB) & Perangkat

### 1. Memeriksa Perangkat / Emulator yang Terhubung
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

### 2. Menginstal APK Debug ke Perangkat Secara Langsung
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```

### 3. Menjalankan Aplikasi via ADB
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am start -n id.maripartner.fuel/.MainActivity
```

### 4. Memantau Live Logcat Khusus Aplikasi Mari Partner Fuel
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" logcat -s "MariPartnerFuel" "AndroidRuntime:E"
```

---

## 🧹 Maintenance & Housekeeping

### 1. Membersihkan Seluruh Build Cache
```powershell
.\gradlew.bat clean
```

### 2. Menghentikan Gradle Daemon yang Menggantung / Stale
Gunakan perintah ini jika daemon Gradle terkunci atau memakan RAM berlebih:
```powershell
.\gradlew.bat --stop
```

### 3. Memeriksa Status Daemon Gradle
```powershell
.\gradlew.bat --status
```

---

## 🗄️ Manajemen Supabase & Migrasi Database

### 1. Memulai Lingkungan Supabase Lokal (Jika Supabase CLI Terpasang)
```powershell
supabase start
```

### 2. Menjalankan Migrasi Database Baru
```powershell
supabase migration up
```

### 3. Membuat Berkas Migrasi Baru
```powershell
supabase migration new create_fuel_transactions_table
```

---

## 📦 Git & Standar Conventional Commits

Format wajib commit sesuai aturan 9 `AGENTS.md`:
```powershell
# 1. Periksa perubahan file dan status git
git status

# 2. Tambahkan perubahan yang siap di-commit
git add .

# 3. Commit dengan format Conventional Commits
git commit -m "feat(auth): implement supabase email authentication repository"

# 4. Push ke remote branch
git push origin main
```

---

## 📊 Tabel Ringkasan Harian (Daily Cheatsheet)

| Kebutuhan Operasional | Perintah Cepat |
| :--- | :--- |
| **Jalankan Unit Test** | `.\gradlew.bat testDebugUnitTest` |
| **Build APK Debug Baru** | `.\gradlew.bat assembleDebug` |
| **Bersihkan Cache Build** | `.\gradlew.bat clean` |
| **Stop Daemon Gradle** | `.\gradlew.bat --stop` |
| **Install APK ke HP** | `adb install -r app\build\outputs\apk\debug\app-debug.apk` |
| **Cek Perangkat ADB** | `adb devices` |
| **Cek Status Git Bersih** | `git status` |
