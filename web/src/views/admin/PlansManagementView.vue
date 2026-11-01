<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { supabase } from '@/services/supabase'

interface PlanItem {
  id: string
  code: string
  name: string
  billing_period: string
  price_idr: number
  ai_scan_quota: number
  max_vehicles: number | null
  is_active: boolean
  description: string
  promo_price_idr: number | null
  promo_max_per_user: number | null
  promo_label: string | null
  is_promo_active: boolean
}

const plans = ref<PlanItem[]>([])
const isLoading = ref(true)
const isSaving = ref(false)
const selectedPlan = ref<PlanItem | null>(null)
const isEditDialogOpen = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

function formatRupiah(value: number | null): string {
  if (value === null || value === undefined) return '-'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    maximumFractionDigits: 0,
  }).format(value)
}

async function loadPlans() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const { data, error } = await supabase
      .from('subscription_plans')
      .select('*')
      .order('price_idr', { ascending: true })

    if (error) throw error
    plans.value = data || []
  } catch (e: any) {
    errorMessage.value = e.message || 'Gagal memuat katalog paket langganan'
  } finally {
    isLoading.value = false
  }
}

function openEditDialog(plan: PlanItem) {
  selectedPlan.value = { ...plan }
  isEditDialogOpen.value = true
}

function closeEditDialog() {
  selectedPlan.value = null
  isEditDialogOpen.value = false
}

async function savePlan() {
  if (!selectedPlan.value) return
  isSaving.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const { error } = await supabase
      .from('subscription_plans')
      .update({
        name: selectedPlan.value.name,
        price_idr: Number(selectedPlan.value.price_idr),
        ai_scan_quota: Number(selectedPlan.value.ai_scan_quota),
        is_active: selectedPlan.value.is_active,
        is_promo_active: selectedPlan.value.is_promo_active,
        promo_price_idr: selectedPlan.value.promo_price_idr ? Number(selectedPlan.value.promo_price_idr) : null,
        promo_max_per_user: selectedPlan.value.promo_max_per_user ? Number(selectedPlan.value.promo_max_per_user) : null,
        promo_label: selectedPlan.value.promo_label?.trim() || null,
        description: selectedPlan.value.description,
      })
      .eq('id', selectedPlan.value.id)

    if (error) throw error
    successMessage.value = `Paket ${selectedPlan.value.name} berhasil diperbarui.`
    closeEditDialog()
    await loadPlans()
  } catch (e: any) {
    errorMessage.value = e.message || 'Gagal menyimpan perubahan paket.'
  } finally {
    isSaving.value = false
  }
}

onMounted(() => {
  loadPlans()
})
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
      <div>
        <h1 class="text-2xl font-black text-white tracking-tight">Katalog Paket & Harga SaaS</h1>
        <p class="text-xs text-slate-400 mt-1">Konfigurasi tarif langganan Pro, kuota scan AI, armada, dan diskon promosi.</p>
      </div>
      <button
        @click="loadPlans"
        :disabled="isLoading"
        class="px-4 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 border border-slate-700/80 text-xs font-bold text-slate-300 hover:text-white transition-all self-start sm:self-auto"
      >
        <span>🔄 Refresh</span>
      </button>
    </div>

    <!-- Alert Messages -->
    <div v-if="successMessage" class="p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs">
      ✅ {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="p-4 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs">
      ⚠️ {{ errorMessage }}
    </div>

    <!-- Plans Cards Grid -->
    <div v-if="isLoading" class="text-center py-12 text-sm text-slate-400">
      Memuat katalog paket...
    </div>
    <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div
        v-for="plan in plans"
        :key="plan.id"
        class="p-6 rounded-3xl bg-slate-900 border border-slate-800 shadow-xl flex flex-col justify-between"
      >
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <span class="text-xs uppercase font-extrabold px-3 py-1 rounded-full bg-slate-800 text-slate-300">
              {{ plan.billing_period || 'SEUMUR HIDUP' }}
            </span>
            <span
              :class="[
                'text-[10px] font-extrabold uppercase px-2.5 py-0.5 rounded-full border',
                plan.is_active
                  ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                  : 'bg-red-500/10 text-red-400 border-red-500/20'
              ]"
            >
              {{ plan.is_active ? 'AKTIF' : 'NONAKTIF' }}
            </span>
          </div>

          <div>
            <h3 class="text-xl font-bold text-white">{{ plan.name }}</h3>
            <p class="text-xs text-slate-400 mt-1">{{ plan.description || 'Tidak ada deskripsi' }}</p>
          </div>

          <div class="py-2">
            <div class="text-2xl font-black text-white">
              {{ formatRupiah(plan.price_idr) }}
              <span class="text-xs font-normal text-slate-400">/ {{ plan.billing_period || 'lifetime' }}</span>
            </div>

            <!-- Promo badge if active -->
            <div v-if="plan.is_promo_active && plan.promo_price_idr" class="mt-2 inline-flex items-center gap-2 px-2.5 py-1 rounded-lg bg-amber-500/10 border border-amber-500/30 text-amber-400 text-xs font-bold">
              <span>🔥 Promo: {{ formatRupiah(plan.promo_price_idr) }}</span>
              <span v-if="plan.promo_label" class="text-[10px] text-amber-300">({{ plan.promo_label }})</span>
            </div>
          </div>

          <div class="space-y-2 text-xs text-slate-300 border-t border-slate-800 pt-3">
            <div class="flex justify-between">
              <span class="text-slate-400">Kuota Scan AI:</span>
              <span class="font-bold text-white">{{ plan.ai_scan_quota }} scan/bln</span>
            </div>
            <div class="flex justify-between">
              <span class="text-slate-400">Batas Armada:</span>
              <span class="font-bold text-white">{{ plan.max_vehicles ? `${plan.max_vehicles} Unit` : 'Unlimited' }}</span>
            </div>
          </div>
        </div>

        <button
          @click="openEditDialog(plan)"
          class="mt-6 w-full py-2.5 rounded-xl bg-slate-800 hover:bg-sky-600 text-white font-bold text-xs transition-all cursor-pointer"
        >
          ✏️ Edit Paket & Promo
        </button>
      </div>
    </div>

    <!-- Edit Plan Modal -->
    <div
      v-if="isEditDialogOpen && selectedPlan"
      class="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <div class="w-full max-w-lg rounded-3xl bg-slate-900 border border-slate-800 p-6 shadow-2xl space-y-4">
        <div class="flex items-center justify-between pb-3 border-b border-slate-800">
          <h3 class="text-lg font-bold text-white">Edit Paket: {{ selectedPlan.name }}</h3>
          <button @click="closeEditDialog" class="text-slate-400 hover:text-white">✕</button>
        </div>

        <div class="space-y-3 text-xs">
          <div>
            <label class="block text-slate-300 font-bold mb-1">Nama Paket</label>
            <input
              v-model="selectedPlan.name"
              type="text"
              class="w-full px-3 py-2 rounded-xl bg-slate-950 border border-slate-700 text-white text-xs"
            />
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-slate-300 font-bold mb-1">Harga Reguler (IDR)</label>
              <input
                v-model.number="selectedPlan.price_idr"
                type="number"
                class="w-full px-3 py-2 rounded-xl bg-slate-950 border border-slate-700 text-white text-xs"
              />
            </div>
            <div>
              <label class="block text-slate-300 font-bold mb-1">Kuota Scan AI</label>
              <input
                v-model.number="selectedPlan.ai_scan_quota"
                type="number"
                class="w-full px-3 py-2 rounded-xl bg-slate-950 border border-slate-700 text-white text-xs"
              />
            </div>
          </div>

          <!-- Promo Section -->
          <div class="p-4 rounded-2xl bg-slate-950 border border-slate-800 space-y-3">
            <div class="flex items-center justify-between">
              <span class="font-bold text-amber-400">Aktifkan Harga Promo</span>
              <input v-model="selectedPlan.is_promo_active" type="checkbox" class="w-4 h-4 rounded text-sky-500" />
            </div>

            <div v-if="selectedPlan.is_promo_active" class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-slate-300 font-bold mb-1">Harga Promo (IDR)</label>
                <input
                  v-model.number="selectedPlan.promo_price_idr"
                  type="number"
                  placeholder="25000"
                  class="w-full px-3 py-2 rounded-xl bg-slate-900 border border-slate-700 text-white text-xs"
                />
              </div>
              <div>
                <label class="block text-slate-300 font-bold mb-1">Tag Promo</label>
                <input
                  v-model="selectedPlan.promo_label"
                  type="text"
                  placeholder="DISKON 15%"
                  class="w-full px-3 py-2 rounded-xl bg-slate-900 border border-slate-700 text-white text-xs"
                />
              </div>
            </div>
          </div>

          <div class="flex items-center gap-2">
            <input v-model="selectedPlan.is_active" type="checkbox" id="isActive" class="w-4 h-4 rounded text-sky-500" />
            <label for="isActive" class="text-slate-300 font-bold">Paket Aktif & Tampil ke Pengguna</label>
          </div>
        </div>

        <div class="flex justify-end gap-3 pt-4 border-t border-slate-800">
          <button
            @click="closeEditDialog"
            class="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold text-xs"
          >
            Batal
          </button>
          <button
            @click="savePlan"
            :disabled="isSaving"
            class="px-5 py-2 rounded-xl bg-sky-500 hover:bg-sky-400 text-white font-bold text-xs shadow-lg shadow-sky-500/20"
          >
            {{ isSaving ? 'Menyimpan...' : 'Simpan Perubahan' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
