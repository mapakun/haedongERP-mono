import type { RouteRecordRaw } from 'vue-router'

export const dutyRoutes: RouteRecordRaw[] = [
  {
    path: 'duty/roster',
    name: 'duty-roster',
    component: () => import('./DutyRosterView.vue'),
    meta: { adminOnly: true },
  },
  {
    path: 'duty/starts',
    name: 'duty-starts',
    component: () => import('./DutyStartView.vue'),
    meta: { adminOnly: true },
  },
]
