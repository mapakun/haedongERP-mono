<script setup lang="ts">
import { formatMobile } from '@/shared/utils/format'
import { jobTypeLabel, type EmployeeSummary } from './employee.types'

defineProps<{
  items: EmployeeSummary[]
}>()

const emit = defineEmits<{
  select: [id: number]
}>()
</script>

<template>
  <div class="overflow-x-auto rounded-md border border-line bg-surface">
    <table class="data-table">
      <thead>
      <tr>
        <th>서열</th>
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
        v-for="employee in items"
        :key="employee.id"
        :class="['cursor-pointer', { 'text-muted': employee.retiredAt }]"
        @click="emit('select', employee.id)"
      >
        <td>{{ employee.seniorityNo ?? '-' }}</td>
        <td class="text-left">{{ employee.name }}</td>
        <td>{{ jobTypeLabel[employee.jobType] }}</td>
        <td>{{ employee.birthDate ?? '-' }}</td>
        <td>{{ formatMobile(employee.mobile) }}</td>
        <td>{{ employee.hasAccount ? '있음' : '-' }}</td>
        <td>{{ employee.retiredAt ? `퇴사 (${employee.retiredAt})` : '재직' }}</td>
      </tr>
      <tr v-if="items.length === 0">
        <td colspan="7" class="py-10 text-muted">조회 결과가 없습니다.</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>
