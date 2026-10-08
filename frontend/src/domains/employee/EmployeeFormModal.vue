<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { getErrorMessage } from '@/shared/api/error'
import { formatMobile } from '@/shared/utils/format'
import { createEmployee, getEmployee, updateEmployee } from './employee.api'
import type { EmployeeSaveRequest, JobType } from './employee.types'

const props = defineProps<{
  open: boolean
  employeeId: number | null
}>()

const emit = defineEmits<{
  close: []
  saved: []
}>()

const isEdit = computed(() => props.employeeId !== null)

const form = reactive({
  name: '',
  jobType: 'DRIVER' as JobType,
  birthDate: '',
  mobile: '',
  seniorityNo: '' as number | '',
})
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')

function resetForm() {
  Object.assign(form, { name: '', jobType: 'DRIVER', birthDate: '', mobile: '', seniorityNo: '' })
  errorMessage.value = ''
}

async function loadEmployee(id: number) {
  loading.value = true
  try {
    const employee = await getEmployee(id)
    Object.assign(form, {
      name: employee.name,
      jobType: employee.jobType,
      birthDate: employee.birthDate ?? '',
      mobile: employee.mobile ? formatMobile(employee.mobile) : '',
      seniorityNo: employee.seniorityNo ?? '',
    })
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '직원 정보를 불러오지 못했습니다.')
  } finally {
    loading.value = false
  }
}

// 모달이 열릴 때마다 폼을 초기화하고, 수정이면 기존 값을 불러온다
watch(
  () => props.open,
  (open) => {
    if (!open) {
      return
    }
    resetForm()
    if (props.employeeId !== null) {
      loadEmployee(props.employeeId)
    }
  },
)

async function onSubmit() {
  saving.value = true
  errorMessage.value = ''

  const request: EmployeeSaveRequest = {
    name: form.name,
    jobType: form.jobType,
    birthDate: form.birthDate || null,
    mobile: form.mobile || null,
    seniorityNo: form.jobType === 'DRIVER' && form.seniorityNo !== '' ? form.seniorityNo : null,
  }

  try {
    if (props.employeeId === null) {
      await createEmployee(request)
    } else {
      await updateEmployee(props.employeeId, request)
    }
    emit('saved')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '저장하지 못했습니다.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <BaseModal :open="open" :title="isEdit ? '직원 수정' : '직원 등록'" @close="emit('close')">
    <p v-if="loading">불러오는 중...</p>

    <form v-else id="employee-form" class="form" @submit.prevent="onSubmit">
      <label>
        <span>이름 <em class="required">*</em></span>
        <input v-model="form.name" type="text" maxlength="50" required placeholder="동명이인은 홍길동1 처럼 입력" />
      </label>

      <label>
        <span>직종 <em class="required">*</em></span>
        <select v-model="form.jobType">
          <option value="DRIVER">기사</option>
          <option value="OFFICE">사무</option>
        </select>
      </label>

      <label v-if="form.jobType === 'DRIVER'">
        <span>입사 서열</span>
        <input v-model.number="form.seniorityNo" type="number" min="1" placeholder="1 = 가장 먼저 입사" />
      </label>

      <label>
        <span>생년월일</span>
        <input v-model="form.birthDate" type="date" />
      </label>

      <label>
        <span>휴대폰</span>
        <input v-model="form.mobile" type="tel" placeholder="010-1234-5678" />
      </label>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </form>

    <template #footer>
      <button type="button" class="btn" @click="emit('close')">취소</button>
      <button type="submit" form="employee-form" class="btn btn-primary" :disabled="saving || loading">
        {{ saving ? '저장 중...' : '저장' }}
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
.form input,
.form select {
  padding: 8px 10px;
  border: 1px solid var(--color-border);
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
