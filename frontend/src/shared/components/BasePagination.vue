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
  <nav class="mt-4 flex flex-wrap justify-center gap-1">
    <button type="button" class="btn px-2.5 py-1.5" :disabled="page <= 1" @click="go(page - 1)">
      이전
    </button>
    <button
      v-for="number in pageNumbers"
      :key="number"
      type="button"
      :class="['btn min-w-9 px-2.5 py-1.5', { 'btn-primary': number === page }]"
      @click="go(number)"
    >
      {{ number }}
    </button>
    <button
      type="button"
      class="btn px-2.5 py-1.5"
      :disabled="page >= totalPages"
      @click="go(page + 1)"
    >
      다음
    </button>
  </nav>
</template>
