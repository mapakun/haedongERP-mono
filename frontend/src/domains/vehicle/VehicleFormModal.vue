<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { getErrorMessage } from '@/shared/api/error'
import type { RouteSummary } from '@/domains/route/route.types'
import { assignVehicle, createVehicle, deleteVehicle } from './vehicle.api'
import type { VehicleSummary } from './vehicle.types'

const props = defineProps<{
  open: boolean
  /** 배정을 바꿀 차량. null 이면 새 차량 등록 */
  target: VehicleSummary | null
  /** 노선 선택 상자에 보여 줄 노선 목록 (배정 현황 포함) */
  routes: RouteSummary[]
}>()

const emit = defineEmits<{
  close: []
  saved: []
}>()

const isEdit = computed(() => props.target !== null)

const vehicleNo = ref('')
const routeNo = ref('') // '' = 예비 차량
const slotNo = ref<number | ''>('') // '' = 슬롯 비움 (v-model.number 는 빈 칸을 '' 로 넣는다)
const saving = ref(false)
const errorMessage = ref('')

/** 선택한 노선의 배정 현황 (예비 차량이면 null) */
const selectedRoute = computed(() => props.routes.find((r) => r.routeNo === routeNo.value) ?? null)

watch(
  () => props.open,
  (open) => {
    if (open) {
      vehicleNo.value = props.target?.vehicleNo ?? ''
      routeNo.value = props.target?.routeNo ?? ''
      slotNo.value = props.target?.slotNo ?? ''
      errorMessage.value = ''
    }
  },
)

// 노선을 "예비 차량"으로 바꾸면 슬롯도 비운다 (슬롯만 있는 상태는 규칙상 불가)
watch(routeNo, (value) => {
  if (value === '') {
    slotNo.value = ''
  }
})

async function onSubmit() {
  saving.value = true
  errorMessage.value = ''
  const assignment = {
    routeNo: routeNo.value || null,
    slotNo: slotNo.value === '' ? null : slotNo.value,
  }
  try {
    if (props.target) {
      await assignVehicle(props.target.vehicleNo, assignment)
    } else {
      await createVehicle({ vehicleNo: vehicleNo.value.trim(), ...assignment })
    }
    emit('saved')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '저장하지 못했습니다.')
  } finally {
    saving.value = false
  }
}

async function onDelete() {
  if (!props.target || !confirm(`${props.target.vehicleNo} 차량을 삭제할까요? 되돌릴 수 없습니다.`)) {
    return
  }
  saving.value = true
  errorMessage.value = ''
  try {
    await deleteVehicle(props.target.vehicleNo)
    emit('saved')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '삭제하지 못했습니다.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <BaseModal :open="open" :title="isEdit ? '차량 배정 변경' : '차량 등록'" @close="emit('close')">
    <form id="vehicle-form" class="flex flex-col gap-3.5" @submit.prevent="onSubmit">
      <label class="flex flex-col gap-1.5">
        <span>차량 번호 <em class="text-danger not-italic">*</em></span>
        <input
          v-model="vehicleNo"
          type="text"
          inputmode="numeric"
          class="input"
          maxlength="4"
          pattern="[0-9]{4}"
          title="숫자 4자리"
          placeholder="예: 0123"
          :disabled="isEdit"
          required
        />
      </label>

      <label class="flex flex-col gap-1.5">
        <span>노선</span>
        <select v-model="routeNo" class="input">
          <option value="">예비 차량 (노선 없음)</option>
          <option v-for="route in routes" :key="route.routeNo" :value="route.routeNo">
            {{ route.routeNo }} ({{ route.assignedCount }}/{{ route.totalSlots }})
          </option>
        </select>
      </label>

      <label class="flex flex-col gap-1.5">
        <span>슬롯 번호</span>
        <input
          v-model.number="slotNo"
          type="number"
          class="input"
          min="1"
          :max="selectedRoute?.totalSlots"
          :disabled="!selectedRoute"
          :placeholder="selectedRoute ? `1 ~ ${selectedRoute.totalSlots} (비워 두면 미배정)` : '노선을 먼저 선택하세요'"
        />
      </label>

      <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    </form>

    <template #footer>
      <button
        v-if="isEdit"
        type="button"
        class="btn btn-danger mr-auto"
        :disabled="saving"
        @click="onDelete"
      >
        삭제
      </button>
      <button type="button" class="btn" @click="emit('close')">취소</button>
      <button type="submit" form="vehicle-form" class="btn btn-primary" :disabled="saving">
        {{ saving ? '저장 중...' : '저장' }}
      </button>
    </template>
  </BaseModal>
</template>
