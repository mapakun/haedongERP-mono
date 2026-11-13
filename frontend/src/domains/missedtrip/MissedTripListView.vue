<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import BasePagination from '@/shared/components/BasePagination.vue'
import { useAuthStore } from '@/domains/auth/auth.store'
import { getErrorMessage } from '@/shared/api/error'
import { getRoutes } from '@/domains/route/route.api'
import { todayString } from '@/shared/utils/date'
import MissedTripExportModal from './MissedTripExportModal.vue'
import MissedTripFormModal from './MissedTripFormModal.vue'
import { deleteMissedTrip, searchMissedTrips } from './missedtrip.api'
import {
  reasonLabel,
  reasonOptions,
  type MissedReason,
  type MissedTrip,
  type MissedTripPage,
} from './missedtrip.types'

const auth = useAuthStore()

/** 이번 달 1일 ~ 오늘 (기본 조회 기간) */
function monthStart(): string {
  return todayString().slice(0, 8) + '01'
}

const condition = reactive<{
  fromDate: string
  toDate: string
  routeNo: string
  reason: MissedReason | ''
  keyword: string
  size: number
}>({
  fromDate: monthStart(),
  toDate: todayString(),
  routeNo: '',
  reason: '',
  keyword: '',
  size: 20,
})

const routeNos = ref<string[]>([])
const result = ref<MissedTripPage | null>(null)
const loading = ref(false)
const errorMessage = ref('')

const formOpen = ref(false)
const editing = ref<MissedTrip | null>(null)
const exportOpen = ref(false)

// 빠르게 조회를 연달아 눌렀을 때 늦게 도착한 옛 응답이 화면을 덮지 않도록 순번을 센다
let requestSeq = 0

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']
function weekday(date: string): string {
  return WEEKDAYS[new Date(date + 'T00:00:00').getDay()] ?? ''
}

async function load(page: number) {
  const seq = ++requestSeq
  loading.value = true
  errorMessage.value = ''
  try {
    const data = await searchMissedTrips({
      fromDate: condition.fromDate || undefined,
      toDate: condition.toDate || undefined,
      routeNo: condition.routeNo || undefined,
      reason: condition.reason || undefined,
      keyword: condition.keyword.trim() || undefined,
      page,
      size: condition.size,
    })
    if (seq === requestSeq) {
      result.value = data
    }
  } catch (error) {
    if (seq === requestSeq) {
      errorMessage.value = getErrorMessage(error, '결행 목록을 불러오지 못했습니다.')
    }
  } finally {
    if (seq === requestSeq) {
      loading.value = false
    }
  }
}

function onSearch() {
  load(1)
}

function openCreate() {
  editing.value = null
  formOpen.value = true
}

function openEdit(item: MissedTrip) {
  if (!auth.isAdmin) {
    return
  }
  editing.value = item
  formOpen.value = true
}

function onSaved() {
  formOpen.value = false
  load(result.value?.page ?? 1)
}

async function onDelete(item: MissedTrip) {
  if (!confirm(`${item.missedOn} ${item.routeNo}노선 ${item.vehicleNo} 결행 기록을 삭제할까요?`)) {
    return
  }
  try {
    await deleteMissedTrip(item.id)
    // 마지막 줄을 지웠다면 한 페이지 앞으로
    const current = result.value
    const page = current && current.items.length === 1 && current.page > 1 ? current.page - 1 : (current?.page ?? 1)
    await load(page)
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '삭제하지 못했습니다.')
  }
}

onMounted(async () => {
  load(1)
  try {
    routeNos.value = (await getRoutes()).map((route) => route.routeNo)
  } catch {
    routeNos.value = []
  }
})
</script>

<template>
  <section>
    <div class="mb-4 flex items-center justify-between gap-2">
      <h2 class="text-xl font-bold">결행 관리</h2>
      <div v-if="auth.isAdmin" class="flex gap-2">
        <button type="button" class="btn" @click="exportOpen = true">엑셀 내보내기</button>
        <button type="button" class="btn btn-primary" @click="openCreate">+ 결행 입력</button>
      </div>
    </div>

    <form class="mb-4 flex flex-wrap items-center gap-2" @submit.prevent="onSearch">
      <input v-model="condition.fromDate" type="date" class="input w-auto" aria-label="시작일" />
      <span>~</span>
      <input v-model="condition.toDate" type="date" class="input w-auto" aria-label="종료일" />
      <select v-model="condition.routeNo" class="input w-auto" aria-label="노선">
        <option value="">전체 노선</option>
        <option v-for="no in routeNos" :key="no" :value="no">{{ no }}</option>
      </select>
      <select v-model="condition.reason" class="input w-auto" aria-label="사유">
        <option value="">전체 사유</option>
        <option v-for="option in reasonOptions" :key="option.value" :value="option.value">
          {{ option.label }}
        </option>
      </select>
      <input
        v-model="condition.keyword"
        type="text"
        class="input sm:w-56"
        placeholder="승무원·차량번호·비고 (예: MCU)"
      />
      <select v-model.number="condition.size" class="input w-auto" aria-label="한 페이지 건수" @change="onSearch">
        <option :value="20">20건</option>
        <option :value="50">50건</option>
        <option :value="100">100건</option>
      </select>
      <button type="submit" class="btn btn-primary" :disabled="loading">조회</button>
    </form>

    <p v-if="errorMessage" class="mb-2 text-danger">{{ errorMessage }}</p>

    <template v-if="result">
      <p class="mb-2 text-muted">
        전체 {{ result.totalCount }}건 · 결행 횟수 합계 {{ result.missedSum }}회
      </p>

      <div class="overflow-x-auto rounded-md border border-line bg-surface">
        <table class="data-table">
          <thead>
            <tr>
              <th>일자</th>
              <th>요일</th>
              <th>노선</th>
              <th>차량번호</th>
              <th>순번</th>
              <th>승무원</th>
              <th>횟수</th>
              <th>사유</th>
              <th>비고</th>
              <th v-if="auth.isAdmin"></th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in result.items"
              :key="item.id"
              :class="{ 'cursor-pointer': auth.isAdmin }"
              @click="openEdit(item)"
            >
              <td>{{ item.missedOn }}</td>
              <td>{{ weekday(item.missedOn) }}</td>
              <td>{{ item.routeNo }}</td>
              <td>{{ item.vehicleNo }}</td>
              <td>{{ item.tripLabel }}</td>
              <td>{{ item.driverName ?? '-' }}</td>
              <td>{{ item.missedCount }}</td>
              <td>{{ reasonLabel[item.reason] }}</td>
              <td class="text-left">{{ item.note ?? '' }}</td>
              <td v-if="auth.isAdmin">
                <button type="button" class="btn btn-danger px-2 py-1" @click.stop="onDelete(item)">
                  삭제
                </button>
              </td>
            </tr>
            <tr v-if="result.items.length === 0">
              <td :colspan="auth.isAdmin ? 10 : 9" class="py-10 text-muted">조회 결과가 없습니다.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <BasePagination
        :page="result.page"
        :size="result.size"
        :total-count="result.totalCount"
        @change="load"
      />
    </template>

    <template v-if="auth.isAdmin">
      <MissedTripFormModal
        :open="formOpen"
        :target="editing"
        :route-nos="routeNos"
        @close="formOpen = false"
        @saved="onSaved"
      />
      <MissedTripExportModal
        :open="exportOpen"
        :initial="{
          fromDate: condition.fromDate,
          toDate: condition.toDate,
          routeNo: condition.routeNo,
          reason: condition.reason,
          keyword: condition.keyword.trim(),
        }"
        @close="exportOpen = false"
      />
    </template>
  </section>
</template>
