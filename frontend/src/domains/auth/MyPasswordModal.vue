<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { getErrorMessage } from '@/shared/api/error'
import { changeMyPassword } from './auth.api'

const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  close: []
}>()

const form = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const saving = ref(false)
const errorMessage = ref('')
const done = ref(false)

// 열릴 때마다 비운다. 비밀번호가 화면 메모리에 남아 있지 않도록
watch(
  () => props.open,
  (open) => {
    if (open) {
      Object.assign(form, { currentPassword: '', newPassword: '', confirmPassword: '' })
      errorMessage.value = ''
      done.value = false
    }
  },
)

async function onSubmit() {
  errorMessage.value = ''

  // 서버에 보내기 전에 화면에서 먼저 확인
  if (form.newPassword !== form.confirmPassword) {
    errorMessage.value = '새 비밀번호가 서로 다릅니다. 다시 확인해 주세요.'
    return
  }

  saving.value = true
  try {
    await changeMyPassword({
      currentPassword: form.currentPassword,
      newPassword: form.newPassword,
    })
    done.value = true
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '비밀번호를 변경하지 못했습니다.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <BaseModal :open="open" title="비밀번호 변경" @close="emit('close')">
    <!-- 완료 -->
    <div v-if="done" class="flex flex-col gap-2">
      <p>비밀번호를 변경했습니다.</p>
      <p class="text-muted">다른 기기나 브라우저에서 로그인되어 있었다면 그곳은 로그아웃됩니다.</p>
    </div>

    <!-- 입력 -->
    <form v-else id="my-password-form" class="flex flex-col gap-3.5" @submit.prevent="onSubmit">
      <label class="flex flex-col gap-1.5">
        <span>현재 비밀번호</span>
        <input
          v-model="form.currentPassword"
          type="password"
          class="input"
          autocomplete="current-password"
          required
        />
      </label>

      <label class="flex flex-col gap-1.5">
        <span>새 비밀번호</span>
        <input
          v-model="form.newPassword"
          type="password"
          class="input"
          autocomplete="new-password"
          minlength="4"
          required
          placeholder="4자 이상"
        />
      </label>

      <label class="flex flex-col gap-1.5">
        <span>새 비밀번호 확인</span>
        <input
          v-model="form.confirmPassword"
          type="password"
          class="input"
          autocomplete="new-password"
          minlength="4"
          required
        />
      </label>

      <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>
    </form>

    <template #footer>
      <button v-if="done" type="button" class="btn btn-primary" @click="emit('close')">닫기</button>
      <template v-else>
        <button type="button" class="btn" @click="emit('close')">취소</button>
        <button type="submit" form="my-password-form" class="btn btn-primary" :disabled="saving">
          {{ saving ? '변경 중...' : '변경' }}
        </button>
      </template>
    </template>
  </BaseModal>
</template>
