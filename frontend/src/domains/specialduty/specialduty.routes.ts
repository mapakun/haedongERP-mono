import type { RouteRecordRaw } from 'vue-router'

export const specialDutyRoutes: RouteRecordRaw[] = [
  {
    path: 'special-duty',
    name: 'special-duty',
    component: () => import('./SpecialDutyView.vue'),
  },
]
