<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useAuthStore } from './auth.store'

const router = useRouter()
const auth = useAuthStore()

const loginId = ref('')
const password = ref('')
const errorMessage = ref('')
const loading = ref(false)
const capsLockOn = ref(false)

async function onSubmit() {
  errorMessage.value = ''
  loading.value = true
  try {
    await auth.login({ loginId: loginId.value, password: password.value })
    await router.push({ name: 'main' })
  } catch (e) {
    if (axios.isAxiosError(e) && e.response?.status === 401) {
      errorMessage.value = e.response.data?.message ?? '로그인에 실패했습니다.'
    } else if (axios.isAxiosError(e) && e.response?.status === 400) {
      errorMessage.value = '아이디와 비밀번호를 입력해 주세요.'
    } else {
      errorMessage.value = '서버와 통신할 수 없습니다. 잠시 후 다시 시도해 주세요.'
    }
  } finally {
    loading.value = false
  }
}

function checkCapsLock(event: KeyboardEvent) {
  capsLockOn.value = event.getModifierState('CapsLock')
}
</script>

<template>
  <div class="login-page">
    <form class="login-box" @submit.prevent="onSubmit">
      <h1>해동 ERP</h1>

      <label>
        아이디
        <input v-model="loginId" type="text" autocomplete="username" required />
      </label>

      <label>
        비밀번호
        <input
          v-model="password"
          type="password"
          autocomplete="current-password"
          required
          @keyup="checkCapsLock"
        />
      </label>

      <p v-if="capsLockOn" class="hint">Caps Lock이 켜져 있습니다.</p>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

      <button type="submit" :disabled="loading">
        {{ loading ? '로그인 중...' : '로그인' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
}
.login-box {
  width: 320px;
  padding: 32px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.login-box h1 {
  margin: 0 0 8px;
  text-align: center;
  font-size: 24px;
}
.login-box label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
}
.login-box input {
  padding: 10px;
  font-size: 16px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
}
.login-box button {
  padding: 12px;
  font-size: 16px;
  color: #fff;
  background: #2563eb;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.login-box button:disabled {
  background: #93c5fd;
  cursor: default;
}
.hint {
  margin: 0;
  color: #b45309;
  font-size: 13px;
}
.error {
  margin: 0;
  color: #dc2626;
  font-size: 13px;
}
</style>
