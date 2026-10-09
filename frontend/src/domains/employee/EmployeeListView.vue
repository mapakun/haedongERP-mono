<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import BasePagination from '@/shared/components/BasePagination.vue'
import type { PageResponse } from '@/shared/api/page'
import { formatMobile } from '@/shared/utils/format'
import { useAuthStore } from '@/domains/auth/auth.store'
import EmployeeFormModal from './EmployeeFormModal.vue'
import EmployeeRetireModal from './EmployeeRetireModal.vue'
import { searchEmployees } from './employee.api'
import {
  jobTypeLabel,
  type EmployeeSummary,
  type JobType,
  type RetireTarget,
} from './employee.types'

const PAGE_SIZE = 20

const auth = useAuthStore()

const condition = reactive<{ keyword: string; jobType: JobType | ''; includeRetired: boolean }>({
  keyword: '',
  jobType: '',
  includeRetired: false, // ★ 기본은 퇴사자 제외
})
const result = ref<PageResponse<EmployeeSummary> | null>(null)
const loading = ref(false)
const errorMessage = ref('')

const modalOpen = ref(false)
const editingId = ref<number | null>(null)
const retireTarget = ref<RetireTarget | null>(null) // ★ 퇴사 확인창 대상

async function load(page: number) {
  loading.value = true
  errorMessage.value = ''
  try {
    result.value = await searchEmployees({
      keyword: condition.keyword || undefined,
      jobType: condition.jobType || undefined,
      includeRetired: condition.includeRetired || undefined, // ★
      page,
      size: PAGE_SIZE,
    })
  } catch {
    errorMessage.value = '직원 목록을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

function onSearch() {
  load(1)
}

function reloadCurrentPage() {
  load(result.value?.page ?? 1)
}

function openCreate() {
  editingId.value = null
  modalOpen.value = true
}

function openEdit(id: number) {
  if (!auth.isAdmin) {
    return
  }
  editingId.value = id
  modalOpen.value = true
}

function onSaved() {
  modalOpen.value = false
  reloadCurrentPage()
}

// ★ 수정 모달에서 "퇴사 처리"를 누르면: 수정 모달을 닫고 퇴사 확인창을 연다
function onRetireRequest(target: RetireTarget) {
  modalOpen.value = false
  retireTarget.value = target
}

function onRetired() {
  retireTarget.value = null
  reloadCurrentPage()
}

onMounted(() => {
  load(1)
})
</script>

<template>
  <section>
    <div class="mb-4 flex items-center justify-between gap-2">
      <h2 class="text-xl font-bold">직원 관리</h2>
      <button v-if="auth.isAdmin" type="button" class="btn btn-primary" @click="openCreate">
        + 직원 등록
      </button>
    </div>

    <!-- 휴대폰: 검색창이 한 줄을 차지하고 나머지는 다음 줄로 / sm 이상: 한 줄 -->
    <form class="mb-4 flex flex-wrap items-center gap-2" @submit.prevent="onSearch">
      <input
        v-model="condition.keyword"
        type="text"
        class="input sm:w-60"
        placeholder="이름 또는 휴대폰 번호"
      />
      <select v-model="condition.jobType" class="input w-auto">
        <option value="">전체</option>
        <option value="DRIVER">기사</option>
        <option value="OFFICE">사무</option>
      </select>
      <!-- 퇴사자 포함: 체크를 바꾸면 바로 다시 조회 -->
      <label class="flex cursor-pointer items-center gap-1 whitespace-nowrap">
        <input
          v-model="condition.includeRetired"
          type="checkbox"
          class="accent-primary"
          @change="onSearch"
        />
        퇴사자 포함
      </label>
      <button type="submit" class="btn btn-primary" :disabled="loading">조회</button>
    </form>

    <p v-if="errorMessage" class="text-danger">{{ errorMessage }}</p>

    <template v-else-if="result">
      <p class="mb-2 text-muted">전체 {{ result.totalCount }}명</p>

      <!-- 표가 화면보다 넓으면 이 안에서만 가로로 스크롤 -->
      <div class="overflow-x-auto rounded-md border border-line bg-surface">
        <table class="data-table">
          <thead>
            <tr>
              <th>입사순</th>
              <th>이름</th>
              <th>직종</th>
              <th>생년월일</th>
              <th>휴대폰</th>
              <th>계정</th>
              <th>상태</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="employee in result.items"
              :key="employee.id"
              :class="{ 'cursor-pointer': auth.isAdmin, 'text-muted': employee.retiredAt }"
              @click="openEdit(employee.id)"
            >
              <td>{{ employee.seniorityNo ?? '-' }}</td>
              <td class="text-left">{{ employee.name }}</td>
              <td>{{ jobTypeLabel[employee.jobType] }}</td>
              <td>{{ employee.birthDate ?? '-' }}</td>
              <td>{{ formatMobile(employee.mobile) }}</td>
              <td>{{ employee.hasAccount ? '있음' : '-' }}</td>
              <td>{{ employee.retiredAt ? `퇴사 (${employee.retiredAt})` : '재직' }}</td>
            </tr>
            <tr v-if="result.items.length === 0">
              <td colspan="7" class="py-10 text-muted">조회 결과가 없습니다.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <BasePagination
        :page="result.page"
        :size="result.size"
        :total-count="result.totalCount"
        @change="load"
      />
    </template>

    <EmployeeFormModal
      :open="modalOpen"
      :employee-id="editingId"
      @close="modalOpen = false"
      @saved="onSaved"
      @retire="onRetireRequest"
    />

    <EmployeeRetireModal
      :target="retireTarget"
      @close="retireTarget = null"
      @retired="onRetired"
    />
  </section>
</template>
