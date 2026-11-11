<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/domains/auth/auth.store'
import { getRoutes } from '@/domains/route/route.api'
import type { RouteSummary } from '@/domains/route/route.types'
import { getErrorMessage } from '@/shared/api/error'
import { formatMinutes } from '@/shared/utils/format'
import RoutePointModal from './RoutePointModal.vue'
import TimetableCopyModal from './TimetableCopyModal.vue'
import TimetableFormModal from './TimetableFormModal.vue'
import { getRoutePoints, getTimetable } from './timetable.api'
import {
  dayTypeLabel,
  pointDefaultLabel,
  timeColumns,
  tripShiftLabel,
  type DayType,
  type PointKey,
  type TimetableRow,
  type TripShift,
} from './timetable.types'

const auth = useAuthStore()

const routes = ref<RouteSummary[]>([])
const routeNo = ref('')
const dayType = ref<DayType>('WEEKDAY')
const shift = ref<TripShift | ''>('')
const slotNo = ref<number | null>(null)

const rows = ref<TimetableRow[]>([])
const pointNames = ref<Partial<Record<PointKey, string>>>({})
const loading = ref(false)
const errorMessage = ref('')

const modalOpen = ref(false)
const editing = ref<TimetableRow | null>(null)
const copyOpen = ref(false)
const pointOpen = ref(false)

/** 요청이 겹칠 때 늦게 도착한 옛 응답이 화면을 덮어쓰지 않도록 번호를 매긴다 */
let requestSeq = 0

async function load() {
  if (!routeNo.value) {
    return
  }
  const seq = ++requestSeq
  loading.value = true
  errorMessage.value = ''

  try {
    const [timetable, points] = await Promise.all([
      getTimetable({
        routeNo: routeNo.value,
        dayType: dayType.value,
        shift: shift.value || undefined,
        slotNo: slotNo.value ?? undefined,
      }),
      getRoutePoints(routeNo.value),
    ])
    if (seq !== requestSeq) {
      return
    }
    rows.value = timetable
    pointNames.value = Object.fromEntries(points.map((point) => [point.pointKey, point.name]))
  } catch (error) {
    if (seq === requestSeq) {
      errorMessage.value = getErrorMessage(error, '시간표를 불러오지 못했습니다.')
    }
  } finally {
    if (seq === requestSeq) {
      loading.value = false
    }
  }
}

/** 값이 하나라도 있는 열만 보여 준다 (예: 3006 은 중간지가 없다) */
const visibleColumns = computed(() =>
  timeColumns.filter((column) => column.required || rows.value.some((row) => row[column.field] !== null)),
)

function columnTitle(pointKey: PointKey): string {
  return pointNames.value[pointKey] || pointDefaultLabel[pointKey]
}

/** 순번별로 묶는다. 한 순번의 회차들을 이어서 보여 주고 순번 칸은 합쳐서(rowspan) 표시한다 */
const groups = computed(() => {
  const bySlot = new Map<number, TimetableRow[]>()
  for (const row of rows.value) {
    const list = bySlot.get(row.slotNo) ?? []
    list.push(row)
    bySlot.set(row.slotNo, list)
  }
  return [...bySlot.entries()].map(([slot, list]) => ({ slot, rows: list }))
})

function openCreate() {
  editing.value = null
  modalOpen.value = true
}

function openEdit(row: TimetableRow) {
  if (!auth.isAdmin) {
    return
  }
  editing.value = row
  modalOpen.value = true
}

function onSaved() {
  modalOpen.value = false
  load()
}

/** 복사가 끝나면 복사된 형태로 화면을 옮겨서 결과를 바로 보여 준다 */
function onCopied(toDayType: DayType) {
  copyOpen.value = false
  dayType.value = toDayType
  load()
}

function onPointsSaved() {
  pointOpen.value = false
  load()
}

onMounted(async () => {
  try {
    routes.value = await getRoutes()
    routeNo.value = routes.value[0]?.routeNo ?? ''
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '노선 목록을 불러오지 못했습니다.')
    return
  }
  await load()
})
</script>

<template>
  <section>
    <div class="mb-4 flex flex-wrap items-center justify-between gap-2">
      <h2 class="text-xl font-bold">시간표</h2>
      <div v-if="auth.isAdmin" class="flex flex-wrap gap-2">
        <button type="button" class="btn" @click="pointOpen = true">장소 이름</button>
        <button type="button" class="btn" @click="copyOpen = true">형태 복사</button>
        <button type="button" class="btn btn-primary" @click="openCreate">+ 회차 추가</button>
      </div>
    </div>

    <form class="mb-4 flex flex-wrap items-center gap-2" @submit.prevent="load">
      <select v-model="routeNo" class="input w-auto" @change="load">
        <option v-for="route in routes" :key="route.routeNo" :value="route.routeNo">
          {{ route.routeNo }}번
        </option>
      </select>
      <select v-model="dayType" class="input w-auto" @change="load">
        <option v-for="(label, key) in dayTypeLabel" :key="key" :value="key">{{ label }}</option>
      </select>
      <select v-model="shift" class="input w-auto" @change="load">
        <option value="">오전·오후 전체</option>
        <option v-for="(label, key) in tripShiftLabel" :key="key" :value="key">{{ label }}</option>
      </select>
      <input
        v-model.number="slotNo"
        type="number"
        min="1"
        class="input w-28"
        placeholder="순번"
      />
      <button type="submit" class="btn btn-primary" :disabled="loading">조회</button>
    </form>

    <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    <p v-else-if="loading && rows.length === 0" class="text-muted">불러오는 중...</p>

    <template v-else>
      <p class="mb-2 text-muted">
        {{ groups.length }}개 순번, {{ rows.length }}회차
        <span v-if="auth.isAdmin">· 행을 누르면 수정할 수 있습니다</span>
      </p>

      <div class="overflow-x-auto rounded-md border border-line bg-surface">
        <table class="data-table">
          <thead>
            <tr>
              <th>순번</th>
              <th>회차</th>
              <th>구분</th>
              <th v-for="column in visibleColumns" :key="column.pointKey">
                {{ columnTitle(column.pointKey) }}
              </th>
            </tr>
          </thead>
          <tbody v-for="group in groups" :key="group.slot" class="border-b-2 border-line">
            <tr
              v-for="(row, index) in group.rows"
              :key="row.tripNo"
              :class="{ 'cursor-pointer': auth.isAdmin }"
              @click="openEdit(row)"
            >
              <td
                v-if="index === 0"
                :rowspan="group.rows.length"
                class="bg-canvas font-semibold"
              >
                {{ group.slot }}
              </td>
              <td>{{ row.tripNo }}</td>
              <td :class="row.shift === 'MORNING' ? 'text-primary' : 'text-muted'">
                {{ tripShiftLabel[row.shift] }}
              </td>
              <td v-for="column in visibleColumns" :key="column.pointKey">
                {{ formatMinutes(row[column.field]) }}
              </td>
            </tr>
          </tbody>
          <tbody v-if="groups.length === 0">
            <tr>
              <td :colspan="3 + visibleColumns.length" class="py-10 text-muted">
                조회 결과가 없습니다.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <template v-if="auth.isAdmin">
      <TimetableFormModal
        :open="modalOpen"
        :target="editing"
        :route-no="routeNo"
        :day-type="dayType"
        :point-names="pointNames"
        @close="modalOpen = false"
        @saved="onSaved"
      />
      <TimetableCopyModal
        :open="copyOpen"
        :route-no="routeNo"
        :from-day-type="dayType"
        @close="copyOpen = false"
        @copied="onCopied"
      />
      <RoutePointModal
        :open="pointOpen"
        :route-no="routeNo"
        :point-names="pointNames"
        @close="pointOpen = false"
        @saved="onPointsSaved"
      />
    </template>
  </section>
</template>
