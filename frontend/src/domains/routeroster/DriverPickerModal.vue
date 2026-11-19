<!-- 기사 검색·선택 창. 이름이나 서열 번호로 찾아서 고른다 -->
<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import type { DutyDriver } from '@/domains/duty/duty.types'

export interface PickerTarget {
  /** 창 제목에 쓰는 자리 설명 (예: "3115 A") */
  label: string
  /** 지금 이 자리의 기사 (빈자리면 null) */
  currentName: string | null
}

const props = defineProps<{
  target: PickerTarget | null
  drivers: DutyDriver[]
  /** 이번 달 다른 노선에 배치된 기사 → 노선 번호 (고를 수 없음) */
  elsewhere: Map<string, string>
  /** 이 노선의 다른 자리에 있는 기사 → 자리 설명 (고르면 두 자리를 맞바꾼다) */
  sameRoute: Map<string, string>
  /** 이름 아래 "시작점 변경" 버튼을 보여줄지 */
  showStartButton?: boolean
}>()

const emit = defineEmits<{
  /** null 이면 빈자리로 */
  pick: [name: string | null]
  start: []
  close: []
}>()

const keyword = ref('')
const searchInput = ref<HTMLInputElement | null>(null)

watch(
  () => props.target,
  async (target) => {
    if (target) {
      keyword.value = ''
      await nextTick()
      searchInput.value?.focus()
    }
  },
)

/** 이름 일부 또는 서열 번호로 찾는다 */
const filtered = computed(() => {
  const word = keyword.value.trim()
  if (!word) {
    return props.drivers
  }
  return props.drivers.filter((driver) => driver.name.includes(word) || String(driver.seniorityNo) === word)
})

function isDisabled(name: string): boolean {
  return props.elsewhere.has(name) || name === props.target?.currentName
}

function note(name: string): string {
  if (name === props.target?.currentName) {
    return '지금 이 자리'
  }
  const routeNo = props.elsewhere.get(name)
  if (routeNo) {
    return `${routeNo}번에 배치됨`
  }
  const place = props.sameRoute.get(name)
  return place ? `${place} → 자리 맞바꿈` : ''
}

/**
 * 한글은 글자를 조합하는 동안 v-model 이 값을 바꾸지 않아서(스페이스 등으로 조합이 끝나야 반영),
 * input 이벤트마다 직접 넣어 한 글자씩 바로 걸러지게 한다.
 */
function onSearchInput(event: Event) {
  keyword.value = (event.target as HTMLInputElement).value
}

/** 엔터: 고를 수 있는 첫 번째 기사 */
function pickFirst() {
  const first = filtered.value.find((driver) => !isDisabled(driver.name))
  if (first) {
    emit('pick', first.name)
  }
}
</script>

<template>
  <BaseModal :open="target !== null" :title="`${target?.label ?? ''} 기사 선택`" @close="emit('close')">
    <div class="mb-3 flex items-center justify-between gap-2 text-sm">
      <span>
        지금: <strong>{{ target?.currentName ?? '(빈자리)' }}</strong>
      </span>
      <button v-if="showStartButton && target?.currentName" type="button" class="btn px-2 py-1" @click="emit('start')">
        시작점 변경
      </button>
    </div>

    <input
      ref="searchInput"
      :value="keyword"
      type="search"
      class="input mb-2"
      placeholder="이름 또는 서열 번호"
      @input="onSearchInput"
      @keydown.enter.prevent="pickFirst"
    />

    <ul class="max-h-80 overflow-y-auto rounded-md border border-line">
      <li v-if="target?.currentName">
        <button
          type="button"
          class="flex w-full cursor-pointer items-center justify-between border-b border-line px-3 py-2 text-left text-muted hover:bg-canvas"
          @click="emit('pick', null)"
        >
          (빈자리로 비우기)
        </button>
      </li>
      <li v-for="driver in filtered" :key="driver.name">
        <button
          type="button"
          class="flex w-full items-center justify-between gap-2 border-b border-line px-3 py-2 text-left"
          :class="isDisabled(driver.name) ? 'cursor-not-allowed text-muted' : 'cursor-pointer hover:bg-canvas'"
          :disabled="isDisabled(driver.name)"
          @click="emit('pick', driver.name)"
        >
          <span>
            <span class="mr-2 inline-block w-8 text-right text-muted">{{ driver.seniorityNo }}</span>
            {{ driver.name }}
          </span>
          <span class="text-xs" :class="sameRoute.has(driver.name) ? 'text-primary' : 'text-muted'">
            {{ note(driver.name) }}
          </span>
        </button>
      </li>
      <li v-if="filtered.length === 0" class="px-3 py-4 text-center text-muted">찾는 기사가 없습니다.</li>
    </ul>
    <p class="mt-2 text-xs text-muted">엔터를 누르면 맨 위 기사를 고릅니다. 이 노선의 다른 자리 기사를 고르면 두 자리를 맞바꿉니다.</p>
  </BaseModal>
</template>
