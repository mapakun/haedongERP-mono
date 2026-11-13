<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/domains/auth/auth.store'
import { getErrorMessage } from '@/shared/api/error'
import { todayString } from '@/shared/utils/date'
import ComfortUseModal from './ComfortUseModal.vue'
import { deleteComfortUse, getComfortLeave } from './comfortleave.api'
import type {
  ComfortLeaveResponse,
  ComfortUse,
  ContractLeave,
  RegularLeave,
  UseTarget,
} from './comfortleave.types'

const auth = useAuthStore()

const thisYear = Number(todayString().slice(0, 4))
const years = [thisYear + 1, thisYear, thisYear - 1, thisYear - 2]

const year = ref(thisYear)
const keyword = ref('')
const data = ref<ComfortLeaveResponse | null>(null)
const loading = ref(false)
const errorMessage = ref('')
/** 사용 기록 삭제처럼 화면은 그대로 두고 알려야 하는 오류 */
const actionError = ref('')
const useTarget = ref<UseTarget | null>(null)

/** 연도를 빠르게 바꿀 때 늦게 도착한 옛 응답이 화면을 덮어쓰지 않도록 번호를 매긴다 */
let requestSeq = 0

async function load() {
  const seq = ++requestSeq
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await getComfortLeave(year.value)
    if (seq === requestSeq) {
      data.value = response
    }
  } catch (error) {
    if (seq === requestSeq) {
      errorMessage.value = getErrorMessage(error, '위로휴가 현황을 불러오지 못했습니다.')
    }
  } finally {
    if (seq === requestSeq) {
      loading.value = false
    }
  }
}

function byName<T extends { name: string }>(rows: T[]): T[] {
  const word = keyword.value.trim()
  return word ? rows.filter((row) => row.name.includes(word)) : rows
}

const regulars = computed(() => byName(data.value?.regulars ?? []))
/** 체크하면 이전 계약과 퇴사자까지 모두 보여 준다 */
const showAllContracts = ref(false)

/** 기본: 사람마다 가장 최근 계약만 남기고, 퇴사한 사람은 뺀다 (재계약 전 계약은 숨김) */
const contracts = computed(() => {
  const rows = byName(data.value?.contracts ?? [])
  if (showAllContracts.value) {
    return rows
  }
  const latest = new Map<number, ContractLeave>()
  for (const row of rows) {
    const current = latest.get(row.employeeId)
    if (!current || row.startDate > current.startDate) {
      latest.set(row.employeeId, row)
    }
  }
  return [...latest.values()]
    .filter((row) => row.retiredAt === null)
    .sort((a, b) => a.startDate.localeCompare(b.startDate))
})

/** 표 아래 합계 (검색으로 거른 사람 기준) */
function totals(rows: { granted: number; used: number; remaining: number }[]) {
  return rows.reduce(
    (sum, row) => ({
      granted: sum.granted + row.granted,
      used: sum.used + row.used,
      remaining: sum.remaining + row.remaining,
    }),
    { granted: 0, used: 0, remaining: 0 },
  )
}
const regularTotals = computed(() => totals(regulars.value))
const contractTotals = computed(() => totals(contracts.value))

/** '2026-03-31' → '3/31' */
function shortDate(value: string): string {
  return `${Number(value.slice(5, 7))}/${Number(value.slice(8, 10))}`
}

/** 사용 기록 위에 마우스를 올리면 보이는 설명 */
function useTitle(use: ComfortUse): string {
  const parts = [use.usedOn]
  if (use.memo) {
    parts.push(use.memo)
  }
  if (use.createdBy) {
    parts.push(`기록: ${use.createdBy}`)
  }
  return parts.join(' · ')
}

function isImported(use: ComfortUse): boolean {
  return use.createdBy === '엑셀 이관'
}

function openRegularUse(row: RegularLeave) {
  useTarget.value = { employeeId: row.employeeId, name: row.name, periodLabel: `${year.value}년 정규직` }
}

function openContractUse(row: ContractLeave) {
  useTarget.value = {
    employeeId: row.employeeId,
    name: row.name,
    periodLabel: `촉탁 계약 ${row.startDate} ~ ${row.endDate}`,
    minDate: row.startDate,
    maxDate: row.endDate,
  }
}

async function removeUse(name: string, use: ComfortUse) {
  if (!auth.isAdmin || !confirm(`${name}님의 ${use.usedOn} 사용 기록을 삭제할까요?`)) {
    return
  }
  actionError.value = ''
  try {
    await deleteComfortUse(use.id)
    await load()
  } catch (error) {
    actionError.value = getErrorMessage(error, '삭제하지 못했습니다.')
  }
}

function onSaved() {
  useTarget.value = null
  load()
}

onMounted(load)
</script>

<template>
  <section>
    <div class="mb-4 flex flex-wrap items-center justify-between gap-2">
      <h2 class="text-xl font-bold">위로휴가</h2>
      <RouterLink v-if="auth.isAdmin" :to="{ name: 'comfort-leave-members' }" class="btn">대상자 관리</RouterLink>
    </div>

    <div class="mb-4 flex flex-wrap items-center gap-2">
      <select v-model.number="year" class="input w-auto" @change="load">
        <option v-for="option in years" :key="option" :value="option">{{ option }}년</option>
      </select>
      <input v-model="keyword" type="text" class="input sm:w-48" placeholder="이름 검색" />
    </div>

    <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    <p v-else-if="loading && !data" class="text-muted">불러오는 중...</p>

    <template v-else-if="data">
      <p v-if="actionError" class="mb-2 text-danger">{{ actionError }}</p>
      <p class="mb-4 text-muted">
        사용일 위에 마우스를 올리면 메모와 기록자가 보입니다. 흐린 날짜는 엑셀에서 옮긴 기록(그달 말일로 표시)입니다.
        <span v-if="auth.isAdmin">날짜 옆 ×를 누르면 그 기록을 삭제합니다.</span>
      </p>

      <!-- 정규직 -->
      <h3 class="mb-2 text-lg font-semibold">정규직 ({{ regulars.length }}명)</h3>
      <p class="mb-2 text-muted">매년 1월 1일에 4개가 생기고 해가 바뀌면 초기화됩니다. 입사한 해는 0개입니다.</p>
      <div class="mb-8 overflow-x-auto rounded-md border border-line bg-surface">
        <table class="data-table">
          <thead>
            <tr>
              <th>이름</th>
              <th>입사일자</th>
              <th>발생</th>
              <th>사용</th>
              <th>잔여</th>
              <th>사용일</th>
              <th v-if="auth.isAdmin"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in regulars" :key="row.employeeId">
              <td class="font-semibold">
                {{ row.name }}
                <span v-if="row.retiredAt" class="ml-1 text-xs font-normal text-muted">퇴사 {{ row.retiredAt }}</span>
              </td>
              <td>{{ row.hireDate ?? '-' }}</td>
              <td>{{ row.granted }}</td>
              <td>{{ row.used }}</td>
              <td :class="{ 'font-semibold text-danger': row.remaining < 0 }">{{ row.remaining }}</td>
              <td class="text-left whitespace-normal">
                <span
                  v-for="use in row.uses"
                  :key="use.id"
                  :title="useTitle(use)"
                  :class="[
                    'mr-1 mb-1 inline-flex items-center gap-1 rounded border border-line px-1.5 py-0.5 text-xs',
                    { 'text-muted': isImported(use) },
                  ]"
                >
                  {{ shortDate(use.usedOn) }}
                  <button
                    v-if="auth.isAdmin"
                    type="button"
                    class="cursor-pointer text-muted hover:text-danger"
                    aria-label="사용 기록 삭제"
                    @click="removeUse(row.name, use)"
                  >
                    ×
                  </button>
                </span>
              </td>
              <td v-if="auth.isAdmin">
                <button type="button" class="btn px-2.5 py-1" @click="openRegularUse(row)">+ 사용</button>
              </td>
            </tr>
            <tr v-if="regulars.length === 0">
              <td :colspan="auth.isAdmin ? 7 : 6" class="py-10 text-muted">조회 결과가 없습니다.</td>
            </tr>
          </tbody>
          <tfoot v-if="regulars.length > 0">
            <tr class="font-semibold">
              <td colspan="2">합계</td>
              <td>{{ regularTotals.granted }}</td>
              <td>{{ regularTotals.used }}</td>
              <td>{{ regularTotals.remaining }}</td>
              <td :colspan="auth.isAdmin ? 2 : 1"></td>
            </tr>
          </tfoot>
        </table>
      </div>

      <!-- 촉탁직 -->
      <div class="mb-2 flex flex-wrap items-center justify-between gap-2">
        <h3 class="text-lg font-semibold">
          촉탁직 ({{ contracts.length }}{{ showAllContracts ? '건' : '명' }})
        </h3>
        <label class="flex cursor-pointer items-center gap-1">
          <input v-model="showAllContracts" type="checkbox" class="accent-primary" />
          이전 계약·퇴사자도 보기
        </label>
      </div>
      <p class="mb-2 text-muted">
        계약마다 시작일부터 3개월 1개, 6개월 2개, 9개월 4개가 생깁니다. 발생은 {{ data.asOf }} 기준(끝난 계약은 만료일 기준)입니다.
      </p>
      <div class="mb-8 overflow-x-auto rounded-md border border-line bg-surface">
        <table class="data-table">
          <thead>
            <tr>
              <th>이름</th>
              <th>계약 기간</th>
              <th>발생</th>
              <th>사용</th>
              <th>잔여</th>
              <th>사용일</th>
              <th v-if="auth.isAdmin"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in contracts" :key="`${row.employeeId}-${row.startDate}`">
              <td class="font-semibold">
                {{ row.name }}
                <span v-if="row.retiredAt" class="ml-1 text-xs font-normal text-muted">퇴사 {{ row.retiredAt }}</span>
              </td>
              <td>{{ row.startDate }} ~ {{ row.endDate }}</td>
              <td>{{ row.granted }}</td>
              <td>{{ row.used }}</td>
              <td :class="{ 'font-semibold text-danger': row.remaining < 0 }">{{ row.remaining }}</td>
              <td class="text-left whitespace-normal">
                <span
                  v-for="use in row.uses"
                  :key="use.id"
                  :title="useTitle(use)"
                  :class="[
                    'mr-1 mb-1 inline-flex items-center gap-1 rounded border border-line px-1.5 py-0.5 text-xs',
                    { 'text-muted': isImported(use) },
                  ]"
                >
                  {{ shortDate(use.usedOn) }}
                  <button
                    v-if="auth.isAdmin"
                    type="button"
                    class="cursor-pointer text-muted hover:text-danger"
                    aria-label="사용 기록 삭제"
                    @click="removeUse(row.name, use)"
                  >
                    ×
                  </button>
                </span>
              </td>
              <td v-if="auth.isAdmin">
                <button type="button" class="btn px-2.5 py-1" @click="openContractUse(row)">+ 사용</button>
              </td>
            </tr>
            <tr v-if="contracts.length === 0">
              <td :colspan="auth.isAdmin ? 7 : 6" class="py-10 text-muted">조회 결과가 없습니다.</td>
            </tr>
          </tbody>
          <tfoot v-if="contracts.length > 0">
            <tr class="font-semibold">
              <td colspan="2">합계</td>
              <td>{{ contractTotals.granted }}</td>
              <td>{{ contractTotals.used }}</td>
              <td>{{ contractTotals.remaining }}</td>
              <td :colspan="auth.isAdmin ? 2 : 1"></td>
            </tr>
          </tfoot>
        </table>
      </div>

      <!-- 월말 누계 (정규직) -->
      <template v-if="data.monthly.length > 0">
        <h3 class="mb-2 text-lg font-semibold">월말 누계 (정규직)</h3>
        <p class="mb-2 text-muted">그달 말일까지 위로휴가를 1번 이상, 2번 이상… 쓴 사람 수입니다. 합계는 사용 건수입니다.</p>
        <div class="max-w-xl overflow-x-auto rounded-md border border-line bg-surface">
          <table class="data-table">
            <thead>
              <tr>
                <th>월</th>
                <th>1번째</th>
                <th>2번째</th>
                <th>3번째</th>
                <th>4번째</th>
                <th>합계</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in data.monthly" :key="row.month">
                <td>{{ row.month }}월</td>
                <td>{{ row.first }}</td>
                <td>{{ row.second }}</td>
                <td>{{ row.third }}</td>
                <td>{{ row.fourth }}</td>
                <td class="font-semibold">{{ row.total }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </template>

    <ComfortUseModal
      v-if="auth.isAdmin"
      :target="useTarget"
      @close="useTarget = null"
      @saved="onSaved"
    />
  </section>
</template>
