<!-- 노선 근무표: 배차계획 엑셀의 노선 시트처럼 노선별로 기사를 배치하고, 근무는 근무표 데이터를 그대로 보여준다 -->
<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { getErrorMessage } from '@/shared/api/error'
import { todayString } from '@/shared/utils/date'
import { getRoutes } from '@/domains/route/route.api'
import type { RouteSummary } from '@/domains/route/route.types'
import { fetchDutyDrivers } from '@/domains/duty/duty.api'
import {
  shiftCellClass,
  shiftLabel,
  shiftShortLabel,
  type DutyCell,
  type DutyDriver,
  type Shift,
} from '@/domains/duty/duty.types'
import {
  copyPreviousRouteRoster,
  createRouteRosterFromVehicles,
  fetchRouteRoster,
  saveRouteRoster,
} from './routeroster.api'
import type { RouteRoster, RouteRosterInitResult, RouteRosterRow, RouteRosterSaveItem } from './routeroster.types'
import RouteRosterEditor from './RouteRosterEditor.vue'
import RouteRosterStartModal, { type StartTarget } from './RouteRosterStartModal.vue'
import DriverPickerModal, { type PickerTarget } from './DriverPickerModal.vue'

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']
const SHIFTS: Shift[] = ['MORNING', 'AFTERNOON', 'OFF']

const routes = ref<RouteSummary[]>([])
const routeNo = ref('')
const yearMonth = ref(todayString().slice(0, 7)) // 'YYYY-MM'
const roster = ref<RouteRoster | null>(null)
const loading = ref(false)
const busy = ref(false)
const errorMessage = ref('')
const noticeMessage = ref('')

/** 배치 편집 중이면 전속기사 목록을 들고 편집 표를 보여준다 */
const editing = ref(false)
const drivers = ref<DutyDriver[]>([])

const startTarget = ref<StartTarget | null>(null)

const selectedYearMonth = computed(() => {
  const [year = 0, month = 0] = yearMonth.value.split('-').map(Number)
  return { year, month }
})

const days = computed(() => {
  if (!roster.value) {
    return []
  }
  const { year, month, lastDay } = roster.value
  return Array.from({ length: lastDay }, (_, index) => ({
    day: index + 1,
    weekday: new Date(year, month - 1, index + 1).getDay(),
  }))
})

/**
 * 엑셀처럼 같은 차량(구분·차량번호가 같은 연속된 줄)은 구분·차량 칸을 합친다.
 * 값: 합칠 줄 수 (0 이면 위 칸에 합쳐져서 그리지 않음)
 */
const vehicleSpans = computed(() => {
  const rows = roster.value?.rows ?? []
  const spans = rows.map(() => 1)
  for (let index = 1; index < rows.length; index++) {
    const current = rows[index]
    const previous = rows[index - 1]
    const sameVehicle =
      current &&
      previous &&
      (current.slotNo !== null || current.vehicleNo !== null) &&
      current.slotNo === previous.slotNo &&
      current.vehicleNo === previous.vehicleNo
    if (sameVehicle) {
      let head = index - 1
      while (spans[head] === 0) {
        head--
      }
      spans[head] = (spans[head] ?? 1) + 1
      spans[index] = 0
    }
  }
  return spans
})

async function loadRoutes() {
  try {
    routes.value = await getRoutes()
    if (!routeNo.value && routes.value[0]) {
      routeNo.value = routes.value[0].routeNo
    }
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '노선 목록을 불러오지 못했습니다.')
  }
}

async function load() {
  const { year, month } = selectedYearMonth.value
  if (!routeNo.value || !year || !month) {
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    roster.value = await fetchRouteRoster(year, month, routeNo.value)
  } catch (error) {
    roster.value = null
    errorMessage.value = getErrorMessage(error, '노선 근무표를 불러오지 못했습니다.')
  } finally {
    loading.value = false
  }
}

// 노선이나 달을 바꾸면 편집을 끝내고 다시 불러온다
watch([routeNo, yearMonth], () => {
  editing.value = false
  noticeMessage.value = ''
  load()
})

onMounted(loadRoutes)

function moveMonth(delta: number) {
  const { year, month } = selectedYearMonth.value
  const date = new Date(year, month - 1 + delta, 1)
  yearMonth.value = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
}

function weekdayClass(weekday: number): string {
  if (weekday === 0) {
    return 'text-danger'
  }
  return weekday === 6 ? 'text-primary' : ''
}

/** 그달 근무일 수 (오전 + 오후) */
function workDays(row: RouteRosterRow): number {
  return row.cells.filter((cell) => cell.shift === 'MORNING' || cell.shift === 'AFTERNOON').length
}

function cellTitle(name: string, day: number, cell: DutyCell): string {
  if (!cell.shift) {
    return `${name} ${day}일 미설정 · 눌러서 시작점 변경`
  }
  const parts = [`${name} ${day}일 ${shiftLabel[cell.shift]}`]
  if (cell.cycleDay) {
    parts.push(`(${cell.cycleDay}일차)`)
  }
  if (cell.overridden) {
    parts.push('· 수동 수정')
  }
  if (cell.startChanged) {
    parts.push('· 이날부터 새 시작점')
  }
  parts.push('· 눌러서 시작점 변경')
  return parts.join(' ')
}

function dateOf(day: number): string {
  return `${yearMonth.value}-${String(day).padStart(2, '0')}`
}

/** 날짜 칸을 누르면 그날을 기준일로, 그날의 현재 일차를 기본값으로 연다 */
function openStart(row: RouteRosterRow, day: number) {
  if (!row.employeeName) {
    return
  }
  startTarget.value = {
    employeeName: row.employeeName,
    baseDate: dateOf(day),
    cycleDay: row.cells[day - 1]?.cycleDay ?? null,
  }
}

/** 이름을 누르면 오늘(이번 달이면) 또는 그달 1일을 기준일로 연다 */
function openStartFromName(row: RouteRosterRow) {
  const today = todayString()
  const day = today.startsWith(yearMonth.value) ? Number(today.slice(8, 10)) : 1
  openStart(row, day)
}

async function onStartSaved() {
  startTarget.value = null
  noticeMessage.value = '시작점을 저장했습니다. 근무표 메뉴에도 반영됩니다.'
  await load()
}

/** 전속기사 목록 (기사 고르기·편집에서 쓴다). 실패하면 false */
async function loadDrivers(): Promise<boolean> {
  try {
    drivers.value = await fetchDutyDrivers()
    return true
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '전속기사 목록을 불러오지 못했습니다.')
    return false
  }
}

async function startEditing() {
  errorMessage.value = ''
  noticeMessage.value = ''
  if (await loadDrivers()) {
    editing.value = true
  }
}

// ===== 이름 칸을 눌러 기사 바꾸기 =====

/** 기사 선택 창을 연 줄 (null 이면 닫힘) */
const pickerIndex = ref<number | null>(null)

function placeLabel(row: RouteRosterRow, index: number): string {
  return [row.vehicleNo, row.abLabel].filter(Boolean).join(' ') || `${index + 1}번 줄`
}

const pickerTarget = computed<PickerTarget | null>(() => {
  const row = pickerIndex.value === null ? undefined : roster.value?.rows[pickerIndex.value]
  if (!row || pickerIndex.value === null) {
    return null
  }
  return { label: placeLabel(row, pickerIndex.value), currentName: row.employeeName }
})

const elsewhereMap = computed(
  () => new Map((roster.value?.otherAssignments ?? []).map((item) => [item.employeeName, item.routeNo])),
)

/** 이 노선의 다른 줄에 있는 기사 → 그 자리 설명 */
const sameRouteMap = computed(() => {
  const map = new Map<string, string>()
  roster.value?.rows.forEach((row, index) => {
    if (row.employeeName && index !== pickerIndex.value) {
      map.set(row.employeeName, placeLabel(row, index))
    }
  })
  return map
})

async function openPicker(index: number) {
  errorMessage.value = ''
  if (drivers.value.length === 0 && !(await loadDrivers())) {
    return
  }
  pickerIndex.value = index
}

/** 기사를 고르면 바로 저장한다. 이 노선의 다른 자리 기사를 고르면 두 자리를 맞바꾼다 */
async function onPick(name: string | null) {
  const index = pickerIndex.value
  if (!roster.value || index === null) {
    return
  }
  const rows = roster.value.rows
  const current = rows[index]
  const items: RouteRosterSaveItem[] = rows.map((row) => ({
    slotNo: row.slotNo,
    vehicleNo: row.vehicleNo,
    abLabel: row.abLabel,
    employeeName: row.employeeName,
  }))
  const otherIndex = name ? rows.findIndex((row, i) => i !== index && row.employeeName === name) : -1
  const target = items[index]
  if (!current || !target) {
    return
  }
  if (otherIndex >= 0 && items[otherIndex]) {
    items[otherIndex].employeeName = current.employeeName
  }
  target.employeeName = name

  const { year, month } = selectedYearMonth.value
  busy.value = true
  errorMessage.value = ''
  try {
    await saveRouteRoster(year, month, routeNo.value, items)
    const place = placeLabel(current, index)
    noticeMessage.value =
      otherIndex >= 0
        ? `${name}, ${current.employeeName ?? '빈자리'}의 자리를 맞바꿨습니다.`
        : `${place} 자리를 ${name ?? '빈자리'}(으)로 바꿨습니다.`
    pickerIndex.value = null
    await load()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '기사를 바꾸지 못했습니다.')
    pickerIndex.value = null
  } finally {
    busy.value = false
  }
}

/** 기사 선택 창의 "시작점 변경" */
function onPickerStart() {
  const row = pickerIndex.value === null ? undefined : roster.value?.rows[pickerIndex.value]
  pickerIndex.value = null
  if (row) {
    openStartFromName(row)
  }
}

async function onSave(rows: RouteRosterSaveItem[]) {
  const { year, month } = selectedYearMonth.value
  busy.value = true
  errorMessage.value = ''
  try {
    await saveRouteRoster(year, month, routeNo.value, rows)
    editing.value = false
    noticeMessage.value = rows.length === 0 ? '이 달 배치를 비웠습니다.' : '배치를 저장했습니다.'
    await load()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '배치를 저장하지 못했습니다.')
  } finally {
    busy.value = false
  }
}

async function initialize(kind: 'previous' | 'vehicles') {
  const { year, month } = selectedYearMonth.value
  busy.value = true
  errorMessage.value = ''
  noticeMessage.value = ''
  try {
    const result: RouteRosterInitResult =
      kind === 'previous'
        ? await copyPreviousRouteRoster(year, month, routeNo.value)
        : await createRouteRosterFromVehicles(year, month, routeNo.value)
    noticeMessage.value =
      `${result.count}줄을 만들었습니다.` +
      (result.clearedNames.length
        ? ` 빈자리로 바꾼 기사(퇴사·서열 없음 또는 다른 노선 배치): ${result.clearedNames.join(', ')}`
        : '')
    await load()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '배치를 만들지 못했습니다.')
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <section>
    <div class="mb-4 flex items-center justify-between gap-2">
      <h2 class="text-xl font-bold">노선 근무표</h2>
      <button
        v-if="roster && !editing"
        type="button"
        class="btn"
        :disabled="loading || busy"
        @click="startEditing"
      >
        배치 편집
      </button>
    </div>

    <!-- 노선 선택: 엑셀의 시트 탭처럼 -->
    <div class="mb-3 flex flex-wrap gap-1">
      <button
        v-for="route in routes"
        :key="route.routeNo"
        type="button"
        class="btn"
        :class="{ 'btn-primary': route.routeNo === routeNo }"
        @click="routeNo = route.routeNo"
      >
        {{ route.routeNo }}
      </button>
    </div>

    <div class="mb-3 flex flex-wrap items-center gap-2">
      <button type="button" class="btn" :disabled="loading" @click="moveMonth(-1)">◀</button>
      <input v-model="yearMonth" type="month" class="input w-auto" />
      <button type="button" class="btn" :disabled="loading" @click="moveMonth(1)">▶</button>

      <div v-if="!editing" class="ml-auto flex flex-wrap items-center gap-2 text-xs">
        <span v-for="shift in SHIFTS" :key="shift" class="rounded px-2 py-1" :class="shiftCellClass[shift]">
          {{ shiftShortLabel[shift] }} = {{ shiftLabel[shift] }}
        </span>
        <span class="rounded px-2 py-1 text-muted">· = 미설정</span>
        <span class="rounded border-l-3 border-l-primary px-2 py-1 text-muted">시작점 변경</span>
      </div>
    </div>

    <p v-if="errorMessage" class="mb-2 text-danger">{{ errorMessage }}</p>
    <p v-if="noticeMessage" class="mb-2 text-primary">{{ noticeMessage }}</p>

    <p v-if="routes.length === 0 && !errorMessage" class="text-muted">등록된 노선이 없습니다.</p>
    <p v-else-if="!roster && !errorMessage">불러오는 중...</p>

    <template v-else-if="roster">
      <!-- 배치 편집 -->
      <RouteRosterEditor
        v-if="editing"
        :key="`${roster.routeNo}-${roster.year}-${roster.month}`"
        :rows="roster.rows"
        :drivers="drivers"
        :other-assignments="roster.otherAssignments"
        :saving="busy"
        @save="onSave"
        @cancel="editing = false"
      />

      <!-- 이 달 배치가 아직 없을 때 -->
      <div v-else-if="roster.rows.length === 0" class="rounded-md border border-line bg-surface p-5">
        <p class="mb-3">{{ roster.year }}년 {{ roster.month }}월 {{ roster.routeNo }}번 배치가 아직 없습니다.</p>
        <div class="flex flex-wrap gap-2">
          <button type="button" class="btn btn-primary" :disabled="busy" @click="initialize('previous')">
            전달 배치 복사
          </button>
          <button type="button" class="btn" :disabled="busy" @click="initialize('vehicles')">
            차량 관리 순번으로 만들기
          </button>
          <button type="button" class="btn" :disabled="busy" @click="startEditing">직접 만들기</button>
        </div>
      </div>

      <!-- 근무표: 날짜가 31칸이라 가로로 스크롤한다. 앞쪽 칸은 왼쪽에 고정 -->
      <div
        v-else
        class="overflow-x-auto rounded-md border border-line bg-surface"
        :class="{ 'opacity-60': loading }"
      >
        <table class="border-collapse text-xs">
          <thead>
          <tr>
            <th class="border-b border-r border-line bg-canvas px-2 py-2 whitespace-nowrap">구분</th>
            <th class="border-b border-r border-line bg-canvas px-2 py-2 whitespace-nowrap">차량</th>
            <th class="sticky left-0 z-10 border-b border-r border-line bg-canvas px-3 py-2 text-left whitespace-nowrap">
              기사
            </th>
            <th class="border-b border-r border-line bg-canvas px-2 py-2 whitespace-nowrap">A/B</th>
            <th class="border-b border-r border-line bg-canvas px-2 py-2 whitespace-nowrap">근무</th>
            <th
              v-for="{ day, weekday } in days"
              :key="day"
              class="min-w-8 border-b border-line bg-canvas py-1 font-semibold"
              :class="weekdayClass(weekday)"
            >
              <div>{{ day }}</div>
              <div class="font-normal">{{ WEEKDAYS[weekday] }}</div>
            </th>
          </tr>
          </thead>

          <tbody>
          <tr v-for="(row, rowIndex) in roster.rows" :key="row.sortOrder">
            <td
              v-if="vehicleSpans[rowIndex] !== 0"
              :rowspan="vehicleSpans[rowIndex]"
              class="border-b border-r border-line px-2 py-1 text-center"
            >
              {{ row.slotNo ?? '' }}
            </td>
            <td
              v-if="vehicleSpans[rowIndex] !== 0"
              :rowspan="vehicleSpans[rowIndex]"
              class="border-b border-r border-line px-2 py-1 text-center whitespace-nowrap"
            >
              {{ row.vehicleNo ?? '' }}
            </td>
            <td class="sticky left-0 z-10 border-b border-r border-line bg-surface px-3 py-1 whitespace-nowrap">
              <button
                type="button"
                class="cursor-pointer hover:text-primary hover:underline"
                :class="row.employeeName ? { 'text-danger': !row.activeDriver } : 'text-muted'"
                :title="
                  row.employeeName && !row.activeDriver
                    ? '퇴사했거나 서열이 없는 직원입니다 · 눌러서 기사 바꾸기'
                    : '눌러서 기사 바꾸기'
                "
                :disabled="busy"
                @click="openPicker(rowIndex)"
              >
                {{ row.employeeName ?? '(빈자리)' }}
              </button>
            </td>
            <td class="border-b border-r border-line px-2 py-1 text-center">{{ row.abLabel ?? '' }}</td>
            <td class="border-b border-r border-line px-2 py-1 text-center">
              {{ row.employeeName ? workDays(row) : '' }}
            </td>
            <template v-if="row.employeeName">
              <td v-for="(cell, index) in row.cells" :key="index" class="border-b border-line p-0.5">
                <button
                  type="button"
                  class="flex h-7 w-full cursor-pointer items-center justify-center rounded"
                  :class="[
                      cell.shift ? shiftCellClass[cell.shift] : 'text-muted',
                      { 'outline-2 -outline-offset-2 outline-ink': cell.overridden },
                      { 'border-l-3 border-l-primary': cell.startChanged },
                    ]"
                  :title="cellTitle(row.employeeName, index + 1, cell)"
                  @click="openStart(row, index + 1)"
                >
                  {{ cell.shift ? shiftShortLabel[cell.shift] : '·' }}
                </button>
              </td>
            </template>
            <td v-else :colspan="days.length" class="border-b border-line"></td>
          </tr>
          </tbody>
        </table>
      </div>
    </template>

    <RouteRosterStartModal :target="startTarget" @close="startTarget = null" @saved="onStartSaved" />
    <DriverPickerModal
      :target="pickerTarget"
      :drivers="drivers"
      :elsewhere="elsewhereMap"
      :same-route="sameRouteMap"
      show-start-button
      @pick="onPick"
      @start="onPickerStart"
      @close="pickerIndex = null"
    />
  </section>
</template>
