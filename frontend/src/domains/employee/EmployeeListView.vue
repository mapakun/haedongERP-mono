<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import BasePagination from '@/shared/components/BasePagination.vue'
import type { PageResponse } from '@/shared/api/page'
import { formatMobile } from '@/shared/utils/format'
import { searchEmployees } from './employee.api'
import { jobTypeLabel, type EmployeeSummary, type JobType } from './employee.types'

const PAGE_SIZE = 20

const condition = reactive<{ keyword: string; jobType: JobType | '' }>({
  keyword: '',
  jobType: '',
})
const result = ref<PageResponse<EmployeeSummary> | null>(null)
const loading = ref(false)
const errorMessage = ref('')

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

onMounted(() => {
  load(1)
})
</script>

<template>
  <section>
    <h2 class="title">직원 관리</h2>

    <form class="search-bar" @submit.prevent="onSearch">
      <input v-model="condition.keyword" type="text" placeholder="이름 또는 휴대폰 번호" />
      <select v-model="condition.jobType">
        <option value="">전체</option>
        <option value="DRIVER">기사</option>
        <option value="OFFICE">사무</option>
      </select>
      <button type="submit" :disabled="loading">조회</button>
    </form>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <template v-else-if="result">
      <p class="summary">전체 {{ result.totalCount }}명</p>

      <table class="table">
        <thead>
        <tr>
          <th>입사순서</th>
          <th>이름</th>
          <th>직종</th>
          <th>생년월일</th>
          <th>휴대폰</th>
          <th>계정</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="employee in result.items" :key="employee.id">
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
  </section>
</template>

<style scoped>
.title {
  margin: 0 0 16px;
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
.search-bar button {
  padding: 8px 16px;
  color: #fff;
  background: var(--color-primary);
  border: none;
  border-radius: var(--radius);
  cursor: pointer;
}
.search-bar button:hover:not(:disabled) {
  background: var(--color-primary-hover);
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
}
.table tbody tr:hover {
  background: var(--color-bg);
}
.center {
  text-align: center;
}
.table th {
  text-align: center;
}
.empty {
  padding: 40px;
  text-align: center;
  color: var(--color-text-muted);
}
</style>
