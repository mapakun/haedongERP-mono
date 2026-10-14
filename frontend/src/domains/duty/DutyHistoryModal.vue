<script setup lang="ts">
import { ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { getErrorMessage } from '@/shared/api/error'
import { formatDateTime } from '@/shared/utils/date'
import { fetchDutyStartHistory } from './duty.api'
import { shiftLabel, shiftOfCycleDay, type DutyStartHistory } from './duty.types'

// employeeName 이 있으면 열리고, null 이면 닫힌다
const props = defineProps<{
  employeeName: string | null
}>()

const emit = defineEmits<{
  close: []
}>()

const starts = ref<DutyStartHistory[]>([])
const loading = ref(false)
const errorMessage = ref('')

watch(
  () => props.employeeName,
  async (name) => {
    if (!name) {
      return
    }
    loading.value = true
    errorMessage.value = ''
    starts.value = []
    try {
      starts.value = await fetchDutyStartHistory(name)
    } catch (error) {
      errorMessage.value = getErrorMessage(error, '이력을 불러오지 못했습니다.')
    } finally {
      loading.value = false
    }
  },
)

function periodText(start: DutyStartHistory): string {
  if (start.superseded) {
    return '덮어써짐'
  }
  return `${start.baseDate} ~ ${start.appliedUntil ?? '계속'}`
}
</script>

<template>
  <BaseModal :open="employeeName !== null" :title="`${employeeName ?? ''} 근무표 이력`" @close="emit('close')">
    <h4 class="mb-2.5 font-semibold text-muted">시작점</h4>

    <p v-if="loading">불러오는 중...</p>
    <p v-else-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    <p v-else-if="starts.length === 0" class="text-muted">입력된 시작점이 없습니다. (근무표 미설정)</p>

    <ul v-else class="flex flex-col gap-2">
      <li
        v-for="start in starts"
        :key="start.id"
        class="rounded-md border border-line p-3"
        :class="{ 'opacity-50': start.superseded }"
      >
        <div class="flex items-center justify-between gap-2">
          <span>
            <strong>{{ start.baseDate }} = {{ start.cycleDay }}일차</strong>
            <span class="ml-1 text-muted">({{ shiftLabel[shiftOfCycleDay(start.cycleDay)] }})</span>
          </span>
          <span
            class="rounded px-2 py-0.5 text-xs whitespace-nowrap"
            :class="start.superseded ? 'bg-canvas text-muted' : 'bg-primary/10 text-primary'"
          >
            {{ periodText(start) }}
          </span>
        </div>
        <div class="mt-1 text-xs text-muted">
          {{ start.createdByName ?? '알 수 없음' }} · {{ formatDateTime(start.createdAt) }} 입력
        </div>
      </li>
    </ul>

    <p class="mt-3 text-xs text-muted">
      최근 입력 순입니다. 새 시작점은 기준일부터 적용되고, 그 이후를 기준으로 했던 기존 시작점을 덮어씁니다.
    </p>
  </BaseModal>
</template>
