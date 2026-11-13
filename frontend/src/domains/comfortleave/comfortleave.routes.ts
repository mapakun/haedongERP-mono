import type { RouteRecordRaw } from 'vue-router'

export const comfortLeaveRoutes: RouteRecordRaw[] = [
  {
    path: 'comfort-leave',
    name: 'comfort-leave',
    component: () => import('./ComfortLeaveView.vue'),
  },
  {
    path: 'comfort-leave/members',
    name: 'comfort-leave-members',
    component: () => import('./ComfortMemberView.vue'),
    meta: { adminOnly: true },
  },
]
