<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getErrorMessage } from '@/shared/api/error'
import { formatTime } from '@/shared/utils/date'
import { useAuthStore } from '@/domains/auth/auth.store'
import { roleLabel, type Role } from '@/domains/auth/auth.types'
import {
  changeRole,
  issueAccount,
  resetPassword,
  revokeAccount,
  unlockAccount,
} from './employee.api'
import type { EmployeeDetail } from './employee.types'

const props = defineProps<{
  employee: EmployeeDetail
}>()

const emit = defineEmits<{
  changed: []
}>()

const ROLE_OPTIONS: Role[] = ['USER', 'ADMIN']

const auth = useAuthStore()
const isSelf = computed(() => props.employee.id === auth.user?.id)
// 최고 관리자의 계정은 최고 관리자만 바꿀 수 있다
const isProtectedMaster = computed(() => props.employee.role === 'MASTER' && !auth.isMaster)
// 최고 관리자의 권한은 화면에서 바꿀 수 없다 (DB 에서만)
const canChangeRole = computed(() => props.employee.role !== 'MASTER')

const password = ref('')
const role = ref<Role>('USER')
const showPasswordReset = ref(false)
const busy = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// 다른 직원을 열었거나, 처리 후 정보를 다시 불러왔을 때 입력값을 맞춘다
watch(
  () => props.employee,
  (employee) => {
    password.value = ''
    role.value = employee.hasAccount ? employee.role : 'USER'
    showPasswordReset.value = false
    errorMessage.value = ''
  },
  { immediate: true },
)

/** 버튼마다 반복되는 "처리 중 표시 → API 호출 → 결과 메시지"를 한 곳에서 */
async function run(action: () => Promise<void>, success: string) {
  busy.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    await action()
    successMessage.value = success
    emit('changed')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '처리하지 못했습니다.')
  } finally {
    busy.value = false
  }
}

function onIssue() {
  run(
    () => issueAccount(props.employee.id, { password: password.value, role: role.value }),
    '계정을 발급했습니다.',
  )
}

function onChangeRole() {
  run(() => changeRole(props.employee.id, role.value), '권한을 변경했습니다.')
}

function onResetPassword() {
  run(() => resetPassword(props.employee.id, password.value), '비밀번호를 재설정했습니다.')
}

function onUnlock() {
  run(() => unlockAccount(props.employee.id), '잠금을 해제했습니다.')
}

function onRevoke() {
  const ok = window.confirm(
    `${props.employee.name} 님의 로그인 계정을 회수할까요?\n지금 로그인되어 있다면 바로 로그아웃됩니다.`,
  )
  if (!ok) {
    return
  }
  run(() => revokeAccount(props.employee.id), '계정을 회수했습니다.')
}

function cancelPasswordReset() {
  showPasswordReset.value = false
  password.value = ''
}
</script>

<template>
  <section class="mt-5 border-t border-line pt-4">
    <h4 class="mb-2.5 font-semibold text-muted">계정 관리</h4>

    <!-- 계정이 없을 때 -->
    <template v-if="!employee.hasAccount">
      <p v-if="employee.retiredAt" class="text-muted">퇴사한 직원에게는 계정을 발급할 수 없습니다.</p>

      <form v-else class="flex flex-col gap-2" @submit.prevent="onIssue">
        <p class="text-muted">
          로그인 아이디는 이름(<strong class="text-ink">{{ employee.name }}</strong>)으로 자동
          지정됩니다.
        </p>
        <div class="flex flex-wrap gap-2">
          <input
            v-model="password"
            type="password"
            class="input min-w-0 flex-1"
            autocomplete="new-password"
            minlength="4"
            required
            placeholder="초기 비밀번호 (4자 이상)"
          />
          <select v-model="role" class="input w-auto">
            <option v-for="option in ROLE_OPTIONS" :key="option" :value="option">
              {{ roleLabel[option] }}
            </option>
          </select>
          <button type="submit" class="btn btn-primary" :disabled="busy">계정 발급</button>
        </div>
      </form>
    </template>

    <!-- 계정이 있을 때 -->
    <template v-else>
      <dl class="mb-3 grid grid-cols-[auto_1fr] gap-x-4 gap-y-1">
        <dt class="text-muted">아이디</dt>
        <dd>{{ employee.name }}</dd>
        <dt class="text-muted">권한</dt>
        <dd>{{ roleLabel[employee.role] }}</dd>
        <dt class="text-muted">상태</dt>
        <dd>
          <span v-if="employee.locked" class="text-danger">
            잠김 ({{ formatTime(employee.lockedUntil) }}까지)
          </span>
          <span v-else>정상</span>
        </dd>
      </dl>

      <p v-if="isSelf" class="text-muted">본인 계정은 상단의 [비밀번호 변경]에서 관리합니다.</p>
      <p v-else-if="isProtectedMaster" class="text-muted">
        최고 관리자의 계정은 최고 관리자만 변경할 수 있습니다.
      </p>

      <div v-else class="flex flex-col gap-2">
        <div v-if="employee.locked">
          <button type="button" class="btn" :disabled="busy" @click="onUnlock">잠금 해제</button>
        </div>

        <div v-if="canChangeRole" class="flex flex-wrap items-center gap-2">
          <select v-model="role" class="input w-auto">
            <option v-for="option in ROLE_OPTIONS" :key="option" :value="option">
              {{ roleLabel[option] }}
            </option>
          </select>
          <button
            type="button"
            class="btn"
            :disabled="busy || role === employee.role"
            @click="onChangeRole"
          >
            권한 변경
          </button>
        </div>

        <form v-if="showPasswordReset" class="flex flex-wrap gap-2" @submit.prevent="onResetPassword">
          <input
            v-model="password"
            type="password"
            class="input min-w-0 flex-1"
            autocomplete="new-password"
            minlength="4"
            required
            placeholder="새 비밀번호 (4자 이상)"
          />
          <button type="submit" class="btn btn-primary" :disabled="busy">재설정</button>
          <button type="button" class="btn" @click="cancelPasswordReset">취소</button>
        </form>

        <div class="flex flex-wrap gap-2">
          <button
            v-if="!showPasswordReset"
            type="button"
            class="btn"
            @click="showPasswordReset = true"
          >
            비밀번호 재설정
          </button>
          <button type="button" class="btn btn-danger" :disabled="busy" @click="onRevoke">
            계정 회수
          </button>
        </div>
      </div>
    </template>

    <p v-if="errorMessage" class="mt-2 text-danger">{{ errorMessage }}</p>
    <p v-if="successMessage" class="mt-2 text-primary">✓ {{ successMessage }}</p>
  </section>
</template>
