<!-- 월 근무표 -->
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getErrorMessage } from '@/shared/api/error'
import { todayString } from '@/shared/utils/date'
import { fetchDutyRoster } from './duty.api'
import DutyHistoryModal from './DutyHistoryModal.vue'
import {
  shiftCellClass,
  shiftLabel,
  shiftShortLabel,
  type DutyCell,
  type DutyRoster,
  type Shift,
} from './duty.types'

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']
const SHIFTS: Shift[] = ['MORNING', 'AFTERNOON', 'OFF']

const yearMonth = ref(todayString().slice(0, 7)) // 'YYYY-MM'
const roster = ref<DutyRoster | null>(null)
const loading = ref(false)
const errorMessage = ref('')

/** 표 머리의 날짜와 요일 */
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

/** 날짜별 오전·오후·휴무 인원 (휴무 신청 인원 상한의 근거가 된다) */
const totals = computed(() =>
  SHIFTS.map((shift) => ({
    shift,
    counts: days.value.map(
      (_, index) => roster.value?.rows.filter((row) => row.cells[index]?.shift === shift).length ?? 0,
    ),
  })),
)

/** 이력 모달을 열 기사 이름 (null 이면 닫힘) */
const historyName = ref<string | null>(null)

async function load() {
  const [year, month] = yearMonth.value.split('-').map(Number)
  if (!year || !month) {
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    roster.value = await fetchDutyRoster(year, month)
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '근무표를 불러오지 못했습니다.')
  } finally {
    loading.value = false
  }
}

watch(yearMonth, load, { immediate: true })

/** 이전 달(-1) / 다음 달(+1) */
function moveMonth(delta: number) {
  const [year = 0, month = 1] = yearMonth.value.split('-').map(Number)
  const date = new Date(year, month - 1 + delta, 1)
  yearMonth.value = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
}

function weekdayClass(weekday: number): string {
  if (weekday === 0) {
    return 'text-danger'
  }
  return weekday === 6 ? 'text-primary' : ''
}

function cellTitle(name: string, day: number, cell: DutyCell): string {
  const label = cell.shift ? shiftLabel[cell.shift] : '미설정'
  return `${name} ${day}일 ${label}${cell.overridden ? ' (수동 수정)' : ''}`
}
</script>

<template>
  <section>
    <div class="mb-4 flex items-center justify-between gap-2">
      <h2 class="text-xl font-bold">근무표</h2>
      <RouterLink :to="{ name: 'duty-starts' }" class="btn">시작점 입력</RouterLink>
    </div>

    <div class="mb-3 flex flex-wrap items-center gap-2">
      <button type="button" class="btn" :disabled="loading" @click="moveMonth(-1)">◀</button>
      <input v-model="yearMonth" type="month" class="input w-auto" />
      <button type="button" class="btn" :disabled="loading" @click="moveMonth(1)">▶</button>

      <div class="ml-auto flex items-center gap-2 text-xs">
        <span v-for="shift in SHIFTS" :key="shift" class="rounded px-2 py-1" :class="shiftCellClass[shift]">
          {{ shiftShortLabel[shift] }} = {{ shiftLabel[shift] }}
        </span>
        <span class="rounded px-2 py-1 text-muted">· = 미설정</span>
      </div>
    </div>

    <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    <p v-else-if="!roster">불러오는 중...</p>
    <p v-else-if="roster.rows.length === 0" class="text-muted">전속기사가 없습니다.</p>

    <!-- 날짜가 31칸이라 좁은 화면에서는 가로로 스크롤한다. 이름 칸은 왼쪽에 고정 -->
    <div v-else class="overflow-x-auto rounded-md border border-line bg-surface" :class="{ 'opacity-60': loading }">
      <table class="border-collapse text-xs">
        <thead>
        <tr>
          <th class="sticky left-0 z-10 border-b border-r border-line bg-canvas px-3 py-2 text-left whitespace-nowrap">
            서열 · 이름
          </th>
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
        <tr v-for="row in roster.rows" :key="row.name">
          <td class="sticky left-0 z-10 border-b border-r border-line bg-surface px-3 py-1 whitespace-nowrap">
            <span class="mr-1 text-muted">{{ row.seniorityNo }}</span>
            <button type="button" class="cursor-pointer hover:text-primary hover:underline" @click="historyName = row.name">
              {{ row.name }}
            </button>
          </td>
          <td v-for="(cell, index) in row.cells" :key="index" class="border-b border-line p-0.5">
            <div
              class="flex h-7 items-center justify-center rounded"
              :class="[
                  cell.shift ? shiftCellClass[cell.shift] : 'text-muted',
                  { 'outline-2 -outline-offset-2 outline-ink': cell.overridden },
                ]"
              :title="cellTitle(row.name, index + 1, cell)"
            >
              {{ cell.shift ? shiftShortLabel[cell.shift] : '·' }}
            </div>
          </td>
        </tr>
        </tbody>

        <!-- 날짜별 인원 합계 -->
        <tfoot>
        <tr v-for="{ shift, counts } in totals" :key="shift">
          <th class="sticky left-0 z-10 border-r border-t border-line bg-canvas px-3 py-1 text-left whitespace-nowrap">
            {{ shiftLabel[shift] }} 인원
          </th>
          <td
            v-for="(count, index) in counts"
            :key="index"
            class="border-t border-line bg-canvas py-1 text-center"
          >
            {{ count }}
          </td>
        </tr>
        </tfoot>
      </table>
    </div>
    <DutyHistoryModal :employee-name="historyName" @close="historyName = null" />
  </section>
</template>
