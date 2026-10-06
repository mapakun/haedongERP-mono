<script setup lang="ts">
import { useAuthStore } from '@/domains/auth/auth.store'

const auth = useAuthStore()
</script>

<template>
  <section>
    <h2>홈</h2>
    <p>{{ auth.user?.name }}님, 환영합니다.</p>
  </section>
</template>
