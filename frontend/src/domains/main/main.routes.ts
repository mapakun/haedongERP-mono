import type { RouteRecordRaw } from 'vue-router'

export const mainRoutes: RouteRecordRaw[] = [
  {
    path: '',
    name: 'main',
    component: () => import('./MainView.vue'),
  },
]
