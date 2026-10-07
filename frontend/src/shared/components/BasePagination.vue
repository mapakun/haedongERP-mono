<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  page: number
  size: number
  totalCount: number
}>()

const emit = defineEmits<{
  change: [page: number]
}>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.totalCount / props.size)))

// 현재 페이지 주변으로 최대 5개 번호만 보여준다
const pageNumbers = computed(() => {
  const start = Math.max(1, Math.min(props.page - 2, totalPages.value - 4))
  const end = Math.min(totalPages.value, start + 4)
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

function go(target: number) {
  if (target < 1 || target > totalPages.value || target === props.page) {
    return
  }
  emit('change', target)
}
</script>

<template>
  <nav class="pagination">
    <button type="button" :disabled="page <= 1" @click="go(page - 1)">이전</button>
    <button
      v-for="number in pageNumbers"
      :key="number"
      type="button"
      :class="{ active: number === page }"
      @click="go(number)"
    >
      {{ number }}
    </button>
    <button type="button" :disabled="page >= totalPages" @click="go(page + 1)">다음</button>
  </nav>
</template>

<style scoped>
.pagination {
  display: flex;
  justify-content: center;
  gap: 4px;
  margin-top: 16px;
}
.pagination button {
  min-width: 36px;
  padding: 6px 10px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  cursor: pointer;
}
.pagination button:hover:not(:disabled) {
  background: var(--color-bg);
}
.pagination button:disabled {
  color: var(--color-text-muted);
  cursor: default;
}
.pagination button.active {
  color: #fff;
  background: var(--color-primary);
  border-color: var(--color-primary);
}
</style>
