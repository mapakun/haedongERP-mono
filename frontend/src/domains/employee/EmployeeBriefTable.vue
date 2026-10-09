<script setup lang="ts">
import { jobTypeLabel, type EmployeeBrief } from './employee.types'

defineProps<{
  items: EmployeeBrief[]
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
      </tr>
      </thead>
      <tbody>
      <tr v-for="employee in items" :key="employee.id">
        <td>{{ employee.seniorityNo ?? '-' }}</td>
        <td class="text-left">{{ employee.name }}</td>
        <td>{{ jobTypeLabel[employee.jobType] }}</td>
      </tr>
      <tr v-if="items.length === 0">
        <td colspan="3" class="py-10 text-muted">조회 결과가 없습니다.</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>
