<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { getErrorMessage } from '@/shared/api/error'
import { todayString } from '@/shared/utils/date'
import { createMissedTrip, updateMissedTrip } from './missedtrip.api'
import { reasonOptions, type MissedReason, type MissedTrip } from './missedtrip.types'

const props = defineProps<{
  open: boolean
  /** 수정할 기록. null 이면 새로 입력 */
  target: MissedTrip | null
  /** 노선 선택 목록 */
  routeNos: string[]
}>()

const emit = defineEmits<{
  close: []
  saved: []
}>()

const form = reactive({
  missedOn: '',
  routeNo: '',
  vehicleNo: '',
  tripLabel: '',
  driverName: '',
  missedCount: 1,
  reason: 'TRAFFIC' as MissedReason,
  note: '',
})
const saving = ref(false)
const errorMessage = ref('')

// 창이 열릴 때마다 입력값을 처음 상태(또는 수정 대상 값)로 맞춘다
watch(
  () => props.open,
  (open) => {
    if (!open) {
      return
    }
    const t = props.target
    form.missedOn = t?.missedOn ?? todayString()
    form.routeNo = t?.routeNo ?? props.routeNos[0] ?? ''
    form.vehicleNo = t?.vehicleNo ?? ''
    form.tripLabel = t?.tripLabel ?? ''
    form.driverName = t?.driverName ?? ''
    form.missedCount = t?.missedCount ?? 1
    form.reason = t?.reason ?? 'TRAFFIC'
    form.note = t?.note ?? ''
    errorMessage.value = ''
  },
)

async function onSubmit() {
  saving.value = true
  errorMessage.value = ''
  const request = {
    missedOn: form.missedOn,
    routeNo: form.routeNo,
    vehicleNo: form.vehicleNo.trim(),
    tripLabel: form.tripLabel.trim(),
    driverName: form.driverName.trim() || null,
    missedCount: form.missedCount,
    reason: form.reason,
    note: form.note.trim() || null,
  }
  try {
    if (props.target) {
      await updateMissedTrip(props.target.id, request)
    } else {
      await createMissedTrip(request)
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
  <BaseModal :open="open" :title="target ? '결행 수정' : '결행 입력'" @close="emit('close')">
    <form id="missed-trip-form" class="flex flex-col gap-3.5" @submit.prevent="onSubmit">
      <label class="flex flex-col gap-1.5">
        <span>일자 <em class="text-danger not-italic">*</em></span>
        <input v-model="form.missedOn" type="date" class="input" required />
      </label>

      <div class="grid grid-cols-2 gap-3">
        <label class="flex flex-col gap-1.5">
          <span>노선 <em class="text-danger not-italic">*</em></span>
          <select v-model="form.routeNo" class="input" required>
            <option v-for="no in routeNos" :key="no" :value="no">{{ no }}</option>
          </select>
        </label>
        <label class="flex flex-col gap-1.5">
          <span>차량번호 <em class="text-danger not-italic">*</em></span>
          <input
            v-model="form.vehicleNo"
            type="text"
            class="input"
            inputmode="numeric"
            maxlength="4"
            placeholder="4자리"
            pattern="[0-9]{4}"
            title="숫자 4자리"
            required
          />
        </label>
      </div>

      <div class="grid grid-cols-2 gap-3">
        <label class="flex flex-col gap-1.5">
          <span>순번 <em class="text-danger not-italic">*</em></span>
          <input
            v-model="form.tripLabel"
            type="text"
            class="input"
            maxlength="30"
            placeholder="예: 12, 20, 21, 종일"
            required
          />
        </label>
        <label class="flex flex-col gap-1.5">
          <span>승무원</span>
          <input v-model="form.driverName" type="text" class="input" maxlength="50" />
        </label>
      </div>

      <div class="grid grid-cols-2 gap-3">
        <label class="flex flex-col gap-1.5">
          <span>결행 횟수 <em class="text-danger not-italic">*</em></span>
          <input
            v-model.number="form.missedCount"
            type="number"
            class="input"
            min="0"
            max="100"
            required
          />
        </label>
        <label class="flex flex-col gap-1.5">
          <span>사유 <em class="text-danger not-italic">*</em></span>
          <select v-model="form.reason" class="input" required>
            <option v-for="option in reasonOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </label>
      </div>

      <label class="flex flex-col gap-1.5">
        <span>비고</span>
        <input v-model="form.note" type="text" class="input" maxlength="200" />
      </label>

      <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    </form>

    <template #footer>
      <button type="button" class="btn" @click="emit('close')">취소</button>
      <button type="submit" form="missed-trip-form" class="btn btn-primary" :disabled="saving">
        {{ saving ? '저장 중...' : '저장' }}
      </button>
    </template>
  </BaseModal>
</template>
