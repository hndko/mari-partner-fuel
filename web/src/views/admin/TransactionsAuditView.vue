<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { supabase } from '@/services/supabase'

interface PaymentTx {
  id: string
  user_id: string
  plan_code: string
  provider: string
  amount_idr: number
  status: string
  payment_reference: string
  provider_payment_id: string | null
  created_at: string
  paid_at: string | null
  raw_response: any
}

const transactions = ref<PaymentTx[]>([])
const isLoading = ref(true)
const searchQuery = ref('')
const selectedStatusFilter = ref('all')
const selectedTx = ref<PaymentTx | null>(null)
const isDetailModalOpen = ref(false)

function formatRupiah(value: number): string {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    maximumFractionDigits: 0,
  }).format(value)
}

function formatDate(iso: string | null): string {
  if (!iso) return '-'
  return new Date(iso).toLocaleString('id-ID', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const filteredTransactions = computed(() => {
  return transactions.value.filter((tx) => {
    const matchesQuery =
      !searchQuery.value ||
      tx.payment_reference?.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      tx.user_id.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      tx.plan_code?.toLowerCase().includes(searchQuery.value.toLowerCase())

    const matchesStatus =
      selectedStatusFilter.value === 'all'
        ? true
        : selectedStatusFilter.value === 'paid'
        ? tx.status === 'paid'
        : selectedStatusFilter.value === 'pending'
        ? tx.status === 'pending'
        : tx.status !== 'paid' && tx.status !== 'pending'

    return matchesQuery && matchesStatus
  })
})

async function loadTransactions() {
  isLoading.value = true
  try {
    const { data, error } = await supabase
      .from('payment_transactions')
      .select('*')
      .order('created_at', { ascending: false })

    if (error) throw error
    transactions.value = data || []
  } catch (e) {
    console.error('Gagal memuat transaksi:', e)
  } finally {
    isLoading.value = false
  }
}

function openDetail(tx: PaymentTx) {
  selectedTx.value = tx
  isDetailModalOpen.value = true
}

function closeDetail() {
  selectedTx.value = null
  isDetailModalOpen.value = false
}

onMounted(() => {
  loadTransactions()
})
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
      <div>
        <h1 class="text-2xl font-black text-white tracking-tight">Audit Mutasi Transaksi Masuk</h1>
        <p class="text-xs text-slate-400 mt-1">Rekam jejak mutasi pembayaran langganan tersimpan di basis data Supabase.</p>
      </div>

      <div class="flex flex-wrap items-center gap-3">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Cari Ref ID atau User ID..."
          class="px-4 py-2 rounded-xl bg-slate-900 border border-slate-700 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-sky-500"
        />
        <button
          @click="loadTransactions"
          class="px-3 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 border border-slate-700 text-xs text-slate-300 hover:text-white"
        >
          🔄
        </button>
      </div>
    </div>

    <!-- Filter Status Chips -->
    <div class="flex items-center gap-2">
      <button
        v-for="st in [
          { id: 'all', label: `Semua (${transactions.length})` },
          { id: 'paid', label: `Lunas (${transactions.filter(t => t.status === 'paid').length})` },
          { id: 'pending', label: `Pending (${transactions.filter(t => t.status === 'pending').length})` },
          { id: 'failed', label: `Gagal/Batal (${transactions.filter(t => t.status !== 'paid' && t.status !== 'pending').length})` }
        ]"
        :key="st.id"
        @click="selectedStatusFilter = st.id"
        :class="[
          'px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all border',
          selectedStatusFilter === st.id
            ? 'bg-sky-500 text-white border-sky-500 shadow-md shadow-sky-500/20'
            : 'bg-slate-900 text-slate-400 border-slate-800 hover:text-white'
        ]"
      >
        {{ st.label }}
      </button>
    </div>

    <!-- Table Card -->
    <div class="rounded-3xl bg-slate-900 border border-slate-800 shadow-xl overflow-hidden">
      <div v-if="isLoading" class="text-center py-12 text-sm text-slate-400">
        Memuat riwayat transaksi...
      </div>
      <div v-else-if="filteredTransactions.length === 0" class="text-center py-12 text-sm text-slate-400">
        Tidak ada transaksi yang cocok dengan filter.
      </div>
      <div v-else class="overflow-x-auto">
        <table class="w-full text-left text-xs text-slate-300">
          <thead class="bg-slate-950 text-slate-400 uppercase tracking-wider text-[10px] border-b border-slate-800">
            <tr>
              <th class="py-4 px-6">Nomor Referensi</th>
              <th class="py-4 px-6">Paket & Provider</th>
              <th class="py-4 px-6">Nominal Bayar</th>
              <th class="py-4 px-6">Status</th>
              <th class="py-4 px-6">Waktu Transaksi</th>
              <th class="py-4 px-6 text-right">Audit</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800/60">
            <tr v-for="tx in filteredTransactions" :key="tx.id" class="hover:bg-slate-850/50 transition-colors">
              <td class="py-4 px-6">
                <div class="font-mono font-bold text-white text-xs">{{ tx.payment_reference }}</div>
                <div class="text-[10px] text-slate-400 font-mono">UID: {{ tx.user_id.slice(0, 12) }}...</div>
              </td>
              <td class="py-4 px-6">
                <div class="font-bold text-white">{{ tx.plan_code }}</div>
                <div class="text-[10px] text-slate-400 uppercase">Via {{ tx.provider }}</div>
              </td>
              <td class="py-4 px-6 font-extrabold text-white text-sm">
                {{ formatRupiah(tx.amount_idr) }}
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
              <td class="py-4 px-6 text-slate-400">
                <div>{{ formatDate(tx.created_at) }}</div>
                <div v-if="tx.paid_at" class="text-[10px] text-emerald-400">Lunas: {{ formatDate(tx.paid_at) }}</div>
              </td>
              <td class="py-4 px-6 text-right">
                <button
                  @click="openDetail(tx)"
                  class="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 font-bold transition-all cursor-pointer"
                >
                  Detail
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Detail Audit Modal -->
    <div
      v-if="isDetailModalOpen && selectedTx"
      class="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <div class="w-full max-w-lg rounded-3xl bg-slate-900 border border-slate-800 p-6 shadow-2xl space-y-4">
        <div class="flex items-center justify-between pb-3 border-b border-slate-800">
          <div>
            <h3 class="text-base font-bold text-white">Detail Audit Settlement</h3>
            <p class="text-xs text-slate-400 font-mono">{{ selectedTx.payment_reference }}</p>
          </div>
          <button @click="closeDetail" class="text-slate-400 hover:text-white">✕</button>
        </div>

        <div class="space-y-3 text-xs">
          <div class="grid grid-cols-2 gap-3 p-4 rounded-2xl bg-slate-950 border border-slate-800">
            <div>
              <div class="text-slate-400 text-[10px] uppercase">User ID</div>
              <div class="text-white font-mono break-all">{{ selectedTx.user_id }}</div>
            </div>
            <div>
              <div class="text-slate-400 text-[10px] uppercase">Nominal</div>
              <div class="text-emerald-400 font-black text-sm">{{ formatRupiah(selectedTx.amount_idr) }}</div>
            </div>
            <div>
              <div class="text-slate-400 text-[10px] uppercase">Provider Gateway</div>
              <div class="text-white font-bold">{{ selectedTx.provider }}</div>
            </div>
            <div>
              <div class="text-slate-400 text-[10px] uppercase">Status Pembayaran</div>
              <div class="text-sky-400 font-extrabold uppercase">{{ selectedTx.status }}</div>
            </div>
          </div>

          <div v-if="selectedTx.raw_response">
            <div class="text-slate-400 text-[10px] uppercase mb-1">Payload Gateway / Webhook Data</div>
            <pre class="p-3 rounded-xl bg-slate-950 border border-slate-800 text-[10px] font-mono text-slate-300 max-h-40 overflow-y-auto">{{ JSON.stringify(selectedTx.raw_response, null, 2) }}</pre>
          </div>
        </div>

        <div class="flex justify-end pt-3 border-t border-slate-800">
          <button
            @click="closeDetail"
            class="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-white font-bold text-xs"
          >
            Tutup
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
