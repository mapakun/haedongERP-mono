<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/domains/auth/auth.store'
import { menuItems } from './menu'

const auth = useAuthStore()

// 관리자 전용 메뉴는 관리자 이상에게만 보인다
const visibleItems = computed(() => menuItems.filter((item) => !item.adminOnly || auth.isAdmin))
</script>

<template>
  <nav class="flex flex-col bg-sidebar py-3">
    <RouterLink
      v-for="item in menuItems"
      :key="item.routeName"
      v-slot="{ href, navigate, isExactActive }"
      :to="{ name: item.routeName }"
      custom
    >
      <a
        :href="href"
        :class="[
          'border-l-3 px-5 py-2.5 hover:bg-sidebar-active',
          isExactActive
            ? 'border-primary bg-sidebar-active font-semibold text-white'
            : 'border-transparent text-sidebar-ink',
        ]"
        @click="navigate"
      >
        {{ item.label }}
      </a>
    </RouterLink>
  </nav>
</template>
