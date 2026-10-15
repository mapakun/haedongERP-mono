<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { getErrorMessage } from '@/shared/api/error'
import { createRoute, deleteRoute, updateRouteSlots } from './route.api'
import type { RouteSummary } from './route.types'

const props = defineProps<{
  open: boolean
  /** 수정할 노선. null 이면 새 노선 등록 */
  target: RouteSummary | null
}>()

const emit = defineEmits<{
  close: []
  saved: []
}>()

const isEdit = computed(() => props.target !== null)

const routeNo = ref('')
const totalSlots = ref<number | null>(null)
const saving = ref(false)
const errorMessage = ref('')

// 모달이 열릴 때마다 입력값을 대상 노선(또는 빈 값)으로 초기화
watch(
  () => props.open,
  (open) => {
    if (open) {
      routeNo.value = props.target?.routeNo ?? ''
      totalSlots.value = props.target?.totalSlots ?? null
      errorMessage.value = ''
    }
  },
)

async function onSubmit() {
  if (totalSlots.value === null) {
    return
  }
  saving.value = true
  errorMessage.value = ''
  try {
    if (props.target) {
      await updateRouteSlots(props.target.routeNo, totalSlots.value)
    } else {
      await createRoute({ routeNo: routeNo.value.trim(), totalSlots: totalSlots.value })
    }
    emit('saved')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '저장하지 못했습니다.')
  } finally {
    saving.value = false
  }
}

async function onDelete() {
  if (!props.target || !confirm(`${props.target.routeNo} 노선을 삭제할까요?`)) {
    return
  }
  saving.value = true
  errorMessage.value = ''
  try {
    await deleteRoute(props.target.routeNo)
    emit('saved')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '삭제하지 못했습니다.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <BaseModal :open="open" :title="isEdit ? '노선 수정' : '노선 등록'" @close="emit('close')">
    <form id="route-form" class="flex flex-col gap-3.5" @submit.prevent="onSubmit">
      <label class="flex flex-col gap-1.5">
        <span>노선 번호 <em class="text-danger not-italic">*</em></span>
        <input
          v-model="routeNo"
          type="text"
          class="input"
          maxlength="20"
          placeholder="예: 100-1"
          :disabled="isEdit"
          required
        />
      </label>

      <label class="flex flex-col gap-1.5">
        <span>총 슬롯 수 <em class="text-danger not-italic">*</em></span>
        <input
          v-model.number="totalSlots"
          type="number"
          class="input"
          min="1"
          max="100"
          required
        />
      </label>

      <p v-if="target" class="text-muted">
        현재 {{ target.assignedCount }}대가 배정되어 있어 이보다 작게 줄일 수 없습니다.
      </p>

      <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    </form>

    <template #footer>
      <button
        v-if="isEdit"
        type="button"
        class="btn btn-danger mr-auto"
        :disabled="saving"
        @click="onDelete"
      >
        삭제
      </button>
      <button type="button" class="btn" @click="emit('close')">취소</button>
      <button type="submit" form="route-form" class="btn btn-primary" :disabled="saving">
        {{ saving ? '저장 중...' : '저장' }}
      </button>
    </template>
  </BaseModal>
</template>
