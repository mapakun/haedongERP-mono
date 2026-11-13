<script setup lang="ts">
import { ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { getErrorMessage } from '@/shared/api/error'
import { todayString } from '@/shared/utils/date'
import { addComfortUse } from './comfortleave.api'
import type { UseTarget } from './comfortleave.types'

const props = defineProps<{
  /** 사용을 기록할 사람. null 이면 창이 닫혀 있다 */
  target: UseTarget | null
}>()

const emit = defineEmits<{
  close: []
  saved: []
}>()

const usedOn = ref('')
const memo = ref('')
const saving = ref(false)
const errorMessage = ref('')

watch(
  () => props.target,
  (target) => {
    if (!target) {
      return
    }
    // 기본값은 오늘. 촉탁 계약 기간 밖이면 기간 안쪽 끝으로 맞춘다
    let value = todayString()
    if (target.minDate && value < target.minDate) {
      value = target.minDate
    }
    if (target.maxDate && value > target.maxDate) {
      value = target.maxDate
    }
    usedOn.value = value
    memo.value = ''
    errorMessage.value = ''
  },
)

async function onSubmit() {
  if (!props.target) {
    return
  }
  saving.value = true
  errorMessage.value = ''
  try {
    await addComfortUse({
      employeeId: props.target.employeeId,
      usedOn: usedOn.value,
      memo: memo.value.trim() || null,
    })
    emit('saved')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '저장하지 못했습니다.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <BaseModal :open="target !== null" title="위로휴가 사용 기록" @close="emit('close')">
    <form v-if="target" id="comfort-use-form" class="flex flex-col gap-3.5" @submit.prevent="onSubmit">
      <p>
        <strong>{{ target.name }}</strong>
        <span class="ml-1 text-muted">{{ target.periodLabel }}</span>
      </p>

      <label class="flex flex-col gap-1.5">
        <span>사용일 <em class="text-danger not-italic">*</em></span>
        <input
          v-model="usedOn"
          type="date"
          class="input"
          :min="target.minDate"
          :max="target.maxDate"
          required
        />
      </label>

      <label class="flex flex-col gap-1.5">
        <span>메모</span>
        <input v-model="memo" type="text" class="input" maxlength="100" placeholder="선택 입력" />
      </label>

      <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    </form>

    <template #footer>
      <button type="button" class="btn" @click="emit('close')">취소</button>
      <button type="submit" form="comfort-use-form" class="btn btn-primary" :disabled="saving">
        {{ saving ? '저장 중...' : '사용 기록' }}
      </button>
    </template>
  </BaseModal>
</template>
