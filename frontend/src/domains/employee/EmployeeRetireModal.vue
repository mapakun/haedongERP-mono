<script setup lang="ts">
import { ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { getErrorMessage } from '@/shared/api/error'
import { todayString } from '@/shared/utils/date'
import { retireEmployee } from './employee.api'
import type { RetireTarget } from './employee.types'

const props = defineProps<{
  target: RetireTarget | null
}>()

const emit = defineEmits<{
  close: []
  retired: []
}>()

const today = todayString()
const retiredAt = ref(today)
const saving = ref(false)
const errorMessage = ref('')

// 대상이 정해질 때(확인창이 열릴 때)마다 초기화
watch(
  () => props.target,
  (target) => {
    if (target) {
      retiredAt.value = todayString()
      errorMessage.value = ''
    }
  },
)

async function onSubmit() {
  if (!props.target) {
    return
  }
  saving.value = true
  errorMessage.value = ''
  try {
    await retireEmployee(props.target.id, retiredAt.value)
    emit('retired')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '퇴사 처리하지 못했습니다.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <BaseModal :open="target !== null" title="퇴사 처리" @close="emit('close')">
    <form
      v-if="target"
      id="retire-form"
      class="flex flex-col gap-3.5"
      @submit.prevent="onSubmit"
    >
      <p><strong>{{ target.name }}</strong> 님을 퇴사 처리합니다.</p>

      <label class="flex flex-col gap-1.5">
        <span>퇴사일 <em class="text-danger not-italic">*</em></span>
        <input v-model="retiredAt" type="date" class="input" :max="today" required />
      </label>

      <ul class="list-disc space-y-1 rounded-md bg-canvas py-3 pr-3 pl-7 text-muted">
        <li v-if="target.seniorityNo !== null">입사 서열({{ target.seniorityNo }}번)이 비워집니다.</li>
        <li>로그인 계정이 있다면 로그인이 막힙니다.</li>
        <li>직원 목록에서 기본으로 보이지 않습니다. (퇴사자 포함 시 조회)</li>
      </ul>

      <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    </form>

    <template #footer>
      <button type="button" class="btn" @click="emit('close')">취소</button>
      <button type="submit" form="retire-form" class="btn btn-danger" :disabled="saving">
        {{ saving ? '처리 중...' : '퇴사 처리' }}
      </button>
    </template>
  </BaseModal>
</template>
