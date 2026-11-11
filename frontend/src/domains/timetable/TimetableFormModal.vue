<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { getErrorMessage } from '@/shared/api/error'
import { formatMinutes, parseTime } from '@/shared/utils/format'
import { createTimetableRow, deleteTimetableRow, updateTimetableRow } from './timetable.api'
import {
  dayTypeLabel,
  pointDefaultLabel,
  timeColumns,
  tripShiftLabel,
  type DayType,
  type PointKey,
  type TimeField,
  type TimetableRow,
  type TripShift,
} from './timetable.types'

const props = defineProps<{
  open: boolean
  /** 수정할 회차. null 이면 새 회차 추가 */
  target: TimetableRow | null
  /** 새 회차를 추가할 때의 노선·형태 (지금 화면에서 보고 있는 것) */
  routeNo: string
  dayType: DayType
  /** 노선에 등록된 장소 이름 (입력칸 이름표로 쓴다) */
  pointNames: Partial<Record<PointKey, string>>
}>()

const emit = defineEmits<{
  close: []
  saved: []
}>()

const isEdit = computed(() => props.target !== null)

const slotNo = ref<number | ''>('')
const tripNo = ref<number | ''>('')
const shift = ref<TripShift>('MORNING')
/** 시각 입력칸의 글자 (열 속성 이름 → "HH:MM", 비우면 "") */
const times = reactive<Record<TimeField, string>>({
  departMin: '',
  mid1Min: '',
  mid2Min: '',
  turnMin: '',
  mid3Min: '',
  mid4Min: '',
  arriveMin: '',
})
const saving = ref(false)
const errorMessage = ref('')

function label(pointKey: PointKey): string {
  return props.pointNames[pointKey] || pointDefaultLabel[pointKey]
}

watch(
  () => props.open,
  (open) => {
    if (!open) {
      return
    }
    slotNo.value = props.target?.slotNo ?? ''
    tripNo.value = props.target?.tripNo ?? ''
    shift.value = props.target?.shift ?? 'MORNING'
    for (const column of timeColumns) {
      const minutes = props.target ? props.target[column.field] : null
      times[column.field] = minutes === null ? '' : formatMinutes(minutes)
    }
    errorMessage.value = ''
  },
)

async function onSubmit() {
  errorMessage.value = ''

  // 입력한 글자를 분으로 바꾼다. 비워 둔 칸은 null (필수 칸은 아래에서 걸러낸다)
  const values = {} as Record<TimeField, number | null>
  for (const column of timeColumns) {
    const text = times[column.field].trim()
    if (text === '') {
      if (column.required) {
        errorMessage.value = `${label(column.pointKey)} 시각을 입력해 주세요.`
        return
      }
      values[column.field] = null
      continue
    }
    const minutes = parseTime(text)
    if (minutes === null) {
      errorMessage.value = `${label(column.pointKey)} 시각은 05:30 같은 형식으로 입력해 주세요.`
      return
    }
    values[column.field] = minutes
  }

  // 필수 칸은 위에서 비어 있지 않음을 확인했으므로 숫자다
  const request = {
    shift: shift.value,
    departMin: values.departMin as number,
    mid1Min: values.mid1Min,
    mid2Min: values.mid2Min,
    turnMin: values.turnMin as number,
    mid3Min: values.mid3Min,
    mid4Min: values.mid4Min,
    arriveMin: values.arriveMin as number,
  }

  saving.value = true
  try {
    if (props.target) {
      await updateTimetableRow(props.target, request)
    } else {
      await createTimetableRow({
        routeNo: props.routeNo,
        dayType: props.dayType,
        slotNo: Number(slotNo.value),
        tripNo: Number(tripNo.value),
        ...request,
      })
    }
    emit('saved')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '저장하지 못했습니다.')
  } finally {
    saving.value = false
  }
}

async function onDelete() {
  const target = props.target
  if (!target || !confirm(`${target.slotNo}번 순번 ${target.tripNo}회차를 삭제할까요? 되돌릴 수 없습니다.`)) {
    return
  }
  saving.value = true
  errorMessage.value = ''
  try {
    await deleteTimetableRow(target)
    emit('saved')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '삭제하지 못했습니다.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <BaseModal :open="open" :title="isEdit ? '회차 수정' : '회차 추가'" @close="emit('close')">
    <form id="timetable-form" class="flex flex-col gap-3.5" @submit.prevent="onSubmit">
      <p class="text-muted">{{ routeNo }}번 · {{ dayTypeLabel[dayType] }}</p>

      <div class="grid grid-cols-2 gap-3">
        <label class="flex flex-col gap-1.5">
          <span>순번 <em class="text-danger not-italic">*</em></span>
          <input
            v-model.number="slotNo"
            type="number"
            min="1"
            class="input"
            :disabled="isEdit"
            required
          />
        </label>
        <label class="flex flex-col gap-1.5">
          <span>회차 번호 <em class="text-danger not-italic">*</em></span>
          <input
            v-model.number="tripNo"
            type="number"
            min="1"
            class="input"
            :disabled="isEdit"
            required
          />
        </label>
      </div>

      <label class="flex flex-col gap-1.5">
        <span>오전/오후</span>
        <select v-model="shift" class="input">
          <option v-for="(text, key) in tripShiftLabel" :key="key" :value="key">{{ text }}</option>
        </select>
      </label>

      <div class="grid grid-cols-2 gap-3">
        <label v-for="column in timeColumns" :key="column.field" class="flex flex-col gap-1.5">
          <span>
            {{ label(column.pointKey) }}
            <em v-if="column.required" class="text-danger not-italic">*</em>
          </span>
          <input
            v-model="times[column.field]"
            type="text"
            inputmode="numeric"
            class="input"
            maxlength="5"
            placeholder="예: 05:30"
          />
        </label>
      </div>
      <p class="text-muted">자정이 지난 시각은 24:10 처럼 입력하세요. 없는 중간지는 비워 두세요.</p>

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
      <button type="submit" form="timetable-form" class="btn btn-primary" :disabled="saving">
        {{ saving ? '저장 중...' : '저장' }}
      </button>
    </template>
  </BaseModal>
</template>
