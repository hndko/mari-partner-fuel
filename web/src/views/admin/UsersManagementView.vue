<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { supabase } from '@/services/supabase'

interface AdminUser {
  id: string
  display_name: string
  role: string
  created_at: string
  is_pro: boolean
  plan_name: string
  vehicles_count: number
  transactions_count: number
}

const users = ref<AdminUser[]>([])
const isLoading = ref(true)
const searchQuery = ref('')
const selectedUser = ref<AdminUser | null>(null)
const isGrantModalOpen = ref(false)
const grantDurationDays = ref(30)
const isPermanent = ref(false)
const isGranting = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const filteredUsers = computed(() => {
  if (!searchQuery.value.trim()) return users.value
  const q = searchQuery.value.toLowerCase()
  return users.value.filter(
    (u) =>
      u.display_name?.toLowerCase().includes(q) ||
      u.id.toLowerCase().includes(q) ||
      u.plan_name?.toLowerCase().includes(q)
  )
})

async function loadUsers() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const { data: metricsData, error } = await supabase.rpc('admin_get_saas_metrics', {
      p_timeframe: 'all',
    })
    if (error) throw error

    // recentUsers from metrics
    const recent = metricsData?.recentUsers || []
    users.value = recent.map((u: any) => ({
      id: u.id,
      display_name: u.displayName || 'Pengguna Tanpa Nama',
      role: u.role || 'user',
      created_at: u.createdAt || '',
      is_pro: Boolean(u.isPro),
      plan_name: u.planName || (u.isPro ? 'Pro' : 'Free Tier'),
      vehicles_count: u.vehiclesCount || 0,
      transactions_count: u.transactionsCount || 0,
    }))
  } catch (e: any) {
    errorMessage.value = e.message || 'Gagal memuat daftar pengguna'
  } finally {
    isLoading.value = false
  }
}

function openGrantModal(user: AdminUser) {
  selectedUser.value = user
  grantDurationDays.value = 30
  isPermanent.value = false
  isGrantModalOpen.value = true
}

function closeGrantModal() {
  selectedUser.value = null
  isGrantModalOpen.value = false
}

async function handleGrantPro() {
  if (!selectedUser.value) return
  isGranting.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const { data, error } = await supabase.rpc('admin_grant_pro_subscription', {
      p_target_user_id: selectedUser.value.id,
      p_duration_days: isPermanent.value ? 0 : Number(grantDurationDays.value),
      p_plan_name: isPermanent.value ? 'Pro Permanen' : `Pro ${grantDurationDays.value} Hari`,
      p_is_permanent: isPermanent.value,
    })

    if (error) throw error

    successMessage.value = `Status Pro untuk ${selectedUser.value.display_name} berhasil diaktifkan!`
    closeGrantModal()
    await loadUsers()
  } catch (e: any) {
    errorMessage.value = e.message || 'Gagal memberikan status Pro.'
  } finally {
    isGranting.value = false
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
      <div>
        <h1 class="text-2xl font-black text-white tracking-tight">Manajemen Pengguna & Akses Pro</h1>
        <p class="text-xs text-slate-400 mt-1">Audit pengguna terdaftar, status langganan, dan pemberian Pro manual.</p>
      </div>

      <div class="flex items-center gap-3">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Cari nama atau UID..."
          class="px-4 py-2 rounded-xl bg-slate-900 border border-slate-700 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-sky-500"
        />
        <button
          @click="loadUsers"
          class="px-3 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 border border-slate-700 text-xs text-slate-300 hover:text-white"
        >
          🔄
        </button>
      </div>
    </div>

    <!-- Alert -->
    <div v-if="successMessage" class="p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs">
      ✅ {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="p-4 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs">
      ⚠️ {{ errorMessage }}
    </div>

    <!-- Users Table -->
    <div class="rounded-3xl bg-slate-900 border border-slate-800 shadow-xl overflow-hidden">
      <div v-if="isLoading" class="text-center py-12 text-sm text-slate-400">
        Memuat data pengguna...
      </div>
      <div v-else-if="filteredUsers.length === 0" class="text-center py-12 text-sm text-slate-400">
        Tidak ada pengguna yang sesuai dengan pencarian.
      </div>
      <div v-else class="overflow-x-auto">
        <table class="w-full text-left text-xs text-slate-300">
          <thead class="bg-slate-950 text-slate-400 uppercase tracking-wider text-[10px] border-b border-slate-800">
            <tr>
              <th class="py-4 px-6">Pengguna</th>
              <th class="py-4 px-6">Role</th>
              <th class="py-4 px-6">Paket Langganan</th>
              <th class="py-4 px-6">Armada</th>
              <th class="py-4 px-6">Transaksi</th>
              <th class="py-4 px-6 text-right">Aksi</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800/60">
            <tr v-for="user in filteredUsers" :key="user.id" class="hover:bg-slate-850/50 transition-colors">
              <td class="py-4 px-6">
                <div class="font-bold text-white text-sm">{{ user.display_name }}</div>
                <div class="text-[11px] text-slate-400 font-mono">{{ user.id }}</div>
              </td>
              <td class="py-4 px-6">
                <span
                  :class="[
                    'px-2.5 py-0.5 rounded-full text-[10px] font-extrabold uppercase border',
                    user.role === 'owner' || user.role === 'admin'
                      ? 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                      : 'bg-slate-800 text-slate-400 border-slate-700'
                  ]"
                >
                  {{ user.role }}
                </span>
              </td>
              <td class="py-4 px-6">
                <span
                  :class="[
                    'px-2.5 py-0.5 rounded-full text-[10px] font-extrabold uppercase border',
                    user.is_pro
                      ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                      : 'bg-slate-800 text-slate-400 border-slate-700'
                  ]"
                >
                  {{ user.plan_name }}
                </span>
              </td>
              <td class="py-4 px-6 font-semibold">{{ user.vehicles_count }} Unit</td>
              <td class="py-4 px-6 font-semibold">{{ user.transactions_count }} Log</td>
              <td class="py-4 px-6 text-right">
                <button
                  @click="openGrantModal(user)"
                  class="px-3 py-1.5 rounded-lg bg-sky-500/10 hover:bg-sky-500 text-sky-400 hover:text-white border border-sky-500/30 font-bold transition-all cursor-pointer"
                >
                  👑 Beri Akses Pro
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Grant Pro Modal -->
    <div
      v-if="isGrantModalOpen && selectedUser"
      class="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <div class="w-full max-w-md rounded-3xl bg-slate-900 border border-slate-800 p-6 shadow-2xl space-y-4">
        <div class="flex items-center justify-between pb-3 border-b border-slate-800">
          <div>
            <h3 class="text-base font-bold text-white">Beri Status Pro Manual</h3>
            <p class="text-xs text-slate-400">{{ selectedUser.display_name }}</p>
          </div>
          <button @click="closeGrantModal" class="text-slate-400 hover:text-white">✕</button>
        </div>

        <div class="space-y-4 text-xs">
          <div>
            <label class="block text-slate-300 font-bold mb-2">Pilih Durasi Status Pro</label>
            <div class="grid grid-cols-3 gap-2">
              <button
                type="button"
                @click="grantDurationDays = 30; isPermanent = false"
                :class="[
                  'py-2 px-3 rounded-xl border font-bold transition-all',
                  !isPermanent && grantDurationDays === 30
                    ? 'bg-sky-500 text-white border-sky-500'
                    : 'bg-slate-950 text-slate-400 border-slate-700'
                ]"
              >
                30 Hari
              </button>
              <button
                type="button"
                @click="grantDurationDays = 365; isPermanent = false"
                :class="[
                  'py-2 px-3 rounded-xl border font-bold transition-all',
                  !isPermanent && grantDurationDays === 365
                    ? 'bg-sky-500 text-white border-sky-500'
                    : 'bg-slate-950 text-slate-400 border-slate-700'
                ]"
              >
                1 Tahun
              </button>
              <button
                type="button"
                @click="isPermanent = true"
                :class="[
                  'py-2 px-3 rounded-xl border font-bold transition-all',
                  isPermanent
                    ? 'bg-amber-500 text-black border-amber-500'
                    : 'bg-slate-950 text-slate-400 border-slate-700'
                ]"
              >
                Permanen 👑
              </button>
            </div>
          </div>

          <div class="p-3.5 rounded-2xl bg-slate-950 border border-slate-800 text-[11px] text-slate-400 leading-relaxed">
            Pemberian akses Pro ini dilakukan secara server-authoritative via RPC <code>admin_grant_pro_subscription</code> dan otomatis menambahkan durasi secara kumulatif jika pengguna sudah berstatus Pro.
          </div>
        </div>

        <div class="flex justify-end gap-3 pt-4 border-t border-slate-800">
          <button
            @click="closeGrantModal"
            class="px-4 py-2 rounded-xl bg-slate-800 text-slate-300 font-bold text-xs hover:bg-slate-700"
          >
            Batal
          </button>
          <button
            @click="handleGrantPro"
            :disabled="isGranting"
            class="px-5 py-2 rounded-xl bg-gradient-to-r from-sky-500 to-blue-600 hover:from-sky-400 hover:to-blue-500 text-white font-bold text-xs shadow-lg shadow-sky-500/20"
          >
            {{ isGranting ? 'Memproses...' : 'Aktifkan Pro Sekarang' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
