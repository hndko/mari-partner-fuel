<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { supabase, fetchQrispyMerchantData, type QrispyMerchantData } from '@/services/supabase'

const router = useRouter()

const selectedTimeframe = ref('30d')
const isLoading = ref(true)
const isRefreshingQrispy = ref(false)

const metrics = ref<any>({
  kpi: {
    totalRevenueIdr: 0,
    totalUsers: 0,
    activeProUsers: 0,
    totalTransactionsRecorded: 0,
  },
  planConversion: {
    freeUsers: 0,
    proMonthlyUsers: 0,
    proYearlyUsers: 0,
    totalProUsers: 0,
    conversionRatePercent: 0,
  },
  dailyGrowth: [],
})

const qrispyData = ref<QrispyMerchantData | null>(null)

function formatRupiah(value: number): string {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    maximumFractionDigits: 0,
  }).format(value)
}

async function loadDashboardMetrics() {
  isLoading.value = true
  try {
    const { data, error } = await supabase.rpc('admin_get_saas_metrics', {
      p_timeframe: selectedTimeframe.value,
    })
    if (!error && data) {
      metrics.value = data
    }
  } catch (e) {
    console.error('Gagal memuat metrik admin:', e)
  } finally {
    isLoading.value = false
  }
}

async function loadQrispyBalance() {
  isRefreshingQrispy.value = true
  try {
    qrispyData.value = await fetchQrispyMerchantData()
  } catch (e) {
    console.warn('Gagal memuat saldo QRISpy gateway:', e)
  } finally {
    isRefreshingQrispy.value = false
  }
}

function changeTimeframe(tf: string) {
  selectedTimeframe.value = tf
  loadDashboardMetrics()
}

onMounted(() => {
  loadDashboardMetrics()
  loadQrispyBalance()
})
</script>

<template>
  <div class="space-y-6">
    <!-- Header Page & Timeframe Filter -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
      <div>
        <div class="flex items-center gap-2">
          <span class="text-xl">👑</span>
          <h1 class="text-2xl font-black text-white tracking-tight">Ringkasan Eksekutif SaaS</h1>
          <span class="px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-[10px] font-black uppercase tracking-wider">
            LIVE METRICS
          </span>
        </div>
        <p class="text-xs text-slate-400 mt-1">Pemantauan performa bisnis, registrasi armada, dan omset real-time.</p>
      </div>

      <div class="flex items-center gap-2 bg-slate-900 p-1.5 rounded-xl border border-slate-800 self-start sm:self-auto">
        <button
          v-for="tf in [
            { id: '7d', label: '7 Hari' },
            { id: '30d', label: '30 Hari' },
            { id: 'all', label: 'Semua Waktu' }
          ]"
          :key="tf.id"
          @click="changeTimeframe(tf.id)"
          :class="[
            'px-3 py-1.5 rounded-lg text-xs font-bold transition-all',
            selectedTimeframe === tf.id
              ? 'bg-sky-500 text-white shadow-md shadow-sky-500/20'
              : 'text-slate-400 hover:text-white'
          ]"
        >
          {{ tf.label }}
        </button>
      </div>
    </div>

    <!-- Executive Revenue Banner -->
    <div class="p-6 rounded-3xl bg-gradient-to-br from-slate-900 via-slate-900 to-sky-950/40 border border-sky-500/30 shadow-2xl relative overflow-hidden">
      <div class="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <div class="flex items-center gap-2 mb-2">
            <span class="text-xs font-bold uppercase tracking-wider text-sky-400">TOTAL OMSET TERVERIFIKASI</span>
            <span class="px-2 py-0.5 rounded-md bg-emerald-500/20 text-emerald-400 text-[10px] font-extrabold">
              TERVERIFIKASI
            </span>
          </div>
          <div class="text-4xl sm:text-5xl font-black text-white tracking-tight">
            {{ formatRupiah(metrics.kpi?.totalRevenueIdr || 0) }}
          </div>
          <p class="text-xs text-slate-400 mt-2">
            Akumulasi omset dari seluruh transaksi pembayaran paket Pro yang lunas tercatat di basis data.
          </p>
        </div>

        <div class="grid grid-cols-2 sm:grid-cols-3 gap-4">
          <div class="p-4 rounded-2xl bg-slate-950/60 border border-slate-800">
            <div class="text-[10px] uppercase font-bold text-slate-400">PRO AKTIF</div>
            <div class="text-xl font-black text-sky-400 mt-1">{{ metrics.kpi?.activeProUsers || 0 }} Akun</div>
            <div class="text-[10px] text-slate-400">Rasio {{ metrics.planConversion?.conversionRatePercent || 0 }}%</div>
          </div>
          <div class="p-4 rounded-2xl bg-slate-950/60 border border-slate-800">
            <div class="text-[10px] uppercase font-bold text-slate-400">ESTIMASI MRR</div>
            <div class="text-xl font-black text-emerald-400 mt-1">
              {{ formatRupiah(Math.round((metrics.kpi?.totalRevenueIdr || 0) / 12)) }}
            </div>
            <div class="text-[10px] text-slate-400">Run-rate / bln</div>
          </div>
          <div class="p-4 rounded-2xl bg-slate-950/60 border border-slate-800 col-span-2 sm:col-span-1">
            <div class="text-[10px] uppercase font-bold text-slate-400">GATEWAY</div>
            <div class="text-xl font-black text-cyan-400 mt-1">QRIS & PG</div>
            <div class="text-[10px] text-slate-400">Otomatis Lunas</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Saldo Gateway QRISpy Card -->
    <div class="p-6 rounded-3xl bg-slate-900 border border-slate-800 shadow-xl">
      <div class="flex items-center justify-between">
        <div>
          <div class="flex items-center gap-2">
            <span class="text-xs font-bold uppercase tracking-wider text-sky-400">SALDO GATEWAY QRISPY</span>
            <span class="px-2 py-0.5 rounded-md bg-sky-500/10 text-sky-400 text-[10px] font-extrabold border border-sky-500/20">
              OFFICIAL API
            </span>
          </div>
          <div class="text-xs text-slate-400 mt-0.5">{{ qrispyData?.balance?.merchantName || 'Mari Partner Fuel' }}</div>
        </div>
        <button
          @click="loadQrispyBalance"
          :disabled="isRefreshingQrispy"
          class="p-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white transition-all cursor-pointer"
          title="Perbarui Saldo"
        >
          <svg :class="['w-4 h-4', isRefreshingQrispy && 'animate-spin']" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
        </button>
      </div>

      <div class="mt-4 flex items-baseline justify-between">
        <div class="text-3xl font-black text-white">
          {{ formatRupiah(qrispyData?.balance?.balanceIdr || 0) }}
        </div>
        <router-link to="/admin/qrispy" class="text-xs font-bold text-sky-400 hover:text-sky-300 transition-colors flex items-center gap-1">
          <span>Lihat Mutasi Gateway</span>
          <span>→</span>
        </router-link>
      </div>
      <div class="text-[11px] text-slate-400 mt-1">
        Mutasi gateway: {{ qrispyData?.transactions?.length || 0 }} transaksi tercatat langsung dari QRISpy
      </div>
    </div>

    <!-- 4 Secondary KPI Grid -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="p-5 rounded-2xl bg-slate-900 border border-slate-800">
        <div class="text-xs font-bold text-slate-400 uppercase">Total Pengguna</div>
        <div class="text-2xl font-black text-white mt-1">{{ metrics.kpi?.totalUsers || 0 }}</div>
        <div class="text-xs text-slate-400 mt-1">{{ metrics.planConversion?.freeUsers || 0 }} Free • {{ metrics.planConversion?.totalProUsers || 0 }} Pro</div>
      </div>
      <div class="p-5 rounded-2xl bg-slate-900 border border-slate-800">
        <div class="text-xs font-bold text-slate-400 uppercase">Pelanggan Pro</div>
        <div class="text-2xl font-black text-sky-400 mt-1">{{ metrics.planConversion?.totalProUsers || 0 }}</div>
        <div class="text-xs text-slate-400 mt-1">{{ metrics.planConversion?.conversionRatePercent || 0 }}% Rasio Konversi</div>
      </div>
      <div class="p-5 rounded-2xl bg-slate-900 border border-slate-800">
        <div class="text-xs font-bold text-slate-400 uppercase">Pro Bulanan vs Tahunan</div>
        <div class="text-2xl font-black text-emerald-400 mt-1">{{ metrics.planConversion?.proMonthlyUsers || 0 }} / {{ metrics.planConversion?.proYearlyUsers || 0 }}</div>
        <div class="text-xs text-slate-400 mt-1">Distribusi Paket Aktif</div>
      </div>
      <div class="p-5 rounded-2xl bg-slate-900 border border-slate-800">
        <div class="text-xs font-bold text-slate-400 uppercase">Transaksi Tercatat</div>
        <div class="text-2xl font-black text-purple-400 mt-1">{{ metrics.kpi?.totalTransactionsRecorded || 0 }}</div>
        <div class="text-xs text-slate-400 mt-1">Aktivitas Pengisian BBM</div>
      </div>
    </div>

    <!-- Quick Navigation Shortcuts -->
    <div class="p-6 rounded-3xl bg-slate-900 border border-slate-800">
      <h3 class="text-sm font-bold text-white uppercase tracking-wider mb-4">PINTASAN KONTROL SAAS</h3>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <router-link
          to="/admin/plans"
          class="p-4 rounded-2xl bg-slate-950 border border-slate-800 hover:border-sky-500/50 hover:bg-slate-850 transition-all flex items-center justify-between group"
        >
          <div>
            <div class="font-bold text-white text-sm group-hover:text-sky-400 transition-colors">Atur Paket & Harga</div>
            <div class="text-xs text-slate-400 mt-0.5">Katalog & diskon promo</div>
          </div>
          <span class="text-slate-500 group-hover:text-white transition-colors">→</span>
        </router-link>

        <router-link
          to="/admin/users"
          class="p-4 rounded-2xl bg-slate-950 border border-slate-800 hover:border-sky-500/50 hover:bg-slate-850 transition-all flex items-center justify-between group"
        >
          <div>
            <div class="font-bold text-white text-sm group-hover:text-sky-400 transition-colors">Pengguna & Akses Pro</div>
            <div class="text-xs text-slate-400 mt-0.5">Grant Pro manual</div>
          </div>
          <span class="text-slate-500 group-hover:text-white transition-colors">→</span>
        </router-link>

        <router-link
          to="/admin/transactions"
          class="p-4 rounded-2xl bg-slate-950 border border-slate-800 hover:border-sky-500/50 hover:bg-slate-850 transition-all flex items-center justify-between group"
        >
          <div>
            <div class="font-bold text-white text-sm group-hover:text-sky-400 transition-colors">Audit Transaksi</div>
            <div class="text-xs text-slate-400 mt-0.5">Riwayat settlement database</div>
          </div>
          <span class="text-slate-500 group-hover:text-white transition-colors">→</span>
        </router-link>

        <router-link
          to="/admin/qrispy"
          class="p-4 rounded-2xl bg-slate-950 border border-slate-800 hover:border-sky-500/50 hover:bg-slate-850 transition-all flex items-center justify-between group"
        >
          <div>
            <div class="font-bold text-white text-sm group-hover:text-sky-400 transition-colors">Mutasi QRISpy Gateway</div>
            <div class="text-xs text-slate-400 mt-0.5">Ledger API langsung</div>
          </div>
          <span class="text-slate-500 group-hover:text-white transition-colors">→</span>
        </router-link>
      </div>
    </div>
  </div>
</template>
