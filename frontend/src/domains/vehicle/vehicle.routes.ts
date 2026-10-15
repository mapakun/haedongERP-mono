import type { RouteRecordRaw } from 'vue-router'

export const vehicleRoutes: RouteRecordRaw[] = [
  {
    path: 'vehicles',
    name: 'vehicle-list',
    component: () => import('./VehicleListView.vue'),
  },
]
