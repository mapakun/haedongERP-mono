<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import BasePagination from '@/shared/components/BasePagination.vue'
import type { PageResponse } from '@/shared/api/page'
import { formatMobile } from '@/shared/utils/format'
import { useAuthStore } from '@/domains/auth/auth.store'
import EmployeeFormModal from './EmployeeFormModal.vue'
import { searchEmployees } from './employee.api'
import { jobTypeLabel, type EmployeeSummary, type JobType } from './employee.types'

const PAGE_SIZE = 20

const auth = useAuthStore()

const condition = reactive<{ keyword: string; jobType: JobType | '' }>({
  keyword: '',
  jobType: '',
})
const result = ref<PageResponse<EmployeeSummary> | null>(null)
const loading = ref(false)
const errorMessage = ref('')

// ★ 모달 상태
const modalOpen = ref(false)
const editingId = ref<number | null>(null)

async function load(page: number) {
  loading.value = true
  errorMessage.value = ''
  try {
    result.value = await searchEmployees({
      keyword: condition.keyword || undefined,
      jobType: condition.jobType || undefined,
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

// ★ 등록 / 수정 / 저장 완료
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
  load(result.value?.page ?? 1)
}

onMounted(() => {
  load(1)
})
</script>

<template>
  <section>
    <!-- ★ 제목 + 등록 버튼 -->
    <div class="page-header">
      <h2 class="title">직원 관리</h2>
      <button v-if="auth.isAdmin" type="button" class="btn btn-primary" @click="openCreate">
        + 직원 등록
      </button>
    </div>

    <form class="search-bar" @submit.prevent="onSearch">
      <input v-model="condition.keyword" type="text" placeholder="이름 또는 휴대폰 번호" />
      <select v-model="condition.jobType">
        <option value="">전체</option>
        <option value="DRIVER">기사</option>
        <option value="OFFICE">사무</option>
      </select>
      <button type="submit" class="btn btn-primary" :disabled="loading">조회</button>
    </form>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <template v-else-if="result">
      <p class="summary">전체 {{ result.totalCount }}명</p>

      <table class="table">
        <thead>
        <tr>
          <th>서열</th>
          <th>이름</th>
          <th>직종</th>
          <th>생년월일</th>
          <th>휴대폰</th>
          <th>계정</th>
        </tr>
        </thead>
        <tbody>
        <!-- ★ 관리자면 행 클릭으로 수정 -->
        <tr
          v-for="employee in result.items"
          :key="employee.id"
          :class="{ clickable: auth.isAdmin }"
          @click="openEdit(employee.id)"
        >
          <td class="center">{{ employee.seniorityNo ?? '-' }}</td>
          <td>{{ employee.name }}</td>
          <td class="center">{{ jobTypeLabel[employee.jobType] }}</td>
          <td class="center">{{ employee.birthDate ?? '-' }}</td>
          <td class="center">{{ formatMobile(employee.mobile) }}</td>
          <td class="center">{{ employee.hasAccount ? '있음' : '-' }}</td>
        </tr>
        <tr v-if="result.items.length === 0">
          <td colspan="6" class="empty">조회 결과가 없습니다.</td>
        </tr>
        </tbody>
      </table>

      <BasePagination
        :page="result.page"
        :size="result.size"
        :total-count="result.totalCount"
        @change="load"
      />
    </template>

    <!-- ★ 등록·수정 모달 -->
    <EmployeeFormModal
      :open="modalOpen"
      :employee-id="editingId"
      @close="modalOpen = false"
      @saved="onSaved"
    />
  </section>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.title {
  margin: 0;
}
.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.search-bar input,
.search-bar select {
  padding: 8px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-surface);
}
.search-bar input {
  width: 240px;
}
.summary {
  margin: 0 0 8px;
  color: var(--color-text-muted);
}
.error {
  color: var(--color-danger);
}
.table {
  width: 100%;
  border-collapse: collapse;
  background: var(--color-surface);
}
.table th,
.table td {
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border);
  text-align: left;
}
.table th {
  background: var(--color-bg);
  font-weight: 600;
  text-align: center;
}
.table tbody tr:hover {
  background: var(--color-bg);
}
.clickable {
  cursor: pointer;
}
.center {
  text-align: center;
}
.empty {
  padding: 40px;
  text-align: center;
  color: var(--color-text-muted);
}
</style>
