<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { supabase, getCurrentOwner } from '@/services/supabase'

const router = useRouter()

const email = ref('')
const password = ref('')
const isLoading = ref(false)
const errorMessage = ref('')

async function handleLogin() {
  errorMessage.value = ''
  if (!email.value || !password.value) {
    errorMessage.value = 'Silakan masukkan email dan kata sandi Anda.'
    return
  }

  isLoading.value = true
  try {
    const { error: signInError } = await supabase.auth.signInWithPassword({
      email: email.value.trim(),
      password: password.value,
    })

    if (signInError) {
      throw signInError
    }

    const owner = await getCurrentOwner()
    if (!owner?.isOwner) {
      await supabase.auth.signOut()
      throw new Error('Akses ditolak: Akun Anda tidak memiliki hak akses Pemilik SaaS / Administrator.')
    }

    router.push('/admin/dashboard')
  } catch (err: any) {
    errorMessage.value = err.message || 'Gagal masuk. Periksa kembali email dan password Anda.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-slate-950 flex flex-col justify-center items-center p-4 selection:bg-sky-500 selection:text-white">
    <div class="w-full max-w-md">
      <!-- Logo & Branding -->
      <div class="text-center mb-8">
        <div class="inline-flex w-14 h-14 rounded-2xl bg-gradient-to-tr from-sky-600 to-cyan-400 items-center justify-center shadow-xl shadow-sky-500/20 text-3xl mb-4">
          ⛽
        </div>
        <h1 class="text-2xl font-black text-white tracking-tight">Panel Pemilik SaaS</h1>
        <p class="text-sm text-slate-400 mt-1">Mari Partner Fuel Management Console</p>
      </div>

      <!-- Login Card -->
      <div class="p-8 rounded-3xl bg-slate-900/90 border border-slate-800 shadow-2xl backdrop-blur-xl">
        <form @submit.prevent="handleLogin" class="space-y-5">
          <!-- Alert Error -->
          <div v-if="errorMessage" class="p-4 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs leading-relaxed flex items-start gap-3">
            <span class="text-base">⚠️</span>
            <span>{{ errorMessage }}</span>
          </div>

          <div>
            <label class="block text-xs font-bold uppercase tracking-wider text-slate-300 mb-2">
              Email Administrator
            </label>
            <input
              v-model="email"
              type="email"
              required
              placeholder="kyuoko13@gmail.com"
              class="w-full px-4 py-3 rounded-xl bg-slate-950 border border-slate-700/80 text-white placeholder-slate-500 text-sm focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all"
            />
          </div>

          <div>
            <label class="block text-xs font-bold uppercase tracking-wider text-slate-300 mb-2">
              Kata Sandi
            </label>
            <input
              v-model="password"
              type="password"
              required
              placeholder="••••••••"
              class="w-full px-4 py-3 rounded-xl bg-slate-950 border border-slate-700/80 text-white placeholder-slate-500 text-sm focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all"
            />
          </div>

          <button
            type="submit"
            :disabled="isLoading"
            class="w-full py-3.5 rounded-xl bg-gradient-to-r from-sky-500 to-blue-600 hover:from-sky-400 hover:to-blue-500 disabled:opacity-50 text-white font-extrabold text-sm shadow-lg shadow-sky-500/25 transition-all flex items-center justify-center gap-2 cursor-pointer"
          >
            <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span>{{ isLoading ? 'Memverifikasi Akses...' : 'Masuk ke Dashboard' }}</span>
          </button>
        </form>
      </div>

      <!-- Back to Landing Page -->
      <div class="text-center mt-6">
        <router-link to="/" class="text-xs font-semibold text-slate-400 hover:text-white transition-colors flex items-center justify-center gap-2">
          <span>←</span>
          <span>Kembali ke Halaman Beranda</span>
        </router-link>
      </div>
    </div>
  </div>
</template>
