<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { getErrorMessage } from '@/shared/api/error'
import { todayString } from '@/shared/utils/date'
import { getMissedTripStats } from './missedtrip.api'
import {
  reasonLabel,
  reasonOptions,
  statGroupOptions,
  type MissedReason,
  type MissedTripStatResponse,
  type StatGroup,
} from './missedtrip.types'

defineProps<{
  /** 노선 선택 목록 */
  routeNos: string[]
}>()

const condition = reactive<{
  fromDate: string
  toDate: string
  group: StatGroup
  routeNo: string
  reason: MissedReason | ''
  keywords: string
}>({
  // 기본 기간: 올해 1월 1일 ~ 오늘
  fromDate: todayString().slice(0, 4) + '-01-01',
  toDate: todayString(),
  group: 'REASON',
  routeNo: '',
  reason: '',
  keywords: 'MCU, 휠모터',
})

const result = ref<MissedTripStatResponse | null>(null)
// 결과를 만든 기준 (조건을 바꿔도 표 이름은 조회한 기준으로 보여 주기 위해)
const shownGroup = ref<StatGroup>('REASON')
const loading = ref(false)
const errorMessage = ref('')

// 빠르게 연달아 조회했을 때 늦게 온 옛 응답이 덮지 않도록 순번을 센다
let requestSeq = 0

/** 막대 길이를 정할 기준: 가장 큰 결행 횟수 */
const maxSum = computed(() => Math.max(1, ...(result.value?.rows.map((row) => row.missedSum) ?? [0])))

function labelText(label: string | null): string {
  if (label === null) {
    return '(미기재)'
  }
  return shownGroup.value === 'REASON' ? (reasonLabel[label as MissedReason] ?? label) : label
}

const labelHeader = computed(
  () => statGroupOptions.find((option) => option.value === shownGroup.value)?.label.replace(/ \(.*\)/, '') ?? '',
)

async function load() {
  const seq = ++requestSeq
  loading.value = true
  errorMessage.value = ''
  try {
    const data = await getMissedTripStats({
      fromDate: condition.fromDate,
      toDate: condition.toDate,
      group: condition.group,
      routeNo: condition.routeNo || undefined,
      reason: condition.reason || undefined,
      keywords: condition.group === 'KEYWORD' ? condition.keywords : undefined,
    })
    if (seq === requestSeq) {
      result.value = data
      shownGroup.value = condition.group
    }
  } catch (error) {
    if (seq === requestSeq) {
      errorMessage.value = getErrorMessage(error, '통계를 불러오지 못했습니다.')
    }
  } finally {
    if (seq === requestSeq) {
      loading.value = false
    }
  }
}

onMounted(load)
</script>

<template>
  <div>
    <form class="mb-4 flex flex-wrap items-center gap-2" @submit.prevent="load">
      <select v-model="condition.group" class="input w-auto" aria-label="집계 기준">
        <option v-for="option in statGroupOptions" :key="option.value" :value="option.value">
          {{ option.label }}
        </option>
      </select>
      <input v-model="condition.fromDate" type="date" class="input w-auto" aria-label="시작일" required />
      <span>~</span>
      <input v-model="condition.toDate" type="date" class="input w-auto" aria-label="종료일" required />
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
        v-if="condition.group === 'KEYWORD'"
        v-model="condition.keywords"
        type="text"
        class="input sm:w-64"
        placeholder="비고에서 찾을 단어 (쉼표로 구분)"
      />
      <button type="submit" class="btn btn-primary" :disabled="loading">조회</button>
    </form>

    <p v-if="errorMessage" class="mb-2 text-danger">{{ errorMessage }}</p>

    <template v-if="result">
      <p class="mb-2 text-muted">
        조건 전체 {{ result.totalCount }}건 · 결행 횟수 합계 {{ result.missedSum }}회
      </p>

      <div class="overflow-x-auto rounded-md border border-line bg-surface">
        <table class="data-table">
          <thead>
            <tr>
              <th>{{ labelHeader }}</th>
              <th>건수</th>
              <th>결행 횟수</th>
              <th class="w-2/5">비율</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in result.rows" :key="row.label ?? '__none__'">
              <td>{{ labelText(row.label) }}</td>
              <td>{{ row.tripCount }}</td>
              <td>{{ row.missedSum }}</td>
              <td>
                <div class="h-3 rounded bg-canvas">
                  <div
                    class="h-3 rounded bg-primary"
                    :style="{ width: `${(row.missedSum / maxSum) * 100}%` }"
                  ></div>
                </div>
              </td>
            </tr>
            <tr v-if="result.rows.length === 0">
              <td colspan="4" class="py-10 text-muted">조회 결과가 없습니다.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <p v-if="shownGroup === 'KEYWORD'" class="mt-2 text-muted">
        한 기록에 단어가 여러 개 적혀 있으면 각 줄에 모두 세어서, 줄들을 더한 값이 위의 전체 합계와 다를 수 있어요.
      </p>
      <p v-else-if="shownGroup === 'VEHICLE' || shownGroup === 'DRIVER'" class="mt-2 text-muted">
        결행 횟수가 많은 상위 20개만 보여줘요. 위의 전체 합계는 모든 기록 기준이에요.
      </p>
    </template>
  </div>
</template>
