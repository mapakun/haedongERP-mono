<script setup lang="ts">
import { ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { getErrorMessage } from '@/shared/api/error'
import { downloadMissedTripExcel } from './missedtrip.api'
import type { MissedReason } from './missedtrip.types'

const props = defineProps<{
  open: boolean
  /** 목록에서 보던 조건을 기본값으로 넘겨받는다 */
  initial: { fromDate: string; toDate: string; routeNo: string; reason: MissedReason | ''; keyword: string }
}>()

const emit = defineEmits<{
  close: []
}>()

const fromDate = ref('')
const toDate = ref('')
const downloading = ref(false)
const errorMessage = ref('')

watch(
  () => props.open,
  (open) => {
    if (!open) {
      return
    }
    fromDate.value = props.initial.fromDate
    toDate.value = props.initial.toDate
    errorMessage.value = ''
  },
)

async function onSubmit() {
  if (!fromDate.value || !toDate.value) {
    errorMessage.value = '시작일과 종료일을 모두 입력해 주세요.'
    return
  }
  downloading.value = true
  errorMessage.value = ''
  try {
    await downloadMissedTripExcel({
      fromDate: fromDate.value,
      toDate: toDate.value,
      routeNo: props.initial.routeNo || undefined,
      reason: props.initial.reason || undefined,
      keyword: props.initial.keyword || undefined,
    })
    emit('close')
  } catch (error) {
    errorMessage.value = await readBlobError(error)
  } finally {
    downloading.value = false
  }
}

/** blob 으로 받은 오류 응답은 글자로 한 번 풀어서 서버 메시지를 꺼낸다 */
async function readBlobError(error: unknown): Promise<string> {
  const data = (error as { response?: { data?: unknown } })?.response?.data
  if (data instanceof Blob) {
    try {
      const message = JSON.parse(await data.text()).message
      if (typeof message === 'string' && message) {
        return message
      }
    } catch {
      /* 아래 기본 메시지 */
    }
    return '내보내지 못했습니다.'
  }
  return getErrorMessage(error, '내보내지 못했습니다.')
}
</script>

<template>
  <BaseModal :open="open" title="엑셀 내보내기" @close="emit('close')">
    <form id="missed-trip-export-form" class="flex flex-col gap-3.5" @submit.prevent="onSubmit">
      <div class="grid grid-cols-2 gap-3">
        <label class="flex flex-col gap-1.5">
          <span>시작일 <em class="text-danger not-italic">*</em></span>
          <input v-model="fromDate" type="date" class="input" required />
        </label>
        <label class="flex flex-col gap-1.5">
          <span>종료일 <em class="text-danger not-italic">*</em></span>
          <input v-model="toDate" type="date" class="input" required />
        </label>
      </div>

      <p class="text-muted">
        한 번에 최대 366일, 10,000건까지 내보낼 수 있어요.
        <template v-if="initial.routeNo || initial.reason || initial.keyword">
          목록의 노선·사유·검색어 조건도 함께 적용돼요.
        </template>
      </p>

      <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    </form>

    <template #footer>
      <button type="button" class="btn" @click="emit('close')">취소</button>
      <button type="submit" form="missed-trip-export-form" class="btn btn-primary" :disabled="downloading">
        {{ downloading ? '만드는 중...' : '엑셀 받기' }}
      </button>
    </template>
  </BaseModal>
</template>
