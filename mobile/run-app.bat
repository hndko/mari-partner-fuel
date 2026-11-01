@echo off
setlocal enabledelayedexpansion

REM ==============================================================================
REM MARI PARTNER FUEL - LIGHTWEIGHT ANDROID TEST LAUNCHER
REM Script otomatis untuk menjalankan aplikasi di HP Fisik (USB/Wi-Fi) atau
REM Emulator AVD lokal tanpa perlu membuka Android Studio yang berat.
REM ==============================================================================

chcp 65001 >nul
title Mari Partner Fuel - Run & Test Launcher

echo.
echo ==============================================================================
echo    MARI PARTNER FUEL - RUN AND TEST LAUNCHER [LIGHTWEIGHT MODE]
echo ==============================================================================
echo.

REM 1. Deteksi Lokasi Android SDK
set "SDK_DIR="
if defined ANDROID_HOME (
    if exist "%ANDROID_HOME%\platform-tools\adb.exe" set "SDK_DIR=%ANDROID_HOME%"
)
if not defined SDK_DIR (
    if defined ANDROID_SDK_ROOT (
        if exist "%ANDROID_SDK_ROOT%\platform-tools\adb.exe" set "SDK_DIR=%ANDROID_SDK_ROOT%"
    )
)
if not defined SDK_DIR (
    if exist "C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe" (
        set "SDK_DIR=C:\Users\%USERNAME%\AppData\Local\Android\Sdk"
    )
)

if not defined SDK_DIR (
    echo [ERROR] Android SDK tidak ditemukan di lokasi standar!
    echo Silakan masukkan path folder Android SDK Anda:
    set /p "USER_SDK=Path SDK: "
    if exist "!USER_SDK!\platform-tools\adb.exe" (
        set "SDK_DIR=!USER_SDK!"
    ) else (
        echo [FATAL] adb.exe tidak ditemukan di "!USER_SDK!". Keluar.
        pause
        exit /b 1
    )
)

set "ADB=%SDK_DIR%\platform-tools\adb.exe"
set "EMULATOR=%SDK_DIR%\emulator\emulator.exe"

echo [OK] Menggunakan Android SDK di: %SDK_DIR%
echo.

REM 2. Mulai ADB Server jika belum berjalan
"%ADB%" start-server >nul 2>&1

REM 3. Periksa Perangkat yang Sedang Terhubung
set "DEVICE_COUNT=0"
for /f "skip=1 tokens=1,2" %%A in ('"%ADB%" devices') do (
    if "%%B"=="device" (
        set /a DEVICE_COUNT+=1
        set "DEVICE_!DEVICE_COUNT!=%%A"
    )
)

REM 4. Deteksi Daftar AVD Lokal
set "AVD_COUNT=0"
if exist "%EMULATOR%" (
    for /f "tokens=*" %%A in ('"%EMULATOR%" -list-avds 2^>nul') do (
        set /a AVD_COUNT+=1
        set "AVD_!AVD_COUNT!=%%A"
    )
)

REM 5. Menu Pilihan Target Perangkat
echo ------------------------------------------------------------------------------
echo PILIH TARGET PERANGKAT UNTUK TESTING:
echo ------------------------------------------------------------------------------

set "MENU_INDEX=0"

if %DEVICE_COUNT% gtr 0 (
    echo [PERANGKAT AKTIF TERDETEKSI]
    for /l %%I in (1,1,%DEVICE_COUNT%) do (
        set /a MENU_INDEX+=1
        set "TARGET_TYPE_!MENU_INDEX!=ONLINE"
        set "TARGET_VAL_!MENU_INDEX!=!DEVICE_%%I!"
        echo   [!MENU_INDEX!] Perangkat Aktif: !DEVICE_%%I! [REKOMENDASI: Bebas Beban Laptop!]
    )
    echo.
)

if %AVD_COUNT% gtr 0 (
    echo [EMULATOR AVD TERSEDIA]
    for /l %%I in (1,1,%AVD_COUNT%) do (
        set /a MENU_INDEX+=1
        set "TARGET_TYPE_!MENU_INDEX!=AVD"
        set "TARGET_VAL_!MENU_INDEX!=!AVD_%%I!"
        echo   [!MENU_INDEX!] Jalankan Emulator AVD: !AVD_%%I! [Mode Hemat: No Boot Anim, 2 Core, 2GB RAM]
    )
    echo.
)

set /a MENU_INDEX+=1
set "TARGET_TYPE_!MENU_INDEX!=EXIT"
echo   [!MENU_INDEX!] Keluar
echo ------------------------------------------------------------------------------

set /p "USER_CHOICE=Pilih nomor [1-%MENU_INDEX%]: "
if "%USER_CHOICE%"=="" (
    echo [ERROR] Tidak ada pilihan yang dimasukkan. Keluar.
    exit /b 1
)

set "CHOSEN_TYPE="
set "CHOSEN_VAL="
for %%I in (%USER_CHOICE%) do (
    set "CHOSEN_TYPE=!TARGET_TYPE_%%I!"
    set "CHOSEN_VAL=!TARGET_VAL_%%I!"
)

if not defined CHOSEN_TYPE (
    echo [ERROR] Pilihan tidak valid. Keluar.
    exit /b 1
)

if "%CHOSEN_TYPE%"=="EXIT" (
    echo Dibatalkan. Sampai jumpa!
    exit /b 0
)

set "SELECTED_TARGET=%CHOSEN_VAL%"
set "SELECTED_TYPE=%CHOSEN_TYPE%"

REM 6. Jika memilih Emulator AVD, Jalankan dengan Parameter Hemat Resource
if "%SELECTED_TYPE%"=="AVD" (
    echo.
    echo [INFO] Menyalakan Emulator '%SELECTED_TARGET%' dengan konfigurasi hemat daya...
    echo Parameter: -no-boot-anim -netdelay none -netspeed full -cores 2 -memory 2048 -gpu host
    
    start "" "%EMULATOR%" -avd "%SELECTED_TARGET%" -no-boot-anim -netdelay none -netspeed full -cores 2 -memory 2048 -gpu host
    
    echo [INFO] Menunggu emulator terhubung ke ADB...
    "%ADB%" wait-for-device
    
    echo [INFO] Menunggu sistem operasi Android selesai booting...
    :WAIT_BOOT
    set "BOOT_STATUS="
    for /f "tokens=*" %%C in ('"%ADB%" shell getprop sys.boot_completed 2^>nul') do set "BOOT_STATUS=%%C"
    if not "!BOOT_STATUS!"=="1" (
        timeout /t 2 /nobreak >nul
        goto :WAIT_BOOT
    )
    echo [OK] Emulator berhasil menyala dan siap digunakan!
)

REM 7. Menu Pilihan Kompilasi Gradle
echo.
echo ------------------------------------------------------------------------------
echo PROSES INSTALASI APLIKASI:
echo ------------------------------------------------------------------------------
echo [1] Compile dan Install Ulang [gradlew assembleDebug] - Rekomendasi jika ada edit kode
echo [2] Fast Install [Pasang APK debug yang sudah jadi tanpa compile ulang]
echo ------------------------------------------------------------------------------
set /p "BUILD_CHOICE=Pilih mode instalasi [1/2, default 1]: "
if "%BUILD_CHOICE%"=="" set "BUILD_CHOICE=1"

if "%BUILD_CHOICE%"=="1" (
    echo.
    echo [BUILD] Menjalankan Gradle Build [assembleDebug]...
    call .\gradlew.bat assembleDebug
    if errorlevel 1 (
        echo [ERROR] Kompilasi Gradle gagal! Periksa error di atas.
        pause
        exit /b 1
    )
    echo [OK] Build berhasil dikompilasi!
)

REM 8. Pasang APK ke Perangkat
set "APK_PATH=app\build\outputs\apk\debug\app-debug.apk"
if not exist "%APK_PATH%" (
    echo.
    echo [WARNING] APK belum ditemukan di %APK_PATH%. Mengompilasi sekarang...
    call .\gradlew.bat assembleDebug
    if errorlevel 1 (
        echo [ERROR] Kompilasi gagal!
        pause
        exit /b 1
    )
)

echo.
echo [INSTALL] Memasang aplikasi ke perangkat...
"%ADB%" install -r -d "%APK_PATH%"
if errorlevel 1 (
    echo [ERROR] Gagal memasang APK ke perangkat!
    pause
    exit /b 1
)
echo [OK] Aplikasi Mari Partner Fuel berhasil terpasang!

REM 9. Luncurkan Aplikasi
echo.
echo [LAUNCH] Membuka Mari Partner Fuel di layar...
"%ADB%" shell am start -n id.maripartner.fuel/id.maripartner.fuel.MainActivity
echo.
echo ==============================================================================
echo    [SUCCESS] APLIKASI BERHASIL DIJALANKAN!
echo ==============================================================================
echo.
echo Ingin memantau Logcat real-time khusus Mari Partner Fuel?
echo   [1] Ya, buka logcat streaming
echo   [2] Tidak, selesai
set /p "LOG_CHOICE=Pilihan [1/2, default 2]: "
if "%LOG_CHOICE%"=="1" (
    echo.
    echo [LOGCAT] Menampilkan log Mari Partner Fuel [Tekan Ctrl+C untuk berhenti]...
    echo --------------------------------------------------------------------------
    "%ADB%" logcat -v time | findstr /i "id.maripartner.fuel MariPartner"
)

echo.
echo Terima kasih telah menggunakan Mari Partner Fuel Launcher!
exit /b 0
