<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { getErrorMessage } from '@/shared/api/error'
import { saveRoutePoints } from './timetable.api'
import { pointDefaultLabel, timeColumns, type PointKey } from './timetable.types'

const props = defineProps<{
  open: boolean
  routeNo: string
  /** 지금 등록된 장소 이름 */
  pointNames: Partial<Record<PointKey, string>>
}>()

const emit = defineEmits<{
  close: []
  saved: []
}>()

/** 시간표의 시각 열과 같은 순서의 위치 목록 (출발 → 중간지 → 회차 → 도착) */
const pointKeys = timeColumns.map((column) => column.pointKey)

const names = reactive<Record<PointKey, string>>({
  START: '',
  MID1: '',
  MID2: '',
  TURN: '',
  MID3: '',
  MID4: '',
  END: '',
})
const saving = ref(false)
const errorMessage = ref('')

watch(
  () => props.open,
  (open) => {
    if (!open) {
      return
    }
    for (const key of pointKeys) {
      names[key] = props.pointNames[key] ?? ''
    }
    errorMessage.value = ''
  },
)

async function onSubmit() {
  saving.value = true
  errorMessage.value = ''
  try {
    // 7칸을 모두 보낸다. 비워 둔 칸은 서버가 지운다
    await saveRoutePoints(
      props.routeNo,
      pointKeys.map((key) => ({ pointKey: key, name: names[key].trim() })),
    )
    emit('saved')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '저장하지 못했습니다.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <BaseModal :open="open" title="장소 이름" @close="emit('close')">
    <form id="route-point-form" class="flex flex-col gap-3.5" @submit.prevent="onSubmit">
      <p class="text-muted">
        {{ routeNo }}번 노선의 정류장 이름입니다. 시간표 표의 열 제목으로 보입니다.
      </p>

      <label v-for="key in pointKeys" :key="key" class="flex flex-col gap-1.5">
        <span>{{ pointDefaultLabel[key] }}</span>
        <input
          v-model="names[key]"
          type="text"
          class="input"
          maxlength="50"
          placeholder="비워 두면 이름 없음"
        />
      </label>

      <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    </form>

    <template #footer>
      <button type="button" class="btn" @click="emit('close')">취소</button>
      <button type="submit" form="route-point-form" class="btn btn-primary" :disabled="saving">
        {{ saving ? '저장 중...' : '저장' }}
      </button>
    </template>
  </BaseModal>
</template>
