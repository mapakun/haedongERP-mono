import type { RouteRecordRaw } from 'vue-router'

export const routeRoutes: RouteRecordRaw[] = [
  {
    path: 'routes',
    name: 'route-list',
    component: () => import('./RouteListView.vue'),
  },
]
