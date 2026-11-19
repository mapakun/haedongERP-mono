<!-- 노선 근무표에서 한 기사의 시작점 변경. 근무표 메뉴와 같은 데이터(hd_duty_start)에 이력으로 추가한다 -->
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { getErrorMessage } from '@/shared/api/error'
import { createDutyStarts, fetchDutyStartHistory } from '@/domains/duty/duty.api'
import { shiftLabel, shiftOfCycleDay, type DutyStartHistory } from '@/domains/duty/duty.types'

export interface StartTarget {
  employeeName: string
  /** 기준일 기본값 (누른 날짜) */
  baseDate: string
  /** 주기 일차 기본값 (누른 날짜의 현재 일차, 없으면 null) */
  cycleDay: number | null
}

const props = defineProps<{
  target: StartTarget | null
}>()

const emit = defineEmits<{
  close: []
  saved: []
}>()

const CYCLE_DAYS = Array.from({ length: 12 }, (_, index) => index + 1)

const baseDate = ref('')
const cycleDay = ref<number | ''>('')
const current = ref<DutyStartHistory | null>(null)
const saving = ref(false)
const errorMessage = ref('')

/** 기준일부터 12일 동안의 근무 미리보기 */
const preview = computed(() => {
  if (typeof cycleDay.value !== 'number' || !baseDate.value) {
    return []
  }
  const [year = 0, month = 1, day = 1] = baseDate.value.split('-').map(Number)
  return CYCLE_DAYS.map((offset) => {
    const date = new Date(year, month - 1, day + offset - 1)
    const value = ((Number(cycleDay.value) - 1 + offset - 1) % 12) + 1
    return { label: `${date.getMonth() + 1}/${date.getDate()}`, shift: shiftLabel[shiftOfCycleDay(value)] }
  })
})

watch(
  () => props.target,
  async (target) => {
    if (!target) {
      return
    }
    baseDate.value = target.baseDate
    cycleDay.value = target.cycleDay ?? ''
    errorMessage.value = ''
    current.value = null
    try {
      const history = await fetchDutyStartHistory(target.employeeName)
      current.value = history.find((start) => !start.superseded && start.appliedUntil === null) ?? null
    } catch {
      // 현재 시작점 표시는 참고용이라 실패해도 입력은 할 수 있다
    }
  },
)

async function onSubmit() {
  if (!props.target) {
    return
  }
  errorMessage.value = ''
  if (!baseDate.value) {
    errorMessage.value = '기준일을 입력해 주세요.'
    return
  }
  if (typeof cycleDay.value !== 'number') {
    errorMessage.value = '주기 일차를 골라 주세요.'
    return
  }
  // 이 창 자체가 확인 단계라 window.confirm 은 쓰지 않는다 (브라우저에 따라 막혀서 조용히 취소될 수 있다)
  saving.value = true
  try {
    await createDutyStarts({
      baseDate: baseDate.value,
      items: [{ employeeName: props.target.employeeName, cycleDay: cycleDay.value }],
    })
    emit('saved')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '시작점을 저장하지 못했습니다.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <BaseModal :open="target !== null" :title="`${target?.employeeName ?? ''} 시작점 변경`" @close="emit('close')">
    <p class="mb-3 text-sm text-muted">
      현재:
      <template v-if="current">{{ current.baseDate }} = {{ current.cycleDay }}일차부터 계속</template>
      <template v-else>계속 적용 중인 시작점 없음</template>
    </p>

    <form id="route-roster-start-form" class="flex flex-col gap-3" @submit.prevent="onSubmit">
      <label class="flex flex-col gap-1">
        <span class="text-sm font-semibold">기준일</span>
        <input v-model="baseDate" type="date" class="input" required />
      </label>
      <label class="flex flex-col gap-1">
        <span class="text-sm font-semibold">기준일의 주기 일차</span>
        <select v-model="cycleDay" class="input" required>
          <option value="" disabled>골라 주세요</option>
          <option v-for="day in CYCLE_DAYS" :key="day" :value="day">
            {{ day }}일차 ({{ shiftLabel[shiftOfCycleDay(day)] }})
          </option>
        </select>
      </label>
    </form>

    <div v-if="preview.length" class="mt-3 grid grid-cols-6 gap-1 text-center text-xs">
      <div v-for="item in preview" :key="item.label" class="rounded bg-canvas py-1">
        <div class="text-muted">{{ item.label }}</div>
        <div>{{ item.shift }}</div>
      </div>
    </div>

    <p v-if="errorMessage" class="mt-3 text-danger">{{ errorMessage }}</p>
    <p class="mt-3 text-xs text-danger">저장하면 근무표 메뉴에도 똑같이 반영됩니다.</p>
    <p class="mt-1 text-xs text-muted">
      기준일부터 새 시작점이 적용되고, 그 이후를 기준으로 했던 기존 시작점은 덮어써집니다. 수동 수정한 칸은 그대로 남습니다.
    </p>

    <template #footer>
      <button type="button" class="btn" @click="emit('close')">취소</button>
      <button type="submit" form="route-roster-start-form" class="btn btn-primary" :disabled="saving">
        {{ saving ? '저장 중...' : '저장' }}
      </button>
    </template>
  </BaseModal>
</template>
