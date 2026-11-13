<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { getErrorMessage } from '@/shared/api/error'
import { addContract, deleteContract, getContracts, saveMember } from './comfortleave.api'
import {
  employmentTypeLabel,
  type ComfortContract,
  type DriverMember,
  type EmploymentType,
} from './comfortleave.types'

const props = defineProps<{
  /** 편집할 운전직. null 이면 창이 닫혀 있다 */
  member: DriverMember | null
}>()

const emit = defineEmits<{
  close: []
  /** 저장·계약 변경이 일어나서 목록을 다시 불러와야 할 때 */
  changed: []
}>()

const employmentType = ref<EmploymentType>('REGULAR')
const hireDate = ref('')
const note = ref('')
const saving = ref(false)
const errorMessage = ref('')
const message = ref('')

const contracts = ref<ComfortContract[]>([])
const contractStart = ref('')
const contractEnd = ref('')
const contractBusy = ref(false)
const contractError = ref('')

const title = computed(() => (props.member ? `${props.member.name} 위로휴가 정보` : '위로휴가 정보'))

async function loadContracts() {
  if (!props.member) {
    return
  }
  try {
    contracts.value = await getContracts(props.member.employeeId)
  } catch (error) {
    contractError.value = getErrorMessage(error, '계약을 불러오지 못했습니다.')
  }
}

// 다른 사람을 열었을 때만 입력칸을 초기화한다 (저장 후 목록이 다시 불려도 입력 중인 값이 지워지지 않게)
watch(
  () => props.member?.employeeId,
  () => {
    const member = props.member
    if (!member) {
      return
    }
    employmentType.value = member.employmentType ?? 'REGULAR'
    hireDate.value = member.hireDate ?? ''
    note.value = member.note ?? ''
    errorMessage.value = ''
    message.value = ''
    contracts.value = []
    contractStart.value = ''
    contractEnd.value = ''
    contractError.value = ''
    if (member.employmentType === 'CONTRACT') {
      loadContracts()
    }
  },
  { immediate: true },
)

async function onSave() {
  if (!props.member) {
    return
  }
  saving.value = true
  errorMessage.value = ''
  message.value = ''
  try {
    await saveMember(props.member.employeeId, {
      employmentType: employmentType.value,
      hireDate: hireDate.value || null,
      note: note.value.trim() || null,
    })
    message.value = '저장했습니다.'
    emit('changed')
    if (employmentType.value === 'CONTRACT') {
      await loadContracts()
    }
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '저장하지 못했습니다.')
  } finally {
    saving.value = false
  }
}

async function onAddContract() {
  if (!props.member || !contractStart.value) {
    return
  }
  contractBusy.value = true
  contractError.value = ''
  try {
    await addContract(props.member.employeeId, {
      startDate: contractStart.value,
      endDate: contractEnd.value || null,
    })
    contractStart.value = ''
    contractEnd.value = ''
    await loadContracts()
    emit('changed')
  } catch (error) {
    contractError.value = getErrorMessage(error, '계약을 추가하지 못했습니다.')
  } finally {
    contractBusy.value = false
  }
}

async function onDeleteContract(contract: ComfortContract) {
  if (!props.member || !confirm(`${contract.startDate} ~ ${contract.endDate} 계약을 삭제할까요?`)) {
    return
  }
  contractBusy.value = true
  contractError.value = ''
  try {
    await deleteContract(props.member.employeeId, contract.startDate)
    await loadContracts()
    emit('changed')
  } catch (error) {
    contractError.value = getErrorMessage(error, '계약을 삭제하지 못했습니다.')
  } finally {
    contractBusy.value = false
  }
}
</script>

<template>
  <BaseModal :open="member !== null" :title="title" @close="emit('close')">
    <template v-if="member">
      <form id="comfort-member-form" class="flex flex-col gap-3.5" @submit.prevent="onSave">
        <label class="flex flex-col gap-1.5">
          <span>고용 형태 <em class="text-danger not-italic">*</em></span>
          <select v-model="employmentType" class="input">
            <option v-for="(label, key) in employmentTypeLabel" :key="key" :value="key">{{ label }}</option>
          </select>
        </label>

        <label class="flex flex-col gap-1.5">
          <span>입사일자</span>
          <input v-model="hireDate" type="date" class="input" />
          <span class="text-xs text-muted">
            정규직은 입사한 다음 해부터 위로휴가가 생깁니다. 비워 두면 이미 근무 중인 것으로 보고 4개가 생깁니다.
          </span>
        </label>

        <label class="flex flex-col gap-1.5">
          <span>메모</span>
          <input v-model="note" type="text" class="input" maxlength="100" placeholder="선택 입력" />
        </label>

        <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
        <p v-else-if="message" class="text-primary">{{ message }}</p>
      </form>

      <!-- 촉탁 계약 (저장된 고용 형태가 촉탁직일 때만) -->
      <section class="mt-6 border-t border-line pt-4">
        <h4 class="mb-2 font-semibold">촉탁 계약</h4>

        <p v-if="member.employmentType !== 'CONTRACT'" class="text-muted">
          고용 형태를 촉탁직으로 저장하면 계약을 추가할 수 있습니다.
        </p>

        <template v-else>
          <ul class="mb-3 flex flex-col gap-1.5">
            <li
              v-for="contract in contracts"
              :key="contract.startDate"
              class="flex items-center justify-between rounded border border-line px-3 py-2"
            >
              <span>{{ contract.startDate }} ~ {{ contract.endDate }}</span>
              <button
                type="button"
                class="cursor-pointer text-muted hover:text-danger"
                :disabled="contractBusy"
                @click="onDeleteContract(contract)"
              >
                삭제
              </button>
            </li>
            <li v-if="contracts.length === 0" class="text-muted">등록된 계약이 없습니다.</li>
          </ul>

          <form class="flex flex-wrap items-end gap-2" @submit.prevent="onAddContract">
            <label class="flex flex-col gap-1.5">
              <span>시작일</span>
              <input v-model="contractStart" type="date" class="input" required />
            </label>
            <label class="flex flex-col gap-1.5">
              <span>만료일</span>
              <input v-model="contractEnd" type="date" class="input" :min="contractStart" />
            </label>
            <button type="submit" class="btn btn-primary" :disabled="contractBusy">계약 추가</button>
          </form>
          <p class="mt-1 text-xs text-muted">만료일을 비우면 시작일 + 1년 - 1일로 정해집니다.</p>
          <p v-if="contractError" class="mt-2 text-danger">{{ contractError }}</p>
        </template>
      </section>
    </template>

    <template #footer>
      <button type="button" class="btn" @click="emit('close')">닫기</button>
      <button type="submit" form="comfort-member-form" class="btn btn-primary" :disabled="saving">
        {{ saving ? '저장 중...' : '저장' }}
      </button>
    </template>
  </BaseModal>
</template>
