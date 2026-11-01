<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const appVersion = (import.meta.env.VITE_APP_VERSION as string) || '2.0.8'
const apkFilename = (import.meta.env.VITE_APP_APK_FILENAME as string) || `mari-partner-fuel-v${appVersion}.apk`
const apkDownloadUrl = (import.meta.env.VITE_APP_APK_URL as string) || `/downloads/${apkFilename}`

const isDownloading = ref(false)

function handleDownloadApk() {
  isDownloading.value = true
  const link = document.createElement('a')
  link.href = apkDownloadUrl
  link.setAttribute('download', apkFilename)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  setTimeout(() => {
    isDownloading.value = false
  }, 1500)
}

function navigateToAdmin() {
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-slate-950 text-slate-100 selection:bg-sky-500 selection:text-white">
    <!-- 1. Header Navbar -->
    <header class="sticky top-0 z-50 backdrop-blur-md bg-slate-950/80 border-b border-slate-800/80">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-18 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-tr from-sky-600 to-cyan-400 flex items-center justify-center shadow-lg shadow-sky-500/20 font-black text-xl text-white">
            ⛽
          </div>
          <div>
            <div class="font-extrabold text-lg tracking-tight text-white flex items-center gap-2">
              Mari Partner Fuel
              <span class="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-sky-500/10 text-sky-400 border border-sky-500/20">
                v{{ appVersion }}
              </span>
            </div>
            <p class="text-xs text-slate-400">AI-Powered Fuel Expense Tracker</p>
          </div>
        </div>

        <nav class="hidden md:flex items-center gap-8 text-sm font-medium text-slate-300">
          <a href="#fitur" class="hover:text-sky-400 transition-colors">Fitur Unggulan</a>
          <a href="#keunggulan" class="hover:text-sky-400 transition-colors">Keunggulan</a>
          <a href="#harga" class="hover:text-sky-400 transition-colors">Paket Harga</a>
          <a href="#instalasi" class="hover:text-sky-400 transition-colors">Cara Pasang</a>
        </nav>

        <div class="flex items-center gap-3">
          <button
            @click="navigateToAdmin"
            class="px-4 py-2 text-xs sm:text-sm font-semibold rounded-xl text-slate-300 hover:text-white hover:bg-slate-800 border border-slate-700/80 transition-all flex items-center gap-1.5"
          >
            <span>👑</span>
            <span>Owner Console</span>
          </button>
          <button
            @click="handleDownloadApk"
            :disabled="isDownloading"
            class="px-4 py-2 text-xs sm:text-sm font-bold rounded-xl bg-gradient-to-r from-sky-500 to-blue-600 hover:from-sky-400 hover:to-blue-500 text-white shadow-lg shadow-sky-500/25 transition-all flex items-center gap-2"
          >
            <span>⬇️</span>
            <span>{{ isDownloading ? 'Mengunduh...' : 'Download APK' }}</span>
          </button>
        </div>
      </div>
    </header>

    <!-- 2. Hero Section -->
    <section class="relative pt-20 pb-28 overflow-hidden">
      <div class="absolute inset-0 bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(14,165,233,0.15),rgba(255,255,255,0))]"></div>
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative">
        <div class="text-center max-w-3xl mx-auto space-y-6">
          <div class="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-slate-900 border border-sky-500/30 text-xs font-semibold text-sky-400 shadow-inner">
            <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
            Rilis Terbaru v{{ appVersion }} Resmi Siap Dipasang di Android
          </div>

          <h1 class="text-4xl sm:text-6xl font-black tracking-tight text-white leading-tight">
            Pantau Konsumsi BBM & Biaya Armada <span class="bg-gradient-to-r from-sky-400 via-cyan-300 to-blue-500 bg-clip-text text-transparent">Lebih Cerdas dengan AI</span>
          </h1>

          <p class="text-base sm:text-lg text-slate-300 leading-relaxed">
            Aplikasi pencatat bensin modern untuk motor, mobil, dan armada bisnis. Dilengkapi pemindai struk instan Google ML Kit on-device tanpa internet, radar SPBU sekitar, kalkulator trip, dan HUD pantulan kaca depan.
          </p>

          <!-- Primary Download Action Box -->
          <div class="pt-4 flex flex-col sm:flex-row items-center justify-center gap-4">
            <button
              @click="handleDownloadApk"
              :disabled="isDownloading"
              class="w-full sm:w-auto px-8 py-4 rounded-2xl bg-gradient-to-r from-sky-500 to-blue-600 hover:from-sky-400 hover:to-blue-500 text-white font-extrabold text-base shadow-xl shadow-sky-500/30 hover:scale-[1.02] active:scale-[0.98] transition-all flex items-center justify-center gap-3 cursor-pointer"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
              </svg>
              <span>{{ isDownloading ? 'Sedang Memulai Unduhan...' : `Download ${apkFilename}` }}</span>
            </button>
            <a
              href="#fitur"
              class="w-full sm:w-auto px-6 py-4 rounded-2xl bg-slate-900 hover:bg-slate-800 border border-slate-700/80 text-slate-200 font-bold text-base transition-all text-center"
            >
              Eksplorasi Fitur
            </a>
          </div>

          <!-- Trust Badges -->
          <div class="pt-8 flex flex-wrap items-center justify-center gap-6 text-xs text-slate-400">
            <div class="flex items-center gap-2">
              <span class="text-emerald-400">✓</span>
              <span>Android 8.0 Oreo s.d. Android 15</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-emerald-400">✓</span>
              <span>Google ML Kit 100% On-Device</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-emerald-400">✓</span>
              <span>Zero-Secret & RLS Proteksi Database</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 3. Bento Grid Features -->
    <section id="fitur" class="py-20 border-t border-slate-850 bg-slate-900/40">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center max-w-2xl mx-auto mb-16">
          <h2 class="text-xs uppercase font-extrabold tracking-widest text-sky-400 mb-2">FITUR GENERASI BARU</h2>
          <p class="text-3xl font-black text-white">Dirancang untuk Efisiensi & Hemat Pengeluaran BBM</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <!-- Feature 1 -->
          <div class="p-8 rounded-3xl bg-slate-900/90 border border-slate-800 hover:border-sky-500/50 transition-all flex flex-col justify-between">
            <div class="space-y-4">
              <div class="w-12 h-12 rounded-2xl bg-sky-500/10 border border-sky-500/20 text-sky-400 flex items-center justify-center text-2xl font-bold">
                📸
              </div>
              <h3 class="text-xl font-bold text-white">Scan Struk BBM On-Device</h3>
              <p class="text-sm text-slate-300 leading-relaxed">
                Ekstraksi instan nominal rupiah, liter bensin, dan nama SPBU menggunakan teknologi Google ML Kit. Berjalan 100% di perangkat Anda tanpa memakan kuota internet.
              </p>
            </div>
            <div class="mt-6 pt-4 border-t border-slate-800 text-xs font-semibold text-sky-400">
              ⚡ Local First • Privasi Terjamin
            </div>
          </div>

          <!-- Feature 2 -->
          <div class="p-8 rounded-3xl bg-slate-900/90 border border-slate-800 hover:border-sky-500/50 transition-all flex flex-col justify-between">
            <div class="space-y-4">
              <div class="w-12 h-12 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 flex items-center justify-center text-2xl font-bold">
                ⛽
              </div>
              <h3 class="text-xl font-bold text-white">Radar Harga & SPBU Sekitar</h3>
              <p class="text-sm text-slate-300 leading-relaxed">
                Pantau perbandingan harga bahan bakar resmi seluruh brand di Indonesia (Pertamina, Shell, BP, Vivo) dan deteksi kehadiran otomatis SPBU dalam radius ~250 meter.
              </p>
            </div>
            <div class="mt-6 pt-4 border-t border-slate-800 text-xs font-semibold text-emerald-400">
              📍 Geofence Offline • Info Akurat
            </div>
          </div>

          <!-- Feature 3 -->
          <div class="p-8 rounded-3xl bg-slate-900/90 border border-slate-800 hover:border-sky-500/50 transition-all flex flex-col justify-between">
            <div class="space-y-4">
              <div class="w-12 h-12 rounded-2xl bg-amber-500/10 border border-amber-500/20 text-amber-400 flex items-center justify-center text-2xl font-bold">
                🚘
              </div>
              <h3 class="text-xl font-bold text-white">Night HUD Windshield Reflector</h3>
              <p class="text-sm text-slate-300 leading-relaxed">
                Mode layar OLED Hitam Pekat dengan tampilan cermin terbalik (*horizontal mirroring*) untuk diproyeksikan langsung ke kaca depan mobil saat berkendara malam hari.
              </p>
            </div>
            <div class="mt-6 pt-4 border-t border-slate-800 text-xs font-semibold text-amber-400">
              🌙 Safe Night Driving • OLED Save
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 4. Pricing Section -->
    <section id="harga" class="py-20 border-t border-slate-850">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center max-w-2xl mx-auto mb-16">
          <h2 class="text-xs uppercase font-extrabold tracking-widest text-sky-400 mb-2">PILIHAN PAKET TERJANGKAU</h2>
          <p class="text-3xl font-black text-white">Mulai Gratis, Upgrade Kapan Saja Sesuai Kebutuhan</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <!-- Free Tier -->
          <div class="p-8 rounded-3xl bg-slate-900/60 border border-slate-800 flex flex-col justify-between">
            <div>
              <h3 class="text-xl font-bold text-white mb-2">Free Tier</h3>
              <p class="text-xs text-slate-400 mb-6">Cocok untuk pengemudi pribadi harian</p>
              <div class="text-3xl font-black text-white mb-6">Rp 0 <span class="text-xs font-normal text-slate-400">/ selamanya</span></div>
              <ul class="space-y-3 text-sm text-slate-300 mb-8">
                <li class="flex items-center gap-2">✓ Maksimal 2 Armada Aktif</li>
                <li class="flex items-center gap-2">✓ 5 Scan AI OCR per Bulan</li>
                <li class="flex items-center gap-2">✓ Catat Transaksi Manual Bebas Batas</li>
                <li class="flex items-center gap-2">✓ Sinkronisasi Cloud Supabase</li>
              </ul>
            </div>
            <button @click="handleDownloadApk" class="w-full py-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-white font-bold text-sm transition-all">
              Unduh & Pasang Sekarang
            </button>
          </div>

          <!-- Pro Bulanan -->
          <div class="p-8 rounded-3xl bg-gradient-to-b from-sky-950/80 to-slate-900 border-2 border-sky-500 flex flex-col justify-between relative shadow-2xl shadow-sky-500/10">
            <span class="absolute -top-3.5 left-1/2 -translate-x-1/2 px-3 py-1 rounded-full bg-sky-500 text-[10px] font-black uppercase text-white tracking-widest">
              POPULER
            </span>
            <div>
              <h3 class="text-xl font-bold text-white mb-2">Paket Pro Bulanan</h3>
              <p class="text-xs text-slate-400 mb-6">Pilihan fleksibel operasional bisnis & armada</p>
              <div class="text-3xl font-black text-white mb-6">Rp 29.000 <span class="text-xs font-normal text-slate-400">/ bulan</span></div>
              <ul class="space-y-3 text-sm text-slate-300 mb-8">
                <li class="flex items-center gap-2">✓ Armada Tak Terbatas (Unlimited)</li>
                <li class="flex items-center gap-2">✓ 100 Scan AI OCR per Bulan</li>
                <li class="flex items-center gap-2">✓ Analitik Konsumsi km/Liter Lengkap</li>
                <li class="flex items-center gap-2">✓ Pembayaran Otomatis QRIS & Play Store</li>
              </ul>
            </div>
            <button @click="handleDownloadApk" class="w-full py-3 rounded-xl bg-gradient-to-r from-sky-500 to-blue-600 hover:from-sky-400 hover:to-blue-500 text-white font-bold text-sm shadow-lg shadow-sky-500/25 transition-all">
              Mulai Pakai Pro
            </button>
          </div>

          <!-- Pro Tahunan -->
          <div class="p-8 rounded-3xl bg-slate-900/60 border border-slate-800 flex flex-col justify-between">
            <div>
              <h3 class="text-xl font-bold text-white mb-2">Paket Pro Tahunan</h3>
              <p class="text-xs text-slate-400 mb-6">Hemat 17% dengan masa aktif 365 hari penuh</p>
              <div class="text-3xl font-black text-white mb-6">Rp 290.000 <span class="text-xs font-normal text-slate-400">/ tahun</span></div>
              <ul class="space-y-3 text-sm text-slate-300 mb-8">
                <li class="flex items-center gap-2">✓ Seluruh Benefit Paket Pro</li>
                <li class="flex items-center gap-2">✓ Hemat 2 Bulan Biaya Langganan</li>
                <li class="flex items-center gap-2">✓ Prioritas Pemrosesan Struk Cloud</li>
                <li class="flex items-center gap-2">✓ Dukungan Tim Teknis Prioritas</li>
              </ul>
            </div>
            <button @click="handleDownloadApk" class="w-full py-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-white font-bold text-sm transition-all">
              Pilih Paket Tahunan
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 5. Cara Pasang APK -->
    <section id="instalasi" class="py-20 border-t border-slate-850 bg-slate-900/30">
      <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-12">
          <h2 class="text-xs uppercase font-extrabold tracking-widest text-sky-400 mb-2">PANDUAN INSTALASI CEPAT</h2>
          <p class="text-2xl sm:text-3xl font-black text-white">Pasang Aplikasi di Smartphone Android dalam 3 Langkah</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="p-6 rounded-2xl bg-slate-900 border border-slate-800 text-center">
            <div class="w-10 h-10 rounded-full bg-sky-500/20 text-sky-400 font-black mx-auto mb-4 flex items-center justify-center">1</div>
            <h4 class="font-bold text-white mb-2">Unduh File APK</h4>
            <p class="text-xs text-slate-400">Klik tombol download dan tunggu berkas <code>{{ apkFilename }}</code> selesai terunduh di HP Anda.</p>
          </div>
          <div class="p-6 rounded-2xl bg-slate-900 border border-slate-800 text-center">
            <div class="w-10 h-10 rounded-full bg-sky-500/20 text-sky-400 font-black mx-auto mb-4 flex items-center justify-center">2</div>
            <h4 class="font-bold text-white mb-2">Izinkan Pemasangan</h4>
            <p class="text-xs text-slate-400">Buka file notifikasi download, lalu izinkan <em>"Install unknown apps"</em> di menu pengaturan browser jika diminta.</p>
          </div>
          <div class="p-6 rounded-2xl bg-slate-900 border border-slate-800 text-center">
            <div class="w-10 h-10 rounded-full bg-sky-500/20 text-sky-400 font-black mx-auto mb-4 flex items-center justify-center">3</div>
            <h4 class="font-bold text-white mb-2">Langsung Gunakan</h4>
            <p class="text-xs text-slate-400">Buka aplikasi Mari Partner Fuel, daftar akun gratis atau login satu ketukan dengan Akun Google.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 6. Footer -->
    <footer class="py-12 border-t border-slate-850 bg-slate-950 text-slate-400 text-xs">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-6">
        <div class="flex items-center gap-2">
          <span class="text-lg">⛽</span>
          <span class="font-bold text-white">Mari Partner Fuel</span>
          <span>• Hak Cipta &copy; 2026. Seluruh hak dilindungi undang-undang.</span>
        </div>
        <div class="flex items-center gap-6">
          <button @click="navigateToAdmin" class="hover:text-white transition-colors">Owner Console</button>
          <a href="#fitur" class="hover:text-white transition-colors">Fitur</a>
          <a href="#harga" class="hover:text-white transition-colors">Paket Pro</a>
        </div>
      </div>
    </footer>
  </div>
</template>
