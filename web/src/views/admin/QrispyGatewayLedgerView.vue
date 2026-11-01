<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchQrispyMerchantData, type QrispyMerchantData } from '@/services/supabase'

const qrispyData = ref<QrispyMerchantData | null>(null)
const isLoading = ref(true)
const errorMessage = ref('')

function formatRupiah(value: number): string {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    maximumFractionDigits: 0,
  }).format(value)
}

function formatDate(iso: string): string {
  if (!iso) return '-'
  return iso.replace('T', ' ').slice(0, 19)
}

async function loadGatewayData() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    qrispyData.value = await fetchQrispyMerchantData()
  } catch (e: any) {
    errorMessage.value = e.message || 'Gagal memuat mutasi dari gateway QRISpy'
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  loadGatewayData()
})
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
      <div>
        <div class="flex items-center gap-2">
          <span class="text-xl">💳</span>
          <h1 class="text-2xl font-black text-white tracking-tight">Mutasi Gateway QRISpy</h1>
          <span class="px-2 py-0.5 rounded-full bg-sky-500/10 text-sky-400 border border-sky-500/20 text-[10px] font-black uppercase tracking-wider">
            OFFICIAL LIVE LEDGER
          </span>
        </div>
        <p class="text-xs text-slate-400 mt-1">Data mutasi real-time langsung ditarik dari server gateway QRISpy via Edge Function.</p>
      </div>

      <button
        @click="loadGatewayData"
        :disabled="isLoading"
        class="px-4 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 border border-slate-700/80 text-xs font-bold text-slate-300 hover:text-white transition-all flex items-center gap-2 self-start sm:self-auto cursor-pointer"
      >
        <span :class="isLoading && 'animate-spin'">🔄</span>
        <span>Perbarui Mutasi</span>
      </button>
    </div>

    <!-- Alert Error -->
    <div v-if="errorMessage" class="p-4 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs">
      ⚠️ {{ errorMessage }}
    </div>

    <!-- Saldo Merchant Banner -->
    <div class="p-6 rounded-3xl bg-slate-900 border border-slate-800 shadow-xl flex flex-col sm:flex-row sm:items-center justify-between gap-6">
      <div>
        <div class="text-[10px] uppercase font-bold text-sky-400 tracking-wider">SALDO AKTIF MERCHANT</div>
        <div class="text-xs text-slate-400 mt-0.5">{{ qrispyData?.balance?.merchantName || 'Mari Partner Fuel' }}</div>
        <div class="text-4xl font-black text-white mt-2">
          {{ formatRupiah(qrispyData?.balance?.balanceIdr || 0) }}
        </div>
      </div>
      <div class="p-4 rounded-2xl bg-slate-950 border border-slate-800 text-xs text-slate-300 space-y-1">
        <div class="text-[10px] uppercase font-bold text-slate-400">STATUS KONEKSI API</div>
        <div class="text-emerald-400 font-bold flex items-center gap-2">
          <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
          Terhubung ke api.qrispy.id
        </div>
        <div class="text-[11px] text-slate-400">Total Mutasi Gateway: {{ qrispyData?.transactions?.length || 0 }} order</div>
      </div>
    </div>

    <!-- Gateway Transactions Table -->
    <div class="rounded-3xl bg-slate-900 border border-slate-800 shadow-xl overflow-hidden">
      <div v-if="isLoading" class="text-center py-12 text-sm text-slate-400">
        Menghubungi gateway QRISpy...
      </div>
      <div v-else-if="!qrispyData?.transactions || qrispyData.transactions.length === 0" class="text-center py-16 text-slate-400 space-y-2">
        <div class="text-3xl">💳</div>
        <div class="font-bold text-white text-base">Belum Ada Mutasi di Gateway</div>
        <p class="text-xs max-w-sm mx-auto">Transaksi QRIS yang di-generate oleh pengguna akan tampil langsung dari gateway di sini.</p>
      </div>
      <div v-else class="overflow-x-auto">
        <table class="w-full text-left text-xs text-slate-300">
          <thead class="bg-slate-950 text-slate-400 uppercase tracking-wider text-[10px] border-b border-slate-800">
            <tr>
              <th class="py-4 px-6">QRIS ID & Referensi</th>
              <th class="py-4 px-6">Nominal Transaksi</th>
              <th class="py-4 px-6">Status Gateway</th>
              <th class="py-4 px-6">Dibuat Pada</th>
              <th class="py-4 px-6">Kedaluwarsa</th>
              <th class="py-4 px-6 text-right">Tipe</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800/60">
            <tr v-for="tx in qrispyData.transactions" :key="tx.qrisId" class="hover:bg-slate-850/50 transition-colors">
              <td class="py-4 px-6">
                <div class="font-bold text-white font-mono text-xs">{{ tx.paymentReference || tx.qrisId }}</div>
                <div class="text-[10px] text-slate-400 font-mono">ID: {{ tx.qrisId }}</div>
              </td>
              <td class="py-4 px-6 font-black text-white text-sm">
                {{ formatRupiah(tx.amountIdr) }}
              </td>
              <td class="py-4 px-6">
                <span
                  :class="[
                    'px-2.5 py-0.5 rounded-full text-[10px] font-extrabold uppercase border',
                    tx.status === 'paid'
                      ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                      : tx.status === 'pending'
                      ? 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                      : 'bg-red-500/10 text-red-400 border-red-500/30'
                  ]"
                >
                  {{ tx.status }}
                </span>
              </td>
              <td class="py-4 px-6 text-slate-400 font-mono">
                {{ formatDate(tx.createdAt) }}
              </td>
              <td class="py-4 px-6 text-slate-400 font-mono">
                {{ formatDate(tx.expiredAt) }}
              </td>
              <td class="py-4 px-6 text-right font-mono uppercase text-slate-400">
                {{ tx.generatedVia }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
