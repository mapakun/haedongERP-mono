<script setup lang="ts">
import { ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { getErrorMessage } from '@/shared/api/error'
import { copyTimetable } from './timetable.api'
import { dayTypeLabel, type DayType } from './timetable.types'

const props = defineProps<{
  open: boolean
  routeNo: string
  /** 복사해 올 형태 (지금 화면에서 보고 있는 형태) */
  fromDayType: DayType
}>()

const emit = defineEmits<{
  close: []
  /** 복사가 끝났을 때, 복사된 형태를 알려 준다 (화면이 그 형태로 바뀌어 결과를 보여 준다) */
  copied: [toDayType: DayType]
}>()

const allDayTypes = Object.keys(dayTypeLabel) as DayType[]

const toDayType = ref<DayType>('HOLIDAY')
const overwrite = ref(false)
const saving = ref(false)
const errorMessage = ref('')

watch(
  () => props.open,
  (open) => {
    if (open) {
      toDayType.value = allDayTypes.find((type) => type !== props.fromDayType) ?? 'HOLIDAY'
      overwrite.value = false
      errorMessage.value = ''
    }
  },
)

async function onSubmit() {
  if (
    overwrite.value &&
    !confirm(`${dayTypeLabel[toDayType.value]} 시간표를 모두 지우고 덮어씁니다. 계속할까요?`)
  ) {
    return
  }
  saving.value = true
  errorMessage.value = ''
  try {
    await copyTimetable({
      routeNo: props.routeNo,
      fromDayType: props.fromDayType,
      toDayType: toDayType.value,
      overwrite: overwrite.value,
    })
    emit('copied', toDayType.value)
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '복사하지 못했습니다.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <BaseModal :open="open" title="시간표 형태 복사" @close="emit('close')">
    <form id="timetable-copy-form" class="flex flex-col gap-3.5" @submit.prevent="onSubmit">
      <p>
        <strong>{{ routeNo }}번</strong>의 <strong>{{ dayTypeLabel[fromDayType] }}</strong> 시간표를
        다른 형태로 복사합니다.
      </p>

      <label class="flex flex-col gap-1.5">
        <span>복사할 형태</span>
        <select v-model="toDayType" class="input">
          <option
            v-for="type in allDayTypes.filter((t) => t !== fromDayType)"
            :key="type"
            :value="type"
          >
            {{ dayTypeLabel[type] }}
          </option>
        </select>
      </label>

      <label class="flex cursor-pointer items-center gap-2">
        <input v-model="overwrite" type="checkbox" class="accent-primary" />
        이미 있으면 덮어쓰기 (대상 형태의 시간표가 모두 지워집니다)
      </label>

      <p class="text-muted">복사한 뒤에 필요한 회차만 고치면 됩니다.</p>

      <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    </form>

    <template #footer>
      <button type="button" class="btn" @click="emit('close')">취소</button>
      <button type="submit" form="timetable-copy-form" class="btn btn-primary" :disabled="saving">
        {{ saving ? '복사 중...' : '복사' }}
      </button>
    </template>
  </BaseModal>
</template>
