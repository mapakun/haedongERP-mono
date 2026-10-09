<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'

// 휴대폰에서 왼쪽 메뉴가 열려 있는지 (넓은 화면에서는 항상 보이므로 쓰지 않음)
const sidebarOpen = ref(false)

// 메뉴를 눌러 화면이 바뀌면 메뉴를 닫는다
const route = useRoute()
watch(
  () => route.fullPath,
  () => {
    sidebarOpen.value = false
  },
)
</script>

<template>
  <div class="flex h-dvh flex-col">
    <AppHeader @toggle-menu="sidebarOpen = !sidebarOpen" />

    <div class="flex min-h-0 flex-1">
      <!-- 휴대폰: 메뉴가 열렸을 때 뒤를 어둡게, 누르면 닫힘 -->
      <div
        v-if="sidebarOpen"
        class="fixed inset-0 z-30 bg-black/40 md:hidden"
        @click="sidebarOpen = false"
      />

      <AppSidebar
        :class="[
          'fixed inset-y-0 left-0 z-40 w-56 transition-transform md:static md:translate-x-0',
          sidebarOpen ? 'translate-x-0' : '-translate-x-full',
        ]"
      />

      <main class="min-w-0 flex-1 overflow-auto p-4 md:p-6">
        <RouterView />
      </main>
    </div>
  </div>
</template>
