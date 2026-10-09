<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/domains/auth/auth.store'
import { roleLabel } from '@/domains/auth/auth.types'
import MyPasswordModal from '@/domains/auth/MyPasswordModal.vue'

const emit = defineEmits<{
  toggleMenu: []
}>()

const router = useRouter()
const auth = useAuthStore()

const userMenuOpen = ref(false)
const passwordModalOpen = ref(false)

function openPasswordModal() {
  userMenuOpen.value = false
  passwordModalOpen.value = true
}

async function onLogout() {
  userMenuOpen.value = false
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

    <!-- 사용자 메뉴 -->
    <div class="relative">
      <button
        type="button"
        class="flex cursor-pointer items-center gap-1 rounded-md px-2 py-1.5 hover:bg-canvas"
        :aria-expanded="userMenuOpen"
        @click="userMenuOpen = !userMenuOpen"
      >
        {{ auth.user?.name }}님
        <span v-if="auth.user" class="hidden text-muted sm:inline">
          ({{ roleLabel[auth.user.role] }})
        </span>
        <svg class="size-4 text-muted" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 9l6 6 6-6" />
        </svg>
      </button>

      <template v-if="userMenuOpen">
        <!-- 메뉴 바깥 아무 곳이나 누르면 닫히도록 화면 전체를 덮는 투명한 막 -->
        <div class="fixed inset-0 z-40" @click="userMenuOpen = false" />
        <div
          class="absolute right-0 z-50 mt-1 w-40 overflow-hidden rounded-md border border-line bg-surface shadow-lg"
        >
          <button
            type="button"
            class="block w-full cursor-pointer px-4 py-2.5 text-left hover:bg-canvas"
            @click="openPasswordModal"
          >
            비밀번호 변경
          </button>
          <button
            type="button"
            class="block w-full cursor-pointer px-4 py-2.5 text-left text-danger hover:bg-canvas"
            @click="onLogout"
          >
            로그아웃
          </button>
        </div>
      </template>
    </div>

    <MyPasswordModal :open="passwordModalOpen" @close="passwordModalOpen = false" />
  </header>
</template>
