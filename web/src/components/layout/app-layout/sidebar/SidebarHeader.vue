<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { SidebarHeader } from '@/components/ui/sidebar'

interface AppSidebarHeaderProps {
  showContent: boolean
}

defineProps<AppSidebarHeaderProps>()
</script>

<template>
  <SidebarHeader class="px-5 py-6 border-b border-slate-800">
    <RouterLink to="/admin/dashboard" class="flex items-center gap-3">
      <div class="w-9 h-9 rounded-xl bg-gradient-to-tr from-sky-600 to-cyan-400 flex items-center justify-center text-lg shadow-lg shadow-sky-500/20 font-black text-white shrink-0">
        ⛽
      </div>
      <div v-if="showContent" class="overflow-hidden">
        <div class="font-extrabold text-sm tracking-tight text-white flex items-center gap-1.5 whitespace-nowrap">
          Mari Partner Fuel
          <span class="text-[9px] font-black uppercase px-1.5 py-0.2 rounded bg-amber-500/20 text-amber-400 border border-amber-500/30">
            OWNER
          </span>
        </div>
        <p class="text-[10px] text-slate-400 truncate">SaaS Executive Console</p>
      </div>
    </RouterLink>
  </SidebarHeader>
</template>
