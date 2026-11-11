<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useAuthStore } from '@/domains/auth/auth.store'
import type { RouteSummary } from '@/domains/route/route.types'
import { getErrorMessage } from '@/shared/api/error'
import { formatMinutes, parseTime } from '@/shared/utils/format'
import { getNightRules, getNightTrips, saveNightRule, saveNightTrips } from './specialduty.api'
import type { NightRule, NightTrip } from './specialduty.types'

const props = defineProps<{
  /** 최소 휴식 시간 표에 보여 줄 노선 목록 */
  routes: RouteSummary[]
}>()

const auth = useAuthStore()

const trips = ref<NightTrip[]>([])
const rules = ref<NightRule[]>([])
/** 입력칸의 글자: 심야 순번 → "HH:MM" */
const tripTexts = reactive<Record<number, string>>({})
/** 입력칸의 값: 노선 번호 → 최소 휴식(분). 비우면 '' (v-model.number 는 빈 칸을 '' 로 넣는다) */
const restValues = reactive<Record<string, number | ''>>({})

const loadError = ref('')
const tripError = ref('')
const tripMessage = ref('')
const ruleError = ref('')
const ruleMessage = ref('')
const savingTrips = ref(false)
const savingRules = ref(false)

async function load() {
  loadError.value = ''
  try {
    const [tripList, ruleList] = await Promise.all([getNightTrips(), getNightRules()])
    trips.value = tripList
    rules.value = ruleList

    for (const trip of tripList) {
      tripTexts[trip.nightNo] = formatMinutes(trip.departMin)
    }
    const restByRoute = new Map(ruleList.map((rule) => [rule.routeNo, rule.minRestMin]))
    for (const route of props.routes) {
      restValues[route.routeNo] = restByRoute.get(route.routeNo) ?? ''
    }
  } catch (error) {
    loadError.value = getErrorMessage(error, '심야 기준을 불러오지 못했습니다.')
  }
}

onMounted(load)

/** 서버에 저장되어 있는 노선의 최소 휴식 시간 (없으면 '') */
function savedRest(routeNo: string): number | '' {
  return rules.value.find((rule) => rule.routeNo === routeNo)?.minRestMin ?? ''
}

const tripsDirty = computed(() =>
  trips.value.some((trip) => (tripTexts[trip.nightNo] ?? '').trim() !== formatMinutes(trip.departMin)),
)

const rulesDirty = computed(() =>
  props.routes.some((route) => restValues[route.routeNo] !== savedRest(route.routeNo)),
)

async function onSaveTrips() {
  tripError.value = ''
  tripMessage.value = ''

  const request: NightTrip[] = []
  for (const trip of trips.value) {
    const minutes = parseTime(tripTexts[trip.nightNo] ?? '')
    if (minutes === null) {
      tripError.value = `심야 ${trip.nightNo}순번 시각은 23:00 같은 형식으로 입력해 주세요.`
      return
    }
    request.push({ nightNo: trip.nightNo, departMin: minutes })
  }

  savingTrips.value = true
  try {
    await saveNightTrips(request)
    await load()
    tripMessage.value = '저장했습니다.'
  } catch (error) {
    tripError.value = getErrorMessage(error, '저장하지 못했습니다.')
  } finally {
    savingTrips.value = false
  }
}

async function onSaveRules() {
  ruleError.value = ''
  ruleMessage.value = ''

  // 바뀐 노선만 골라서 검사한다
  const changed: { routeNo: string; minRestMin: number }[] = []
  for (const route of props.routes) {
    const value = restValues[route.routeNo] ?? ''
    if (value === savedRest(route.routeNo)) {
      continue
    }
    if (value === '') {
      ruleError.value = `${route.routeNo}번 노선의 최소 휴식 시간은 비울 수 없습니다.`
      return
    }
    if (!Number.isInteger(value) || value < 0 || value > 300) {
      ruleError.value = `${route.routeNo}번 노선의 최소 휴식 시간은 0 ~ 300 사이의 정수로 입력해 주세요.`
      return
    }
    changed.push({ routeNo: route.routeNo, minRestMin: value })
  }

  savingRules.value = true
  try {
    for (const item of changed) {
      await saveNightRule(item.routeNo, item.minRestMin)
    }
    ruleMessage.value = '저장했습니다.'
  } catch (error) {
    ruleError.value = getErrorMessage(error, '저장하지 못했습니다.')
  } finally {
    // 중간에 실패해도 화면이 서버의 실제 값과 맞도록 다시 불러온다
    await load()
    savingRules.value = false
  }
}
</script>

<template>
  <section class="mb-8">
    <h3 class="mb-2 text-lg font-semibold">심야 시간표</h3>
    <p class="mb-3 text-muted">
      하루에 심야 차가 3대 나가며, 출발 시각은 순번대로 늦어져야 합니다. 24:10 처럼 자정 이후도 입력할 수 있습니다.
    </p>

    <p v-if="loadError" class="text-danger">{{ loadError }}</p>

    <template v-else>
      <form class="mb-2 flex flex-wrap items-end gap-3" @submit.prevent="onSaveTrips">
        <label v-for="trip in trips" :key="trip.nightNo" class="flex flex-col gap-1.5">
          <span>심야 {{ trip.nightNo }}순번</span>
          <input
            v-model="tripTexts[trip.nightNo]"
            type="text"
            inputmode="numeric"
            maxlength="5"
            class="input w-28"
            placeholder="23:00"
            :disabled="!auth.isAdmin"
          />
        </label>
        <button
          v-if="auth.isAdmin"
          type="submit"
          class="btn btn-primary"
          :disabled="!tripsDirty || savingTrips"
        >
          {{ savingTrips ? '저장 중...' : '저장' }}
        </button>
      </form>
      <p v-if="tripError" class="mb-2 text-danger">{{ tripError }}</p>
      <p v-else-if="tripMessage" class="mb-2 text-primary">{{ tripMessage }}</p>

      <h3 class="mt-6 mb-2 text-lg font-semibold">노선별 최소 휴식 시간</h3>
      <p class="mb-3 text-muted">
        순번의 마지막 도착 후 이 시간(분)이 지나야 심야 출발이 가능하다고 보는 참고 기준입니다.
        아래 "심야 가능 순번"에서 규칙상 가능 표시와 "규칙대로 채우기"에 쓰입니다.
      </p>

      <div class="mb-2 max-w-sm overflow-x-auto rounded-md border border-line bg-surface">
        <table class="data-table">
          <thead>
            <tr>
              <th>노선</th>
              <th>최소 휴식(분)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="route in routes" :key="route.routeNo">
              <td class="font-semibold">{{ route.routeNo }}</td>
              <td>
                <input
                  v-model.number="restValues[route.routeNo]"
                  type="number"
                  min="0"
                  max="300"
                  class="input w-24 text-center"
                  placeholder="미설정"
                  :disabled="!auth.isAdmin"
                />
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <button
        v-if="auth.isAdmin"
        type="button"
        class="btn btn-primary"
        :disabled="!rulesDirty || savingRules"
        @click="onSaveRules"
      >
        {{ savingRules ? '저장 중...' : '저장' }}
      </button>
      <p v-if="ruleError" class="mt-2 text-danger">{{ ruleError }}</p>
      <p v-else-if="ruleMessage" class="mt-2 text-primary">{{ ruleMessage }}</p>
    </template>
  </section>
</template>
