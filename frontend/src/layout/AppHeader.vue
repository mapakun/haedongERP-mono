<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/domains/auth/auth.store'

const router = useRouter()
const auth = useAuthStore()

async function onLogout() {
  await auth.logout()
  await router.push({ name: 'login' })
}
</script>

<template>
  <header class="header">
    <strong class="logo">해동 ERP</strong>
    <div class="user">
      <span>{{ auth.user?.name }}님 ({{ auth.user?.role === 'ADMIN' ? '관리자' : '사용자' }})</span>
      <button type="button" class="logout" @click="onLogout">로그아웃</button>
    </div>
  </header>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}
.logo {
  font-size: 18px;
}
.user {
  display: flex;
  align-items: center;
  gap: 12px;
}
.logout {
  padding: 6px 12px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  cursor: pointer;
}
.logout:hover {
  background: var(--color-bg);
}
</style>
