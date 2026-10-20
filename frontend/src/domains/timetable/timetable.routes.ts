import type { RouteRecordRaw } from 'vue-router'

export const timetableRoutes: RouteRecordRaw[] = [
  {
    path: 'timetables',
    name: 'timetable-list',
    component: () => import('./TimetableListView.vue'),
  },
]
