<!-- 노선 근무표 배치 편집: 줄 순서 옮기기, 구분·차량·A/B 지정, 기사 바꾸기, 줄 추가·삭제 -->
<script setup lang="ts">
import { computed, ref } from 'vue'
import type { DutyDriver } from '@/domains/duty/duty.types'
import type { RouteRosterAssignment, RouteRosterRow, RouteRosterSaveItem } from './routeroster.types'
import DriverPickerModal, { type PickerTarget } from './DriverPickerModal.vue'

const props = defineProps<{
  rows: RouteRosterRow[]
  drivers: DutyDriver[]
  otherAssignments: RouteRosterAssignment[]
  saving: boolean
}>()

const emit = defineEmits<{
  save: [rows: RouteRosterSaveItem[]]
  cancel: []
}>()

/** 편집 중인 줄. key 는 화면에서 줄을 구분하는 용도(순서를 바꿔도 입력 상태가 섞이지 않게) */
interface EditLine {
  key: number
  slotNo: number | ''
  vehicleNo: string
  abLabel: 'A' | 'B' | ''
  employeeName: string
}

let nextKey = 0
const lines = ref<EditLine[]>(
  props.rows.map((row) => ({
    key: nextKey++,
    slotNo: row.slotNo ?? '',
    vehicleNo: row.vehicleNo ?? '',
    abLabel: row.abLabel ?? '',
    employeeName: row.employeeName ?? '',
  })),
)

/** 다른 노선에 이미 배치된 기사 → 노선 번호 */
const elsewhere = computed(
  () => new Map(props.otherAssignments.map((assignment) => [assignment.employeeName, assignment.routeNo])),
)

/** 지금 전속기사 목록에 없는 기사 (퇴사·서열 없음) → 빨간 글씨로 표시 */
const activeNames = computed(() => new Set(props.drivers.map((driver) => driver.name)))

// ===== 기사 선택 창 =====

/** 기사 선택 창을 연 줄의 key (null 이면 닫힘) */
const pickerKey = ref<number | null>(null)

function placeLabel(line: EditLine, index: number): string {
  return [line.vehicleNo.trim(), line.abLabel].filter(Boolean).join(' ') || `${index + 1}번 줄`
}

const pickerTarget = computed<PickerTarget | null>(() => {
  const index = lines.value.findIndex((line) => line.key === pickerKey.value)
  const line = lines.value[index]
  return line ? { label: placeLabel(line, index), currentName: line.employeeName || null } : null
})

/** 이 목록의 다른 줄에 있는 기사 → 그 자리 설명 (고르면 맞바꾼다) */
const sameRouteMap = computed(() => {
  const map = new Map<string, string>()
  lines.value.forEach((line, index) => {
    if (line.employeeName && line.key !== pickerKey.value) {
      map.set(line.employeeName, placeLabel(line, index))
    }
  })
  return map
})

function onPick(name: string | null) {
  const line = lines.value.find((item) => item.key === pickerKey.value)
  if (line) {
    const other = name ? lines.value.find((item) => item !== line && item.employeeName === name) : undefined
    if (other) {
      other.employeeName = line.employeeName
    }
    line.employeeName = name ?? ''
  }
  pickerKey.value = null
}

function move(index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= lines.value.length) {
    return
  }
  const copy = [...lines.value]
  const [moved] = copy.splice(index, 1)
  if (moved) {
    copy.splice(target, 0, moved)
  }
  lines.value = copy
}

/** 줄 추가: 마지막 줄 다음에. 마지막 줄이 A 면 같은 차량의 B 로 채워 준다 */
function addLine() {
  const last = lines.value[lines.value.length - 1]
  const pairB = last?.abLabel === 'A'
  lines.value.push({
    key: nextKey++,
    slotNo: pairB ? last.slotNo : '',
    vehicleNo: pairB ? last.vehicleNo : '',
    abLabel: pairB ? 'B' : '',
    employeeName: '',
  })
}

function removeLine(index: number) {
  lines.value.splice(index, 1)
}

// 확인창(window.confirm)은 브라우저에 따라 막혀서 조용히 취소될 수 있다.
// 줄이 없으면 저장 버튼 자체를 "비우고 저장"으로 바꾸고 경고 문구를 보여주는 것으로 확인을 대신한다.
function onSave() {
  emit(
    'save',
    lines.value.map((line) => ({
      slotNo: typeof line.slotNo === 'number' ? line.slotNo : null,
      vehicleNo: line.vehicleNo.trim() || null,
      abLabel: line.abLabel || null,
      employeeName: line.employeeName || null,
    })),
  )
}
</script>

<template>
  <div class="rounded-md border border-line bg-surface">
    <div class="overflow-x-auto">
      <table class="w-full border-collapse text-sm">
        <thead class="bg-canvas text-left">
        <tr>
          <th class="border-b border-line px-2 py-2 whitespace-nowrap">순서</th>
          <th class="border-b border-line px-2 py-2 whitespace-nowrap">구분</th>
          <th class="border-b border-line px-2 py-2 whitespace-nowrap">차량</th>
          <th class="border-b border-line px-2 py-2 whitespace-nowrap">A/B</th>
          <th class="border-b border-line px-2 py-2 whitespace-nowrap">기사</th>
          <th class="border-b border-line px-2 py-2"></th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(line, index) in lines" :key="line.key">
          <td class="border-b border-line px-2 py-1 whitespace-nowrap">
            <span class="mr-1 inline-block w-6 text-right text-muted">{{ index + 1 }}</span>
            <button type="button" class="btn px-2 py-1" :disabled="index === 0" aria-label="위로" @click="move(index, -1)">
              ↑
            </button>
            <button
              type="button"
              class="btn ml-1 px-2 py-1"
              :disabled="index === lines.length - 1"
              aria-label="아래로"
              @click="move(index, 1)"
            >
              ↓
            </button>
          </td>
          <td class="border-b border-line px-2 py-1">
            <input v-model.number="line.slotNo" type="number" min="1" class="input w-20" />
          </td>
          <td class="border-b border-line px-2 py-1">
            <input v-model="line.vehicleNo" type="text" maxlength="20" class="input w-24" />
          </td>
          <td class="border-b border-line px-2 py-1">
            <select v-model="line.abLabel" class="input w-20">
              <option value="">-</option>
              <option value="A">A</option>
              <option value="B">B</option>
            </select>
          </td>
          <td class="border-b border-line px-2 py-1">
            <button
              type="button"
              class="input min-w-36 cursor-pointer text-left"
              :class="
                !line.employeeName
                  ? 'text-muted'
                  : activeNames.has(line.employeeName)
                    ? ''
                    : 'text-danger'
              "
              :title="
                line.employeeName && !activeNames.has(line.employeeName)
                  ? '퇴사했거나 서열이 없는 직원입니다'
                  : '눌러서 기사 고르기'
              "
              @click="pickerKey = line.key"
            >
              {{ line.employeeName || '(빈자리)' }}
            </button>
          </td>
          <td class="border-b border-line px-2 py-1 text-right">
            <button type="button" class="btn btn-danger px-2 py-1" @click="removeLine(index)">삭제</button>
          </td>
        </tr>
        <tr v-if="lines.length === 0">
          <td colspan="6" class="px-3 py-4 text-center text-muted">줄이 없습니다. "줄 추가"로 만들어 주세요.</td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="flex flex-wrap items-center gap-2 border-t border-line px-3 py-3">
      <button type="button" class="btn" @click="addLine">줄 추가</button>
      <span v-if="lines.length === 0" class="text-xs text-danger">
        줄이 없습니다. 저장하면 이 달 배치가 비워집니다.
      </span>
      <span v-else class="text-xs text-muted">기사 칸을 누르면 검색해서 고를 수 있습니다.</span>
      <div class="ml-auto flex gap-2">
        <button type="button" class="btn" :disabled="saving" @click="emit('cancel')">취소</button>
        <button
          type="button"
          class="btn"
          :class="lines.length === 0 ? 'btn-danger' : 'btn-primary'"
          :disabled="saving"
          @click="onSave"
        >
          {{ saving ? '저장 중...' : lines.length === 0 ? '비우고 저장' : '저장' }}
        </button>
      </div>
    </div>

    <DriverPickerModal
      :target="pickerTarget"
      :drivers="drivers"
      :elsewhere="elsewhere"
      :same-route="sameRouteMap"
      @pick="onPick"
      @close="pickerKey = null"
    />
  </div>
</template>
