<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/domains/auth/auth.store'

const emit = defineEmits<{
  toggleMenu: []
}>()

const router = useRouter()
const auth = useAuthStore()

async function onLogout() {
  await auth.logout()
  await router.push({ name: 'login' })
}
</script>

<template>
  <header
    class="flex h-14 shrink-0 items-center justify-between border-b border-line bg-surface px-4 md:px-6"
  >
    <div class="flex items-center gap-2">
      <!-- 휴대폰에서만 보이는 메뉴 버튼 -->
      <button
        type="button"
        class="-ml-2 cursor-pointer rounded-md p-2 hover:bg-canvas md:hidden"
        aria-label="메뉴 열기"
        @click="emit('toggleMenu')"
      >
        <svg class="size-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>
      <strong class="text-lg">해동 ERP</strong>
    </div>

    <div class="flex items-center gap-3">
      <span>
        {{ auth.user?.name }}님
        <span class="hidden text-muted sm:inline">
          ({{ auth.user?.role === 'ADMIN' ? '관리자' : '사용자' }})
        </span>
      </span>
      <button type="button" class="btn px-3 py-1.5" @click="onLogout">로그아웃</button>
    </div>
  </header>
</template>
