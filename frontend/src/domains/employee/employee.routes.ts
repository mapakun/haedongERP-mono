import type { RouteRecordRaw } from 'vue-router'

export const employeeRoutes: RouteRecordRaw[] = [
  {
    path: 'employees',
    name: 'employee-list',
    component: () => import('./EmployeeListView.vue'),
  },
]
