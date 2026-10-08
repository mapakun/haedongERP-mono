<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { getErrorMessage } from '@/shared/api/error'
import { formatMobile } from '@/shared/utils/format'
import { useAuthStore } from '@/domains/auth/auth.store'
import { cancelRetirement, createEmployee, getEmployee, updateEmployee } from './employee.api'
import type { EmployeeDetail, EmployeeSaveRequest, JobType, RetireTarget } from './employee.types'

const props = defineProps<{
  open: boolean
  employeeId: number | null
}>()

const emit = defineEmits<{
  close: []
  saved: []
  retire: [target: RetireTarget] // ★ 퇴사 처리 요청
}>()

const auth = useAuthStore()

const isEdit = computed(() => props.employeeId !== null)

const form = reactive({
  name: '',
  jobType: 'DRIVER' as JobType,
  birthDate: '',
  mobile: '',
  seniorityNo: '' as number | '',
})
const loaded = ref<EmployeeDetail | null>(null) // ★ 서버에서 불러온 원래 값
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')

// ★ 퇴사 여부, 본인 여부
const retiredAt = computed(() => loaded.value?.retiredAt ?? null)
const isSelf = computed(() => props.employeeId !== null && props.employeeId === auth.user?.id)

function resetForm() {
  Object.assign(form, { name: '', jobType: 'DRIVER', birthDate: '', mobile: '', seniorityNo: '' })
  loaded.value = null
  errorMessage.value = ''
}

async function loadEmployee(id: number) {
  loading.value = true
  try {
    const employee = await getEmployee(id)
    loaded.value = employee
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
    seniorityNo:
      form.jobType === 'DRIVER' && !retiredAt.value && form.seniorityNo !== ''
        ? form.seniorityNo
        : null,
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

// ★ 퇴사 처리: 확인창은 부모(목록 화면)가 연다
function onRetireClick() {
  if (!loaded.value) {
    return
  }
  emit('retire', {
    id: loaded.value.id,
    name: loaded.value.name,
    seniorityNo: loaded.value.seniorityNo,
  })
}

// ★ 퇴사 취소
async function onCancelRetirement() {
  if (!loaded.value) {
    return
  }
  const ok = window.confirm(
    `${loaded.value.name} 님의 퇴사를 취소할까요?\n입사 서열은 비어 있으니 필요하면 다시 입력해 주세요.`,
  )
  if (!ok) {
    return
  }
  saving.value = true
  errorMessage.value = ''
  try {
    await cancelRetirement(loaded.value.id)
    emit('saved')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '퇴사를 취소하지 못했습니다.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <BaseModal :open="open" :title="isEdit ? '직원 수정' : '직원 등록'" @close="emit('close')">
    <p v-if="loading">불러오는 중...</p>

    <template v-else>
      <form id="employee-form" class="form" @submit.prevent="onSubmit">
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

        <!-- ★ 퇴사자는 서열 입력 칸을 숨긴다 -->
        <label v-if="form.jobType === 'DRIVER' && !retiredAt">
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

      <!-- ★ 재직 관리 (수정일 때만) -->
      <section v-if="loaded" class="status">
        <h4>재직 관리</h4>
        <div v-if="retiredAt" class="status-row">
          <span>퇴사 ({{ retiredAt }})</span>
          <button type="button" class="btn" :disabled="saving" @click="onCancelRetirement">퇴사 취소</button>
        </div>
        <div v-else class="status-row">
          <span>재직 중</span>
          <button v-if="!isSelf" type="button" class="btn btn-danger" @click="onRetireClick">퇴사 처리</button>
        </div>
      </section>
    </template>

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
.status {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}
.status h4 {
  margin: 0 0 10px;
  font-size: 14px;
  color: var(--color-text-muted);
}
.status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
