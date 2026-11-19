import type { RouteRecordRaw } from 'vue-router'

export const routeRosterRoutes: RouteRecordRaw[] = [
  {
    path: 'route-roster',
    name: 'route-roster',
    component: () => import('./RouteRosterView.vue'),
    meta: { adminOnly: true },
  },
]
