<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getErrorMessage } from '@/shared/api/error'
import { useAuthStore } from './auth.store'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const loginId = ref('')
const password = ref('')
const errorMessage = ref('')
const loading = ref(false)
const capsLockOn = ref(false)

// 세션이 끊겨서 이 화면으로 왔는지
const expired = route.query.expired === '1'

async function onSubmit() {
  errorMessage.value = ''
  loading.value = true
  try {
    await auth.login({ loginId: loginId.value, password: password.value })
    await router.push(redirectPath())
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '서버와 통신할 수 없습니다. 잠시 후 다시 시도해 주세요.')
  } finally {
    loading.value = false
  }
}

/** 로그인 후 돌아갈 주소. 우리 사이트 안의 경로만 허용한다 */
function redirectPath(): string {
  const value = route.query.redirect
  if (typeof value === 'string' && value.startsWith('/') && !value.startsWith('//')) {
    return value
  }
  return '/'
}

function checkCapsLock(event: KeyboardEvent) {
  capsLockOn.value = event.getModifierState('CapsLock')
}
</script>

<template>
  <div class="flex min-h-dvh items-center justify-center bg-canvas p-4">
    <form
      class="flex w-full max-w-sm flex-col gap-4 rounded-lg bg-surface p-8 shadow-md"
      @submit.prevent="onSubmit"
    >
      <h1 class="mb-2 text-center text-2xl font-bold">해동 ERP</h1>

      <label class="flex flex-col gap-1.5">
        아이디
        <input v-model="loginId" type="text" class="input" autocomplete="username" required />
      </label>

      <label class="flex flex-col gap-1.5">
        비밀번호
        <input
          v-model="password"
          type="password"
          class="input"
          autocomplete="current-password"
          required
          @keyup="checkCapsLock"
        />
      </label>

      <p v-if="capsLockOn" class="text-xs text-amber-700">Caps Lock이 켜져 있습니다.</p>
      <p v-if="errorMessage" class="text-xs text-danger">{{ errorMessage }}</p>

      <button type="submit" class="btn btn-primary py-3 text-base" :disabled="loading">
        {{ loading ? '로그인 중...' : '로그인' }}
      </button>
    </form>
  </div>
</template>
