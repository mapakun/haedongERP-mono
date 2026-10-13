<!-- 근무 시작점 입력 (1~12) -->
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getErrorMessage } from '@/shared/api/error'
import { todayString } from '@/shared/utils/date'
import { createDutyStarts, fetchDutyDrivers } from './duty.api'
import { shiftLabel, shiftOfCycleDay, type DutyDriver, type DutyStartItem } from './duty.types'

const drivers = ref<DutyDriver[]>([])
const baseDate = ref(todayString())
/** 기사 이름 → 입력한 주기 일차. 비워 둔 기사는 이번에 저장하지 않는다 */
const cycleDays = ref<Record<string, number | ''>>({})
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const unsetCount = computed(() => drivers.value.filter((driver) => driver.latestCycleDay === null).length)

/** 서버로 보낼 목록: 주기 일차를 입력한 기사만 */
const items = computed<DutyStartItem[]>(() =>
  drivers.value.flatMap((driver) => {
    const cycleDay = cycleDays.value[driver.name]
    return typeof cycleDay === 'number' ? [{ employeeName: driver.name, cycleDay }] : []
  }),
)

function isValidCycleDay(value: number): boolean {
  return Number.isInteger(value) && value >= 1 && value <= 12
}

/** 입력한 일차의 기준일 근무 미리보기 */
function preview(name: string): string {
  const cycleDay = cycleDays.value[name]
  if (typeof cycleDay !== 'number' || !isValidCycleDay(cycleDay)) {
    return ''
  }
  return shiftLabel[shiftOfCycleDay(cycleDay)]
}

async function load() {
  loading.value = true
  try {
    drivers.value = await fetchDutyDrivers()
    cycleDays.value = {}
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '전속기사 목록을 불러오지 못했습니다.')
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  errorMessage.value = ''
  successMessage.value = ''

  if (!baseDate.value) {
    errorMessage.value = '기준일을 입력해 주세요.'
    return
  }
  if (items.value.length === 0) {
    errorMessage.value = '주기 일차를 입력한 기사가 없습니다.'
    return
  }
  if (items.value.some((item) => !isValidCycleDay(item.cycleDay))) {
    errorMessage.value = '주기 일차는 1~12 사이의 정수로 입력해 주세요.'
    return
  }
  const ok = window.confirm(
    `${baseDate.value} 기준으로 ${items.value.length}명의 시작점을 저장할까요?\n` +
    `${baseDate.value} 이후를 기준으로 했던 기존 시작점은 더 이상 적용되지 않습니다.`,
  )
  if (!ok) {
    return
  }

  saving.value = true
  try {
    const count = await createDutyStarts({ baseDate: baseDate.value, items: items.value })
    successMessage.value = `${count}명의 시작점을 저장했습니다.`
    await load()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '시작점을 저장하지 못했습니다.')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <section>
    <div class="mb-4 flex items-center justify-between gap-2">
      <h2 class="text-xl font-bold">근무표 시작점 입력</h2>
      <RouterLink :to="{ name: 'duty-roster' }" class="btn">근무표 보기</RouterLink>
    </div>

    <div class="mb-4 rounded-md bg-surface p-3 text-muted">
      <p>기준일에 각 기사가 12일 주기의 <strong class="text-ink">몇 일차</strong>인지 입력합니다.
        (1~5 오전, 6 휴무, 7~11 오후, 12 휴무)</p>
      <p>비워 둔 기사는 이번에 저장하지 않습니다. 새 시작점은 기준일부터 적용되고,
        그 이후 날짜를 기준으로 했던 기존 시작점은 더 이상 적용되지 않습니다. (기록은 남습니다)</p>
    </div>

    <form @submit.prevent="onSubmit">
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <label class="flex items-center gap-2">
          기준일
          <input v-model="baseDate" type="date" class="input w-auto" required />
        </label>
        <span class="text-muted">
          전속기사 {{ drivers.length }}명 · 미설정 {{ unsetCount }}명 · 이번 입력
          <strong class="text-ink">{{ items.length }}</strong>명
        </span>
        <button type="submit" class="btn btn-primary ml-auto" :disabled="saving || loading">
          {{ saving ? '저장 중...' : '저장' }}
        </button>
      </div>

      <p v-if="errorMessage" class="mb-2 text-danger">{{ errorMessage }}</p>
      <p v-if="successMessage" class="mb-2 text-primary">✓ {{ successMessage }}</p>

      <p v-if="loading">불러오는 중...</p>
      <div v-else class="overflow-x-auto rounded-md border border-line bg-surface">
        <table class="data-table">
          <thead>
          <tr>
            <th>서열</th>
            <th>이름</th>
            <th>현재 시작점</th>
            <th>주기 일차</th>
            <th>기준일 근무</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="driver in drivers" :key="driver.name">
            <td>{{ driver.seniorityNo }}</td>
            <td>{{ driver.name }}</td>
            <td>
                <span v-if="driver.latestBaseDate">
                  {{ driver.latestBaseDate }} = {{ driver.latestCycleDay }}일차
                </span>
              <span v-else class="text-danger">미설정</span>
            </td>
            <td>
              <input
                v-model.number="cycleDays[driver.name]"
                type="number"
                min="1"
                max="12"
                class="input w-20 text-center"
              />
            </td>
            <td>{{ preview(driver.name) }}</td>
          </tr>
          </tbody>
        </table>
      </div>
    </form>
  </section>
</template>
