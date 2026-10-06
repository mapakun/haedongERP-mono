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
  <div class="main-page">
    <header class="top-bar">
      <strong>해동 ERP</strong>
      <div>
        <span>{{ auth.user?.name }}님 ({{ auth.user?.role === 'ADMIN' ? '관리자' : '사용자' }})</span>
        <button @click="onLogout">로그아웃</button>
      </div>
    </header>

    <main class="content">
      <h2>메인 화면</h2>
      <p>여기에 메뉴와 대시보드가 들어갈 예정입니다.</p>
    </main>
  </div>
</template>

<style scoped>
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: #1f2937;
  color: #fff;
}
.top-bar button {
  margin-left: 12px;
  padding: 6px 12px;
  cursor: pointer;
}
.content {
  padding: 24px;
}
</style>
