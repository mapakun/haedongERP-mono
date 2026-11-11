<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useAuthStore } from '@/domains/auth/auth.store'
import { getErrorMessage } from '@/shared/api/error'
import { formatMinutes } from '@/shared/utils/format'
import { dayTypeLabel, type DayType } from '@/domains/timetable/timetable.types'
import { getDayTypes, getNightSlots, saveNightSlots } from './specialduty.api'
import type {
  NightSlotItem,
  NightSlotResponse,
  NightSlotSaveItem,
  NightTrip,
} from './specialduty.types'

const props = defineProps<{
  routeNo: string
}>()

const auth = useAuthStore()

const dayTypes = ref<DayType[]>([])
const dayType = ref<DayType | ''>('')
const data = ref<NightSlotResponse | null>(null)
/** 화면에서 체크해 둔 (순번, 심야 순번) 목록. 저장하기 전까지는 화면에서만 바뀐다 */
const checked = ref<Set<string>>(new Set())
/** 서버에 저장되어 있는 목록 (바뀌었는지 비교하는 기준) */
const savedKeys = ref<Set<string>>(new Set())
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const message = ref('')

/** 요청이 겹칠 때 늦게 도착한 옛 응답이 화면을 덮어쓰지 않도록 번호를 매긴다 */
let requestSeq = 0

/** (순번, 심야 순번) 한 쌍을 집합에 넣을 수 있는 글자로 바꾼다 */
function keyOf(slotNo: number, nightNo: number): string {
  return `${slotNo}:${nightNo}`
}

/** reloadDayTypes 가 true 면 노선이 바뀐 것이라 형태 목록부터 다시 받는다 */
async function load(reloadDayTypes: boolean) {
  if (!props.routeNo) {
    return
  }
  const seq = ++requestSeq
  loading.value = true
  errorMessage.value = ''
  message.value = ''

  try {
    if (reloadDayTypes) {
      const types = await getDayTypes(props.routeNo)
      if (seq !== requestSeq) {
        return
      }
      dayTypes.value = types
      // 보던 형태가 새 노선에도 있으면 그대로 두고, 없으면 첫 번째 형태로 옮긴다
      if (!dayType.value || !types.includes(dayType.value)) {
        dayType.value = types[0] ?? ''
      }
    }

    if (!dayType.value) {
      data.value = null
      checked.value = new Set()
      savedKeys.value = new Set()
      return
    }

    const response = await getNightSlots(props.routeNo, dayType.value)
    if (seq !== requestSeq) {
      return
    }
    data.value = response
    const keys = new Set<string>()
    for (const slot of response.slots) {
      for (const nightNo of slot.nightNos) {
        keys.add(keyOf(slot.slotNo, nightNo))
      }
    }
    savedKeys.value = keys
    checked.value = new Set(keys)
  } catch (error) {
    if (seq === requestSeq) {
      errorMessage.value = getErrorMessage(error, '심야 가능 순번을 불러오지 못했습니다.')
    }
  } finally {
    if (seq === requestSeq) {
      loading.value = false
    }
  }
}

watch(() => props.routeNo, () => load(true), { immediate: true })

/** 마지막 도착 + 최소 휴식이 심야 출발보다 늦지 않으면 규칙상 가능하다 */
function ruleOk(slot: NightSlotItem, night: NightTrip): boolean {
  const minRest = data.value?.minRestMin
  return minRest != null && slot.lastArriveMin + minRest <= night.departMin
}

function toggle(slotNo: number, nightNo: number) {
  if (!auth.isAdmin) {
    return
  }
  const next = new Set(checked.value)
  const key = keyOf(slotNo, nightNo)
  if (next.has(key)) {
    next.delete(key)
  } else {
    next.add(key)
  }
  checked.value = next
  message.value = ''
}

/** 체크를 규칙대로 채운다 (저장하기 전까지는 화면에서만 바뀐다) */
function fillByRule() {
  const current = data.value
  if (!current || current.minRestMin === null) {
    return
  }
  const next = new Set<string>()
  for (const slot of current.slots) {
    for (const night of current.nights) {
      if (ruleOk(slot, night)) {
        next.add(keyOf(slot.slotNo, night.nightNo))
      }
    }
  }
  checked.value = next
  message.value = ''
}

function clearAll() {
  checked.value = new Set()
  message.value = ''
}

function reset() {
  checked.value = new Set(savedKeys.value)
  message.value = ''
}

const dirty = computed(
  () =>
    checked.value.size !== savedKeys.value.size ||
    [...checked.value].some((key) => !savedKeys.value.has(key)),
)

async function onSave() {
  const current = data.value
  if (!current || !dayType.value) {
    return
  }
  saving.value = true
  errorMessage.value = ''
  message.value = ''

  // 모든 순번을 보낸다. 체크가 없는 순번은 빈 목록으로 가서 "불가"가 된다
  const slots: NightSlotSaveItem[] = current.slots.map((slot) => ({
    slotNo: slot.slotNo,
    nightNos: current.nights
      .filter((night) => checked.value.has(keyOf(slot.slotNo, night.nightNo)))
      .map((night) => night.nightNo),
  }))

  try {
    await saveNightSlots(props.routeNo, dayType.value, slots)
    await load(false)
    message.value = '저장했습니다.'
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '저장하지 못했습니다.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <section class="mb-8">
    <div class="mb-2 flex flex-wrap items-center justify-between gap-2">
      <h3 class="text-lg font-semibold">심야 가능 순번</h3>
      <div v-if="auth.isAdmin" class="flex flex-wrap items-center gap-2">
        <button
          type="button"
          class="btn"
          :disabled="!data || data.minRestMin === null || saving"
          @click="fillByRule"
        >
          규칙대로 채우기
        </button>
        <button type="button" class="btn" :disabled="!data || saving" @click="clearAll">모두 해제</button>
        <button type="button" class="btn" :disabled="!dirty || saving" @click="reset">되돌리기</button>
        <button type="button" class="btn btn-primary" :disabled="!dirty || saving" @click="onSave">
          {{ saving ? '저장 중...' : '저장' }}
        </button>
      </div>
    </div>

    <p class="mb-3 text-muted">
      각 순번이 어느 심야를 뛸 수 있는지 정합니다. 규칙은 "마지막 도착 + 최소 휴식 ≤ 심야 출발"이며,
      규칙 밖이어도 관리자가 직접 체크해 저장할 수 있습니다.
    </p>

    <div v-if="dayTypes.length > 0" class="mb-3 flex flex-wrap items-center gap-2">
      <select v-model="dayType" class="input w-auto" @change="load(false)">
        <option v-for="type in dayTypes" :key="type" :value="type">{{ dayTypeLabel[type] }}</option>
      </select>
      <span v-if="!dayTypes.includes('SATURDAY')" class="text-muted">
        이 노선은 토요일 시간표가 없어 토요일에는 일요일·공휴일 시간표를 씁니다.
      </span>
    </div>

    <p v-if="errorMessage" class="mb-2 text-danger">{{ errorMessage }}</p>
    <p v-else-if="message" class="mb-2 text-primary">{{ message }}</p>

    <p v-if="loading && !data" class="text-muted">불러오는 중...</p>
    <p v-else-if="!data" class="text-muted">이 노선에는 시간표가 없습니다.</p>

    <template v-else>
      <p class="mb-2 text-muted">
        <template v-if="data.minRestMin !== null">최소 휴식 {{ data.minRestMin }}분 기준</template>
        <template v-else>이 노선의 최소 휴식 시간이 아직 없습니다. 위의 "노선별 최소 휴식 시간"에서 설정하세요.</template>
        · 심야 가능 {{ checked.size }}건
      </p>

      <div class="overflow-x-auto rounded-md border border-line bg-surface">
        <table class="data-table">
          <thead>
            <tr>
              <th>순번</th>
              <th>마지막 도착</th>
              <th v-for="night in data.nights" :key="night.nightNo">
                심야 {{ night.nightNo }}<br />
                <span class="font-normal text-muted">{{ formatMinutes(night.departMin) }}</span>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="slot in data.slots" :key="slot.slotNo">
              <td class="font-semibold">{{ slot.slotNo }}</td>
              <td>{{ formatMinutes(slot.lastArriveMin) }}</td>
              <td v-for="night in data.nights" :key="night.nightNo">
                <label
                  :class="[
                    'flex flex-col items-center gap-0.5',
                    auth.isAdmin ? 'cursor-pointer' : 'cursor-default',
                  ]"
                >
                  <input
                    type="checkbox"
                    class="accent-primary"
                    :checked="checked.has(keyOf(slot.slotNo, night.nightNo))"
                    :disabled="!auth.isAdmin"
                    @change="toggle(slot.slotNo, night.nightNo)"
                  />
                  <span v-if="ruleOk(slot, night)" class="text-xs text-primary">규칙 가능</span>
                  <span
                    v-else-if="checked.has(keyOf(slot.slotNo, night.nightNo))"
                    class="text-xs text-danger"
                  >
                    규칙 밖
                  </span>
                </label>
              </td>
            </tr>
            <tr v-if="data.slots.length === 0">
              <td :colspan="2 + data.nights.length" class="py-10 text-muted">시간표에 순번이 없습니다.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </section>
</template>
