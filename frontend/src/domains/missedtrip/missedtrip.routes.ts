import type { RouteRecordRaw } from 'vue-router'

export const missedTripRoutes: RouteRecordRaw[] = [
  {
    path: 'missed-trips',
    name: 'missed-trip-list',
    component: () => import('./MissedTripListView.vue'),
  },
]
