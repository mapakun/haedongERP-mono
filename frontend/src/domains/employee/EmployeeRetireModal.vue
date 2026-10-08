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
    <form v-if="target" id="retire-form" class="form" @submit.prevent="onSubmit">
      <p class="lead"><strong>{{ target.name }}</strong> 님을 퇴사 처리합니다.</p>

      <label>
        <span>퇴사일 <em class="required">*</em></span>
        <input v-model="retiredAt" type="date" :max="today" required />
      </label>

      <ul class="notice">
        <li v-if="target.seniorityNo !== null">입사 서열({{ target.seniorityNo }}번)이 비워집니다.</li>
        <li>로그인 계정이 있다면 로그인이 막힙니다.</li>
        <li>직원 목록에서 기본으로 보이지 않습니다. (퇴사자 포함 시 조회)</li>
      </ul>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </form>

    <template #footer>
      <button type="button" class="btn" @click="emit('close')">취소</button>
      <button type="submit" form="retire-form" class="btn btn-danger" :disabled="saving">
        {{ saving ? '처리 중...' : '퇴사 처리' }}
      </button>
    </template>
  </BaseModal>
</template>

<style scoped>
.form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.form label {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form input {
  padding: 8px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
}
.lead {
  margin: 0;
}
.notice {
  margin: 0;
  padding: 12px 12px 12px 28px;
  color: var(--color-text-muted);
  background: var(--color-bg);
  border-radius: var(--radius);
}
.required {
  font-style: normal;
  color: var(--color-danger);
}
.error {
  margin: 0;
  color: var(--color-danger);
}
</style>
