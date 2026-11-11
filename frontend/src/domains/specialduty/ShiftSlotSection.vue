<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useAuthStore } from '@/domains/auth/auth.store'
import { getErrorMessage } from '@/shared/api/error'
import { getShiftSlots, saveShiftSlots } from './specialduty.api'
import type { ShiftSlotItem } from './specialduty.types'

const props = defineProps<{
  routeNo: string
}>()

const auth = useAuthStore()

const slots = ref<ShiftSlotItem[]>([])
/** 화면에서 켜 둔 순번 (저장하기 전까지는 화면에서만 바뀐다) */
const selected = ref<Set<number>>(new Set())
/** 서버에 저장되어 있는 순번 (바뀌었는지 비교하는 기준) */
const savedNos = ref<number[]>([])
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const message = ref('')

/** 노선을 빠르게 바꿀 때 늦게 도착한 옛 응답이 화면을 덮어쓰지 않도록 번호를 매긴다 */
let requestSeq = 0

async function load() {
  if (!props.routeNo) {
    return
  }
  const seq = ++requestSeq
  loading.value = true
  errorMessage.value = ''
  message.value = ''

  try {
    const response = await getShiftSlots(props.routeNo)
    if (seq !== requestSeq) {
      return
    }
    slots.value = response.slots
    const enabled = response.slots.filter((slot) => slot.enabled).map((slot) => slot.slotNo)
    savedNos.value = enabled
    selected.value = new Set(enabled)
  } catch (error) {
    if (seq === requestSeq) {
      errorMessage.value = getErrorMessage(error, '단축 순번을 불러오지 못했습니다.')
    }
  } finally {
    if (seq === requestSeq) {
      loading.value = false
    }
  }
}

watch(() => props.routeNo, load, { immediate: true })

const selectedList = computed(() => [...selected.value].sort((a, b) => a - b))

/** 저장된 값과 다르면 true (저장 버튼을 켠다) */
const dirty = computed(
  () =>
    selectedList.value.length !== savedNos.value.length ||
    selectedList.value.some((slotNo, index) => slotNo !== savedNos.value[index]),
)

function toggle(slotNo: number) {
  if (!auth.isAdmin) {
    return
  }
  const next = new Set(selected.value)
  if (next.has(slotNo)) {
    next.delete(slotNo)
  } else {
    next.add(slotNo)
  }
  selected.value = next
  message.value = ''
}

function reset() {
  selected.value = new Set(savedNos.value)
  message.value = ''
}

async function onSave() {
  saving.value = true
  errorMessage.value = ''
  message.value = ''
  try {
    await saveShiftSlots(props.routeNo, selectedList.value)
    await load()
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
      <h3 class="text-lg font-semibold">단축 순번</h3>
      <div v-if="auth.isAdmin" class="flex items-center gap-2">
        <button type="button" class="btn" :disabled="!dirty || saving" @click="reset">되돌리기</button>
        <button type="button" class="btn btn-primary" :disabled="!dirty || saving" @click="onSave">
          {{ saving ? '저장 중...' : '저장' }}
        </button>
      </div>
    </div>

    <p class="mb-3 text-muted">
      단축을 배정할 수 있는 순번입니다.
      <span v-if="auth.isAdmin">카드를 눌러 켜고 끈 뒤 저장하세요.</span>
      카드 아래의 평·토·일·공은 그 순번이 평일·토요일·일요일/공휴일 시간표에 있는지 보여 주며,
      취소선이면 그 시간표에 순번이 없다는 뜻입니다.
    </p>

    <p v-if="errorMessage" class="mb-2 text-danger">{{ errorMessage }}</p>
    <p v-else-if="message" class="mb-2 text-primary">{{ message }}</p>

    <p v-if="loading && slots.length === 0" class="text-muted">불러오는 중...</p>

    <template v-else>
      <p class="mb-2 text-muted">
        단축 가능: {{ selectedList.length > 0 ? selectedList.join(', ') + '번' : '없음' }}
      </p>

      <div class="grid grid-cols-[repeat(auto-fill,minmax(5.5rem,1fr))] gap-2">
        <button
          v-for="slot in slots"
          :key="slot.slotNo"
          type="button"
          :aria-pressed="selected.has(slot.slotNo)"
          :class="[
            'flex flex-col items-center gap-0.5 rounded-md border px-2 py-2 text-center',
            selected.has(slot.slotNo) ? 'border-primary bg-primary text-white' : 'border-line bg-surface',
            auth.isAdmin ? 'cursor-pointer' : 'cursor-default',
          ]"
          @click="toggle(slot.slotNo)"
        >
          <span class="text-base font-semibold">{{ slot.slotNo }}</span>
          <span :class="['text-xs', selected.has(slot.slotNo) ? 'text-white/80' : 'text-muted']">
            {{ slot.inWeekday ? `오전 ${slot.weekdayAmTrips}회` : '평일 없음' }}
          </span>
          <span
            :class="['flex gap-1 text-xs', selected.has(slot.slotNo) ? 'text-white/80' : 'text-muted']"
          >
            <span :class="{ 'line-through opacity-50': !slot.inWeekday }">평</span>
            <span :class="{ 'line-through opacity-50': !slot.inSaturday }">토</span>
            <span :class="{ 'line-through opacity-50': !slot.inHoliday }">일·공</span>
          </span>
        </button>
      </div>
    </template>
  </section>
</template>
