# 04. Panduan Deployment, Rilis & Distribusi

Dokumen ini memandu langkah demi langkah pembangunan artefak produksi (*Release APK / Android App Bundle*), penandatanganan sertifikat digital (*Keystore Signing*), deployment Supabase Edge Functions, serta kepatuhan kebijakan Google Play Store.

---

## 🏗️ 1. Pembangunan Artefak Android

### 1.1 Menghasilkan Android App Bundle (AAB) untuk Google Play Store
Format yang diwajibkan oleh Google Play Store untuk aplikasi modern:
```powershell
.\gradlew.bat bundleRelease
```
Berkas keluaran:
```text
app/build/outputs/bundle/release/app-release.aab
```

### 1.2 Menghasilkan APK Rilis Mandiri (Self-Hosted / Direct Download)
```powershell
.\gradlew.bat assembleRelease
```
Berkas keluaran:
```text
app/build/outputs/apk/release/app-release.apk
```

---

## 🔑 2. Konfigurasi Keystore & Penandatanganan (Signing)

> [!CAUTION]
> DILARANG KERAS menyimpan file keystore produksi (`*.jks` / `*.keystore`) atau kata sandinya ke dalam repositori Git!

### 2.1 Menghasilkan Keystore Produksi Baru
Gunakan perintah `keytool` yang disertakan dalam JDK:
```powershell
& "C:\Users\Kyoo\.jdks\jbr-21.0.11\bin\keytool.exe" -genkey -v `
  -keystore "mari-partner-fuel-release.jks" `
  -alias "mari_partner_release" `
  -keyalg RSA `
  -keysize 2048 `
  -validity 10000 `
  -storepass "GANTI_PASSWORD_KUAT_DISINI" `
  -keypass "GANTI_PASSWORD_KUAT_DISINI" `
  -dname "CN=Mari Partner, OU=Mobile Division, O=Mari Partner, L=Jakarta, ST=DKI Jakarta, C=ID"
```

### 2.2 Konfigurasi Signing Aman via `local.properties` atau Environment CI/CD
Tambahkan parameter penandatanganan pada file rahasia lokal (`local.properties`):
```properties
RELEASE_STORE_FILE=C\:\\path\\to\\mari-partner-fuel-release.jks
RELEASE_STORE_PASSWORD=password_keystore
RELEASE_KEY_ALIAS=mari_partner_release
RELEASE_KEY_PASSWORD=password_key
```

Konfigurasikan pada `app/build.gradle.kts` secara kondisional:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file(project.findProperty("RELEASE_STORE_FILE") ?: "dummy.jks")
        storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as? String
        keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as? String
        keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as? String
    }
}
buildTypes {
    release {
        isMinifyEnabled = true
        signingConfig = signingConfigs.getByName("release")
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

---

## 🛡️ 3. Optimasi Ukuran & Obfuscation (R8 / Proguard)

Mari Partner Fuel menggunakan R8 untuk mengecilkan ukuran APK dan melindungi kode sumber dari *reverse engineering*:
- Berkas aturan berada di [app/proguard-rules.pro](file:///d:/Kyoo/OneDrive/Documents/Project/Android%20Native/mari-partner-fuel/app/proguard-rules.pro).
- Pertahankan kelas serialisasi Kotlinx Serializable dan DTO Supabase:
  ```proguard
  -keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
  -keepclassmembers class * {
      @kotlinx.serialization.Serializable <fields>;
  }
  ```
- Google ML Kit Text Recognition dan Ktor OkHttp Engine telah menyertakan aturan Proguard konsumen (*consumer rules*) secara otomatis.

---

## ⚡ 4. Deployment Supabase Edge Functions (Backend)

Supabase Edge Functions bertindak sebagai serverless proxy untuk operasi berizin tinggi (AI OCR Fallback, pembuatan order QRISpy, verifikasi webhook).

### 4.1 Login ke Supabase CLI
```powershell
supabase login
```

### 4.2 Menautkan Proyek Cloud
```powershell
supabase link --project-ref your-project-id
```

### 4.3 Mengatur Secret di Supabase Secret Manager
```powershell
supabase secrets set QRISPY_API_TOKEN="token_rahasia_qrispy"
supabase secrets set GEMINI_API_KEY="api_key_gemini"
supabase secrets set SUPABASE_SERVICE_ROLE_KEY="service_role_key_supabase"
```

### 4.4 Deploy Fungsi
```powershell
supabase functions deploy create-payment
supabase functions deploy qrispy-webhook
supabase functions deploy receipt-ai-proxy
```

---

## 🌐 5. Kepatuhan Google Play Store (Release Checklist)

Sebelum merilis aplikasi ke Google Play Console:

| Aspek | Syarat Wajib Google Play | Status Mari Partner Fuel |
| :--- | :--- | :---: |
| **Target API** | Wajib menargetkan API 34 atau API 35 | ✅ `targetSdk = 35` |
| **Minimum API** | Mendukung perangkat luas | ✅ `minSdk = 26` (Android 8.0+) |
| **64-bit Architecture** | Mendukung ABI arm64-v8a & x86_64 | ✅ Otomatis via AGP |
| **Kebijakan Pembayaran** | Evaluasi Google Play Billing vs Third-party payment untuk langganan | ⚠️ Periksa regulasi Play Store untuk kategori regional Indonesia sebelum rilis produksi |
| **Izin Kamera** | Dilengkapi alasan penggunaan (*Usage Description*) di manifest | ✅ Hanya untuk scan struk SPBU |
| **Kebijakan Privasi** | Wajib memiliki URL Kebijakan Privasi aktif yang memuat pemrosesan foto | ✅ Tercantum di dokumen [SECURITY.md](SECURITY.md) |
| **Alur Hapus Akun** | Pengguna dapat menghapus akun dan seluruh data dari dalam aplikasi | ✅ Tersedia di menu Profil & database cascade |

---

## ✅ 6. Checklist Verifikasi Sebelum Rilis Publik (Pre-Flight)

- [ ] Seluruh unit test lulus 100%: `.\gradlew.bat testDebugUnitTest`.
- [ ] Build release AAB berhasil: `.\gradlew.bat bundleRelease`.
- [ ] SHA-1 dan SHA-256 fingerprint dari Keystore Produksi telah didaftarkan pada Google Cloud Console dan Supabase Auth Settings.
- [ ] File `.env` dan `local.properties` tidak tersertifikasi dalam APK/Bundle.
- [ ] Edge Functions Supabase aktif dan endpoint webhook merespons `HTTP 200`.
